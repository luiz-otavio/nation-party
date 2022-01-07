package com.nationcraft.party.pojo.invite

import com.nationcraft.party.event.PartyDeclineEvent
import com.nationcraft.party.event.PartyJoinEvent
import com.nationcraft.party.pojo.Party
import com.nationcraft.party.pojo.member.PartyMember
import com.nationcraft.party.repository.invite.InviteRepository
import com.nationcraft.party.util.callTo
import com.nationcraft.party.util.translate
import org.bukkit.Bukkit
import java.util.*

data class Invite(
    val player: UUID,
    val from: UUID,
    val party: Party,
    val created_at: Long = System.currentTimeMillis()
) {

    fun accept() {
        val bukkitPlayer = Bukkit.getPlayer(player) ?: return

        val event = callTo(
            PartyJoinEvent(bukkitPlayer, party)
        )

        if (event.isCancelled) {
            return
        }

        party.addPlayer(
            PartyMember(bukkitPlayer.uniqueId)
        )

        val message = translate("&a${bukkitPlayer.name} &7joined the party!")

        for (member in party.members) {
            val player = member.getPlayer() ?: continue

            player.sendMessage(message)
        }

        InviteRepository.removeInvite(this)
    }

    fun decline() {
        val bukkitPlayer = Bukkit.getPlayer(player) ?: return

        val event = callTo(
            PartyDeclineEvent(this)
        )

        if (event.isCancelled) {
            return
        }

        val leader = party.getLeader() ?: return
        val player = leader.getPlayer() ?: return

        player.sendMessage(
            translate("&c${bukkitPlayer.name} &7declined your party invite!")
        )

        InviteRepository.removeInvite(this)
    }

    fun hasExpired() = System.currentTimeMillis() - created_at > 30000

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Invite

        if (player != other.player) return false
        if (from != other.from) return false
        if (party != other.party) return false
        if (created_at != other.created_at) return false

        return true
    }

    override fun hashCode(): Int {
        var result = player.hashCode()
        result = 31 * result + from.hashCode()
        result = 31 * result + party.hashCode()
        result = 31 * result + created_at.hashCode()
        return result
    }
}