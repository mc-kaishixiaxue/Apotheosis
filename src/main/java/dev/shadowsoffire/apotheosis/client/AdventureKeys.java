package dev.shadowsoffire.apotheosis.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants.Type;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.net.RadialStateChangePayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.network.PacketDistributor;

public class AdventureKeys {

    public static final KeyMapping TOGGLE_RADIAL = new KeyMapping("key." + Apotheosis.MODID + ".toggle_radial_mining", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, Type.KEYSYM, GLFW.GLFW_KEY_O,
        "key.categories." + Apotheosis.MODID);

    @SubscribeEvent
    public static void handleKeys(ClientTickEvent.Post e) {
        if (Minecraft.getInstance().player == null) return;

        while (TOGGLE_RADIAL.consumeClick() && TOGGLE_RADIAL.isConflictContextAndModifierActive()) {
            if (Minecraft.getInstance().screen == null) {
                PacketDistributor.sendToServer(RadialStateChangePayload.INSTANCE);
            }
        }
    }
}
