package io.icker.factions.util;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;

import io.icker.factions.FactionsMod;
import io.icker.factions.api.persistents.Faction;
import io.icker.factions.api.persistents.User;
import io.icker.factions.api.persistents.Claim;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

import xyz.nucleoid.server.translations.api.Localization;
import xyz.nucleoid.server.translations.api.language.ServerLanguage;

import java.util.List;
import java.util.function.Function;

public class PlaceholdersWrapper {
    private static final Component UNFORMATTED_NULL =
            Component.translatable("factions.papi.factionless");
    private static final Component FORMATTED_NULL =
            UNFORMATTED_NULL.copy().withStyle(ChatFormatting.DARK_GRAY);

    private static void register(String identifier, Function<User, Component> handler) {
        Placeholders.register(
                Identifier.fromNamespaceAndPath(FactionsMod.MODID, identifier),
                (ctx, argument) -> {
                    if (!ctx.hasPlayer())
                        return PlaceholderResult.invalid(
                                Localization.raw(
                                        "argument.entity.notfound.player",
                                        ServerLanguage.getLanguage(FactionsMod.CONFIG.LANGUAGE)));

                    User member = User.get(ctx.player().getUUID());
                    return PlaceholderResult.value(handler.apply(member));
                });
    }

    public static void init() {
        register(
                "name",
                (member) -> {
                    Faction faction = member.getFaction();
                    if (faction == null) return Component.literal("Outcast").withStyle(ChatFormatting.GRAY);

                    return Component.literal(faction.getName())
                            .withStyle(member.getFaction().getColor());
                });

        register(
                "colorless_name",
                (member) -> {
                    Faction faction = member.getFaction();
                    if (faction == null) return FORMATTED_NULL;

                    return Component.nullToEmpty(faction.getName());
                });

        register(
                "chat",
                (member) -> {
                    if (member.chat == User.ChatMode.GLOBAL || !member.isInFaction())
                        return Component.translatable("factions.papi.chat.global");

                    return Component.translatable("factions.papi.chat.faction");
                });

        register(
                "rank",
                (member) -> {
                    if (!member.isInFaction()) return Component.literal("None").withStyle(ChatFormatting.GRAY);

                    String rankName = member.getRankName();
                    String capitalizedRank = rankName.substring(0, 1).toUpperCase() + rankName.substring(1);

                    return Component.nullToEmpty(capitalizedRank);
                });

        register(
                "color",
                (member) -> {
                    if (!member.isInFaction()) return Component.nullToEmpty("reset");

                    return Component.nullToEmpty(member.getFaction().getColor().getName());
                });

        register(
                "description",
                (member) -> {
                    Faction faction = member.getFaction();
                    if (faction == null) return FORMATTED_NULL;

                    return Component.nullToEmpty(faction.getDescription());
                });

        register(
                "state",
                (member) -> {
                    Faction faction = member.getFaction();
                    if (faction == null) return UNFORMATTED_NULL;

                    return Component.nullToEmpty(String.valueOf(faction.isOpen()));
                });

        register(
                "power",
                (member) -> {
                    Faction faction = member.getFaction();
                    if (faction == null) return Component.literal("0");

                    return Component.nullToEmpty(String.valueOf(faction.getPower()));
                });

        register(
                "power_formatted",
                (member) -> {
                    Faction faction = member.getFaction();
                    if (faction == null) return FORMATTED_NULL;

                    int red =
                            mapBoundRange(
                                    faction.calculateMaxPower(), 0, 170, 255, faction.getPower());
                    int green =
                            mapBoundRange(
                                    0, faction.calculateMaxPower(), 170, 255, faction.getPower());
                    return Component.literal(String.valueOf(faction.getPower()))
                            .setStyle(Style.EMPTY.withColor(rgbToInt(red, green, 170)));
                });

        register(
                "max_power",
                (member) -> {
                    Faction faction = member.getFaction();
                    if (faction == null) return Component.literal("0");

                    return Component.nullToEmpty(String.valueOf(faction.calculateMaxPower()));
                });

        register(
                "player_power",
                (member) -> {
                    return Component.nullToEmpty(String.valueOf(FactionsMod.CONFIG.POWER.MEMBER));
                });

        register(
                "required_power",
                (member) -> {
                    Faction faction = member.getFaction();
                    if (faction == null) return Component.literal("0");

                    return Component.nullToEmpty(
                            String.valueOf(
                                    faction.getClaims().size()
                                            * FactionsMod.CONFIG.POWER.CLAIM_WEIGHT));
                });

        register(
                "required_power_formatted",
                (member) -> {
                    Faction faction = member.getFaction();
                    if (faction == null) return FORMATTED_NULL;

                    int reqPower =
                            faction.getClaims().size() * FactionsMod.CONFIG.POWER.CLAIM_WEIGHT;
                    int red = mapBoundRange(0, faction.getPower(), 85, 255, reqPower);
                    return Component.literal(String.valueOf(reqPower))
                            .setStyle(Style.EMPTY.withColor(rgbToInt(red, 85, 85)));
                });

        register(
                "expand_status",
                (member) -> {
                    Faction faction = member.getFaction();
                    if (faction == null) return Component.empty();

                    int maxPower = faction.calculateMaxPower() + faction.getAdminPower();
                    
                    int reqPower = faction.getClaims().size() * FactionsMod.CONFIG.POWER.CLAIM_WEIGHT;
                    int expandPower = reqPower + FactionsMod.CONFIG.POWER.CLAIM_WEIGHT;

                    if (maxPower < expandPower) {
                        return Component.empty()
                                .append(Component.literal("Not Expandable").withStyle(ChatFormatting.RED));
                    } 
                    else {
                        return Component.empty()
                                .append(Component.literal("Expandable").withStyle(ChatFormatting.GREEN));
                    }
                });

        register(
                "is_protected",
                (member) -> {
                    Faction faction = member.getFaction();
                    
                    if (faction == null) return Component.literal("Unprotected").withStyle(ChatFormatting.RED);

                    int currentPower = faction.getPower();
                    int reqPower = faction.getClaims().size() * FactionsMod.CONFIG.POWER.CLAIM_WEIGHT;

                    if ((currentPower >= reqPower) && (reqPower != 0)) {
                        return Component.literal("Protected").withStyle(ChatFormatting.GREEN);
                    } else {
                        return Component.literal("Unprotected").withStyle(ChatFormatting.RED);
                    }
                });
        register(
                "shield_req",
                (member) -> {
                    Faction faction = member.getFaction();
                    if (faction == null) return Component.empty();

                    int currentPower = faction.getPower();
                    int reqPower = faction.getClaims().size() * FactionsMod.CONFIG.POWER.CLAIM_WEIGHT;

                    if ((reqPower == 0)) {
                        return Component.empty();
                    }
                    else if ((currentPower >= reqPower)) {
                        return Component.literal((reqPower + "/" + currentPower)).withStyle(ChatFormatting.GREEN);
                    } else {
                        return Component.literal((reqPower + "/" + currentPower)).withStyle(ChatFormatting.RED);
                    }
                });
        Placeholders.register(
                Identifier.fromNamespaceAndPath(FactionsMod.MODID, "radar"),
                (ctx, argument) -> {
                    if (!ctx.hasPlayer()) return PlaceholderResult.invalid("No player context");
                    net.minecraft.server.level.ServerPlayer player = ctx.player();
                    
                    int x = player.getBlockX() >> 4;
                    int z = player.getBlockZ() >> 4;
                    String level = player.level().dimension().identifier().toString();
                    
                    Claim claim = Claim.get(x, z, level);
                    if (claim != null) {
                        Faction owner = claim.getFaction();
                        return PlaceholderResult.value(Component.literal(owner.getName()).withStyle(owner.getColor()));
                    }
                    return PlaceholderResult.value(Component.literal("Wilderness").withStyle(ChatFormatting.DARK_GREEN));
                });
        Placeholders.register(
                Identifier.fromNamespaceAndPath(FactionsMod.MODID, "relation"),
                (ctx, argument) -> {
                    if (!ctx.hasPlayer()) return PlaceholderResult.invalid("No player context");
                    net.minecraft.server.level.ServerPlayer player = ctx.player();
                    
                    int x = player.getBlockX() >> 4;
                    int z = player.getBlockZ() >> 4;
                    String level = player.level().dimension().identifier().toString();
                    
                    Claim claim = Claim.get(x, z, level);
                    if (claim == null) {
                        return PlaceholderResult.value(Component.literal("Neutral").withStyle(ChatFormatting.WHITE));
                    }
                    
                    User user = User.get(player.getUUID());
                    Faction playerFaction = user.getFaction();
                    Faction claimOwner = claim.getFaction();
                    
                    if (playerFaction == null) {
                        return PlaceholderResult.value(Component.literal("Neutral").withStyle(ChatFormatting.WHITE));
                    }
                    
                    if (claimOwner.getID().equals(playerFaction.getID())) {
                        return PlaceholderResult.value(Component.literal("Your Land").withStyle(ChatFormatting.GREEN));
                    }
                    
                    io.icker.factions.api.persistents.Relationship rel = playerFaction.getRelationship(claimOwner.getID());
                    
                    if (rel == null) {
                        return PlaceholderResult.value(Component.literal("Neutral").withStyle(ChatFormatting.WHITE));
                    } else if (rel.status == io.icker.factions.api.persistents.Relationship.Status.ALLY) {
                        return PlaceholderResult.value(Component.literal("Ally").withStyle(ChatFormatting.LIGHT_PURPLE));
                    } else if (rel.status == io.icker.factions.api.persistents.Relationship.Status.ENEMY) {
                        return PlaceholderResult.value(Component.literal("Enemy").withStyle(ChatFormatting.RED));
                    }
                    
                    return PlaceholderResult.value(Component.literal("Neutral").withStyle(ChatFormatting.WHITE));
                });

        register("top_1", (member) -> getTopFaction(0, member));
        register("top_2", (member) -> getTopFaction(1, member));
        register("top_3", (member) -> getTopFaction(2, member));

        register(
                "leaderboard_rank",
                (member) -> {
                    Faction faction = member.getFaction();
                    if (faction == null) return Component.empty();

                    List<Faction> sorted = Faction.all().stream()
                            .sorted((f1, f2) -> Integer.compare(f2.getPower(), f1.getPower()))
                            .collect(java.util.stream.Collectors.toList());

                    int rank = sorted.indexOf(faction) + 1;
                    
                    if (rank <= 3 && rank > 0) {
                        return Component.empty();
                    }
                    
                    return Component.empty()
                            .append(Component.literal(rank + ". ").withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(faction.getName()).withStyle(faction.getColor()))
                            .append(Component.literal(" - " + faction.getPower()).withStyle(ChatFormatting.YELLOW));
                });
    }

