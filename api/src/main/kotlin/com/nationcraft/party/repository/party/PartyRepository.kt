package com.nationcraft.party.repository.party

import com.nationcraft.party.pojo.Party
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList

object PartyRepository {

    private val parties = arrayListOf<Party>()

    fun getParties(): ArrayList<Party> {
        return parties
    }

    fun addParty(party: Party) {
        parties.add(party)
    }

    fun removeParty(party: Party) {
        parties.remove(party)
    }

    fun getParty(name: String) = parties.firstOrNull { it.id == name }

    fun getParty(player: Player) = getParty(player.uniqueId)

    fun getParty(uniqueId: UUID): Party? = parties.firstOrNull { it.contains(uniqueId) }

}