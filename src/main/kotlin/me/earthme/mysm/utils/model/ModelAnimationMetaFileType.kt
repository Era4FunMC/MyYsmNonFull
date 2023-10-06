package me.earthme.mysm.utils.model

enum class ModelAnimationMetaFileType(
    private val fileName: String
){
    MAIN("main.animation.json"),
    ARM("arm.animation.json"),
    EXTRA("extra.animation.json");

    fun getFileName(): String{
        return this.fileName
    }
}
