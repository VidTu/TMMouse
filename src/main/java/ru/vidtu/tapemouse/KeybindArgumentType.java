package ru.vidtu.tapemouse;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.command.CommandSource;

public class KeybindArgumentType implements ArgumentType<KeyBinding> {
	public static final KeybindArgumentType INSTANCE = new KeybindArgumentType();
	@Override
	public KeyBinding parse(StringReader reader) throws CommandSyntaxException {
		String key = reader.readString();
		KeyBinding keyBinding = KeyBinding.KEYS_BY_ID.get("key." + key);
		if (keyBinding == null) keyBinding = KeyBinding.KEYS_BY_ID.get(key);
		if (keyBinding == null) throw new CommandSyntaxException(new SimpleCommandExceptionType(
				new LiteralMessage("No keybinding")), new LiteralMessage("No keybinding found"));
		return keyBinding;
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return CommandSource.suggestMatching(KeyBinding.KEYS_BY_ID.keySet().stream().map(s -> s.replaceFirst("^key\\.", "")).sorted(), builder);
	}
	
	@Override
	public Collection<String> getExamples() {
		return KeyBinding.KEYS_BY_ID.keySet();
	}

}
