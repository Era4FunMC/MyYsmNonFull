package me.earthme.mysm.utils.ysm

import java.io.*
import java.nio.ByteBuffer
import java.util.*

object MiscUtils {
    @Throws(IOException::class)
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

}