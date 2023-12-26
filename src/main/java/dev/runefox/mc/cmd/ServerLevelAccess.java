package dev.runefox.mc.cmd;

import dev.runefox.mc.cmd.pregen.Pregenerator;

public interface ServerLevelAccess {
    Pregenerator rfx_cmd_pregenerator();
    void rfx_cmd_pregenerator(Pregenerator pregen);
}
