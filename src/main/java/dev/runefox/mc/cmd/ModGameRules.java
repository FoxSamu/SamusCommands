package dev.runefox.mc.cmd;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

public class ModGameRules {
    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_HOME_TP = GameRuleRegistry.register(
        "allowHomeTp",
        GameRules.Category.PLAYER,
        GameRuleFactory.createBooleanRule(true, (server, value) -> resendCommands(server))
    );

    public static final GameRules.Key<GameRules.IntegerValue> MAX_HOMES = GameRuleRegistry.register(
        "maxHomes",
        GameRules.Category.PLAYER,
        GameRuleFactory.createIntRule(0, 0)
    );

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_SPAWN_TP = GameRuleRegistry.register(
        "allowSpawnTp",
        GameRules.Category.PLAYER,
        GameRuleFactory.createBooleanRule(true, (server, value) -> resendCommands(server))
    );

    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_TPA = GameRuleRegistry.register(
        "allowTpa",
        GameRules.Category.PLAYER,
        GameRuleFactory.createBooleanRule(true, (server, value) -> resendCommands(server))
    );

    public static final GameRules.Key<GameRules.IntegerValue> TPA_REQUEST_TIMEOUT = GameRuleRegistry.register(
        "tpaRequestTimeout",
        GameRules.Category.PLAYER,
        GameRuleFactory.createIntRule(5 * 60, 1, 60 * 60)
    );

    public static final GameRules.Key<GameRules.BooleanValue> TPA_REQUIRES_SAME_TEAM = GameRuleRegistry.register(
        "tpaRequiresSameTeam",
        GameRules.Category.PLAYER,
        GameRuleFactory.createBooleanRule(false)
    );

    public static final GameRules.Key<EnumRule<TeleportMode>> TPA_MODE = GameRuleRegistry.register(
        "tpaMode",
        GameRules.Category.PLAYER,
        GameRuleFactory.createEnumRule(TeleportMode.both, (server, value) -> resendCommands(server))
    );

    public static final GameRules.Key<GameRules.IntegerValue> MAX_NOTES = GameRuleRegistry.register(
        "maxNotes",
        GameRules.Category.PLAYER,
        GameRuleFactory.createIntRule(1024, 1, 4096)
    );

    public static void init() {
    }

    private static void resendCommands(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers())
            server.getCommands().sendCommands(player);
    }
}
