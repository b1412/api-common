package com.github.b1412.encrypt

import java.net.URLDecoder
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec

object DESUtil {

    private fun encryptProcess(message: String, key: String): ByteArray {
        val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
        val desKeySpec = DESKeySpec(key.toByteArray(charset("UTF-8")))
        val keyFactory = SecretKeyFactory.getInstance("DES")
        val secretKey = keyFactory.generateSecret(desKeySpec)
        val iv = IvParameterSpec(key.toByteArray(charset("UTF-8")))
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
        return cipher.doFinal(message.toByteArray(charset("UTF-8")))
    }

    private fun decryptProcess(message: String, key: String): String {
        val bytesrc = convertHexString(message)
        val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
        val desKeySpec = DESKeySpec(key.toByteArray(charset("UTF-8")))
        val keyFactory = SecretKeyFactory.getInstance("DES")
        val secretKey = keyFactory.generateSecret(desKeySpec)
        val iv = IvParameterSpec(key.toByteArray(charset("UTF-8")))
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
        val retByte = cipher.doFinal(bytesrc)
        return String(retByte)
    }

    private fun convertHexString(ss: String): ByteArray {
        val digest = ByteArray(ss.length / 2)
        for (i in digest.indices) {
            val byteString = ss.substring(2 * i, 2 * i + 2)
            val byteValue = Integer.parseInt(byteString, 16)
            digest[i] = byteValue.toByte()
        }
        return digest
    }
    private fun toHexString(b: ByteArray): String {
        val hexString = StringBuffer()
        for (i in b.indices) {
            var plainText = Integer.toHexString(0xff and b[i].toInt())
            if (plainText.length < 2)
                plainText = "0$plainText"
            hexString.append(plainText)
        }

        return hexString.toString()
    }

    fun encrypt(message: String, key: String): String {
        val orignStr = java.net.URLEncoder.encode(message, "utf-8")
        return toHexString(encryptProcess(orignStr, key))
    }

    fun decrypt(message: String, key: String): String {
        return URLDecoder.decode(decryptProcess(message, key), "utf-8")
    }

}
