package com.github.b1412.jpa

import arrow.core.*
import mu.KotlinLogging
import org.hibernate.graph.EntityGraphs
import org.hibernate.graph.GraphParser
import java.lang.Boolean
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.persistence.EntityGraph
import javax.persistence.EntityManager
import javax.persistence.criteria.*
import javax.persistence.criteria.Predicate
import javax.persistence.metamodel.Attribute
import javax.persistence.metamodel.EntityType
import javax.persistence.metamodel.PluralAttribute
import javax.persistence.metamodel.SingularAttribute

private val logger = KotlinLogging.logger {}

object JpaUtil {
    fun <T> createPredicate(filter: Map<String, String>, root: Root<T>, cb: CriteriaBuilder): Option<Predicate> {
        val filterFields = filter.filter {
            it.key.contains("f_") && !it.key.endsWith("_op")
        }
        if (filterFields.isEmpty()) {
            return Option.empty()
        } else {
            val entityType = root.model
            val predicates = filterFields.map {
                val value = it.value
                val operator = QueryOp.valueOf(filter[it.key + "_op"].toOption().getOrElse { "=" }.toUpperCase())
                val field = it.key.replace("f_", "")
                val fieldList = field.split("-")
                if (fieldList.size == 1) {
                    cb.and(getPredicate(fieldList[0], root, entityType, value, operator, cb))
                } else { // multiple fields query use OR
                    cb.or(getPredicate(fieldList[0], root, entityType, value, operator, cb), getPredicate(fieldList[1], root, entityType, value, operator, cb))
                }
            }.reduce { l, r ->
                cb.and(l, r)
            }
            return predicates.toOption()
        }
    }

    private fun <T> getPredicate(field: String, root: Root<T>, entityType: EntityType<T>, value: String, operator: QueryOp, cb: CriteriaBuilder): Predicate? {
        var entityType1 = entityType
        val searchPath: Path<Any>
        val fields = field.split(".")
        var javaType: Class<*>? = null
        val convertedValues: List<Any>
        if (fields.size > 1) {
            var join: Join<Any, Any> = root.join<Any, Any>(fields[0])
            for (i in 1 until fields.size - 1) {
                join = join.join(fields[i])
            }
            searchPath = join.get(fields[fields.size - 1])
            fields.windowed(2, 1).forEach { (e, f) ->
                entityType1 = getReferenceEntityType(entityType1, e)
                javaType = getJavaType(entityType1, f)
            }
        } else {
            javaType = getJavaType(entityType1, field)
            searchPath = root.get<Any>(field)
        }
        convertedValues = when {
            javaType!!.isAssignableFrom(java.lang.String::class.java) -> value.split(",")
            javaType!!.isAssignableFrom(java.lang.Long::class.java) -> value.split(",").map { it.toLong() }
            javaType!!.isAssignableFrom(Boolean::class.java) -> value.split(",").map { it.toBoolean() }
            javaType!!.isAssignableFrom(ZonedDateTime::class.java) -> value.split(",")
                    .map { ZonedDateTime.ofInstant(Instant.ofEpochMilli(it.toLong()), ZoneId.systemDefault()) }
            javaType!!.isEnum -> {
                value.split(",").map { v ->
                    javaType!!.getDeclaredMethod("valueOf", String::class.java).invoke(javaType, v)
                }
            }
            else -> throw UnsupportedOperationException(javaType.toString())
        }
        return when (operator) {
            QueryOp.EQ -> {
                cb.equal(searchPath, convertedValues.first())
            }
            QueryOp.LIKE -> {
                cb.like(searchPath as Path<String>, "%$value%")
            }
            QueryOp.IN -> {
                searchPath.`in`(*convertedValues.toTypedArray())
            }
            QueryOp.BETWEEN -> {
                when {
                    javaType!!.isAssignableFrom(java.lang.Long::class.java) -> {
                        val (lowerBond, upperBond) = convertedValues.map { it as Long }
                        cb.between(searchPath as Path<Long>, lowerBond, upperBond)
                    }
                    javaType!!.isAssignableFrom(ZonedDateTime::class.java) -> {
                        val (lowerBond, upperBond) = convertedValues.map { it as ZonedDateTime }
                        cb.between(searchPath as Path<ZonedDateTime>, lowerBond, upperBond)
                    }
                    else -> {
                        throw  IllegalArgumentException(convertedValues.toString())
                    }
                }
            }

        }
    }

    fun <T> createPredicateV2(filter: Map<String, String>, root: Root<T>, cb: CriteriaBuilder): Option<Predicate> {
        val newFilter = mutableMapOf<String, String>()
        filter.filter { it.key.split("_").size == 2 }.forEach { (key, value) ->
            val (field, op) = key.split("_")
            newFilter["f_${field}"] = value
            newFilter["f_${field}_op"] = op

        }
        return createPredicate(newFilter, root, cb)
    }

    fun <T> createEntityGraphFromURL(entityManager: EntityManager, domainClass: Class<T>, filter: Map<String, String>): EntityGraph<T> {
        val embedded = filter["embedded"]
                .toOption()
                .filter { it.isNotBlank() }
                .map { it.split(",") }
                .getOrElse {
                    listOf()
                }
        logger.debug { "embedded $embedded" }
        val graphs = embedded.map {
            it.split(".").reversed().reduce { l, r ->
                val s = "$r($l)"
                s
            }
        }.map {
            GraphParser.parse(domainClass, it, entityManager)
        }
        return EntityGraphs.merge(entityManager, domainClass, *graphs.toTypedArray())
    }

    private fun getJavaType(entityType: EntityType<*>, field: String): Class<*> {
        val argumentEntityAttribute = entityType.getAttribute(field)
        return if (argumentEntityAttribute is PluralAttribute<*, *, *>) argumentEntityAttribute.elementType.javaType else argumentEntityAttribute.javaType
    }

    private fun <T> getReferenceEntityType(entityType: EntityType<T>, field: String): EntityType<T> {
        val attribute = entityType.getAttribute(field)
        return when {
            attribute.persistentAttributeType == Attribute.PersistentAttributeType.ONE_TO_MANY || attribute.persistentAttributeType == Attribute.PersistentAttributeType.MANY_TO_MANY -> {
                val foreignType = (attribute as PluralAttribute<*, *, *>).elementType as EntityType<T>
                foreignType
            }
            attribute.persistentAttributeType == Attribute.PersistentAttributeType.MANY_TO_ONE || attribute.persistentAttributeType == Attribute.PersistentAttributeType.ONE_TO_ONE -> {
                val foreignType = (attribute as SingularAttribute<*, *>).type as EntityType<T>
                foreignType
            }
            else -> throw  IllegalArgumentException()
        }
    }

}
