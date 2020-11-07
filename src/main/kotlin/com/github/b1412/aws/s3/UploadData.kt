package com.github.b1412.aws.s3


import java.io.Serializable


data class UploadData(
        val name: String,
        val data: ByteArray

) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
