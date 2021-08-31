package net.dries007.tapemouse;

import static net.dries007.tapemouse.TapeMouse.LOGGER;

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
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
	 * Register tick handler
	 * @see #tickEvent(MinecraftClient)
	 */
	public static void init() {
		ClientTickEvents.START_CLIENT_TICK.register(ClientEventHandler::tickEvent);
	}
	
	/**
	 * Register command (<code>/tapemouse</code>)
	 * @param cd Command dispatcher to register
	 */
	@SuppressWarnings({ "rawtypes", "unchecked", "resource" }) //This is easier by doing it unchecked.
	public static void registerCommands(CommandDispatcher cd) {
		cd.register(LiteralArgumentBuilder.literal("tapemouse")
				.executes(c -> {
					MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText("TapeMouse help: ").formatted(Formatting.AQUA));
					MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText("Run '/tapemouse list' to get a list of keybindings."));
					MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText("Run '/tapemouse off' to stop TapeMouse."));
					MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText("Run '/tapemouse <binding> <delay>' to start TapeMouse."));
					MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText("  delay is the number of ticks between every keypress. Set to 0 to hold down the key."));
					return 1;
				})
				.then(LiteralArgumentBuilder.literal("off").executes(c -> {
					keyBinding = null;
					return 1;
				})).then(LiteralArgumentBuilder.literal("list").executes(c -> {
					List<String> keys = KeyBinding.keysById.keySet().stream().map(k -> k.replaceFirst("^key\\.", ""))
							.sorted().collect(Collectors.toList());
					MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText(String.join(", ", keys)));
					return keys.size();
				})).then(RequiredArgumentBuilder.argument("key", KeybindArgumentType.INSTANCE)
						.then(RequiredArgumentBuilder.argument("delay", IntegerArgumentType.integer(0)).executes(c -> {
							ClientEventHandler.delay = c.getArgument("delay", int.class);
							ClientEventHandler.i = 0;
							ClientEventHandler.keyBinding = c.getArgument("key", KeyBinding.class);
							return 1;
						}))));
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
		list.add("TapeMouse active: " + keyBinding.getLocalizedName() + " (" + keyBinding.getName().replaceFirst("^key\\.", "") + ')');
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
		if (delay == 0) KeyBinding.setKeyPressed(keyBinding.keyCode, true);
		KeyBinding.onKeyPressed(keyBinding.keyCode);
	}

	/**
	 * DIY Client side command
	 * @return Should be sending of this message canceled
	 */
	public static boolean chatEvent(ClientPlayerEntity cpe, String s) {
		if (!s.startsWith("/tapemouse")) return false;
		String[] args = s.split("\\s");
		try {
			handleCommand(cpe, args);
		} catch (Exception e) {
			cpe.addChatMessage(new LiteralText("An error occurred trying to run the tapemouse command:").formatted(Formatting.RED), false);
			cpe.addChatMessage(new LiteralText(e.toString()).formatted(Formatting.RED), false);
			LOGGER.error("An error occurred trying to run the tapemouse command:", e);
		}
		return true;
	}

	private static void handleCommand(ClientPlayerEntity cpe, String[] args) throws Exception {
		if (args.length == 2) {
			if (args[1].equalsIgnoreCase("off")) {
				keyBinding = null;
				return;
			} else if (args[1].equalsIgnoreCase("list")) {
				List<String> keys = KeyBinding.keysById.keySet().stream().map(k -> k.replaceFirst("^key\\.", "")).sorted().collect(Collectors.toList());
				cpe.addChatMessage(new LiteralText(String.join(", ", keys)), false);
			} else {
				cpe.addChatMessage(new LiteralText("Missing delay parameter.").formatted(Formatting.RED), false);
			}
		} else if (args.length == 3) {
			KeyBinding keyBinding = KeyBinding.keysById.get("key." + args[1]);
			if (keyBinding == null) keyBinding = KeyBinding.keysById.get(args[1]);
			if (keyBinding == null) {
				cpe.addChatMessage(new LiteralText(args[1] + " is not a valid keybinding.").formatted(Formatting.RED), false);
				return;
			}
			int delay;
			try {
				delay = Integer.parseInt(args[2]);
				if (delay < 0) throw new Exception("bad user");
			} catch (Exception e) {
				cpe.addChatMessage(new LiteralText(args[1] + " is not a positive number or 0.").formatted(Formatting.RED), false);
				return;
			}

			ClientEventHandler.delay = delay;
			ClientEventHandler.i = 0;
			ClientEventHandler.keyBinding = keyBinding;
		} else {
			cpe.addChatMessage(new LiteralText("TapeMouse help: ").formatted(Formatting.AQUA), false);
			cpe.addChatMessage(new LiteralText("Run '/tapemouse list' to get a list of keybindings."), false);
			cpe.addChatMessage(new LiteralText("Run '/tapemouse off' to stop TapeMouse."), false);
			cpe.addChatMessage(new LiteralText("Run '/tapemouse <binding> <delay>' to start TapeMouse."), false);
			cpe.addChatMessage(new LiteralText("  delay is the number of ticks between every keypress. Set to 0 to hold down the key."), false);
		}
	}
}
