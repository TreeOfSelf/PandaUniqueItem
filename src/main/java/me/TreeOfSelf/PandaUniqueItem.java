package me.TreeOfSelf;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PandaUniqueItem implements ModInitializer {
	public static final String MOD_ID = "panda-unique-item";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("PandaUniqueItem Started");
	}
}