package com.nationcraft.party.repository.invite

import com.nationcraft.party.pojo.Party
import com.nationcraft.party.pojo.invite.Invite
import java.util.*

object InviteRepository {

    private val invites = hashSetOf<Invite>()

    fun addInvite(invite: Invite) {
        invites.add(invite)
    }

    fun getInvite(uuid: UUID, name: String): Invite? {
        val invite = invites.find { it.player == uuid && it.party.id == name } ?: return null

        if (invite.hasExpired()) {
            invites.remove(invite)

            return null
        }

        return invite
    }

    fun hasInvite(party: Party, uuid: UUID): Boolean {
        val invite = invites.find { it.player == uuid && it.party == party } ?: return false

        return !invite.hasExpired()
    }

    fun removeInvite(invite: Invite) {
        invites.remove(invite)
    }


}