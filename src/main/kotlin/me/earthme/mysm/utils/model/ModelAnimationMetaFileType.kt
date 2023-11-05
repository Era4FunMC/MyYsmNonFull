package me.earthme.mysm.utils.model

/**
 * 模型动作类型的枚举类,具体对应的哪个动作文件可以看下面()
 */
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
