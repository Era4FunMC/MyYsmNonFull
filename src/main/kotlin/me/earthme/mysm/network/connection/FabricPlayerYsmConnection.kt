package me.earthme.mysm.network.connection

import com.github.retrooper.packetevents.PacketEvents
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import me.earthme.mysm.utils.SchedulerUtils
import me.earthme.mysm.data.PlayerModelData
import me.earthme.mysm.events.PlayerChangeModelEvent
import me.earthme.mysm.manager.ModelPermissionManager
import me.earthme.mysm.model.loaders.VersionedCacheLoader
import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.network.YsmClientConnectionManager
import me.earthme.mysm.network.YsmClientConnectionManager.getConnection
import me.earthme.mysm.utils.AsyncExecutor
import me.earthme.mysm.network.YsmPacketHelper
import me.earthme.mysm.utils.mc.MCPacketCodecUtils
import me.earthme.mysm.utils.ysm.EncryptUtils
import me.earthme.mysm.utils.ysm.MiscUtils
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class FabricPlayerYsmConnection(
    private val player: Player,
    private val pluginInstance: Plugin
) : PlayerYsmConnection {
    companion object{
        const val NETWORK_CHANNEL_NAMESPACE = "yes_steve_model"
    }

    override fun isChannelNameMatched(channel: String): Boolean {
        if (channel == "$NETWORK_CHANNEL_NAMESPACE:network"){
            return false
        }

        val split = channel.split(":")

        if (split.size != 2){
            return false
        }

        return split[0] == NETWORK_CHANNEL_NAMESPACE
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

    override fun sendPacket(packetData: ByteBuf,packetId: NamespacedKey) {
        YsmPacketHelper.sendCustomPayLoad(this.player,packetId,packetData)
    }

    override fun onMessageIncoming(key: NamespacedKey, packetData: ByteArray) {
        val resourceLocationStringSplit = key.toString().split(":")
        if (resourceLocationStringSplit.size == 2 && resourceLocationStringSplit[0] == YsmPacketHelper.NETWORK_CHANNEL_NAMESPACE){
            SchedulerUtils.schedulerAsExecutor(this.player.location).execute {
                this.processInternal(resourceLocationStringSplit[1], Unpooled.wrappedBuffer(packetData))
            }
        }
    }

    private fun processInternal(idString: String,byteBuf: ByteBuf){
        val playerProtocolVersion = PacketEvents.getAPI().playerManager.getClientVersion(this.player).protocolVersion
        when(Integer.parseInt(idString)){
            5 -> {
                //TODO Limit the packet speed?
                val targetModelResourceLocation: NamespacedKey = MCPacketCodecUtils.readResourceLocation(byteBuf)
                val targetModelTextureResourceLocation: NamespacedKey = MCPacketCodecUtils.readResourceLocation(byteBuf)

                val playerChangeModelEvent = PlayerChangeModelEvent(this.player,targetModelResourceLocation,targetModelTextureResourceLocation)

                //Check model access
                if ((!ModelPermissionManager.isModelNeedAuth(targetModelResourceLocation) || ModelPermissionManager.isPlayerHeldModel(this.player,targetModelResourceLocation)) && playerChangeModelEvent.callEvent()){
                    val targetData = PlayerDataManager.createOrGetPlayerData(this.player.name)
                    targetData.mainResourceLocation = targetModelResourceLocation
                    targetData.mainTextPngResourceLocation = targetModelTextureResourceLocation
                    targetData.isDirty = true

                    for (singlePlayer in Bukkit.getOnlinePlayers()){
                        singlePlayer.getConnection()?.sendModelUpdate(this.player)
                    }

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
                    }, VersionedCacheLoader.getVersionMeta("fabric",playerProtocolVersion)!!)

                    val passwordData = VersionedCacheLoader.getPasswordData()
                    val processedPasswordData = EncryptUtils.encryptDataWithKnownKey(MiscUtils.uuidToByte(this.player.uniqueId),passwordData) //Encrypt logic in ysm
                    this.pluginInstance.logger.info("Password data length:" + processedPasswordData.size) //Debug //TODO Remove this
                    this.sendModelOrPasswordData(processedPasswordData) //Send password data
                }
            }

            7 ->{
                val id: Int = byteBuf.readInt() //Animation id
                if (-1 <= id && id < 8) {
                    val currentHeld: PlayerModelData = PlayerDataManager.createOrGetPlayerData(this.player.name)
                    currentHeld.sendAnimation = true //Set send latch to true to make the players around could see the player's animation
                    if (id != -1) {
                        me.earthme.mysm.utils.MiscUtils.playAnimationOnPlayer(this.player,"extra$id")
                    } else {
                        currentHeld.doAnimation = false
                    }
                }
            }
        }
    }

    override fun sendHeldModes(models: Set<NamespacedKey>){
        val channelName = NamespacedKey(NETWORK_CHANNEL_NAMESPACE,"6")
        val dataBuf = YsmPacketHelper.heldModelsData(Unpooled.buffer(),models)

        this.sendPacket(dataBuf,channelName)
    }

    override fun sendReload(){
        val channelName = NamespacedKey(NETWORK_CHANNEL_NAMESPACE,"2")
        val dataBuf: ByteBuf = YsmPacketHelper.reloadPacketData(Unpooled.buffer())

        this.sendPacket(dataBuf,channelName)
    }

    override fun sendModelUpdate(ownerEntity: Player){
        val channelName = NamespacedKey(NETWORK_CHANNEL_NAMESPACE,"4")
        val dataBuf = YsmPacketHelper.modelUpdatePacketData(Unpooled.buffer(),ownerEntity)

        this.sendPacket(dataBuf,channelName)
    }

    override fun sendMd5Contained(containedMd5: String){
        val channelName = NamespacedKey(NETWORK_CHANNEL_NAMESPACE,"3")
        val dataBuffer: ByteBuf = YsmPacketHelper.md5ContainedPacketData(Unpooled.buffer(),containedMd5)

        this.sendPacket(dataBuffer,channelName)
    }

    override fun sendModelOrPasswordData(data: ByteArray){
        val channelName = NamespacedKey(NETWORK_CHANNEL_NAMESPACE,"1")
        val dataBuf: ByteBuf = YsmPacketHelper.cacheDataPacketData(Unpooled.buffer(),data)

        this.sendPacket(dataBuf,channelName)
    }
}
