package com.github.alexthe668.citadel.item.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Data component for citadel:effect_item so an advancement can show one mob effect icon.
 */
public record EffectItemDisplay(String effect) {
    public static final Codec<EffectItemDisplay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("effect").forGetter(EffectItemDisplay::effect)
    ).apply(instance, EffectItemDisplay::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EffectItemDisplay> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, EffectItemDisplay::effect,
            EffectItemDisplay::new
    );
}
