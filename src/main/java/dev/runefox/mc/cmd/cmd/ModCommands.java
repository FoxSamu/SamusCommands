package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.CommandDispatcher;
import dev.runefox.mc.cmd.FallbackLanguage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.GameRules;

import java.util.function.Predicate;

public class ModCommands {
    public static void init(CommandDispatcher<CommandSourceStack> dispatcher) {
        new HomeCommand("home").alias("h").register(dispatcher);
        new SetHomeCommand("sethome").alias("sh").register(dispatcher);
        new GetHomeCommand("gethome").alias("gh").register(dispatcher);
        new DelHomeCommand("delhome").register(dispatcher);
        new HomesCommand("homes").register(dispatcher);
        new ClearHomesCommand("clearhomes").register(dispatcher);

        new SpawnCommand("spawn").alias("s").register(dispatcher);

        new TpaCommand("tpa", false).register(dispatcher);
        new TpaCommand("tpc", true).register(dispatcher);
        new TpAcceptCommand("tpaccept", false).register(dispatcher);
        new TpAcceptCommand("tpdeny", true).register(dispatcher);
        new TpBlacklistCommand("tpblacklist", false).alias("tpbl").register(dispatcher);
        new TpBlacklistCommand("tpwhitelist", true).alias("tpwl").register(dispatcher);

        new NoteCommand("note").register(dispatcher);
        new NotesCommand("notes").register(dispatcher);
    }

    public static <T extends GameRules.Value<T>> Predicate<CommandSourceStack> requireGameRule(GameRules.Key<T> key, Predicate<T> pred) {
        return src -> pred.test(src.getServer().getGameRules().getRule(key));
    }

    public static Predicate<CommandSourceStack> requireTrue(GameRules.Key<GameRules.BooleanValue> key) {
        return requireGameRule(key, GameRules.BooleanValue::get);
    }

    public static MutableComponent message(String command, String key) {
        String k = "commands.rfx-cmd." + command + "." + key;
        return Component.translatableWithFallback(k, FallbackLanguage.get(k));
    }

    public static MutableComponent message(String command, String key, Object... format) {
        String k = "commands.rfx-cmd." + command + "." + key;
        return Component.translatableWithFallback(k, FallbackLanguage.get(k), format);
    }
}
