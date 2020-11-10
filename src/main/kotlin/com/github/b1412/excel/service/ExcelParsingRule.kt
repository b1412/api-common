package com.github.b1412.excel.service

import com.github.b1412.files.parser.FileParser


interface ExcelParsingRule<T> {

    val fileParser: FileParser

    val entityClass: Class<*>

    val ruleName: String

    fun process(data: List<T>)
}
