package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.runefox.mc.cmd.ModGameRules;
import dev.runefox.mc.cmd.ModPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

import java.util.List;

public class NotesCommand extends Command {
    public static final int MAX_NOTE_LENGTH = 1024;

    public NotesCommand(String name) {
        super(name);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        return cmd.then(
            Commands.literal("add").then(
                Commands.argument("text", StringArgumentType.greedyString())
                        .executes(this::addNote)
            )
        ).then(
            Commands.literal("edit").then(
                Commands.argument("line", IntegerArgumentType.integer(1)).then(
                    Commands.argument("text", StringArgumentType.greedyString())
                            .executes(this::editNote)
                )
            )
        ).then(
            Commands.literal("remove").then(
                Commands.argument("from", IntegerArgumentType.integer(1)).then(
                    Commands.argument("to", IntegerArgumentType.integer(1))
                            .executes(this::removeRangeNotes)
                ).executes(this::removeOneNote)
            ).then(
                Commands.literal("all")
                        .executes(this::removeAllNotes)
            )
        ).then(
            Commands.literal("last").then(
                        Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> lastNote(ctx, IntegerArgumentType.getInteger(ctx, "amount")))
                    )
                    .executes(ctx -> lastNote(ctx, 1))
        ).then(
            Commands.literal("move").then(
                Commands.argument("from", IntegerArgumentType.integer(1)).then(
                    Commands.argument("to", IntegerArgumentType.integer(1))
                            .executes(this::moveNote)
                )
            )
        ).then(
            Commands.literal("get").then(
                Commands.argument("from", IntegerArgumentType.integer(1)).then(
                            Commands.argument("to", IntegerArgumentType.integer(1))
                                    .executes(this::listRangeNotes)
                        )
                        .executes(this::listOneNote)
            ).then(
                Commands.literal("all")
                        .executes(this::listAllNotes)
            )
        ).executes(this::listAllNotes);
    }

    private int addNote(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer controller = ModPlayer.get(player);

        int max = src.getServer().getGameRules().getInt(ModGameRules.MAX_NOTES);
        if (controller.noteCount() >= max) {
            src.sendFailure(ModCommands.message("notes", "too_much"));
            return 0;
        }

        String text = formatNote(StringArgumentType.getString(ctx, "text"), src);
        if (text.length() > NotesCommand.MAX_NOTE_LENGTH) {
            src.sendFailure(ModCommands.message("notes", "too_long"));
            return 0;
        }

        int index = controller.note(text);
        src.sendSuccess(() -> makeNoteResult(index, text), false);
        return index;
    }

    private int editNote(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer controller = ModPlayer.get(player);

        int line = IntegerArgumentType.getInteger(ctx, "line");
        if (line > controller.noteCount()) {
            src.sendFailure(message("no_such_line", line));
            return 0;
        }

        String text = formatNote(StringArgumentType.getString(ctx, "text"), src);
        if (text.length() > NotesCommand.MAX_NOTE_LENGTH) {
            src.sendFailure(ModCommands.message("notes", "too_long"));
            return 0;
        }

        String old = controller.editNote(line, text);
        src.sendSuccess(() -> makeNoteDifference(line, old, text), false);
        return line;
    }

    private int removeOneNote(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer controller = ModPlayer.get(player);

        int line = IntegerArgumentType.getInteger(ctx, "from");
        if (line > controller.noteCount()) {
            src.sendFailure(message("no_such_line", line));
            return 0;
        }

        String old = controller.removeNote(line);
        src.sendSuccess(() -> makeNoteRemoval(line, old), false);
        return line;
    }

    private int removeRangeNotes(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer controller = ModPlayer.get(player);

        int from = IntegerArgumentType.getInteger(ctx, "from");
        int to = IntegerArgumentType.getInteger(ctx, "to");
        if (from > to) {
            src.sendFailure(message("backwards_range", from, to));
            return 0;
        }

        List<String> old = controller.removeNotes(from, to);
        if (old.isEmpty()) {
            src.sendFailure(message("none_removed"));
            return 0;
        }

        int f = Math.max(from, 1);
        for (String ln : old) {
            int f1 = f++;
            src.sendSuccess(() -> makeNoteRemoval(f1, ln), false);
        }
        return old.size();
    }

    private int removeAllNotes(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer controller = ModPlayer.get(player);

        List<String> old = controller.clearNotes();
        if (old.isEmpty()) {
            src.sendFailure(message("none_removed"));
            return 0;
        }

        int f = 1;
        for (String ln : old) {
            int f1 = f++;
            src.sendSuccess(() -> makeNoteRemoval(f1, ln), false);
        }
        return old.size();
    }

    private int lastNote(CommandContext<CommandSourceStack> ctx, int amount) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer controller = ModPlayer.get(player);

        if (controller.noteCount() == 0) {
            src.sendFailure(message("no_notes"));
            return 0;
        }

        int from = Math.max(controller.noteCount() - amount + 1, 1);
        int to = controller.noteCount();

        for (int i = from; i <= to; i++) {
            int i1 = i;
            src.sendSuccess(() -> makeNoteResult(i1, controller.getNote(i1)), false);
        }
        return to - from + 1;
    }

    private int moveNote(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer controller = ModPlayer.get(player);

        int from = IntegerArgumentType.getInteger(ctx, "from");
        int to = IntegerArgumentType.getInteger(ctx, "to");

        String text = controller.moveNote(from, to);
        if (text == null) {
            src.sendFailure(message("no_such_note"));
            return 0;
        }

        src.sendSuccess(() -> makeNoteDifference(from, to, text), false);
        return to;
    }

    private int listOneNote(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer controller = ModPlayer.get(player);

        int line = IntegerArgumentType.getInteger(ctx, "from");
        if (line > controller.noteCount()) {
            src.sendFailure(message("no_such_line", line));
            return 0;
        }

        String old = controller.getNote(line);
        src.sendSuccess(() -> makeNoteResult(line, old), false);
        return line;
    }

    private int listRangeNotes(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer controller = ModPlayer.get(player);

        int from = IntegerArgumentType.getInteger(ctx, "from");
        int to = IntegerArgumentType.getInteger(ctx, "to");
        if (from > to) {
            src.sendFailure(message("backwards_range", from, to));
            return 0;
        }

        List<String> old = controller.getNotes(from, to);
        if (old.isEmpty()) {
            src.sendFailure(message("none_removed"));
            return 0;
        }
        int f = Math.max(from, 1);
        for (String ln : old) {
            int f1 = f++;
            src.sendSuccess(() -> makeNoteResult(f1, ln), false);
        }
        return old.size();
    }

    private int listAllNotes(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer controller = ModPlayer.get(player);

        if (controller.noteCount() == 0) {
            src.sendFailure(message("no_notes"));
            return 0;
        }

        for (int i = 1, to = controller.noteCount(); i <= to; i++) {
            int i1 = i;
            src.sendSuccess(() -> makeNoteResult(i1, controller.getNote(i1)), false);
        }
        return controller.noteCount();
    }

    public static MutableComponent makeNoteResult(int index, String text) {
        return Component.empty()
                        .append(Component.literal("[" + index + "]").withStyle(ChatFormatting.GREEN))
                        .append(" ")
                        .append(noteText(text));
    }

    public static MutableComponent makeNoteDifference(int index, String from, String to) {
        return Component.empty()
                        .append(Component.literal("[" + index + "]").withStyle(ChatFormatting.RED, ChatFormatting.STRIKETHROUGH))
                        .append(" ")
                        .append(noteText(from).withStyle(ChatFormatting.YELLOW, ChatFormatting.STRIKETHROUGH))
                        .append("\n")
                        .append(Component.literal("[" + index + "]").withStyle(ChatFormatting.GREEN))
                        .append(" ")
                        .append(noteText(to));
    }

    public static MutableComponent makeNoteDifference(int from, int to, String text) {
        return Component.empty()
                        .append(Component.literal("[" + from + "]").withStyle(ChatFormatting.RED, ChatFormatting.STRIKETHROUGH))
                        .append(" ")
                        .append(Component.literal("[" + to + "]").withStyle(ChatFormatting.GREEN))
                        .append(" ")
                        .append(noteText(text));
    }

    public static MutableComponent makeNoteRemoval(int index, String text) {
        return Component.empty()
                        .append(Component.literal("[" + index + "]").withStyle(ChatFormatting.RED, ChatFormatting.STRIKETHROUGH))
                        .append(" ")
                        .append(noteText(text).withStyle(ChatFormatting.YELLOW, ChatFormatting.STRIKETHROUGH));
    }

    private static MutableComponent noteText(String text) {
        return Component.literal(text)
                        .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text))
                                                 .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click"))));
    }

    public static String formatNote(String text, CommandSourceStack src) throws CommandSyntaxException {
        if (src.getServer().getGameRules().getBoolean(GameRules.RULE_REDUCEDDEBUGINFO) && !src.hasPermission(2))
            return text;

        BlockPos pos = src.getPlayerOrException().blockPosition();
        ResourceLocation dimension = src.getLevel().dimension().location();

        text = text.replace("{xyz}", pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        text = text.replace("{pos}", pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        text = text.replace("{coords}", pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        text = text.replace("{xz}", pos.getX() + ", " + pos.getZ());
        text = text.replace("{x}", pos.getX() + "");
        text = text.replace("{y}", pos.getY() + "");
        text = text.replace("{z}", pos.getZ() + "");
        text = text.replace("{dimension}", dimension.toString());
        text = text.replace("{dim}", dimension.toString());

        return text;
    }
}
