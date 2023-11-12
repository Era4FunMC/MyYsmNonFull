package me.earthme.mysm.network.connection

import com.github.retrooper.packetevents.PacketEvents
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import me.earthme.mysm.utils.SchedulerUtils
import me.earthme.mysm.data.PlayerModelData
import me.earthme.mysm.events.PlayerChangeModelEvent
import me.earthme.mysm.events.PlayerExtraAnimationEvent
import me.earthme.mysm.manager.ModelPermissionManager
import me.earthme.mysm.model.loaders.VersionedCacheLoader
import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.network.YsmClientConnectionManager
import me.earthme.mysm.network.YsmClientConnectionManager.getConnection
import me.earthme.mysm.utils.AsyncExecutor
import me.earthme.mysm.network.YsmPacketHelper
import me.earthme.mysm.utils.mc.MCPacketCodecUtils
import me.earthme.mysm.utils.ysm.AESEncryptUtils
import me.earthme.mysm.utils.ysm.MiscUtils
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class ForgePlayerYsmConnection(
    private val player: Player,
    private val pluginInstance: Plugin
): PlayerYsmConnection {
    companion object {
        private val CHANNEL = NamespacedKey("yes_steve_model", "network")
    }

    override fun isChannelNameMatched(channel: String): Boolean {
        return CHANNEL.toString() == channel
    }

    override fun tick() {
        val playerData = PlayerDataManager.createOrGetPlayerData(this.player.name)
        if (playerData.sendAnimation){
            for (singlePlayer in Bukkit.getOnlinePlayers()){
                singlePlayer.getConnection()?.sendModelUpdate(this.player)
            }
            playerData.sendAnimation = false
        }
    }

    override fun onTrackerUpdate(see: Player) {
        this.sendModelUpdate(see)
    }

    override fun onPlayerJoin(player: Player) {
        //Send reload & model update
        this.sendReload()
        //Set the player model to default before sync
        PlayerDataManager.setToDefaultIfIncorrect(this.player)
        //Sync models the players currently held
        this.sendHeldModes(ModelPermissionManager.getHeldModelsOfPlayer(this.player))
        //Sync current model
        this.sendModelUpdate(this.player)
    }

    override fun onPlayerLeft(player: Player) {
        //Remove player from the installed list
        YsmClientConnectionManager.modInstalledPlayerList.remove(player)
    }

    override fun sendPacket(packetData: ByteBuf, packetId: NamespacedKey) {
        YsmPacketHelper.sendCustomPayLoad(this.player,packetId,packetData)
    }

    override fun onMessageIncoming(key: NamespacedKey, packetData: ByteArray) {
        val byteBuf = Unpooled.copiedBuffer(packetData)
        SchedulerUtils.schedulerAsExecutor(this.player.location).execute { this.processInternal(byteBuf.readByte(),byteBuf) }
    }

    private fun processInternal(id: Byte,byteBuf: ByteBuf){
        val playerProtocolVersion = PacketEvents.getAPI().playerManager.getClientVersion(this.player).protocolVersion
        when(id.toInt()){
            5 -> {
                //TODO Limit the packet speed?
                val targetModelResourceLocation: NamespacedKey = MCPacketCodecUtils.readResourceLocation(byteBuf)
                val targetModelTextureResourceLocation: NamespacedKey = MCPacketCodecUtils.readResourceLocation(byteBuf)

                val playerChangeModelEvent = PlayerChangeModelEvent(this.player,targetModelResourceLocation,targetModelTextureResourceLocation)

                //Check model access
                if ((!ModelPermissionManager.isModelNeedAuth(targetModelResourceLocation) || ModelPermissionManager.isPlayerHeldModel(this.player,targetModelResourceLocation)) && playerChangeModelEvent.callEvent()){
                    me.earthme.mysm.utils.MiscUtils.setModelForPlayer(this.player,targetModelResourceLocation,targetModelTextureResourceLocation)
                    this.pluginInstance.logger.info("Player ${this.player.name} has changed model to $targetModelResourceLocation")
                }else{
                    this.pluginInstance.logger.info("Player ${this.player.name} has tried to use a un-authed model")
                    this.sendModelUpdate(this.player) //Correct the player model it currently has
                }
            }

            0 ->{
                //TODO Limit the packet speed?
                //Push to an async thread to execute
                AsyncExecutor.ASYNC_EXECUTOR_INSTANCE.execute {
                    this.pluginInstance.logger.info("Sending models to player ${this.player.name}")
                    //Add to the installed list
                    if(!YsmClientConnectionManager.modInstalledPlayerList.contains(this.player)){
                        YsmClientConnectionManager.modInstalledPlayerList.add(this.player)
                    }

                    val size = MCPacketCodecUtils.readVarInt(byteBuf) //Md5Hit-List-Size
                    val alreadyExists: MutableList<String> = ArrayList()
                    for (i in 0 until size){
                        alreadyExists.add(MCPacketCodecUtils.readUtf(32767,byteBuf)) //Read single data
                    }

                    VersionedCacheLoader.getCachesWithoutMd5Contained(alreadyExists,{
                        this.sendModelOrPasswordData(it) //If not contained
                    },{
                        this.sendMd5Contained(it) //If the md5 is in the cache list
                    }, VersionedCacheLoader.getVersionMeta("forge",playerProtocolVersion)!!)

                    val passwordData = VersionedCacheLoader.getPasswordData()
                    val processedPasswordData = AESEncryptUtils.encryptDataWithKnownKey(MiscUtils.uuidToByte(this.player.uniqueId),passwordData) //Encrypt logic in ysm
                    this.pluginInstance.logger.info("Password data length:" + processedPasswordData.size) //Debug //TODO Remove this
                    this.sendModelOrPasswordData(processedPasswordData) //Send password data
                }
            }

            7 ->{
                val aid: Int = byteBuf.readInt() //Animation id
                if (-1 <= aid && aid < 8 && PlayerExtraAnimationEvent(this.player,aid).callEvent()) {
                    val currentHeld: PlayerModelData = PlayerDataManager.createOrGetPlayerData(this.player.name)
                    currentHeld.sendAnimation = true //Set send latch to true to make the players around could see the player's animation
                    if (aid != -1) {
                        me.earthme.mysm.utils.MiscUtils.playAnimationOnPlayer(this.player,"extra$aid")
                    } else {
                        currentHeld.doAnimation = false
                    }
                }
            }
        }
    }


    override fun sendHeldModes(models: Set<NamespacedKey>){
        val dataBufPre = Unpooled.buffer()
        dataBufPre.writeByte(6 and 255)

        val dataBuf = YsmPacketHelper.heldModelsData(dataBufPre,models)

        this.sendPacket(dataBuf, CHANNEL)
    }

    override fun sendReload(){
        val dataBufPre = Unpooled.buffer()
        dataBufPre.writeByte(2 and 255)

        val dataBuf: ByteBuf = YsmPacketHelper.reloadPacketData(dataBufPre)

        this.sendPacket(dataBuf, CHANNEL)
    }

    override fun sendModelUpdate(ownerEntity: Player){
        val dataBufPre = Unpooled.buffer()
        dataBufPre.writeByte(4 and 255)

        val dataBuf = YsmPacketHelper.modelUpdatePacketDataForge(dataBufPre,ownerEntity)

        this.sendPacket(dataBuf, CHANNEL)
    }

    override fun sendMd5Contained(containedMd5: String){
        val dataBufPre = Unpooled.buffer()
        dataBufPre.writeByte(3 and 255)

        val dataBuffer: ByteBuf = YsmPacketHelper.md5ContainedPacketData(dataBufPre,containedMd5)

        this.sendPacket(dataBuffer, CHANNEL)
    }

    override fun sendModelOrPasswordData(data: ByteArray){
        val dataBufPre = Unpooled.buffer()
        dataBufPre.writeByte(1 and 255)

        val dataBuf: ByteBuf = YsmPacketHelper.cacheDataPacketData(dataBufPre,data)

        this.sendPacket(dataBuf, CHANNEL)
    }
}