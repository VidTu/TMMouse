package ru.vidtu.tapemouse.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tapemouse.ClientEventHandler;
import net.minecraft.client.gui.hud.DebugHud;
	
@Mixin(DebugHud.class)
public class DebugHudMixin {
	@Inject(method = "getLeftText", at = @At("RETURN"))
	public void getLeftText(CallbackInfoReturnable<List<String>> cir) {
		ClientEventHandler.textToRender(cir.getReturnValue());
	}
}
