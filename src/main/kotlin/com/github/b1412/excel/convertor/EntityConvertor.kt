package com.github.b1412.excel.convertor

import com.github.b1412.files.convert.CellConverter
import javax.persistence.EntityManager

class EntityConvertor : CellConverter {
    lateinit var em: EntityManager
    lateinit var name: String
    lateinit var fieldName: String
    override fun convert(value: String, obj: Any): Any {
        val hql = "SELECT e from $name e where e.${fieldName} = :value"
        val entity = kotlin.runCatching { em.createQuery(hql).setParameter("value", value).singleResult }
        return if (entity.isSuccess) {
            entity.getOrNull()!!
        } else {
            TODO("if not found, create new one")
        }
    }
}