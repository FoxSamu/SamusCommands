package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public abstract class Command {
    private final String name;

    private final List<String> aliases = new ArrayList<>();

    public Command(String name) {
        this.name = name;
    }

    public Command alias(String name) {
        aliases.add(name);
        return this;
    }

    public String name() {
        return name;
    }

    public MutableComponent message(String key) {
        return ModCommands.message(name, key);
    }

    public MutableComponent message(String key, Object... format) {
        return ModCommands.message(name, key, format);
    }

    public abstract LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd);

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(make(Commands.literal(name)));

        aliases.forEach(alias -> {
            dispatcher.register(make(Commands.literal(alias)));
        });
    }
}
