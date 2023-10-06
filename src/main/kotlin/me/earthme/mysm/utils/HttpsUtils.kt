package me.earthme.mysm.utils

import java.net.HttpURLConnection
import java.net.URL

object HttpsUtils {
    fun downloadFrom(url1: String?): ByteArray? {
        try {
            val url = URL(url1)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.47")
            connection.setReadTimeout(30000)
            connection.setConnectTimeout(3000)
            connection.connect()
            try {
                if (connection.responseCode === 200) {
                    return FileUtils.readInputStreamToByte(connection.inputStream)
                } else {
                    throw IllegalStateException(
                        "Response code:${connection.getResponseCode()} Response:${String(FileUtils.readInputStreamToByte(connection.inputStream)!!)}",
                    )
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

}