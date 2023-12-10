package me.earthme.mysm.utils.ysm

import org.jetbrains.annotations.Contract
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object AESEncryptUtils {
    @Throws(Exception::class)
    fun encryptAES(paramArrayOfbyte1: ByteArray?, paramArrayOfbyte2: ByteArray?): ByteArray {
        val secretKeySpec = SecretKeySpec(paramArrayOfbyte1, "AES")
        val ivParameterSpec = IvParameterSpec(paramArrayOfbyte1)
        return encryptAES(secretKeySpec, ivParameterSpec, paramArrayOfbyte2).toByteArray()
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    fun encryptAES(secretKey: SecretKey?, paramAlgorithmParameterSpec: AlgorithmParameterSpec?, data: ByteArray?): ByteArrayOutputStream {
        val inputBuffer = ByteArrayInputStream(data)
        val outputBuffer = ByteArrayOutputStream()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramAlgorithmParameterSpec)
        val buffer = ByteArray(64)
        var bytesRead: Int
        while (inputBuffer.read(buffer).also { bytesRead = it } != -1) {
            val encryptedData = cipher.update(buffer, 0, bytesRead)
            if (encryptedData != null) {
                outputBuffer.write(encryptedData)
            }
        }
        val finalData = cipher.doFinal()
        if (finalData != null) {
            outputBuffer.write(finalData)
        }
        return outputBuffer
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    fun decryptAES(secretKey: SecretKey?, paramAlgorithmParameterSpec: AlgorithmParameterSpec?, data: ByteArray?): ByteArrayOutputStream {
        val inputBuffer = ByteArrayInputStream(data)
        val outputBuffer = ByteArrayOutputStream()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, paramAlgorithmParameterSpec)
        val buffer = ByteArray(64)
        var bytesRead: Int
        while (inputBuffer.read(buffer).also { bytesRead = it } != -1) {
            val decryptedData = cipher.update(buffer, 0, bytesRead)
            if (decryptedData != null) {
                outputBuffer.write(decryptedData)
            }
        }
        val finalData = cipher.doFinal()
        if (finalData != null) {
            outputBuffer.write(finalData)
        }
        return outputBuffer
    }

    fun newRandomAESKey(): SecretKey {
        val keyGenerator: KeyGenerator? = try {
            KeyGenerator.getInstance("AES")
        } catch (noSuchAlgorithmException: NoSuchAlgorithmException) {
            throw RuntimeException(noSuchAlgorithmException)
        }
        keyGenerator!!.init(128)
        return keyGenerator.generateKey()
    }

    @Contract(value = "_ -> new", pure = true)
    fun newRandomAESKey(keyData: ByteArray?): SecretKey {
        return SecretKeySpec(keyData, "AES")
    }

    @Contract(" -> new")
    fun newRandomIV(): IvParameterSpec {
        val ivBytes = ByteArray(16)
        SecureRandom().nextBytes(ivBytes)
        return IvParameterSpec(ivBytes)
    }
}

