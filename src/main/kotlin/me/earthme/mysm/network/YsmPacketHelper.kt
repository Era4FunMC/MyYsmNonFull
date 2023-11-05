package me.earthme.mysm.network

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage
import io.netty.buffer.ByteBuf
import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.utils.mc.MCPacketCodecUtils
import me.earthme.mysm.utils.ysm.YsmNbtUtils
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

object YsmPacketHelper {
    const val NETWORK_CHANNEL_NAMESPACE = "yes_steve_model"

    fun heldModelsData(dataBuf: ByteBuf,models: Set<NamespacedKey>): ByteBuf{
        MCPacketCodecUtils.writeVarInt(models.size,dataBuf)
        for (singleModel in models){
            MCPacketCodecUtils.writeUtf(singleModel.toString(),32767,dataBuf)
        }

        return dataBuf
    }

    fun reloadPacketData(dataBuf: ByteBuf): ByteBuf{
        //dataBuf.writeByte(1) //Garbage message
        return dataBuf
    }

    fun sendCustomPayLoad(target: Player, channel: NamespacedKey, data: ByteBuf){
        val byteArray = ByteArray(data.readableBytes())
        data.readBytes(byteArray)
        PacketEvents.getAPI().playerManager.sendPacket(target,WrapperPlayServerPluginMessage(channel.toString(),byteArray))
    }

    fun modelUpdatePacketData(dataBuf: ByteBuf,ownerEntity: Player): ByteBuf{
        val modelData = PlayerDataManager.createOrGetPlayerData(ownerEntity.name)
        MCPacketCodecUtils.writeVarInt(ownerEntity.entityId,dataBuf)
        MCPacketCodecUtils.writeUUID(ownerEntity.uniqueId,dataBuf)//Fabric only
        dataBuf.writeBytes(YsmNbtUtils.createNbtForSync(modelData))
        return dataBuf
    }

    fun modelUpdatePacketDataForge(dataBuf: ByteBuf,ownerEntity: Player): ByteBuf{
        val modelData = PlayerDataManager.createOrGetPlayerData(ownerEntity.name)
        MCPacketCodecUtils.writeVarInt(ownerEntity.entityId,dataBuf)
        dataBuf.writeBytes(YsmNbtUtils.createNbtForSync(modelData))
        return dataBuf
    }

    fun md5ContainedPacketData(dataBuffer: ByteBuf,containedMd5: String): ByteBuf{
        MCPacketCodecUtils.writeUtf(containedMd5,32767,dataBuffer)
        return dataBuffer
    }

    fun cacheDataPacketData(dataBuf: ByteBuf,data: ByteArray): ByteBuf{
        MCPacketCodecUtils.writeVarInt(data.size,dataBuf)
        dataBuf.writeBytes(data)
        return dataBuf
    }
}