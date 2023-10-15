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

    fun writeUUID(uuid: UUID,buf: ByteBuf){
        buf.writeLong(uuid.mostSignificantBits)
        buf.writeLong(uuid.leastSignificantBits)
    }

    fun writeUtf(string: String, maxLength: Int,buf: ByteBuf){
        if (string.length > maxLength) {
            val j = string.length
            throw EncoderException("String too big (was $j characters, max $maxLength)")
        } else {
            val abyte = string.toByteArray(StandardCharsets.UTF_8)
            val k: Int = getMaxEncodedUtfLength(maxLength)
            if (abyte.size > k) {
                throw EncoderException("String too big (was " + abyte.size + " bytes encoded, max " + k + ")")
            } else {
                writeVarInt(abyte.size,buf)
                buf.writeBytes(abyte)
            }
        }
    }

    fun readVarInt(byteBuf: ByteBuf): Int {
        var value = 0
        var position = 0
        var currentByte: Byte
        while (true) {
            currentByte = byteBuf.readByte()
            value = value or (currentByte.toInt() and SEGMENT_BITS shl position)
            if (currentByte.toInt() and CONTINUE_BIT == 0) break
            position += 7
            if (position >= 32) throw RuntimeException("VarInt is too big")
        }
        return value
    }

    fun readVarLong(byteBuf: ByteBuf): Long {
        var value: Long = 0
        var position = 0
        var currentByte: Byte
        while (true) {
            currentByte = byteBuf.readByte()
            value = value or ((currentByte.toInt() and SEGMENT_BITS).toLong() shl position)
            if (currentByte.toInt() and CONTINUE_BIT == 0) break
            position += 7
            if (position >= 64) throw java.lang.RuntimeException("VarLong is too big")
        }
        return value
    }

    fun writeVarInt(value: Int,byteBuf: ByteBuf) {
        var value = value
        while (true) {
            if (value and SEGMENT_BITS.inv() == 0) {
                byteBuf.writeByte(value)
                return
            }
            byteBuf.writeByte(value and SEGMENT_BITS or CONTINUE_BIT)

            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value = value ushr 7
        }
    }

    fun writeVarLong(value: Long,byteBuf: ByteBuf) {
        var value = value
        while (true) {
            if (value and SEGMENT_BITS.toLong().inv() == 0L) {
                byteBuf.writeByte(value.toInt())
                return
            }
            byteBuf.writeByte((value and SEGMENT_BITS.toLong() or CONTINUE_BIT.toLong()).toInt())

            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value = value ushr 7
        }
    }

    private fun getMaxEncodedUtfLength(decodedLength: Int): Int {
        return decodedLength * 3
    }

    fun readUtf(maxLength: Int,buf: ByteBuf): String {
        val j: Int = getMaxEncodedUtfLength(maxLength)
        val k = readVarInt(buf)
        return if (k > j) {
            throw DecoderException("The received encoded string buffer length is longer than maximum allowed ($k > $j)")
        } else if (k < 0) {
            throw DecoderException("The received encoded string buffer length is less than zero! Weird string!")
        } else {
            val s = toString(buf.readerIndex(), k, StandardCharsets.UTF_8,buf)
            buf.readerIndex(buf.readerIndex() + k)
            if (s.length > maxLength) {
                val l = s.length
                throw DecoderException("The received string length is longer than maximum allowed ($l > $maxLength)")
            } else {
                s
            }
        }
    }

    private fun toString(i: Int, j: Int, charset: Charset?, buf: ByteBuf): String {
        return buf.toString(i, j, charset)
    }

    fun readResourceLocation(buf: ByteBuf): NamespacedKey{
        return NamespacedKey.fromString(readUtf(32767,buf))!!
    }
}