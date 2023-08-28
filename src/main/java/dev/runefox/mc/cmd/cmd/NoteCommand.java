package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.runefox.mc.cmd.ModGameRules;
import dev.runefox.mc.cmd.ModPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class NoteCommand extends Command {
    public NoteCommand(String name) {
        super(name);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> make(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        return cmd.then(
            Commands.argument("text", StringArgumentType.greedyString())
                    .executes(this::addNote)
        )
                  .executes(this::lastNote);
    }

    private int lastNote(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ModPlayer controller = ModPlayer.get(player);

        if (controller.noteCount() == 0) {
            src.sendFailure(ModCommands.message("notes", "no_notes"));
            return 0;
        }

        int index = controller.lastNoteLine();
        src.sendSuccess(() -> NotesCommand.makeNoteResult(index, controller.getNote(index)), false);
        return index;
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

        String text = NotesCommand.formatNote(StringArgumentType.getString(ctx, "text"), src);
        if (text.length() > NotesCommand.MAX_NOTE_LENGTH) {
            src.sendFailure(ModCommands.message("notes", "too_long"));
            return 0;
        }

        int index = controller.note(text);
        src.sendSuccess(() -> NotesCommand.makeNoteResult(index, text), false);
        return index;
    }
}
