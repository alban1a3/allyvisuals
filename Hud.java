package com.visualclient;

import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

/**
 * Render is draw-only: all strings, widths and colors are prepared in update()
 * (called 10x/sec from the tick event), so a frame does no formatting,
 * no string concatenation and no allocations.
 */
public class Hud {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private static final String[] lines = {"", "", ""};
    private static int textWidth = 0;

    private static final ItemStack[] stacks = {
            ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    private static final String[] durText = new String[4];
    private static final int[] durColor = new int[4];

    private static long shownAt = -1;

    public static void update(MinecraftClient mc) {
        if (!Config.hud || mc.player == null) return;

        lines[0] = "FPS: " + mc.getCurrentFps();
        lines[1] = String.format("XYZ: %.1f / %.1f / %.1f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ());
        lines[2] = "Facing: " + mc.player.getHorizontalFacing().asString();

        int w = 0;
        for (String s : lines) w = Math.max(w, mc.textRenderer.getWidth(s));
        textWidth = w;

        if (Config.armor) {
            for (int i = 0; i < 4; i++) {
                ItemStack st = mc.player.getEquippedStack(SLOTS[i]);
                stacks[i] = st;
                if (!st.isEmpty() && st.isDamageable()) {
                    int left = st.getMaxDamage() - st.getDamage();
                    durText[i] = left + "/" + st.getMaxDamage();
                    durColor[i] = Color.HSBtoRGB(left / (float) st.getMaxDamage() * 0.33f, 0.9f, 1f)
                            | 0xFF000000;
                } else {
                    durText[i] = null;
                }
            }
        }
    }

    public static void render(DrawContext ctx, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!Config.hud || mc.player == null || mc.options.hudHidden) {
            shownAt = -1;
            return;
        }
        long now = System.currentTimeMillis();
        if (shownAt < 0) shownAt = now;

        // slide-in animation (ease-out cubic, 500 ms); skipped math once finished
        float e = 1f;
        long dt = now - shownAt;
        if (dt < 500) {
            float t = dt / 500f;
            e = 1f - (1f - t) * (1f - t) * (1f - t);
        }

        int w = textWidth;
        int h = lines.length * 11;
        int x = e >= 1f ? 4 : (int) (-(w + 10) + (w + 14) * e);
        int y = 6;

        ctx.fill(x - 4, y - 3, x + w + 4, y + h + 1, 0x90000000);
        ctx.fill(x - 4, y - 3, x - 2, y + h + 1, accentColor());

        for (int i = 0; i < lines.length; i++) {
            ctx.drawTextWithShadow(mc.textRenderer, lines[i], x + 2, y + i * 11, 0xFFFFFFFF);
        }

        if (Config.armor) {
            int ay = y + h + 8;
            for (int i = 0; i < 4; i++) {
                if (stacks[i].isEmpty()) continue;
                ctx.fill(x - 4, ay - 1, x + w + 4, ay + 17, 0x70000000);
                ctx.drawItem(stacks[i], x, ay);
                if (durText[i] != null) {
                    ctx.drawTextWithShadow(mc.textRenderer, durText[i], x + 20, ay + 4, durColor[i]);
                }
                ay += 19;
            }
        }
    }

    public static int accentColor() {
        if (!Config.rainbow) return 0xFF55AAFF;
        float hue = ((System.currentTimeMillis() / 20) % 360) / 360f;
        return Color.HSBtoRGB(hue, 0.7f, 1f) | 0xFF000000;
    }
}
