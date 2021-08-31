package ru.vidtu.tapemouse.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.brigadier.CommandDispatcher;

import net.dries007.tapemouse.ClientEventHandler;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.server.command.CommandSource;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Shadow private CommandDispatcher<CommandSource> commandDispatcher;
	@Shadow @Final private ClientCommandSource commandSource;
	
	@Inject(method = "onCommandTree", at = @At("RETURN"))
	private void onCommandTree(CommandTreeS2CPacket packet, CallbackInfo info) {
		ClientEventHandler.registerCommands(commandDispatcher);
	}
}
