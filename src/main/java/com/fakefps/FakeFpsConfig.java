package com.fakefps;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FakeFpsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("fakefps.json");
    public static FakeFpsConfig INSTANCE = new FakeFpsConfig();

    public boolean enabled = true;
    public int minFps = 140;
    public int maxFps = 180;
    public int intervalMs = 500;
    public String format = "[FPS: {fps}]";
    public int color = 0xFFFFFF;
    public boolean shadow = true;
    public float scale = 1.0f;
    public int x = 4;
    public int y = 4;

    public void sanitize() {
        if (minFps < 0) minFps = 0;
        if (maxFps < 0) maxFps = 0;
        if (minFps > maxFps) {
            int t = minFps;
            minFps = maxFps;
            maxFps = t;
        }
        if (intervalMs < 50) intervalMs = 50;
        if (scale < 0.5f) scale = 0.5f;
        if (scale > 4f) scale = 4f;
        if (format == null || format.isEmpty()) format = "[FPS: {fps}]";
    }

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                FakeFpsConfig c = GSON.fromJson(Files.readString(PATH), FakeFpsConfig.class);
                if (c != null) INSTANCE = c;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        INSTANCE.sanitize();
    }

    public static void save() {
        try {
            INSTANCE.sanitize();
            Files.writeString(PATH, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
