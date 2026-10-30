package com.github.alexthe668.citadel.config.biome;

import com.github.alexthe668.citadel.Citadel;
import com.google.gson.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpawnBiomeData {

    private List<List<SpawnBiomeEntry>> biomes = new ArrayList<>();

    public SpawnBiomeData() {
    }

    private SpawnBiomeData(SpawnBiomeEntry[][] biomesRead) {
        biomes = new ArrayList<>();
        for (SpawnBiomeEntry[] innerArray : biomesRead) {
            for (SpawnBiomeEntry entry : innerArray) {
                if (entry != null && entry.type == BiomeEntryType.BIOME_TAG && entry.value != null) {
                    entry.value = conventionTag(entry.value);
                }
            }
            List<SpawnBiomeEntry> pool = new ArrayList<>(Arrays.asList(innerArray));
            repairContradictions(pool);
            biomes.add(pool);
        }
    }

    private static void repairContradictions(List<SpawnBiomeEntry> pool) {
        boolean requiresWardenBiome = false;
        for (SpawnBiomeEntry entry : pool) {
            if (entry != null && !entry.negate && entry.type == BiomeEntryType.BIOME_TAG
                    && "alexsmobs:skreechers_can_spawn_wardens".equals(entry.value)) {
                requiresWardenBiome = true;
                break;
            }
        }
        if (requiresWardenBiome) {
            pool.removeIf(entry -> entry != null && entry.negate && entry.type == BiomeEntryType.BIOME_TAG
                    && entry.value != null && entry.value.endsWith(":no_default_monsters"));
        }
    }

    public SpawnBiomeData addBiomeEntry(BiomeEntryType type, boolean negate, String value, int pool) {
        if (biomes.isEmpty() || biomes.size() < pool + 1) {
            biomes.add(new ArrayList<>());
        }
        biomes.get(pool).add(new SpawnBiomeEntry(type, negate, value));
        return this;
    }

    public boolean matches(@Nullable Holder<Biome> biomeHolder, Identifier registryName) {
        for (List<SpawnBiomeEntry> all : biomes) {
            boolean overall = true;
            for (SpawnBiomeEntry cond : all) {
                if (!cond.matches(biomeHolder, registryName)) {
                    overall = false;
                }
            }
            if (overall) {
                return true;
            }
        }
        return false;
    }

    private static String conventionTag(String value) {
        if (value.startsWith("c:")) {
            String path = value.substring("c:".length());
            return switch (path) {
                case "is_snowy", "is_plains", "is_wasteland" -> "alexsmobs:" + path;
                default -> value;
            };
        }
        if (!value.startsWith("forge:")) {
            return value;
        }
        String path = value.substring("forge:".length());
        return switch (path) {
            case "is_dense/overworld" -> "c:is_dense_vegetation/overworld";
            case "is_coniferous" -> "c:is_tree/coniferous";
            case "is_snowy", "is_plains", "is_wasteland" -> "alexsmobs:" + path;
            default -> "c:" + path;
        };
    }

    public static class Deserializer implements JsonDeserializer<SpawnBiomeData>, JsonSerializer<SpawnBiomeData> {

        @Override
        public SpawnBiomeData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonobject = json.getAsJsonObject();
            SpawnBiomeEntry[][] biomesRead = GsonHelper.getAsObject(jsonobject, "biomes", new SpawnBiomeEntry[0][0], context, SpawnBiomeEntry[][].class);
            return new SpawnBiomeData(biomesRead);
        }

        @Override
        public JsonElement serialize(SpawnBiomeData src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonobject = new JsonObject();
            jsonobject.add("biomes", context.serialize(src.biomes));
            return jsonobject;
        }
    }

    private static class SpawnBiomeEntry {
        BiomeEntryType type;
        boolean negate;
        String value;

        public SpawnBiomeEntry(BiomeEntryType type, boolean remove, String value) {
            this.type = type;
            this.negate = remove;
            this.value = type == BiomeEntryType.BIOME_TAG ? conventionTag(value) : value;
        }

        public boolean matches(@Nullable Holder<Biome> biomeHolder, Identifier registryName) {
            if (type.isDepreciated()) {
                Citadel.LOGGER.warn("biome config: BIOME_DICT and BIOME_CATEGORY are no longer valid in 1.19+. Please use BIOME_TAG instead.");
                return false;
            }
            if (type == BiomeEntryType.BIOME_TAG) {
                if (biomeHolder == null) {
                    return negate;
                }
                Identifier tagId = Identifier.parse(value);
                TagKey<Biome> tag = TagKey.create(Registries.BIOME, tagId);
                boolean matched = biomeHolder.is(tag)
                        || biomeHolder.tags().anyMatch(biomeTagKey -> biomeTagKey.location().toString().equals(value));
                return matched ? !negate : negate;
            }
            if (registryName != null && registryName.toString().equals(value)) {
                return !negate;
            }
            return negate;
        }
    }
}
