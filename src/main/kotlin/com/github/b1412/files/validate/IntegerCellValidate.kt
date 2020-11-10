package com.github.b1412.files.validate

class IntegerCellValidate : CellValidate {
    override fun validate(obj: Any): Boolean {
        return try {
            Integer.parseInt(obj.toString())
            true
        } catch (e: Exception) {
            false
        }

    }
}
