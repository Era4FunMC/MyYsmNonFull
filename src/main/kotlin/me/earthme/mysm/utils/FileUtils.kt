package me.earthme.mysm.utils

import java.io.*
import java.util.function.Consumer
import java.util.zip.ZipInputStream


object FileUtils {
    fun readZipFile(zipFile: File): Map<String, ByteArray> {
        val fileMap: MutableMap<String, ByteArray> = HashMap()
        try {
            ZipInputStream(FileInputStream(zipFile)).use { zipInputStream ->
                var entry = zipInputStream.getNextEntry()
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val fileName = entry.name
                        val outputStream = ByteArrayOutputStream()
                        val buffer = ByteArray(1024)
                        var length: Int
                        while (zipInputStream.read(buffer).also { length = it } > 0) {
                            outputStream.write(buffer, 0, length)
                        }
                        val fileData = outputStream.toByteArray()
                        fileMap[fileName] = fileData
                        outputStream.close()
                    }
                    zipInputStream.closeEntry()
                    entry = zipInputStream.getNextEntry()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return fileMap
    }

    fun fileNameWithoutExtension(input: String): String{
        val dotIndex: Int = input.lastIndexOf(".")
        return if (dotIndex == -1) input else input.substring(0, dotIndex)
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