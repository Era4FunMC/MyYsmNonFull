package me.earthme.mysm.utils.ysm


import org.jetbrains.annotations.Contract
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets


object YsmCodecUtil {
    @Throws(IOException::class)
    fun writeString(paramByteArrayOutputStream: ByteArrayOutputStream, paramString: String) {
        val arrayOfByte = paramString.toByteArray(StandardCharsets.UTF_8)
        paramByteArrayOutputStream.write(intToByteArray(arrayOfByte.size))
        paramByteArrayOutputStream.write(arrayOfByte)
    }

    @Throws(IOException::class)
    fun writeBoolean(paramByteArrayOutputStream: ByteArrayOutputStream, paramBoolean: Boolean) {
        paramByteArrayOutputStream.write(intToByteArray(if (paramBoolean) 1 else 0))
    }

    @Throws(IOException::class)
    fun writeMapKey(paramByteArrayOutputStream: ByteArrayOutputStream, paramString: String, paramArrayOfbyte: ByteArray) {
        writeString(paramByteArrayOutputStream, paramString)
        paramByteArrayOutputStream.write(intToByteArray(paramArrayOfbyte.size))
    }

    @Throws(IOException::class)
    fun writeMap(paramByteArrayOutputStream: ByteArrayOutputStream, paramMap: Map<String, ByteArray?>) {
        paramByteArrayOutputStream.write(intToByteArray(paramMap.size))
        for (str in paramMap.keys) {
            writeMapKey(paramByteArrayOutputStream, str, paramMap[str]!!)
        }
    }

    @Throws(IOException::class)
    fun writeBytes(paramByteArrayOutputStream: ByteArrayOutputStream, paramMap: Map<String, ByteArray>) {
        for (arrayOfByte in paramMap.values) {
            paramByteArrayOutputStream.write(arrayOfByte)
        }
    }

    @Contract(pure = true)
    fun intToByteArray(input: Int): ByteArray {
        val arrayOfByte = ByteArray(4)
        for (b in 0..3) {
            arrayOfByte[3 - b] = (input shr 8 * b).toByte()
        }
        return arrayOfByte
    }

    fun byteToIntArray(data: ByteArray, begin: Int): Int {
        var b: Byte = 4
        var i = 0
        val j = begin + b
        for (k in begin until j) {
            var m = data[k].toInt() and 0xFF
            m = m shl --b * 8
            i += m
        }
        return i
    }
}

