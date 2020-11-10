package com.github.b1412.excel.convertor

import com.github.b1412.files.convert.CellConverter


class LongConventor : CellConverter{
    override fun convert(value: String, obj: Any): Any {
        if (value.isEmpty()) {
            return 0L
        }
        return value.toLong()
    }
}
