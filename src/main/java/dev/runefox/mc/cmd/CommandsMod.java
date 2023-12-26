package dev.runefox.mc.cmd;

import dev.runefox.mc.cmd.cmd.ModCommands;
import dev.runefox.mc.cmd.net.ClientPingPacket;
import dev.runefox.mc.cmd.net.Network;
import dev.runefox.mc.cmd.poi.PoiManager;
import dev.runefox.mc.cmd.pregen.Pregenerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandsMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("rfx-cmd");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Commands world!");

		FallbackLanguage.load();
		ModGameRules.init();
		Network.init();
		ItemClickHandler.init();
		PoiManager.init();

		CommandRegistrationCallback.EVENT.register((dispatcher, regs, environment) -> ModCommands.init(dispatcher));

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> Network.sendClient(handler.player, new ClientPingPacket()));

        ServerTickEvents.END_WORLD_TICK.register(Pregenerator::tick);
	}
}