    private static Component getTopFaction(int index, User member) {
        List<Faction> sorted = Faction.all().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getPower(), f1.getPower()))
                .collect(java.util.stream.Collectors.toList());

        if (index < sorted.size()) {
            Faction f = sorted.get(index);
            
            boolean isPlayerFaction = member.isInFaction() && member.getFaction().getID().equals(f.getID());
            
            if (isPlayerFaction) {
                return Component.empty()
                        .append(Component.literal((index + 1) + ". ").withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(f.getName()).withStyle(f.getColor()))
                        .append(Component.literal(" - " + f.getPower()).withStyle(ChatFormatting.YELLOW));
            } else {
                return Component.empty()
                        .append(Component.literal((index + 1) + ". ").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(f.getName()).withStyle(f.getColor()))
                        .append(Component.literal(" - " + f.getPower()).withStyle(ChatFormatting.WHITE));
            }
        } else {
            return Component.literal((index + 1) + ". ---").withStyle(ChatFormatting.DARK_GRAY);
        }
    }

    private static int rgbToInt(int red, int green, int blue) {
        return (red & 255 << 16) | (green & 255 << 8) | (blue & 255);
    }

    private static int mapBoundRange(
            int from_min, int from_max, int to_min, int to_max, int value) {
        return Math.min(
                to_max,
                Math.max(
                        to_min,
                        to_min + ((value - from_min) * (to_max - to_min)) / (from_max - from_min)));
    }
}
