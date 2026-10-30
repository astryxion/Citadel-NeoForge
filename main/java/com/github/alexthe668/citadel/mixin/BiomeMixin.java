package com.github.alexthe668.citadel.mixin;

import com.github.alexthe668.citadel.CitadelConstants;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * In 26.3, natural mob spawns live on {@link EnvironmentAttributeMap} via
 * {@code NATURAL_MOB_SPAWNS}. NeoForge biome modifiers write those into
 * {@link ModifiableBiomeInfo}, matching {@link Biome#getGenerationSettings()},
 * but {@link Biome#getAttributes()} still returned the original map — so
 * {@code EnvironmentAttributeSystem} never saw added spawns.
 */
@Mixin(Biome.class)
public class BiomeMixin {
    @Shadow
    @Final
    private ModifiableBiomeInfo modifiableBiomeInfo;

    @Inject(method = "getAttributes", at = @At("HEAD"), cancellable = true, remap = CitadelConstants.REMAPREFS)
    private void citadel$getModifiedAttributes(CallbackInfoReturnable<EnvironmentAttributeMap> cir) {
        cir.setReturnValue(this.modifiableBiomeInfo.get().attributes());
    }
}
