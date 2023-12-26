package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.runefox.mc.cmd.pregen.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.commands.Commands.*;

public class PregenCommand extends Command {
    public PregenCommand(String name) {
        super(name);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        return cmd.requires(src -> src.hasPermission(2))
                  .then(makeStart(literal("start")))
                  .then(makeStop(literal("stop")))
                  .then(makeStatus(literal("status")));
    }

    private LiteralArgumentBuilder<CommandSourceStack> makeStart(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        return cmd
                   .executes(this::startHere)
                   .then(
                       literal("one")
                           .executes(this::startHere)
                           .then(
                               argument("pos", ColumnPosArgument.columnPos())
                                   .executes(this::startPos)
                           )
                   )
                   .then(
                       literal("onion")
                           .then(
                               argument("layers", IntegerArgumentType.integer(1))
                                   .executes(this::startOnionHere)
                                   .then(
                                       argument("pos", ColumnPosArgument.columnPos())
                                           .executes(this::startOnion)
                                   )
                                   .then(
                                       literal("spawn")
                                           .executes(this::startOnionSpawn)
                                   )
                           )
                   )
                   .then(
                       literal("rect")
                           .then(
                               argument("pos1", ColumnPosArgument.columnPos())
                                   .then(
                                       argument("pos2", ColumnPosArgument.columnPos())
                                           .executes(this::startRect)
                                   )
                           )
                   );
    }

    private LiteralArgumentBuilder<CommandSourceStack> makeStop(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        return cmd.executes(this::stop);
    }

    private LiteralArgumentBuilder<CommandSourceStack> makeStatus(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        return cmd.executes(this::status);
    }

    private int start(CommandSourceStack src, PositionIterator iterator) {
        if (Pregenerator.get(src.getLevel()) != null) {
            src.sendFailure(message("already_generating"));
            return 0;
        } else {
            Pregenerator pregen = new Pregenerator(src.getLevel(), iterator, new PregenListener() {
                @Override
                public void update(int ok, int fail, int total) {
                }

                @Override
                public void finish(int ok, int fail, int total) {
                    if (ok + fail == total)
                        src.sendSuccess(() -> message("finished.all", ok, fail, total), true);
                    else
                        src.sendSuccess(() -> message("finished.some", ok + fail, ok, fail, total), true);
                }
            });
            pregen.start();
            src.sendSuccess(() -> message("started", iterator.total()), true);
            return 1;
        }
    }

    private int stop(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        Pregenerator pregen = Pregenerator.get(src.getLevel());

        if (pregen == null) {
            src.sendFailure(message("not_generating"));
            return 0;
        } else {
            pregen.stop();
            src.sendSuccess(() -> message("stopping"), true);
            return 1;
        }
    }

    private int status(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        Pregenerator pregen = Pregenerator.get(src.getLevel());

        if (pregen == null) {
            src.sendSuccess(() -> message("not_generating"), false);
            return 0;
        } else {
            PregenStatus status = pregen.status();
            src.sendSuccess(() -> message("status", status.ok(), status.fail(), status.total()), false);
            return 1;
        }
    }

    private static ChunkPos chunk(CommandSourceStack src) {
        Vec3 pos = src.getPosition();
        return new ChunkPos(
            (int) pos.x >> 4,
            (int) pos.z >> 4
        );
    }

    private static ChunkPos chunk(ColumnPos pos) {
        return new ChunkPos(
            pos.x() >> 4,
            pos.z() >> 4
        );
    }

    private int startHere(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();

        ChunkPos pos = chunk(src);

        return start(src, new SingleIterator(pos.x, pos.z));
    }

    private int startPos(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();

        ChunkPos pos = chunk(ColumnPosArgument.getColumnPos(ctx, "pos"));

        return start(src, new SingleIterator(pos.x, pos.z));
    }

    private int startOnion(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();

        ChunkPos pos = chunk(ColumnPosArgument.getColumnPos(ctx, "pos"));
        int layers = IntegerArgumentType.getInteger(ctx, "layers");

        return start(src, new SquareOnionIterator(pos.x, pos.z, layers));
    }

    private int startOnionHere(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();

        ChunkPos pos = chunk(src);
        int layers = IntegerArgumentType.getInteger(ctx, "layers");

        return start(src, new SquareOnionIterator(pos.x, pos.z, layers));
    }

    private int startOnionSpawn(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();

        ChunkPos pos = new ChunkPos(src.getLevel().getSharedSpawnPos());
        int layers = IntegerArgumentType.getInteger(ctx, "layers");

        return start(src, new SquareOnionIterator(pos.x, pos.z, layers));
    }

    private int startRect(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();

        ChunkPos pos1 = chunk(ColumnPosArgument.getColumnPos(ctx, "pos1"));
        ChunkPos pos2 = chunk(ColumnPosArgument.getColumnPos(ctx, "pos2"));

        return start(src, new RowWiseRectIterator(pos1.x, pos1.z, pos2.x, pos2.z));
    }
}
