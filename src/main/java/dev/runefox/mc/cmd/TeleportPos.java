package dev.runefox.mc.cmd;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public record TeleportPos(BlockPos pos, ResourceLocation dimension, float yrot, float xrot) {
}
