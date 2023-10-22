package me.earthme.mysm.utils

import java.security.*
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

object RSAEncryptUtils {
    fun getPublicKeyEncoded(publicKey: PublicKey): ByteArray{
        val keySpec = X509EncodedKeySpec(publicKey.encoded)
        return keySpec.encoded
    }

    fun getPublicKeyFromBytes(publicKeyBytes: ByteArray): PublicKey {
        val keySpec = X509EncodedKeySpec(publicKeyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(1024)
        return keyPairGenerator.generateKeyPair()
    }

    fun rsaEncrypt(data: ByteArray?, privateKey: PrivateKey): ByteArray {
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.ENCRYPT_MODE, privateKey)
        return cipher.doFinal(data)
    }

    fun rsaDecrypt(data: ByteArray?, publicKey: PublicKey): ByteArray {
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.DECRYPT_MODE, publicKey)
        return cipher.doFinal(data)
    }
}