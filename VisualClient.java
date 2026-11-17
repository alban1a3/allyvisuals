package com.visualclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
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

        // 1.21.9+: key categories are objects, not plain strings
        KeyBinding.Category category =
                KeyBinding.Category.create(Identifier.of("visualclient", "main"));

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visualclient.menu", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        fullbrightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visualclient.fullbright", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G, category));

        // HudRenderCallback is deprecated; new HUD API
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT, Identifier.of("visualclient", "hud"), Hud::render);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ticks++;
            while (menuKey.wasPressed()) client.setScreen(new MenuScreen());
            while (fullbrightKey.wasPressed()) Config.fullbright = !Config.fullbright;

            if (client.player == null) {
                appliedByUs = false;
                return;
            }

            if (ticks % 2 == 0) Hud.update(client);

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
