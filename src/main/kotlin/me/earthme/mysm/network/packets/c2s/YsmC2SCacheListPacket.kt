package me.earthme.mysm.network.packets.c2s

import com.github.retrooper.packetevents.PacketEvents
import io.netty.buffer.ByteBuf
import me.earthme.mysm.MyYSM
import me.earthme.mysm.data.mod.YsmVersionMeta
import me.earthme.mysm.model.loaders.VersionedCacheLoader
import me.earthme.mysm.network.EnumConnectionType
import me.earthme.mysm.network.YsmClientConnectionManager
import me.earthme.mysm.network.YsmClientConnectionManager.getConnection
import me.earthme.mysm.network.packets.IYsmPacket
import me.earthme.mysm.network.packets.s2c.YsmS2CCacheHitPacket
import me.earthme.mysm.network.packets.s2c.YsmS2CModelDataPacket
import me.earthme.mysm.utils.AsyncExecutor
import me.earthme.mysm.utils.AutoDiscardStack
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.readUtf
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.readVarInt
import me.earthme.mysm.utils.ysm.AESEncryptUtils
import me.earthme.mysm.utils.ysm.YsmCodecUtil
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap



class YsmC2SCacheListPacket : IYsmPacket {
    private val maxRequestPerSec = 15
    private val lastRequestTimes = ConcurrentHashMap<Player, AutoDiscardStack<Long>>()
    private val md5List: MutableList<String> = ArrayList()

    private fun canProcessRequest(player: Player): Boolean {
        // Create a AutoDiscardStack if not exist
        if (!this.lastRequestTimes.containsKey(player)) {
            this.lastRequestTimes[player] = AutoDiscardStack(maxRequestPerSec)
            this.lastRequestTimes[player]!!.push(System.currentTimeMillis())
            return true
        }

        // judge requests in one second
        return (System.currentTimeMillis() - this.lastRequestTimes[player]!!.first) >= 1
    }

    override fun process(connectionType: EnumConnectionType,player: Player) {
        if (!this.canProcessRequest(player)) {
            MyYSM.instance!!.logger.warning("${player.name()} is sending too many packets to MyYSM")
            return
        }

        val playerProtocolVersion = PacketEvents.getAPI().playerManager.getClientVersion(player).protocolVersion
        AsyncExecutor.ASYNC_EXECUTOR_INSTANCE.execute {
            //Add to the installed list
            if(!YsmClientConnectionManager.modInstalledPlayerList.contains(player)){
                YsmClientConnectionManager.modInstalledPlayerList.add(player)
            }
            MyYSM.instance!!.logger.info("Player ${player.name} has requested for model cache")

            val matchedVersionMeta: YsmVersionMeta = VersionedCacheLoader.getVersionMeta(connectionType.getModLoaderName(),playerProtocolVersion) ?: return@execute

            VersionedCacheLoader.getCachesWithoutMd5Contained(this.md5List,{
                player.getConnection()!!.sendPacket(YsmS2CModelDataPacket(it)) //If not contained
            },{
                player.getConnection()!!.sendPacket(YsmS2CCacheHitPacket(it)) //If the md5 is in the cache list
            }, matchedVersionMeta)

            MyYSM.instance!!.logger.info("Sent missing model caches to player ${player.name}")

            val passwordData = VersionedCacheLoader.getPasswordData() //From cache loader
            val processedPasswordData = AESEncryptUtils.encryptAES(YsmCodecUtil.uuidToByte(player.uniqueId),passwordData) //Encrypt logic in ysm
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