package me.earthme.mysm.utils.ysm

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

object CompressUtil {
    fun compress(data: ByteArray): ByteArray? {
        if (data.isEmpty()) {
            return ByteArray(0)
        }

        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        val deflater = Deflater(9)
        deflater.setInput(data)
        deflater.finish()
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        deflater.end()
        return outputStream.toByteArray()
    }

    fun decompress(compressedData: ByteArray): ByteArray {
        if (compressedData.isEmpty()) {
            return ByteArray(0)
        }

        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        val inflater = Inflater()
        inflater.setInput(compressedData, 0, compressedData.size)
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        inflater.end()
        return outputStream.toByteArray()
    }
}