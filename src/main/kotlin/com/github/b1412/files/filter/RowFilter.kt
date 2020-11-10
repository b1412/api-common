package com.github.b1412.files.filter

@FunctionalInterface
interface RowFilter {
    fun doFilter(rowNum: Int, list: List<String>): Boolean
}
