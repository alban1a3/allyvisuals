package com.visualclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.lwjgl.glfw.GLFW;

public class VisualClient implements ClientModInitializer {
    public static KeyBinding menuKey;
    public static KeyBinding fullbrightKey;
    private static boolean appliedByUs = false;
    private static boolean lastFullbright = false;
    private static int ticks = 0;

    @Override
    public void onInitializeClient() {
        Config.load();

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visualclient.menu", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, "category.visualclient"));
        fullbrightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visualclient.fullbright", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G, "category.visualclient"));

        HudRenderCallback.EVENT.register(Hud::render);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ticks++;
            while (menuKey.wasPressed()) client.setScreen(new MenuScreen());
            while (fullbrightKey.wasPressed()) Config.fullbright = !Config.fullbright;

            if (client.player == null) {
                appliedByUs = false;
                return;
            }

            // HUD data is rebuilt 10x/sec instead of every frame
            if (ticks % 2 == 0) Hud.update(client);

            // Fullbright check: only on change, or once every 2 seconds
            // (re-applies the effect after death / dimension change)
            if (Config.fullbright != lastFullbright || ticks % 40 == 0) {
                lastFullbright = Config.fullbright;
                boolean has = client.player.hasStatusEffect(StatusEffects.NIGHT_VISION);
                if (Config.fullbright && !has) {
                    client.player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.NIGHT_VISION, 32000, 0, false, false, false));
                    appliedByUs = true;
                } else if (!Config.fullbright && appliedByUs) {
                    client.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                    appliedByUs = false;
                }
            }
        });
    }
}
