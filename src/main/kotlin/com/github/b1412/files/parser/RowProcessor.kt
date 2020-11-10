package com.github.b1412.files.parser

interface RowProcessor {
    fun exec(model: Any, list: List<String>, rowIndex: Int)
}
