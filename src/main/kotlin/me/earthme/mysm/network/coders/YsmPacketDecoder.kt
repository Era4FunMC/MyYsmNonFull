package me.earthme.mysm.network.coders

import io.netty.buffer.ByteBuf
import me.earthme.mysm.network.EnumConnectionType
import me.earthme.mysm.network.EnumConnectionType.*
import me.earthme.mysm.network.packets.IYsmPacket
import me.earthme.mysm.network.packets.c2s.YsmC2SCacheListPacket
import me.earthme.mysm.network.packets.c2s.YsmC2SExtraAnimationPacket
import me.earthme.mysm.network.packets.c2s.YsmC2SModelChangePacket
import org.bukkit.NamespacedKey

class YsmPacketDecoder (
    private val packetRegistryMap: Map<Int,Class<*>>
){
    companion object{
        val INSTANCE: YsmPacketDecoder

        init {
            val registryMap: MutableMap<Int,Class<*>> = HashMap()

            //Default c->s packet registry
            registryMap[0] = YsmC2SCacheListPacket::class.java
            registryMap[7] = YsmC2SExtraAnimationPacket::class.java
            registryMap[5] = YsmC2SModelChangePacket::class.java

            this.INSTANCE = YsmPacketDecoder(registryMap)
        }
    }

    fun readFromCustomPayload(packetBuffer: ByteBuf,channel: NamespacedKey,connectionType: EnumConnectionType): IYsmPacket?{
        if (channel.namespace != "yes_steve_model"){
            return null
        }

        when(connectionType){
            FORGE -> {
                if (channel.key != "network"){
                    return null
                }

                val packetId = packetBuffer.readByte()

                val packetClass = this.packetRegistryMap[packetId.toInt()] ?: return null
                val packetConstructor = packetClass.getConstructor()

                val newInstance = packetConstructor.newInstance() as IYsmPacket

                newInstance.readPacketData(packetBuffer,connectionType)

                return newInstance
            }

            FABRIC -> {
                val packetIdStr = channel.key

                val packetClass = this.packetRegistryMap[Integer.parseInt(packetIdStr)] ?: return null
                val packetConstructor = packetClass.getConstructor()

                val newInstance = packetConstructor.newInstance() as IYsmPacket

                newInstance.readPacketData(packetBuffer,connectionType)

                return newInstance
            }

            VANILLA -> {
                //Do nothing here
                return null
            }
        }
    }
}