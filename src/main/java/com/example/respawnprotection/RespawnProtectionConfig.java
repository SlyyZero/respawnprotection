package com.example.respawnprotection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class RespawnProtectionConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/respawnprotection.json");

    public int protectionTimeSeconds = 5; // Default value

    public static RespawnProtectionConfig load() {
        if (!CONFIG_FILE.exists()) {
            System.out.println("[RespawnProtection] Config not found, creating default config...");
            RespawnProtectionConfig config = new RespawnProtectionConfig();
            config.save();
            return config;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            System.out.println("[RespawnProtection] Config file loaded successfully.");
            return GSON.fromJson(reader, RespawnProtectionConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("[RespawnProtection] Failed to load config", e);
        }
    }

    public void save() {
        try {
            File parent = CONFIG_FILE.getParentFile();
            if (!parent.exists()) {
                boolean created = parent.mkdirs();
                if (created) {
                    System.out.println("[RespawnProtection] Created config folder: " + parent.getPath());
                } else {
                    System.out.println("[RespawnProtection] Failed to create config folder: " + parent.getPath());
                }
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
                System.out.println("[RespawnProtection] Default config saved.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("[RespawnProtection] Failed to save config", e);
        }
    }
}
