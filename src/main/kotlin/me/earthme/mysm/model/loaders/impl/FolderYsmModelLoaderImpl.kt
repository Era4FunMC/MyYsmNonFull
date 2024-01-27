package me.earthme.mysm.model.loaders.impl

import com.google.common.collect.Maps
import me.earthme.mysm.ResourceConstants
import me.earthme.mysm.data.mod.management.EnumModelFileType
import me.earthme.mysm.model.IModelLoader
import me.earthme.mysm.model.YsmModelData
import java.io.File
import java.nio.file.Files
import java.util.function.Function

class FolderYsmModelLoaderImpl: IModelLoader {
    override fun canLoad(modelFile: File): Boolean {
        return modelFile.isDirectory
    }

    override fun getFileType(): EnumModelFileType {
        return EnumModelFileType.FOLDER
    }

    override fun loadModel(modelFile: File, authChecker: Function<String, Boolean>): YsmModelData {
        val metaData: MutableMap<String, ByteArray> = Maps.newHashMap()
        val animationData: MutableMap<String, ByteArray> = Maps.newHashMap()
        val textureData: MutableMap<String, ByteArray> = Maps.newHashMap()

        val missingFileNames: MutableList<String> = mutableListOf(
            "main.json",
            "arm.json",
            "main.animation.json",
            "arm.animation.json",
            "extra.animation.json"
        )

        for (singleUnknownFile in modelFile.listFiles()!!) {
            val fileName = singleUnknownFile.name
            val read = Files.readAllBytes(singleUnknownFile.toPath())
            missingFileNames.remove(fileName)
            if (!fileName.endsWith(".png")) {
                when (fileName) {
                    "main.json" -> metaData["main"] = read
                    "arm.json" -> metaData["arm"] = read
                    "main.animation.json" -> animationData["main"] = read
                    "arm.animation.json" -> animationData["arm"] = read
                    "extra.animation.json" -> animationData["extra"] = read
                }
            } else {
                textureData[fileName] = read
            }
        }

        for (missingFileName in missingFileNames) {
            require(!(missingFileName == "main.json" || missingFileName == "arm.json")) {
                "Model meta has not found!Missing: $missingFileName"
            }

            when (missingFileName) {
                "main.animation.json" -> animationData["main"] = ResourceConstants.defaultMainAnimationJsonContent!!.toByteArray()

                "arm.animation.json" -> animationData["arm"] = ResourceConstants.defaultArmAnimationJsonContent!!.toByteArray()
                "extra.animation.json" -> animationData["extra"] = ResourceConstants.defaultExtraAnimationJsonContent!!.toByteArray()
            }
        }

        return YsmModelData(modelFile.name,authChecker,metaData,animationData,textureData)
    }
}