package com.nationcraft.party.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.bukkit.entity.Player;

public class Entities {

    public static EntityPlayer fromPlayer(MinecraftServer server, Player player) {
        for (WorldServer worldServer : server.worldServers) {
            EntityPlayer entityPlayer = worldServer.getPlayerEntityByName(
              player.getName()
            );

            if (entityPlayer != null) {
                return entityPlayer;
            }
        }

        return null;
    }

}
