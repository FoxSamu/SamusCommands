package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.runefox.mc.cmd.poi.PoiManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class DelPoiCommand extends Command {
    public DelPoiCommand(String name) {
        super(name);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        return cmd.requires(SetPoiCommand.PERMS)
                  .executes(this::main)
                  .then(
                      Commands.argument("name", StringArgumentType.greedyString())
                              .requires(SetPoiCommand.PERMS)
                              .executes(this::named)
                  );
    }

    private int main(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();

        PoiManager pois = PoiManager.of(src.getServer());
        if (!pois.hasPoi()) {
            throw CommandExceptions.DELETE_NO_MAIN_POI.create();
        }

        pois.deletePoi();
        src.sendSuccess(() -> message("success.main", PoiCommand.deadMainName(false)), true);
        return 1;
    }

    private int named(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();

        String name = ctx.getArgument("name", String.class);

        PoiManager pois = PoiManager.of(src.getServer());
        if (!pois.hasPoi(name)) {
            throw CommandExceptions.DELETE_NO_NAMED_POI.create(name);
        }

        pois.deletePoi(name);
        src.sendSuccess(() -> message("success.main", PoiCommand.deadPoiName(name)), true);
        return 1;
    }
}
