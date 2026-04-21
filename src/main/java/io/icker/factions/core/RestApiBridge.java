package io.icker.factions.core;

import com.google.gson.JsonObject;
import io.icker.factions.FactionsMod;
import io.icker.factions.api.events.ClaimEvents;
import io.icker.factions.api.events.FactionEvents;
import io.icker.factions.api.persistents.Faction;
import io.icker.factions.api.persistents.User;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.natxo.mcrestapi.MCRestAPI;
import net.natxo.mcrestapi.collectors.ServerEvent;

import java.time.Instant;

/**
 * Bridges Factions mod events into the MCRestAPI SSE stream.
 *
 * Only registered when mcrestapi is present (checked in
 * FactionsMod.onInitialize).
 * Events are available on the /api/events/stream endpoint with the following
 * types:
 *
 * faction.created — a new faction was created
 * faction.disbanded — a faction was disbanded
 * faction.member_join — a player joined a faction
 * faction.member_leave — a player left (or was kicked from) a faction
 * faction.modified — faction name/description/motd/color/open changed
 * faction.home_set — faction home was set or cleared
 * claim.added — a chunk was claimed
 * claim.removed — a chunk claim was removed
 * global.chat — a player sent a message in global chat
 * faction.chat — a player sent a message in faction chat
 *
 * REST endpoints registered via MCRestAPI's endpointProviders:
 * GET /api/factions — list all factions
 * GET /api/factions/{id}/members — list members of a specific faction
 *
 * Each SSE event's `content` field is a JSON string with structured data.
 * The `player` field holds the triggering player's UUID where applicable,
 * otherwise null.
 *
 * Note: faction.power_change is intentionally omitted — it fires very
 * frequently
 * (every power tick and on player deaths) and would flood the stream.
 */
public class RestApiBridge {

