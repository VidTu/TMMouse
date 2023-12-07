package net.dries007.tapemouse;

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import ru.vidtu.tapemouse.KeybindArgumentType;

/**
 * Client side only code
 *
 * @author Dries007
 * @author VidTu
 * @implNote What do you me by clientside code? Isn't this mod clientside?
 */
public class ClientEventHandler {
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	private static int delay;
	private static KeyBinding keyBinding;
	private static int i;

	/**
	 * Register tick handler and command dispatcher
	 * @see #tickEvent(MinecraftClient)
	 */
	public static void init() {
		ClientTickEvents.START_CLIENT_TICK.register(ClientEventHandler::tickEvent);
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal("tapemouse")
						.executes(c -> {
							c.getSource().sendFeedback(Text.of("TapeMouse help: "));
							c.getSource().sendFeedback(Text.of("Run '/tapemouse list' to get a list of keybindings."));
							c.getSource().sendFeedback(Text.of("Run '/tapemouse off' to stop TapeMouse."));
							c.getSource().sendFeedback(Text.of("Run '/tapemouse <binding> <delay>' to start TapeMouse."));
							c.getSource().sendFeedback(Text.of("  delay is the number of ticks between every keypress. Set to 0 to hold down the key."));
							return 1;
						})
						.then(ClientCommandManager.literal("off").executes(c -> {
							keyBinding = null;
							return 1;
						})).then(ClientCommandManager.literal("list").executes(c -> {
							List<String> keys = KeyBinding.KEYS_BY_ID.keySet().stream().map(k -> k.replaceFirst("^key\\.", ""))
									.sorted().collect(Collectors.toList());
							c.getSource().sendFeedback(Text.of(String.join(", ", keys)));
							return keys.size();
						})).then(ClientCommandManager.argument("key", KeybindArgumentType.INSTANCE)
								.then(ClientCommandManager.argument("delay", IntegerArgumentType.integer(0)).executes(c -> {
									ClientEventHandler.delay = c.getArgument("delay", int.class);
									ClientEventHandler.i = 0;
									ClientEventHandler.keyBinding = c.getArgument("key", KeyBinding.class);
									return 1;
								})))
		));
	}

	/**
	 * Draw info on the screen
	 */
	public static void textToRender(List<String> list) {
		if (keyBinding == null) return;
		if (mc.currentScreen instanceof TitleScreen || mc.currentScreen instanceof ChatScreen) {
			list.add("TapeMouse paused. If you want to AFK, use ALT+TAB.");
			return;
		}
		list.add("TapeMouse active: " + keyBinding.getBoundKeyLocalizedText().getString() + " (" + keyBinding.getTranslationKey().replaceFirst("^key\\.", "") + ')');
		list.add("Delay: " + i + " / " + delay);
	}

	/**
	 * Actually trigger the keybinding.
	 */
	public static void tickEvent(MinecraftClient mc) {
		if (mc.currentScreen instanceof TitleScreen || mc.currentScreen instanceof ChatScreen) return;
		if (keyBinding == null) return;
		if (i++ < delay) return;
		i = 0;
		if (delay == 0) KeyBinding.setKeyPressed(keyBinding.boundKey, true);
		KeyBinding.onKeyPressed(keyBinding.boundKey);
	}
}
