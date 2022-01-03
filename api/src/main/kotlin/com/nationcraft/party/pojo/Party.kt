package com.nationcraft.party.pojo

import com.nationcraft.party.pojo.member.PartyMember
import java.util.*
import kotlin.collections.ArrayList

data class Party(
    val id: String,
    val players: ArrayList<PartyMember> = ArrayList()
) {

    fun contains(uniqueId: UUID) = players.any { it.uniqueId == uniqueId }

    fun getLeader() = players.firstOrNull { it.isLeader }

    fun addPlayer(player: PartyMember) {
        players.add(player)
    }

    fun removePlayer(player: PartyMember) {
        players.remove(player)
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