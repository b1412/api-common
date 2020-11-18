package com.github.b1412.extenstions

import arrow.core.getOrElse
import arrow.core.toOption
import org.springframework.http.ResponseEntity
import java.math.BigDecimal

fun <T> T?.responseEntityOk(): ResponseEntity<T> {
    return ResponseEntity.ok(this!!)
}

fun <T> T?.responseEntityBadRequest(): ResponseEntity<T> {
    return ResponseEntity.badRequest().body(this)
}

fun Any?.println() {
    println(this)
}

fun Any?.print() {
    print(this)
}

fun <T> T?.orElse(default: T): T {
    return this.toOption().getOrElse { default }
}

fun <T> Iterable<T>.sumByBigDecimal(selector: (T) -> BigDecimal): BigDecimal {
    return this.map { selector(it) }.fold(BigDecimal.ZERO, BigDecimal::add)
}



