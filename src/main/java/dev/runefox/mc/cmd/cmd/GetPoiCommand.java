package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.runefox.mc.cmd.ModGameRules;
import dev.runefox.mc.cmd.TeleportPos;
import dev.runefox.mc.cmd.poi.PoiManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.GameRules;

import java.util.function.Predicate;

public class GetPoiCommand extends Command {
    public GetPoiCommand(String name) {
        super(name);
    }

    static final Predicate<CommandSourceStack> PERMS = ModCommands.requireFalse(ModGameRules.HIDE_POI_INFO)
                                                                  .and(ModCommands.requireTrue(ModGameRules.ALLOW_POI))
                                                                  .or(src -> src.hasPermission(2));

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        return cmd.requires(PERMS)
                  .executes(this::main)
                  .then(
                      Commands.argument("name", StringArgumentType.greedyString())
                              .requires(PERMS)
                              .executes(this::named)
                  );
    }

    private int main(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        PoiManager pois = PoiManager.of(src.getServer());

        if (!pois.hasPoi()) {
            throw CommandExceptions.GET_NO_MAIN_POI.create();
        }

        boolean reduced = src.getServer().getGameRules().getBoolean(GameRules.RULE_REDUCEDDEBUGINFO) && !src.hasPermission(2);

        TeleportPos pos = pois.getPoi();
        if (reduced)
            src.sendSuccess(() -> message("reduced.main", PoiCommand.mainName(false)), false);
        else
            src.sendSuccess(() -> message("success.main", PoiCommand.mainName(false), PoisCommand.makePosText(pos, src.getLevel())), false);
        return 1;
    }

    private int named(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        PoiManager pois = PoiManager.of(src.getServer());

        String name = ctx.getArgument("name", String.class);

        if (!pois.hasPoi(name)) {
            throw CommandExceptions.GET_NO_NAMED_POI.create(name);
        }

        boolean reduced = src.getServer().getGameRules().getBoolean(GameRules.RULE_REDUCEDDEBUGINFO) && !src.hasPermission(2);

        TeleportPos pos = pois.getPoi(name);
        if (reduced)
            src.sendSuccess(() -> message("reduced.named", PoiCommand.poiName(name)), false);
        else
            src.sendSuccess(() -> message("success.named", PoiCommand.poiName(name), PoisCommand.makePosText(pos, src.getLevel())), false);
        return 1;
    }
}
