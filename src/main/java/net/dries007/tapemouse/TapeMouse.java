package net.dries007.tapemouse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ClientModInitializer;

/**
 * Main mod file
 * @author Dries007
 * @author VidTu
 */
public class TapeMouse implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitializeClient() {
		ClientEventHandler.init();
	}
}
