package me.TreeOfSelf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Config {
    public List<String> loreFormat;

    public static Config load(Path configPath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            if (!Files.exists(configPath)) {
                // Create default config
                Config defaultConfig = createDefault();
                save(defaultConfig, configPath);
                return defaultConfig;
            }

            Reader reader = Files.newBufferedReader(configPath);
            Config config = gson.fromJson(reader, Config.class);
            reader.close();
            return config;
        } catch (IOException e) {
            PandaUniqueItem.LOGGER.error("Failed to load config, using defaults", e);
            return createDefault();
        }
    }

    public static void save(Config config, Path configPath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            Files.createDirectories(configPath.getParent());
            Writer writer = Files.newBufferedWriter(configPath);
            gson.toJson(config, writer);
            writer.close();
        } catch (IOException e) {
            PandaUniqueItem.LOGGER.error("Failed to save config", e);
        }
    }

    private static Config createDefault() {
        Config config = new Config();
        config.loreFormat = List.of(
            "",
            "<white>Forged by %player_name%",
            "<yellow>%data%"
        );
        return config;
    }
}
