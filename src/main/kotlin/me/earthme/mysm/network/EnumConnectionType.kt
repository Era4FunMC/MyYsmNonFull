package me.earthme.mysm.network

import me.earthme.mysm.network.connection.FabricPlayerYsmConnection
import me.earthme.mysm.network.connection.ForgePlayerYsmConnection
import me.earthme.mysm.network.connection.PlayerYsmConnection

enum class EnumConnectionType (
    private val modLoaderName: String
){
    FORGE("forge"),
    FABRIC("fabric"),
    VANILLA("none");

    companion object{
        fun fromConnection(connection: PlayerYsmConnection?): EnumConnectionType{
            if (connection == null){
                return VANILLA
            }

            if (connection is FabricPlayerYsmConnection){
                return FABRIC
            }

            if (connection is ForgePlayerYsmConnection){
                return FORGE
            }

            throw IllegalArgumentException("Connection type mismatch!")
        }
    }

    fun getModLoaderName(): String{
        return this.modLoaderName
    }
}