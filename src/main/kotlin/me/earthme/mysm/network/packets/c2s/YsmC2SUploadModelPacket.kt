package me.earthme.mysm.network.packets.c2s

import io.netty.buffer.ByteBuf
import me.earthme.mysm.MyYSM
import me.earthme.mysm.data.mod.management.EnumModelActionScope
import me.earthme.mysm.model.loaders.GlobalModelLoader
import me.earthme.mysm.network.EnumConnectionType
import me.earthme.mysm.network.YsmClientConnectionManager.getConnection
import me.earthme.mysm.network.packets.IYsmPacket
import me.earthme.mysm.network.packets.s2c.YsmS2CModelUploadedPacket
import me.earthme.mysm.utils.AsyncExecutor
import me.earthme.mysm.utils.api.MiscUtils
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.readByteArray
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.readUtf
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.readVarInt
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException
import java.nio.file.Files

class YsmC2SUploadModelPacket : IYsmPacket{
    private lateinit var fileName: String
    private lateinit var fileData: ByteArray
    private lateinit var actionScope: EnumModelActionScope

    override fun process(connectionType: EnumConnectionType, player: Player) {
        if (!player.hasPermission("myysm.model.management")){
            return
        }

        AsyncExecutor.ASYNC_EXECUTOR_INSTANCE.execute{
            try{
                //TODO Any file type and size checks?
                val targetFile = File(GlobalModelLoader.getModelDir(),this.fileName)
                if (targetFile.exists()){
                    return@execute
                }

                Files.write(targetFile.toPath(),this.fileData)

                MiscUtils.hotLoadModelFile(targetFile)
            }catch (e : Exception){
                if (e is IOException || e is IllegalArgumentException){
                    //Do nothing because this would be an illegal model file or no target loader found for this file
                    MyYSM.instance!!.logger.info("Player ${player.name} has uploaded an illegal model file! File name: ${this.fileName}")
                    return@execute
                }

                e.printStackTrace()
            }

            player.getConnection()?.sendPacket(YsmS2CModelUploadedPacket())
        }
    }

    override fun writePacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        //We are not client to send this packet
    }

    override fun readPacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        this.fileName = dataBuf.readUtf(32767)
        this.fileData = dataBuf.readByteArray()
        this.actionScope = EnumModelActionScope.values()[dataBuf.readVarInt()]
    }

    override fun getPacketId(): Int {
        return 11
    }
}