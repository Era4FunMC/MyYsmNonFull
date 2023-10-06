package me.earthme.mysm.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import java.io.File
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap

data class ModelPermissionData (
    val adminList: MutableSet<String>,
    val modelsNeedAuth: MutableSet<String>,
    val playerHeldModelInfo: MutableSet<PlayerHeldModelData>,
    @Volatile @Transient
    var isDirty: Boolean = false
){
    companion object{
        private val gson: Gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()

        fun fromFile(file: File): ModelPermissionData{
            val readContent = Files.readString(file.toPath())
            return gson.fromJson(readContent,ModelPermissionData::class.java)
        }
    }

    fun setModelNeedAuth(modelLocation: NamespacedKey,needAuth: Boolean){
        this.isDirty = true
        if (needAuth){
            this.modelsNeedAuth.add(modelLocation.toString())
        }else{
            this.modelsNeedAuth.remove(modelLocation.toString())
        }
    }

    fun removePlayerHeldModel(modelLocation: NamespacedKey,player: Player){
        this.isDirty = true
        val modelLocationString = modelLocation.toString()

        for (singleInfo in this.playerHeldModelInfo){
            if (singleInfo.playerName == player.name){
                singleInfo.heldModels.remove(modelLocationString)
            }
            break
        }
    }

    fun addPlayerHeldModel(modelLocation: NamespacedKey,player: Player){
        this.isDirty = true
        val modelLocationString = modelLocation.toString()

        for (singleInfo in this.playerHeldModelInfo){
            if (singleInfo.playerName == player.name){
                if (singleInfo.heldModels.contains(modelLocationString)){
                    return
                }
                singleInfo.heldModels.add(modelLocationString)
                break
            }
        }

        val newInfo = PlayerHeldModelData(player.name,ConcurrentHashMap.newKeySet())
        newInfo.heldModels.add(modelLocationString)
        this.playerHeldModelInfo.add(newInfo)
    }

    fun doesPlayerHeldModel(modelLocation: NamespacedKey,player: Player): Boolean{
        val modelLocationString = modelLocation.toString()
        for (singleInfo in this.playerHeldModelInfo){
            if (singleInfo.playerName == player.name){
                return singleInfo.heldModels.contains(modelLocationString)
            }
        }

        return false
    }

    fun writeToFile(targetFile: File){
        val encoded = gson.toJson(this)
        Files.writeString(targetFile.toPath(),encoded)
    }

    fun isModelNeedAuth(modelLocation: NamespacedKey): Boolean{
        return this.modelsNeedAuth.contains(modelLocation.toString())
    }

    fun getAllHeldDataOfPlayer(player: Player): Set<NamespacedKey>{
        val ret: MutableSet<NamespacedKey> = HashSet()
        for (data in playerHeldModelInfo){
            if (data.playerName == player.name){
                for (modelLocationString in data.heldModels){
                    ret.add(NamespacedKey.fromString(modelLocationString)!!)
                }
                break
            }
        }

        return ret
    }
}