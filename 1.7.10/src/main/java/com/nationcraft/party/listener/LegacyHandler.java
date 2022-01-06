package com.nationcraft.party.listener;

import com.nationcraft.party.event.PartyGainRewardEvent;
import com.nationcraft.party.impl.DefaultRewardType;
import com.nationcraft.party.pojo.Party;
import com.nationcraft.party.pojo.member.PartyMember;
import com.nationcraft.party.repository.party.PartyRepository;
import com.nationcraft.party.util.Entities;
import com.nationcraft.party.util.EventBus;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.command.ICommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LegacyHandler {

    private final PartyRepository partyRepository = PartyRepository.INSTANCE;
    private final MinecraftServer minecraftServer = MinecraftServer.getServer();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingAttack(LivingAttackEvent event) {
        Entity entity = event.entityLiving;

        if (!(entity instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) entity;

        Party party = partyRepository.getParty(
          player.getUniqueID()
        );

        if (party == null) return;

        DamageSource source = event.source;

        Entity attacker = source.getEntity();

        if (!(attacker instanceof EntityPlayer)) return;

        EntityPlayer attackerPlayer = (EntityPlayer) attacker;

        if (party.contains(attackerPlayer.getUniqueID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCommand(CommandEvent event) {
        if (!(event.sender instanceof MinecraftServer)) return;

        ICommand command = event.command;

        if (!command.getCommandName().equals("jrmctp")) return;

        String[] parameters = event.parameters;

        if (parameters.length != 2) {
            return;
        }

        int amount = Integer.parseInt(
          parameters[0]
        );

        if (amount <= 0) {
            return;
        }

        Player player = Bukkit.getPlayer(
          parameters[1]
        );

        if (player == null) {
            return;
        }

        Party party = partyRepository.getParty(player);

        if (party == null) {
            return;
        }

        PartyGainRewardEvent gain = EventBus.callTo(
          new PartyGainRewardEvent(
            player,
            party,
            DefaultRewardType.TRAINING_POINTS,
            amount
          )
        );

        if (gain.isCancelled()) {
            return;
        }

        int share = (int) Math.ceil(
          (double) amount / party.getMembers().size()
        );

        for (PartyMember member : party.getMembers()) {
            Player user = member.getPlayer();

            if (user == null || user == player) continue;

            EntityPlayer entityPlayer = Entities.fromPlayer(minecraftServer, user);

            if (entityPlayer == null) continue;

            NBTTagCompound compound = entityPlayer.getEntityData()
              .getCompoundTag("PlayerPersisted");

            if (compound == null) continue;

            int trainingPoints = compound.getInteger("jrmcTpint");

            compound.setInteger("jrmcTpint", trainingPoints + share);

            user.sendMessage(
              "You have been awarded " + share + " training points."
            );
        }
    }

}
