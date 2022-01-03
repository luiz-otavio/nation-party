@file:JvmName("Events")

package com.nationcraft.party.event

import com.nationcraft.party.pojo.Party
import com.nationcraft.party.pojo.reward.RewardType
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class PartyEvent : Event(), Cancellable {

    companion object {
        val HANDLER_LIST = HandlerList()

        fun getHandlerList(): HandlerList = HANDLER_LIST
    }

    private var isCancelled = false

    override fun getHandlers(): HandlerList = HANDLER_LIST

    override fun isCancelled(): Boolean = isCancelled

    override fun setCancelled(cancel: Boolean) {
        isCancelled = cancel
    }

    fun call() {
        Bukkit.getPluginManager().callEvent(this)
    }
}

data class PartyGainRewardEvent(
    val player: Player,
    val party: Party,
    val rewardType: RewardType,
    val amount: Int
) : PartyEvent()

data class PartyLeaveEvent(
    val player: Player,
    val party: Party
) : PartyEvent()

data class PartyJoinEvent(
    val player: Player,
    val party: Party
) : PartyEvent()

data class PartyCreateEvent(
    val player: Player,
    val party: Party
) : PartyEvent()

data class PartyDestroyEvent(
    val party: Party
) : PartyEvent()

data class PartyKickEvent(
    val player: Player,
    val party: Party
) : PartyEvent()

data class PartyPromoteEvent(
    val player: Player,
    val leader: Player,
    val party: Party
) : PartyEvent()

data class PartyShareRewardEvent(
    val player: Player,
    val party: Party,
    val rewardType: RewardType,
    val amount: Int
) : PartyEvent()


