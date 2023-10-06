package me.earthme.mysm.utils.ysm

import org.bukkit.NamespacedKey
import org.jetbrains.annotations.Contract
import java.io.*
import java.nio.ByteBuffer
import java.util.*
import java.util.zip.ZipInputStream

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

    @Contract("_ -> new")
    fun stringToResourceLocation(stringIn: String): NamespacedKey {
        val split = stringIn.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        require(split.size == 2) { "Unexpected string input!" }
        return NamespacedKey(split[0], split[1])
    }

    fun getMessageIndex(forgeIndex: Int): Int {
        return forgeIndex and 0xff
    }

    fun uuidToByte(paramUUID: UUID): ByteArray {
        val byteBuffer = ByteBuffer.wrap(ByteArray(16))
        byteBuffer.putLong(paramUUID.mostSignificantBits)
        byteBuffer.putLong(paramUUID.leastSignificantBits)
        return byteBuffer.array()
    }

}