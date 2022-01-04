package com.nationcraft.party.listener

import com.nationcraft.party.event.PartyChatEvent
import com.nationcraft.party.repository.party.PartyRepository
import com.nationcraft.party.util.callTo
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class PartyHandler : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        val player = event.player

        val party = PartyRepository.getParty(player) ?: return

        val member = party.getMember(player) ?: return

        if (!member.isChatting) {
            return
        }

        val call = callTo(
            PartyChatEvent(player, party, event.message)
        )

        if (!call.isCancelled) {
            event.isCancelled = true

            for (each in party.members) {
                val user = each.getPlayer() ?: continue

                user.sendMessage(call.message)
            }
        }
    }

}