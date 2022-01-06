package com.nationcraft.party;

import com.nationcraft.party.listener.LegacyHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod(
  modid = "party",
  name = "NationCraft Party",
  version = "1.0",
  acceptableRemoteVersions = "*"
)
public class PartyMod {

    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        MinecraftForge.EVENT_BUS.register(
          new LegacyHandler()
        );
    }


}
