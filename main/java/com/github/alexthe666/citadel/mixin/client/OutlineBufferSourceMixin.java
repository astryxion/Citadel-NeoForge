package com.github.alexthe666.citadel.mixin.client;

import com.github.alexthe666.citadel.client.shader.PostEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FeatureRenderDispatcher.PreparedFrame.class)
public class OutlineBufferSourceMixin {

    @Inject(method = "executeOutline", at = @At("HEAD"))
    private void citadel_beforeExecuteOutline(CallbackInfo ci) {
        PostEffectRegistry.processEffects(Minecraft.getInstance().gameRenderer.mainRenderTarget());
    }
}
