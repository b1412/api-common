package com.github.b1412.api.controller

import arrow.core.toOption
import com.github.b1412.api.entity.BaseEntity
import com.github.b1412.api.service.BaseService
import com.github.b1412.extenstions.copyFrom
import com.github.b1412.extenstions.responseEntityOk
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.util.UriComponentsBuilder
import java.io.Serializable
import javax.servlet.http.HttpServletRequest

abstract class BaseController<T, ID : Serializable> {
    @Autowired
    lateinit var baseService: BaseService<T, ID>

    open fun page(request: HttpServletRequest, @RequestParam filter: Map<String, String>, pageable: Pageable): ResponseEntity<*> {
        val page = baseService.searchBySecurity(request.method, request.requestURI, filter, pageable)
        return page.responseEntityOk()
    }

    open fun findOne(@PathVariable id: ID, request: HttpServletRequest): ResponseEntity<*> {
        return baseService.findByIdOrNull(id).toOption()
                .fold(
                        { ResponseEntity.notFound().build() },
                        { it.responseEntityOk() }
                )
    }

    open fun saveOne(@Validated @RequestBody input: T, request: HttpServletRequest, uriComponent: UriComponentsBuilder): ResponseEntity<*> {
        baseService.syncSeleceOneFromDb(input as BaseEntity)
        baseService.save(input)
        val uriComponents = uriComponent.path("/v1/user/{id}").buildAndExpand(input.id)
        val headers = HttpHeaders()
        headers.location = uriComponents.toUri()
        return ResponseEntity.created(uriComponents.toUri()).build<Void>()
    }

    open fun updateOne(@PathVariable id: ID, @Validated @RequestBody input: T, request: HttpServletRequest): ResponseEntity<*> {
        baseService.syncSeleceOneFromDb(input as BaseEntity)
        val persisted = baseService.findByIdOrNull(id)
        val merged = (persisted as Any).copyFrom(input) as T
        baseService.save(merged)
        return ResponseEntity.noContent().build<T>()
    }

    open fun deleteOne(@PathVariable id: ID, request: HttpServletRequest): ResponseEntity<*> {
        return runCatching { baseService.deleteById(id) }
                .fold(
                        { ResponseEntity.noContent().build<Void>() },
                        { ResponseEntity.notFound().build() }
                )
    }
}
