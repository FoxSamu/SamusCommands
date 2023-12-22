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




    public static final SimpleCommandExceptionType NO_MAIN_POI = new SimpleCommandExceptionType(
        ModCommands.message("poi", "not_found.main", PoiCommand.deadMainName(false))
    );
    public static final DynamicCommandExceptionType NO_NAMED_POI = new DynamicCommandExceptionType(
        name -> ModCommands.message("poi", "not_found.named", PoiCommand.deadPoiName(name + ""))
    );

    public static final SimpleCommandExceptionType DELETE_NO_MAIN_POI = new SimpleCommandExceptionType(
        ModCommands.message("delpoi", "not_found.main", PoiCommand.deadMainName(false))
    );
    public static final DynamicCommandExceptionType DELETE_NO_NAMED_POI = new DynamicCommandExceptionType(
        name -> ModCommands.message("delpoi", "not_found.named", PoiCommand.deadPoiName(name + ""))
    );

    public static final SimpleCommandExceptionType GET_NO_MAIN_POI = new SimpleCommandExceptionType(
        ModCommands.message("getpoi", "not_found.main", PoiCommand.deadMainName(false))
    );
    public static final DynamicCommandExceptionType GET_NO_NAMED_POI = new DynamicCommandExceptionType(
        name -> ModCommands.message("getpoi", "not_found.named", PoiCommand.deadPoiName(name + ""))
    );
}
