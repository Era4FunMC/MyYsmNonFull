package me.earthme.mysm.data

import it.unimi.dsi.fastutil.bytes.ByteArrays
import me.earthme.mysm.utils.ysm.EncryptUtils
import me.earthme.mysm.utils.ysm.YsmCodecUtil
import org.jetbrains.annotations.Contract
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
class YsmPasswordFileInstance(val secretKey: SecretKey, val algorithmParameterSpec: IvParameterSpec, data: ByteArray?) {
    fun encodeToByte(): ByteArray {
        return try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            byteArrayOutputStream.write(YsmCodecUtil.intToByteArray(1498629968))
            byteArrayOutputStream.write(YsmCodecUtil.intToByteArray(1))
            byteArrayOutputStream.write(secretKey.encoded)
            byteArrayOutputStream.write(algorithmParameterSpec.iv)
            byteArrayOutputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            ByteArray(0)
        }
    }

    val data: ByteArray

    init {
        this.data = data ?: encodeToByte()
    }

    companion object {
        @get:Contract(" -> new")
        val random: YsmPasswordFileInstance
            get() = YsmPasswordFileInstance(EncryptUtils.generateSecretKey(), EncryptUtils.generateIV(), null)

        fun readFromFile(target: File): YsmPasswordFileInstance? {
            val allData = Files.readAllBytes(target.toPath())
            val i: Int = YsmCodecUtil.byteToIntArray(allData, 0)
            val j: Int = YsmCodecUtil.byteToIntArray(allData, 4)
            if (i != 1498629968) {
                return null
            }
            if (j != 1) {
                return null
            }
            val keyData = ByteArrays.copy(allData, 8, 16)
            val ivData = ByteArrays.copy(allData, 24, 16)
            return YsmPasswordFileInstance(SecretKeySpec(keyData, "AES"), IvParameterSpec(ivData), allData)
        }
    }
}

