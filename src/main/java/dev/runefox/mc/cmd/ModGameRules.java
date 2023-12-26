package dev.runefox.mc.cmd;

import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

public class ModGameRules {
    public static final CustomGameRuleCategory CATEGORY = new CustomGameRuleCategory(new ResourceLocation("rfx-cmd", "commands"), Component.translatable(
        "gamerule.category.rfx-cmd.main"
    ));

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_HOME_TP = GameRuleRegistry.register(
        "allowHomeTp",
        CATEGORY,
        GameRuleFactory.createBooleanRule(true, (server, value) -> resendCommands(server))
    );

    public static final GameRules.Key<GameRules.IntegerValue> MAX_HOMES = GameRuleRegistry.register(
        "maxHomes",
        CATEGORY,
        GameRuleFactory.createIntRule(0, 0)
    );

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_TP = GameRuleRegistry.register(
        "allowSpawnTp",
        CATEGORY,
        GameRuleFactory.createBooleanRule(true, (server, value) -> resendCommands(server))
    );

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_TPA = GameRuleRegistry.register(
        "allowTpa",
        CATEGORY,
        GameRuleFactory.createBooleanRule(true, (server, value) -> resendCommands(server))
    );

    public static final GameRules.Key<GameRules.IntegerValue> TPA_REQUEST_TIMEOUT = GameRuleRegistry.register(
        "tpaRequestTimeout",
        CATEGORY,
        GameRuleFactory.createIntRule(5 * 60, 1, 60 * 60)
    );

    public static final GameRules.Key<GameRules.BooleanValue> TPA_REQUIRES_SAME_TEAM = GameRuleRegistry.register(
        "tpaRequiresSameTeam",
        CATEGORY,
        GameRuleFactory.createBooleanRule(false)
    );

    public static final GameRules.Key<EnumRule<TeleportMode>> TPA_MODE = GameRuleRegistry.register(
        "tpaMode",
        CATEGORY,
        GameRuleFactory.createEnumRule(TeleportMode.both, (server, value) -> resendCommands(server))
    );

    public static final GameRules.Key<GameRules.IntegerValue> MAX_NOTES = GameRuleRegistry.register(
        "maxNotes",
        CATEGORY,
        GameRuleFactory.createIntRule(1024, 1, 4096)
    );

    public static final GameRules.Key<EnumRule<DeathTeleportMode>> DEATH_TP = GameRuleRegistry.register(
        "deathTp",
        CATEGORY,
        GameRuleFactory.createEnumRule(DeathTeleportMode.compass, (server, value) -> resendCommands(server))
    );

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_POI = GameRuleRegistry.register(
        "allowPoi",
        CATEGORY,
        GameRuleFactory.createBooleanRule(true, (server, value) -> resendCommands(server))
    );

    public static final GameRules.Key<GameRules.BooleanValue> ALL_CAN_SET_POI = GameRuleRegistry.register(
        "allCanSetPoi",
        CATEGORY,
        GameRuleFactory.createBooleanRule(false, (server, value) -> resendCommands(server))
    );

    public static final GameRules.Key<GameRules.BooleanValue> HIDE_POI_INFO = GameRuleRegistry.register(
        "hidePoiInfo",
        CATEGORY,
        GameRuleFactory.createBooleanRule(false, (server, value) -> resendCommands(server))
    );



    public static final GameRules.Key<EnumRule<AnimalTeleportMode>> TELEPORT_PETS = GameRuleRegistry.register(
        "teleportPets",
        CATEGORY,
        GameRuleFactory.createEnumRule(AnimalTeleportMode.dimension)
    );

    public static final GameRules.Key<EnumRule<AnimalTeleportMode>> TELEPORT_LEASHED_MOBS = GameRuleRegistry.register(
        "teleportLeashedMobs",
        CATEGORY,
        GameRuleFactory.createEnumRule(AnimalTeleportMode.interdimensional)
    );

    public static void init() {
    }

    private static void resendCommands(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers())
            server.getCommands().sendCommands(player);
    }
}
