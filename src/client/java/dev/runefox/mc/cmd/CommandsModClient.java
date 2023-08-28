package dev.runefox.mc.cmd;

import com.mojang.blaze3d.platform.InputConstants;
import dev.runefox.mc.cmd.net.ClientNetwork;
import dev.runefox.mc.cmd.net.Network;
import dev.runefox.mc.cmd.net.ServerGoHomePacket;
import dev.runefox.mc.cmd.net.ServerSetHomePacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class CommandsModClient implements ClientModInitializer, ClientTickEvents.StartTick {
    public static boolean synced;

    private static KeyMapping homeKey;
    private static KeyMapping setHomeKey;

    private static boolean homeKeyDown;
    private static boolean setHomeKeyDown;

    public static KeyMapping homeKey() {
        return homeKey;
    }

    public static KeyMapping setHomeKey() {
        return setHomeKey;
    }

    @Override
    public void onInitializeClient() {
        ClientNetwork.init();

        ClientTickEvents.START_CLIENT_TICK.register(this);

        homeKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.rfx-cmd.home",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.rfx-cmd.categories.homes"
        ));

        setHomeKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.rfx-cmd.sethome",
            InputConstants.Type.KEYSYM,
            -1,
            "key.rfx-cmd.categories.homes"
        ));
    }

    @Override
    public void onStartTick(Minecraft client) {
        ClientPacketListener conn = client.getConnection();

        if (homeKey.isDown()) {
            if (!homeKeyDown) {
                if (conn != null && conn.isAcceptingMessages()) {
                    if (synced)
                        Network.sendServer(new ServerGoHomePacket());
                    else
                        Minecraft.getInstance().gui.setOverlayMessage(
                            Component.translatable("message.rfx-cmd.not_synced"),
                            false
                        );
                }
            }
        }
        homeKeyDown = homeKey.isDown();

        if (setHomeKey.isDown()) {
            if (!setHomeKeyDown) {
                if (conn != null && conn.isAcceptingMessages()) {
                    if (synced)
                        Network.sendServer(new ServerSetHomePacket());
                    else
                        Minecraft.getInstance().gui.setOverlayMessage(
                            Component.translatable("message.rfx-cmd.not_synced"),
                            false
                        );
                }
            }
        }
        setHomeKeyDown = setHomeKey.isDown();
    }
}
