package com.visualclient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MenuScreen extends Screen {
    private final long openedAt = System.currentTimeMillis();
    private final List<ButtonWidget> buttons = new ArrayList<>();
    private final List<Integer> baseY = new ArrayList<>();
    private boolean settled = false;

    public MenuScreen() {
        super(Text.literal("Visual Client"));
    }

    @Override
    protected void init() {
        buttons.clear();
        baseY.clear();
        int bw = 160, x = (width - bw) / 2, y = height / 2 - 40;
        addToggle("HUD", () -> Config.hud, x, y, bw);
        addToggle("Armor", () -> Config.armor, x, y + 24, bw);
        addToggle("Rainbow", () -> Config.rainbow, x, y + 48, bw);
        addToggle("Fullbright", () -> Config.fullbright, x, y + 72, bw);
    }

    private void addToggle(String name, BooleanSupplier get, int x, int y, int w) {
        ButtonWidget b = ButtonWidget.builder(label(name, get.getAsBoolean()), btn -> {
            switch (name) {
                case "HUD" -> Config.hud = !Config.hud;
                case "Armor" -> Config.armor = !Config.armor;
                case "Rainbow" -> Config.rainbow = !Config.rainbow;
                case "Fullbright" -> Config.fullbright = !Config.fullbright;
            }
            btn.setMessage(label(name, get.getAsBoolean()));
        }).dimensions(x, y, w, 20).build();
        addDrawableChild(b);
        buttons.add(b);
        baseY.add(y);
    }

    private Text label(String name, boolean on) {
        return Text.literal(name + ": " + (on ? "§aON" : "§cOFF"));
    }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
        // no vanilla blur (it is expensive); we draw a cheap animated background in render()
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        float t = Math.min(1f, (System.currentTimeMillis() - openedAt) / 300f);
        float e = 1f - (1f - t) * (1f - t) * (1f - t);
        int slide = (int) ((1f - e) * 30);

        ctx.fill(0, 0, width, height, ((int) (150 * e) << 24));

        int pw = 190, ph = 140;
        int px = (width - pw) / 2, py = (height - ph) / 2 + slide;
        ctx.fill(px, py, px + pw, py + ph, ((int) (230 * e) << 24) | 0x101018);
        ctx.fill(px, py, px + pw, py + 2, Hud.accentColor());
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, py + 10, 0xFFFFFFFF);

        // move buttons only while the open animation runs
        if (!settled) {
            for (int i = 0; i < buttons.size(); i++) {
                buttons.get(i).setY(baseY.get(i) + slide);
            }
            settled = t >= 1f;
        }
        super.render(ctx, mx, my, delta);
    }

    @Override
    public void removed() {
        Config.save();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
