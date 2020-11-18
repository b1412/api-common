package com.github.b1412.excel.convertor

import com.github.b1412.files.convert.CellConverter


class DoubleConcerter : CellConverter{
    override fun convert(value: String, obj: Any): Any {
        if (value.isEmpty()) {
            return 0.0
        }
        return java.lang.Double.parseDouble(value)
    }
}
