package com.github.b1412.files.convert

@FunctionalInterface
interface CellConverter {
    fun convert(value: String, obj: Any): Any
}
