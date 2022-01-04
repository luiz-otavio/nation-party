@file:JvmName("EventBus")

package com.nationcraft.party.util

import com.nationcraft.party.event.PartyEvent

fun <T: PartyEvent> callTo(event: T): T {
    event.call()

    return event
}

