package me.earthme.mysm.model

import java.io.File
import java.util.function.Function

interface IModelLoader {
    fun canLoad(modelFile: File): Boolean

    fun loadModel(modelFile: File,authChecker: Function<String,Boolean>): YsmModelData
}