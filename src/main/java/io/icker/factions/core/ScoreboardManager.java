package io.icker.factions.core;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;

public class ScoreboardManager {
    private static int tickCounter = 0;

    public static void register() {
        
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CommandSourceStack silentSource = server.createCommandSourceStack().withSuppressedOutput();
            
            server.getCommands().performPrefixedCommand(silentSource, "scoreboard objectives add factions dummy {\"text\":\"FACTIONS\",\"color\":\"red\"}");
            server.getCommands().performPrefixedCommand(silentSource, "scoreboard objectives modify factions numberformat blank");
            server.getCommands().performPrefixedCommand(silentSource, "scoreboard objectives setdisplay sidebar factions");
            
            server.getCommands().performPrefixedCommand(silentSource, "scoreboard players set FactionName factions 0");
            server.getCommands().performPrefixedCommand(silentSource, "scoreboard players set FactionRank factions -1");
            server.getCommands().performPrefixedCommand(silentSource, "scoreboard players set FactionPower factions -2");
            
            server.getCommands().performPrefixedCommand(silentSource, "scoreboard players set FactionClaimsTitle factions -3");
            
            server.getCommands().performPrefixedCommand(silentSource, "scoreboard players set FactionShield factions -4");
            server.getCommands().performPrefixedCommand(silentSource, "scoreboard players set FactionShieldReq factions -5");

            server.getCommands().performPrefixedCommand(silentSource, "scoreboard players set FactionExpand factions -6");

            server.getCommands().performPrefixedCommand(silentSource, "scoreboard players set FactionTerritoryTitle factions -7");

            server.getCommands().performPrefixedCommand(silentSource, "scoreboard players set FactionRadar factions -8");
            server.getCommands().performPrefixedCommand(silentSource, "scoreboard players set FactionRelation factions -9");

            server.getCommands().performPrefixedCommand(silentSource, "scoreboard players set FactionLeaderTitle factions -10");
            server.getCommands().performPrefixedCommand(silentSource, "scoreboard players set FactionTop1 factions -11");
            server.getCommands().performPrefixedCommand(silentSource, "scoreboard players set FactionTop2 factions -12");
            server.getCommands().performPrefixedCommand(silentSource, "scoreboard players set FactionTop3 factions -13");
            
            server.getCommands().performPrefixedCommand(silentSource, "scoreboard players set FactionLeaderRank factions -14");
            server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display number FactionLeaderRank factions blank");
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;

            if (tickCounter >= 10) {
                tickCounter = 0;
                
                CommandSourceStack silentSource = server.createCommandSourceStack().withSuppressedOutput();

                // Faction Name
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionName factions {\"text\":\"%factions:name%\",\"color\":\"white\"}");
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionName factions {\"text\":\"%factions:name%\"}");

                // Rank
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionRank factions {\"text\":\"Rank: %factions:rank%\",\"color\":\"white\"}");
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionRank factions {\"text\":\"Rank: %factions:rank%\"}");

                // Power
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionPower factions {\"text\":\"Power: %factions:power%/%factions:max_power%\",\"color\":\"white\"}");
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionPower factions {\"text\":\"Power: %factions:power%/%factions:max_power%\"}");

                // Claims Heading
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionClaimsTitle factions {\"text\":\"--- Claims ---\",\"color\":\"gold\"}");

                // Shield
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionShield factions {\"text\":\"%factions:is_protected%\",\"color\":\"white\"}");
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionShield factions {\"text\":\"%factions:is_protected%\"}");

                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionShieldReq factions {\"text\":\"%factions:shield_req%\",\"color\":\"white\"}");
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionShieldReq factions {\"text\":\"%factions:shield_req%\"}");

                // Expand
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionExpand factions {\"text\":\"%factions:expand_status%\",\"color\":\"white\"}");
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionExpand factions {\"text\":\"%factions:expand_status%\"}");

                // Territory Heading
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionTerritoryTitle factions {\"text\":\"--- Territory ---\",\"color\":\"gold\"}");

                // Radar
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionRadar factions {\"text\":\"%factions:radar%\",\"color\":\"white\"}");
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionRadar factions {\"text\":\"%factions:radar%\"}");

                // Relation
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionRelation factions {\"text\":\"%factions:relation%\",\"color\":\"white\"}");
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionRelation factions {\"text\":\"%factions:relation%\"}");

                // Leaderboard
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionLeaderTitle factions {\"text\":\"--- Leaderboard ---\",\"color\":\"gold\"}");
                
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionTop1 factions {\"text\":\"%factions:top_1%\",\"color\":\"white\"}");
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionTop1 factions {\"text\":\"%factions:top_1%\"}");

                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionTop2 factions {\"text\":\"%factions:top_2%\",\"color\":\"white\"}");
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionTop2 factions {\"text\":\"%factions:top_2%\"}");

                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionTop3 factions {\"text\":\"%factions:top_3%\",\"color\":\"white\"}");
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionTop3 factions {\"text\":\"%factions:top_3%\"}");

                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionLeaderRank factions {\"text\":\"%factions:leaderboard_rank%\",\"color\":\"white\"}");
                server.getCommands().performPrefixedCommand(silentSource, "scoreboard players display name FactionLeaderRank factions {\"text\":\"%factions:leaderboard_rank%\"}");
            }
        });
    }
}