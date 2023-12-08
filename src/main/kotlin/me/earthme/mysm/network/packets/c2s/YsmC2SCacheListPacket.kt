package me.earthme.mysm.network.packets.c2s

import com.github.retrooper.packetevents.PacketEvents
import io.netty.buffer.ByteBuf
import me.earthme.mysm.data.mod.YsmVersionMeta
import me.earthme.mysm.model.loaders.VersionedCacheLoader
import me.earthme.mysm.network.EnumConnectionType
import me.earthme.mysm.network.YsmClientConnectionManager
import me.earthme.mysm.network.YsmClientConnectionManager.getConnection
import me.earthme.mysm.network.packets.IYsmPacket
import me.earthme.mysm.network.packets.s2c.YsmS2CCacheHitPacket
import me.earthme.mysm.network.packets.s2c.YsmS2CModelDataPacket
import me.earthme.mysm.utils.AsyncExecutor
import me.earthme.mysm.utils.mc.MCPacketCodecUtils
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.readUtf
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.readVarInt
import me.earthme.mysm.utils.ysm.AESEncryptUtils
import me.earthme.mysm.utils.ysm.MiscUtils
import org.bukkit.entity.Player

//TODO - Limit the speed of this packet?
class YsmC2SCacheListPacket : IYsmPacket {
    private val md5List: MutableList<String> = ArrayList()

    override fun process(connectionType: EnumConnectionType,player: Player) {
        val playerProtocolVersion = PacketEvents.getAPI().playerManager.getClientVersion(player).protocolVersion
        AsyncExecutor.ASYNC_EXECUTOR_INSTANCE.execute {
            //Add to the installed list
            if(!YsmClientConnectionManager.modInstalledPlayerList.contains(player)){
                YsmClientConnectionManager.modInstalledPlayerList.add(player)
            }

            val matchedVersionMeta: YsmVersionMeta = VersionedCacheLoader.getVersionMeta(connectionType.getModLoaderName(),playerProtocolVersion) ?: return@execute

            VersionedCacheLoader.getCachesWithoutMd5Contained(this.md5List,{
                player.getConnection()!!.sendPacket(YsmS2CModelDataPacket(it)) //If not contained
            },{
                player.getConnection()!!.sendPacket(YsmS2CCacheHitPacket(it)) //If the md5 is in the cache list
            }, matchedVersionMeta)

            val passwordData = VersionedCacheLoader.getPasswordData() //From cache loader
            val processedPasswordData = AESEncryptUtils.encryptDataWithKnownKey(MiscUtils.uuidToByte(player.uniqueId),passwordData) //Encrypt logic in ysm
            player.getConnection()!!.sendPacket(YsmS2CModelDataPacket(processedPasswordData)) //Send password data
        }
    }

    override fun writePacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        //We are not client
    }

    override fun readPacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        val size = dataBuf.readVarInt()
        for (i in 0 until size){
            this.md5List.add(dataBuf.readUtf(32767))
        }
    }

    override fun getPacketId(): Int {
        return 0
    }
}