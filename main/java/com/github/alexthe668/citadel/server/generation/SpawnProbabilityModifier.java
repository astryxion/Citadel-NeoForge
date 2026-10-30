package com.github.alexthe668.citadel.server.generation;

import com.github.alexthe668.citadel.config.ServerConfig;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

public class SpawnProbabilityModifier implements BiomeModifier {

    @Override
    public void modify(RegistryAccess registries, Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        EnvironmentAttributeMap.Entry<Float, ?> entry = builder.getAttributes().get(EnvironmentAttributes.CREATURE_WORLD_GEN_SPAWN_PROBABILITY);
        float base = entry != null
                ? entry.applyModifier(EnvironmentAttributes.CREATURE_WORLD_GEN_SPAWN_PROBABILITY.defaultValue())
                : EnvironmentAttributes.CREATURE_WORLD_GEN_SPAWN_PROBABILITY.defaultValue();
        float probability = (float) (ServerConfig.chunkGenSpawnModifierVal) * base;
        if (phase == Phase.MODIFY) {
            builder.getAttributes().set(EnvironmentAttributes.CREATURE_WORLD_GEN_SPAWN_PROBABILITY, Mth.clamp(probability, 0F, 1F));
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return makeCodec();
    }

    public static MapCodec<SpawnProbabilityModifier> makeCodec() {
        return MapCodec.unit(SpawnProbabilityModifier::new);
    }
}
