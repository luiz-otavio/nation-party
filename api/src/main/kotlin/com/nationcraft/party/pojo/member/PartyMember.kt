package com.nationcraft.party.pojo.member

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

data class PartyMember(
    val uniqueId: UUID,
    var isLeader: Boolean = false,
    var isChatting: Boolean = false,
    val created_at: Long = System.currentTimeMillis()
) {

    fun getPlayer(): Player? = Bukkit.getPlayer(uniqueId)

    fun isOnline() = getPlayer() != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PartyMember

        if (uniqueId != other.uniqueId) return false

        return true
    }

    override fun hashCode(): Int =  uniqueId.hashCode()

}