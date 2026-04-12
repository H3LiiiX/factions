package io.icker.factions.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.icker.factions.api.persistents.Faction;
import io.icker.factions.api.persistents.User;
import io.icker.factions.util.WorldUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.natxo.mcrestapi.http.HttpUtil;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles faction REST endpoints registered via MCRestAPI's endpointProviders:
 *
 *   GET /api/factions                — list all factions
 *   GET /api/factions/{id}/members   — list members of a specific faction
 */
public class FactionsEndpoint implements HttpHandler {

    private static final Gson GSON = new GsonBuilder().create();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            HttpUtil.sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        // Strip leading/trailing slashes and split on "/"
        // e.g. "/api/factions"           → ["api","factions"]
        //      "/api/factions/uuid/members" → ["api","factions","uuid","members"]
        String[] parts = exchange.getRequestURI().getPath().replaceAll("^/+|/+$", "").split("/");

        try {
            if (parts.length == 2) {
                handleList(exchange);
            } else if (parts.length == 4 && "members".equals(parts[3])) {
                handleMembers(exchange, parts[2]);
            } else {
                HttpUtil.sendJson(exchange, 404, "{\"error\":\"Not found\"}");
            }
        } catch (Exception e) {
            HttpUtil.sendJson(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    private void handleList(HttpExchange exchange) throws IOException {
        List<Map<String, Object>> list = Faction.all().stream()
                .map(this::factionToMap)
                .toList();
        HttpUtil.sendJson(exchange, 200, GSON.toJson(list));
    }

    private void handleMembers(HttpExchange exchange, String idStr) throws IOException {
        UUID id;
        try {
            id = UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(exchange, 400, "{\"error\":\"Invalid faction ID\"}");
            return;
        }

        Faction faction = Faction.get(id);
        if (faction == null) {
            HttpUtil.sendJson(exchange, 404, "{\"error\":\"Faction not found\"}");
            return;
        }

        List<Map<String, Object>> members = faction.getUsers().stream()
                .map(user -> memberToMap(user))
                .toList();

        HttpUtil.sendJson(exchange, 200, GSON.toJson(members));
    }

    private Map<String, Object> factionToMap(Faction faction) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", faction.getID().toString());
        m.put("name", faction.getName());
        m.put("color", faction.getColor().getName());
        m.put("description", faction.getDescription());
        m.put("motd", faction.getMOTD());
        m.put("open", faction.isOpen());
        m.put("power", faction.getPower());
        m.put("memberCount", faction.getUsers().size());
        m.put("claimCount", faction.getClaims().size());

        if (faction.getHome() != null) {
            Map<String, Object> home = new LinkedHashMap<>();
            home.put("x", faction.getHome().x);
            home.put("y", faction.getHome().y);
            home.put("z", faction.getHome().z);
            home.put("dimension", faction.getHome().level);
            m.put("home", home);
        } else {
            m.put("home", null);
        }

        return m;
    }

    private Map<String, Object> memberToMap(User user) {
        ServerPlayer online = WorldUtils.server.getPlayerList().getPlayer(user.getID());

        String name;
        if (online != null) {
            name = online.getGameProfile().name();
        } else {
            name = WorldUtils.server.services().nameToIdCache()
                    .get(user.getID())
                    .map(NameAndId::name)
                    .orElse("Unknown");
        }

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("uuid", user.getID().toString());
        m.put("name", name);
        m.put("rank", user.getRankName());
        m.put("online", online != null);
        return m;
    }
}
