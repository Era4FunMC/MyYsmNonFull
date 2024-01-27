package me.earthme.mysm.utils.mc

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import org.bukkit.NamespacedKey
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

object MCPacketCodecUtils {
    private const val SEGMENT_BITS = 0x7F
    private const val CONTINUE_BIT = 0x80

    fun ByteBuf.writeUUID(uuid: UUID){
        writeLong(uuid.mostSignificantBits)
        writeLong(uuid.leastSignificantBits)
    }

    fun ByteBuf.writeUtf(string: String, maxLength: Int){
        if (string.length > maxLength) {
            val j = string.length
            throw EncoderException("String too big (was $j characters, max $maxLength)")
        } else {
            val abyte = string.toByteArray(StandardCharsets.UTF_8)
            val k: Int = getMaxEncodedUtfLength(maxLength)
            if (abyte.size > k) {
                throw EncoderException("String too big (was " + abyte.size + " bytes encoded, max " + k + ")")
            } else {
                writeVarInt(abyte.size)
                writeBytes(abyte)
            }
        }
    }

    fun ByteBuf.readVarInt(): Int {
        var value = 0
        var position = 0
        var currentByte: Byte
        while (true) {
            currentByte = readByte()
            value = value or (currentByte.toInt() and SEGMENT_BITS shl position)
            if (currentByte.toInt() and CONTINUE_BIT == 0) break
            position += 7
            if (position >= 32) throw RuntimeException("VarInt is too big")
        }
        return value
    }

    fun ByteBuf.writeVarInt(value: Int) {
        var value = value
        while (true) {
            if (value and SEGMENT_BITS.inv() == 0) {
                writeByte(value)
                return
            }
            writeByte(value and SEGMENT_BITS or CONTINUE_BIT)

            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value = value ushr 7
        }
    }

    private fun getMaxEncodedUtfLength(decodedLength: Int): Int {
        return decodedLength * 3
    }

    fun ByteBuf.readUtf(maxLength: Int): String {
        val j: Int = getMaxEncodedUtfLength(maxLength)
        val k = readVarInt()
        return if (k > j) {
            throw DecoderException("The received encoded string buffer length is longer than maximum allowed ($k > $j)")
        } else if (k < 0) {
            throw DecoderException("The received encoded string buffer length is less than zero! Weird string!")
        } else {
            val s = toString(readerIndex(), k, StandardCharsets.UTF_8)
            readerIndex(readerIndex() + k)
            if (s.length > maxLength) {
                val l = s.length
                throw DecoderException("The received string length is longer than maximum allowed ($l > $maxLength)")
            } else {
                s
            }
        }
    }
    fun ByteBuf.readResourceLocation(): NamespacedKey{
        return NamespacedKey.fromString(readUtf(32767))!!
    }

    fun ByteBuf.writeByteArray(byteArray: ByteArray){
        this.writeVarInt(byteArray.size)
        this.writeBytes(byteArray)
    }

    fun ByteBuf.readByteArray(): ByteArray{
        val byteArrayWrapped = ByteArray(this.readVarInt())
        this.readBytes(byteArrayWrapped)

        return byteArrayWrapped
    }

}