    public static void register() {
        FactionEvents.CREATE.register((faction, owner) -> {
            try {
                if (faction == null || owner == null)
                    return;
                JsonObject data = new JsonObject();
                data.addProperty("factionId", faction.getID().toString());
                data.addProperty("factionName", faction.getName());
                data.addProperty("color", faction.getColor().getName());
                data.addProperty("open", faction.isOpen());
                data.addProperty("ownerUuid", owner.getID().toString());
                push("faction.created", owner.getID().toString(), data);
            } catch (Exception e) {
                FactionsMod.LOGGER.error("[Factions] RestApiBridge error in CREATE handler", e);
            }
        });

        FactionEvents.DISBAND.register((faction) -> {
            try {
                if (faction == null)
                    return;
                JsonObject data = new JsonObject();
                data.addProperty("factionId", faction.getID().toString());
                data.addProperty("factionName", faction.getName());
                data.addProperty("memberCount", faction.getUsers().size());
                data.addProperty("claimCount", faction.getClaims().size());
                push("faction.disbanded", null, data);
            } catch (Exception e) {
                FactionsMod.LOGGER.error("[Factions] RestApiBridge error in DISBAND handler", e);
            }
        });

        FactionEvents.MEMBER_JOIN.register((faction, user) -> {
            try {
                if (faction == null || user == null)
                    return;
                JsonObject data = new JsonObject();
                data.addProperty("factionId", faction.getID().toString());
                data.addProperty("factionName", faction.getName());
                data.addProperty("playerUuid", user.getID().toString());
                data.addProperty("rank", user.getRankName());
                push("faction.member_join", user.getID().toString(), data);
            } catch (Exception e) {
                FactionsMod.LOGGER.error("[Factions] RestApiBridge error in MEMBER_JOIN handler", e);
            }
        });

        FactionEvents.MEMBER_LEAVE.register((faction, user) -> {
            try {
                if (faction == null || user == null)
                    return;
                JsonObject data = new JsonObject();
                data.addProperty("factionId", faction.getID().toString());
                data.addProperty("factionName", faction.getName());
                data.addProperty("playerUuid", user.getID().toString());
                push("faction.member_leave", user.getID().toString(), data);
            } catch (Exception e) {
                FactionsMod.LOGGER.error("[Factions] RestApiBridge error in MEMBER_LEAVE handler", e);
            }
        });

        FactionEvents.MODIFY.register((faction) -> {
            try {
                if (faction == null)
                    return;
                JsonObject data = new JsonObject();
                data.addProperty("factionId", faction.getID().toString());
                data.addProperty("factionName", faction.getName());
                data.addProperty("description", faction.getDescription());
                data.addProperty("motd", faction.getMOTD());
                data.addProperty("color", faction.getColor().getName());
                data.addProperty("open", faction.isOpen());
                push("faction.modified", null, data);
            } catch (Exception e) {
                FactionsMod.LOGGER.error("[Factions] RestApiBridge error in MODIFY handler", e);
            }
        });

        FactionEvents.SET_HOME.register((faction, home) -> {
            try {
                if (faction == null)
                    return;
                JsonObject data = new JsonObject();
                data.addProperty("factionId", faction.getID().toString());
                data.addProperty("factionName", faction.getName());
                if (home != null) {
                    data.addProperty("x", home.x);
                    data.addProperty("y", home.y);
                    data.addProperty("z", home.z);
                    data.addProperty("dimension", home.level);
                } else {
                    data.addProperty("cleared", true);
                }
                push("faction.home_set", null, data);
            } catch (Exception e) {
                FactionsMod.LOGGER.error("[Factions] RestApiBridge error in SET_HOME handler", e);
            }
        });

        ClaimEvents.ADD.register((claim) -> {
            try {
                Faction faction = Faction.get(claim.factionID);
                if (faction == null)
                    return;
                JsonObject data = new JsonObject();
                data.addProperty("factionId", faction.getID().toString());
                data.addProperty("factionName", faction.getName());
                data.addProperty("x", claim.x);
                data.addProperty("z", claim.z);
                data.addProperty("dimension", claim.level);
                push("claim.added", null, data);
            } catch (Exception e) {
                FactionsMod.LOGGER.error("[Factions] RestApiBridge error in CLAIM_ADD handler", e);
            }
        });

        ClaimEvents.REMOVE.register((x, z, level, faction) -> {
            try {
                if (faction == null)
                    return;
                JsonObject data = new JsonObject();
                data.addProperty("factionId", faction.getID().toString());
                data.addProperty("factionName", faction.getName());
                data.addProperty("x", x);
                data.addProperty("z", z);
                data.addProperty("dimension", level);
                push("claim.removed", null, data);
            } catch (Exception e) {
                FactionsMod.LOGGER.error("[Factions] RestApiBridge error in CLAIM_REMOVE handler", e);
            }
        });

        // Chat events — subscribe to global.chat and faction.chat in your bot,
        // not the generic MCRestAPI "chat" event, to avoid double-posting.
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            try {
                User user = User.get(sender.getUUID());
                if (user == null)
                    return;
                String rawMessage = message.signedContent();
                String playerName = sender.getGameProfile().name();
                String playerUuid = sender.getUUID().toString();

                if (user.chat == User.ChatMode.GLOBAL) {
                    JsonObject data = new JsonObject();
                    data.addProperty("playerName", playerName);
                    data.addProperty("playerUuid", playerUuid);
                    data.addProperty("message", rawMessage);
                    // Include faction tag if the player is in one (shown in global chat)
                    if (user.isInFaction()) {
                        Faction faction = user.getFaction();
                        if (faction != null) {
                            data.addProperty("factionId", faction.getID().toString());
                            data.addProperty("factionName", faction.getName());
                            data.addProperty("factionColor", faction.getColor().getName());
                        }
                    }
                    push("global.chat", playerName, data);
                } else if (user.chat == User.ChatMode.FACTION || user.chat == User.ChatMode.FOCUS) {
                    if (!user.isInFaction())
                        return;
                    Faction faction = user.getFaction();
                    if (faction == null)
                        return;
                    JsonObject data = new JsonObject();
                    data.addProperty("factionId", faction.getID().toString());
                    data.addProperty("factionName", faction.getName());
                    data.addProperty("factionColor", faction.getColor().getName());
                    data.addProperty("playerName", playerName);
                    data.addProperty("playerUuid", playerUuid);
                    data.addProperty("message", rawMessage);
                    push("faction.chat", playerName, data);
                }
            } catch (Exception e) {
                FactionsMod.LOGGER.error("[Factions] RestApiBridge error in CHAT_MESSAGE handler", e);
            }
        });

        // Register /api/factions and /api/factions/{id}/members endpoints.
        // These are called by MCRestAPI after the HTTP server is built but before it
        // starts.
        MCRestAPI.endpointProviders
                .add(router -> router.register("/api/factions", new FactionsEndpoint(), "players.read"));

        FactionsMod.LOGGER.info("[Factions] MCRestAPI bridge registered");
    }

    private static void push(String type, String player, JsonObject data) {
        if (MCRestAPI.eventCollector == null)
            return;
        ServerEvent event = new ServerEvent(type, player, data.toString(), Instant.now().toString());
        // Dispatch to a virtual thread so SSE client I/O can never block the server
        // thread.
        Thread.ofVirtual().start(() -> MCRestAPI.eventCollector.push(event));
    }
}
