package me.earthme.mysm.utils

import java.io.*
import java.util.function.Consumer
import java.util.zip.ZipInputStream

object FileUtils {
    fun fileNameWithoutExtension(input: String): String{
        val dotIndex: Int = input.lastIndexOf(".")
        return if (dotIndex == -1) input else input.substring(0, dotIndex)
    }

    @Throws(IOException::class)
    private fun extractFile(zipIn: ZipInputStream, filePath: String, buffer: ByteArray) {
        val bos = BufferedOutputStream(FileOutputStream(filePath))
        var len: Int
        while (zipIn.read(buffer).also { len = it } > 0) {
            bos.write(buffer, 0, len)
        }
        bos.close()
    }

    fun unzip(zipFile: File?, destDirectory: File, override: Boolean) {
        try {
            if (destDirectory.exists() && override) {
                destDirectory.delete()
            }
            if (destDirectory.mkdir()) {
                val zipIn = zipFile?.let { FileInputStream(it) }?.let { ZipInputStream(it) }
                var entry = zipIn?.nextEntry
                val buffer = ByteArray(1024)
                while (entry != null) {
                    val filePath = destDirectory.toString() + File.separator + entry.name
                    if (!entry.isDirectory) {
                        zipIn?.let {
                            extractFile(it, filePath, buffer)
                        }
                    } else {
                        val dir = File(filePath)
                        dir.mkdirs()
                    }
                    if (zipIn != null) {
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
                zipIn?.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    @Throws(IOException::class)
    fun readInputStream(inputStream: InputStream): ByteArrayInputStream {
        val buffer = ByteArray('Ѐ'.code)
        var len: Int
        val bos = ByteArrayOutputStream()
        while (inputStream.read(buffer).also { len = it } != -1) {
            bos.write(buffer, 0, len)
        }
        bos.close()
        return ByteArrayInputStream(bos.toByteArray())
    }

    @Throws(IOException::class)
    fun readInputStreamToByte(inputStream: InputStream): ByteArray? {
        val buffer = ByteArray('Ѐ'.code)
        var len: Int
        val bos = ByteArrayOutputStream()
        while (inputStream.read(buffer).also { len = it } != -1) {
            bos.write(buffer, 0, len)
        }
        bos.close()
        return bos.toByteArray()
    }


    fun forEachFolder(folder: File,action: Consumer<File>){
        if (!folder.isDirectory){
            return
        }

        folder.listFiles()?.let{
            for (singleFile in it){
                if (singleFile.isDirectory){
                    forEachFolder(singleFile,action)
                }

                action.accept(singleFile)
            }
        }
    }
}