package me.TreeOfSelf;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class PandaUniqueItem implements ModInitializer {
	public static final String MOD_ID = "panda-unique-item";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Config config;

	@Override
	public void onInitialize() {
		LOGGER.info("PandaUniqueItem Started");

		// Load config
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve("PandaUniqueItem.json");
		config = Config.load(configPath);
		LOGGER.info("Config loaded from: {}", configPath);
	}
}