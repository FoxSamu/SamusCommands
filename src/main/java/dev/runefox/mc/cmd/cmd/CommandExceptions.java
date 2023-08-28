package dev.runefox.mc.cmd.cmd;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

public interface CommandExceptions {
    public static final SimpleCommandExceptionType NO_MAIN_HOME = new SimpleCommandExceptionType(
        ModCommands.message("home", "not_found.main", HomeCommand.deadMainName(false))
    );
    public static final DynamicCommandExceptionType NO_NAMED_HOME = new DynamicCommandExceptionType(
        name -> ModCommands.message("home", "not_found.named", HomeCommand.deadHomeName(name + ""))
    );

    public static final SimpleCommandExceptionType MAX_HOMES_REACHED = new SimpleCommandExceptionType(
        ModCommands.message("sethome", "max_reached")
    );

    public static final SimpleCommandExceptionType DELETE_NO_MAIN_HOME = new SimpleCommandExceptionType(
        ModCommands.message("delhome", "not_found.main", HomeCommand.deadMainName(false))
    );
    public static final DynamicCommandExceptionType DELETE_NO_NAMED_HOME = new DynamicCommandExceptionType(
        name -> ModCommands.message("delhome", "not_found.named", HomeCommand.deadHomeName(name + ""))
    );

    public static final SimpleCommandExceptionType GET_NO_MAIN_HOME = new SimpleCommandExceptionType(
        ModCommands.message("gethome", "not_found.main", HomeCommand.deadMainName(false))
    );
    public static final DynamicCommandExceptionType GET_NO_NAMED_HOME = new DynamicCommandExceptionType(
        name -> ModCommands.message("gethome", "not_found.named", HomeCommand.deadHomeName(name + ""))
    );
}
