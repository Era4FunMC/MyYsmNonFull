package me.earthme.mysm.utils.ysm

import java.security.MessageDigest

object MD5Utils {
    private var MD5_DIGEST: MessageDigest? = null

    init {
        MD5_DIGEST = try {
            MessageDigest.getInstance("MD5")
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun getMd5(data: ByteArray?): String {
        return byteArrayToHexString(MD5_DIGEST!!.digest(data)).uppercase()
    }

    fun degist(data: ByteArray?): ByteArray? {
        return MD5_DIGEST!!.digest(data)
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