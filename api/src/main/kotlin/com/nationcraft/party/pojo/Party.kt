package com.nationcraft.party.pojo

import com.nationcraft.party.pojo.member.PartyMember
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList

data class Party(
    val id: String,
    val members: ArrayList<PartyMember> = ArrayList()
) {

    fun contains(uniqueId: UUID) = members.any { it.uniqueId == uniqueId }

    fun getLeader() = members.firstOrNull { it.isLeader }

    fun getMember(player: Player) = getMember(player.uniqueId)

    fun getMember(uniqueId: UUID) = members.firstOrNull { it.uniqueId == uniqueId }

    fun addPlayer(player: PartyMember) {
        members.add(player)
    }

    fun removePlayer(player: PartyMember) {
        members.remove(player)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Party

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int = id.hashCode()

}