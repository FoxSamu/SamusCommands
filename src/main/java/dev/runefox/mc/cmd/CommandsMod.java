package dev.runefox.mc.cmd;

import com.mojang.brigadier.CommandDispatcher;
import dev.runefox.mc.cmd.cmd.ModCommands;
import dev.runefox.mc.cmd.net.ClientPingPacket;
import dev.runefox.mc.cmd.net.Network;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
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

		CommandRegistrationCallback.EVENT.register((dispatcher, regs, environment) -> ModCommands.init(dispatcher));

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> Network.sendClient(handler.player, new ClientPingPacket()));
	}
}
