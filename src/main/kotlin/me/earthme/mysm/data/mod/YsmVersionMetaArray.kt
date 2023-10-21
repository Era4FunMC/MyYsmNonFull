package me.earthme.mysm.data.mod

import com.google.gson.Gson

data class YsmVersionMetaArray (
    val versionMetas: Array<YsmVersionMeta>
){
    companion object{
        private val gson: Gson = Gson()

        fun readFromJson(data: String): YsmVersionMetaArray {
            return gson.fromJson(data, YsmVersionMetaArray::class.java)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as YsmVersionMetaArray

        return versionMetas.contentEquals(other.versionMetas)
    }

    override fun hashCode(): Int {
        return versionMetas.contentHashCode()
    }
}