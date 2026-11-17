package com.visualclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

public class Config {
    public static boolean hud = true;
    public static boolean armor = true;
    public static boolean rainbow = true;
    public static boolean fullbright = false;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("visualclient.properties");
    }

    public static void load() {
        Path p = path();
        if (!Files.exists(p)) return;
        try (InputStream in = Files.newInputStream(p)) {
            Properties pr = new Properties();
            pr.load(in);
            hud = Boolean.parseBoolean(pr.getProperty("hud", "true"));
            armor = Boolean.parseBoolean(pr.getProperty("armor", "true"));
            rainbow = Boolean.parseBoolean(pr.getProperty("rainbow", "true"));
            fullbright = Boolean.parseBoolean(pr.getProperty("fullbright", "false"));
        } catch (IOException ignored) {
        }
    }

    // Called only when the menu closes, never per frame
    public static void save() {
        Properties pr = new Properties();
        pr.setProperty("hud", String.valueOf(hud));
        pr.setProperty("armor", String.valueOf(armor));
        pr.setProperty("rainbow", String.valueOf(rainbow));
        pr.setProperty("fullbright", String.valueOf(fullbright));
        try (OutputStream out = Files.newOutputStream(path())) {
            pr.store(out, "Visual Client");
        } catch (IOException ignored) {
        }
    }
}
