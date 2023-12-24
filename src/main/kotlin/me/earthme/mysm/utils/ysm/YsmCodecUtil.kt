package me.earthme.mysm.utils.ysm

import org.jetbrains.annotations.Contract
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*

object YsmCodecUtil {

    fun <T> objectToByteArray(input: T): ByteArray {
        ByteArrayOutputStream().use { baseStream ->
            ObjectOutputStream(baseStream).use { encoder ->
                encoder.writeObject(input)
                encoder.flush()
                return baseStream.toByteArray()
            }
        }
    }

    fun uuidToByte(paramUUID: UUID): ByteArray {
        val byteBuffer = ByteBuffer.wrap(ByteArray(16))
        byteBuffer.putLong(paramUUID.mostSignificantBits)
        byteBuffer.putLong(paramUUID.leastSignificantBits)
        return byteBuffer.array()
    }

    fun ByteArrayOutputStream.writeString(paramString: String) {
        val arrayOfByte = paramString.toByteArray(StandardCharsets.UTF_8)
        this.write(intToByteArray(arrayOfByte.size))
        this.write(arrayOfByte)
    }

    fun ByteArrayOutputStream.writeBoolean(paramBoolean: Boolean) {
        this.write(intToByteArray(if (paramBoolean) 1 else 0))
    }

    private fun ByteArrayOutputStream.writeMapKeyPair(paramString: String, paramArrayOfbyte: ByteArray) {
        this.writeString(paramString)
        this.write(intToByteArray(paramArrayOfbyte.size))
    }

    fun ByteArrayOutputStream.writeMapKeys(paramMap: Map<String, ByteArray?>) {
        this.write(intToByteArray(paramMap.size))
        for (str in paramMap.keys) {
            this.writeMapKeyPair(str, paramMap[str]!!)
        }
    }

    fun ByteArrayOutputStream.writeBytesForMap(paramMap: Map<String, ByteArray>) {
        for (arrayOfByte in paramMap.values) {
            this.write(arrayOfByte)
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

