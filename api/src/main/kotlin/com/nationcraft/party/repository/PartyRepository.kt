package com.nationcraft.party.repository

import com.nationcraft.party.pojo.Party
import java.util.*

object PartyRepository {

    private val parties = hashSetOf<Party>()

    fun getParties(): Set<Party> {
        return parties
    }

    fun addParty(party: Party) {
        parties.add(party)
    }

    fun removeParty(party: Party) {
        parties.remove(party)
    }

    fun getParty(uniqueId: UUID): Party? = parties.firstOrNull { it.contains(uniqueId) }

}