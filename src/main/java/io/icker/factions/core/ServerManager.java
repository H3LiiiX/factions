package io.icker.factions.core;

import io.icker.factions.FactionsMod;
import io.icker.factions.api.events.MiscEvents;
import io.icker.factions.api.persistents.Claim;
import io.icker.factions.api.persistents.Faction;
import io.icker.factions.api.persistents.User;
import io.icker.factions.config.Config;
import io.icker.factions.util.Message;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class ServerManager {
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register(ServerManager::playerJoin);
        MiscEvents.ON_SAVE.register(ServerManager::save);
        ServerLifecycleEvents.SERVER_STARTED.register(ServerManager::initializeSpawn);
    }

    private static void save(MinecraftServer server) {
        Claim.save();
        Faction.save();
        User.save();
    }

    private static void initializeSpawn(MinecraftServer server) {
        Config.SpawnConfig spawnConfig = FactionsMod.CONFIG.SPAWN;
        Faction spawnFaction = Faction.getByName("Spawn");

        if (!spawnConfig.ENABLED) {
            if (spawnFaction != null) {
                spawnFaction.remove();
            }
            return;
        }

        if (spawnFaction == null) {
            spawnFaction = new Faction(
                "Spawn", 
                "Server Spawn Area", 
                "Welcome to Spawn!", 
                ChatFormatting.YELLOW, 
                false, 
                99999
            );
            Faction.add(spawnFaction);
        } else {
            spawnFaction.removeAllClaims();
        }

        int size = spawnConfig.SIZE;
        boolean isCircle = spawnConfig.CIRCLE;
        
        int centerChunkX = spawnConfig.X >> 4;
        int centerChunkZ = spawnConfig.Z >> 4;
        
        String dimension = "minecraft:overworld"; 

        int startX = centerChunkX - (size / 2);
        int startZ = centerChunkZ - (size / 2);
        int endX = startX + size - 1;
        int endZ = startZ + size - 1;

        float exactCenterX = startX + (size - 1) / 2.0f;
        float exactCenterZ = startZ + (size - 1) / 2.0f;
        float radiusSq = (size / 2.0f) * (size / 2.0f);

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                
                if (isCircle) {
                    float dx = x - exactCenterX;
                    float dz = z - exactCenterZ;
                    
                    if (dx * dx + dz * dz > radiusSq) {
                        continue;
                    }
                }
                
                spawnFaction.addClaim(x, z, dimension);
            }
        }
    }

    private static void playerJoin(
            ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        ServerPlayer player = handler.getPlayer();
        User user = User.get(player.getUUID());

        if (user.isInFaction()) {
            Faction faction = user.getFaction();
            new Message(
                            Component.translatable(
                                    "factions.events.member_returns", player.getName().getString()))
                    .send(player, false);
            new Message(faction.getMOTD()).prependFaction(faction).send(player, false);
        }
    }
}