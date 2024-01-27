package me.earthme.mysm.model

import me.earthme.mysm.data.mod.management.EnumModelFileType
import java.io.File
import java.util.function.Function

/**
 * 单个模型加载器的接口
 */
interface IModelLoader {
    /**
     * 这个方法的返回值决定了模型是否会经由这个加载器加载，如果你想要加载某个条件下的文件，这个方法应该返回true
     * @param modelFile 模型文件，可能是目录也可能是文件，具体看模型文件夹下的内容
     * @return 这个模型加载器是否可以加载这个文件
     */
    fun canLoad(modelFile: File): Boolean

    /**
     * 加载模型
     * @param modelFile 模型文件
     * @param authChecker 验证检查用的function，如果这个function返回了true则这个模型会被加载为需要授权的模型
     * @return 加载好的模型数据
     */
    fun loadModel(modelFile: File,authChecker: Function<String,Boolean>): YsmModelData

    fun getFileType(): EnumModelFileType
}