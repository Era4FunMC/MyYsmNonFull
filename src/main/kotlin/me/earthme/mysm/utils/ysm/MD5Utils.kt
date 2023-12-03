package me.earthme.mysm.utils.ysm

import java.security.MessageDigest

object MD5Utils {
    fun getMd5(data: ByteArray?): String {
        return byteArrayToHexString(MessageDigest.getInstance("MD5")!!.digest(data)).uppercase()
    }

    fun degist(data: ByteArray?): ByteArray? {
        return MessageDigest.getInstance("MD5")!!.digest(data)
    }

    private fun byteArrayToHexString(paramArrayOfbyte: ByteArray): String {
        val stringBuilder = StringBuilder()
        for (b in paramArrayOfbyte) {
            val str = Integer.toHexString(0xFF and b.toInt())
            if (str.length == 1) {
                stringBuilder.append('0')
            }
            stringBuilder.append(str)
        }
        return stringBuilder.toString()
    }
}