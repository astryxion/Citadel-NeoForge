package com.github.alexthe666.citadel;

import com.github.alexthe666.citadel.config.ConfigHolder;
import com.github.alexthe666.citadel.config.ServerConfig;
import com.github.alexthe666.citadel.item.CitadelDataComponents;
import com.github.alexthe666.citadel.item.ItemCitadelBook;
import com.github.alexthe666.citadel.item.ItemCitadelDebug;
import com.github.alexthe666.citadel.item.ItemCustomRender;
import com.github.alexthe666.citadel.server.CitadelEvents;
import com.github.alexthe666.citadel.server.block.CitadelLecternBlock;
import com.github.alexthe666.citadel.server.block.CitadelLecternBlockEntity;
import com.github.alexthe666.citadel.server.block.LecternBooks;
import com.github.alexthe666.citadel.server.generation.SpawnProbabilityModifier;
import com.github.alexthe666.citadel.server.generation.SurfaceRuleInitializer;
import com.github.alexthe666.citadel.server.generation.VillageHouseManager;
import com.github.alexthe666.citadel.server.message.*;
import com.github.alexthe666.citadel.web.WebHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@Mod("citadel")
@EventBusSubscriber
public class Citadel {
    public static final Logger LOGGER = LogManager.getLogger("citadel");
    private static final String PROTOCOL_VERSION = Integer.toString(1);

    public static ServerProxy PROXY = unsafeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);
    public static List<String> PATREONS = new ArrayList<>();
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("citadel");
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("citadel");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "citadel");
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "citadel");

    /**
     * Replaces pre-26.1 {@link net.minecraft.network.syncher.SynchedEntityData} storage (disallowed on mixin-modified entity classes).
     * Serialized + synced + copied on death like the old synced entity field + save hooks.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CompoundTag>> CITADEL_ENTITY_DATA = ATTACHMENT_TYPES.register(
        "citadel_entity_data",
        () -> AttachmentType.builder(() -> new CompoundTag())
            .serialize(CompoundTag.CODEC.fieldOf("data"))
            .copyOnDeath()
            .sync(ByteBufCodecs.COMPOUND_TAG)
            .build());

    public static final DeferredItem<ItemCitadelDebug> DEBUG_ITEM = ITEMS.registerItem("debug", ItemCitadelDebug::new);
    public static final DeferredItem<ItemCitadelBook> CITADEL_BOOK = ITEMS.registerItem("citadel_book", ItemCitadelBook::new, p -> p.stacksTo(1));
    public static final DeferredItem<ItemCustomRender> EFFECT_ITEM = ITEMS.registerItem("effect_item", ItemCustomRender::new, p -> p.stacksTo(1));
    public static final DeferredItem<ItemCustomRender> FANCY_ITEM = ITEMS.registerItem("fancy_item", ItemCustomRender::new, p -> p.stacksTo(1));
    public static final DeferredItem<ItemCustomRender> ICON_ITEM = ITEMS.registerItem("icon_item", ItemCustomRender::new, p -> p.stacksTo(1));

    public static final DeferredBlock<CitadelLecternBlock> LECTERN = BLOCKS.registerBlock(
        "lectern",
        CitadelLecternBlock::new,
        props -> BlockBehaviour.Properties.ofFullCopy(Blocks.LECTERN));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CitadelLecternBlockEntity>> LECTERN_BE =
        BLOCK_ENTITIES.register("lectern", () -> new BlockEntityType<>(CitadelLecternBlockEntity::new, LECTERN.get()));

    public Citadel(ModContainer modContainer, IEventBus bus) {
        ITEMS.register(bus);
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);
        ATTACHMENT_TYPES.register(bus);
        CitadelDataComponents.DATA_COMPONENTS.register(bus);
        final DeferredRegister<MapCodec<? extends BiomeModifier>> serializers = DeferredRegister.create(NeoForgeRegistries.BIOME_MODIFIER_SERIALIZERS, "citadel");
        serializers.register(bus);
        serializers.register("mob_spawn_probability", SpawnProbabilityModifier::makeCodec);
        // Only register PROXY to event bus on client side (ClientProxy has @SubscribeEvent methods, ServerProxy doesn't)
        if (FMLEnvironment.getDist().isClient()) {
            NeoForge.EVENT_BUS.register(PROXY);
        }
        modContainer.registerConfig(ModConfig.Type.COMMON, ConfigHolder.SERVER_SPEC);
        NeoForge.EVENT_BUS.register(new CitadelEvents());
    }

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PROXY.onPreInit();
            LecternBooks.init();
            BufferedReader urlContents = WebHelper.getURLContents("https://raw.githubusercontent.com/Alex-the-666/Citadel/master/src/main/resources/assets/citadel/patreon.txt", "assets/citadel/patreon.txt");
            if (urlContents != null) {
                try {
                    String line;
                    while ((line = urlContents.readLine()) != null) {
                        PATREONS.add(line);
                    }
                } catch (IOException e) {
                    LOGGER.warn("Failed to load patreon contributor perks");
                }
            } else LOGGER.warn("Failed to load patreon contributor perks");
        });
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent.Reloading event) {
        final ModConfig config = event.getConfig();
        // Rebake the configs when they change
        ServerConfig.skipWarnings = ConfigHolder.SERVER.skipDatapackWarnings.get();
        if (config.getSpec() == ConfigHolder.SERVER_SPEC) {
            ServerConfig.citadelEntityTrack = ConfigHolder.SERVER.citadelEntityTracker.get();
            ServerConfig.chunkGenSpawnModifierVal = ConfigHolder.SERVER.chunkGenSpawnModifier.get();
            ServerConfig.aprilFools = ConfigHolder.SERVER.aprilFoolsContent.get();
            //citadelTestBiomeData = SpawnBiomeConfig.create(Identifier.parse("citadel:config_biome"), CitadelBiomeDefinitions.TERRALITH_TEST);
        }
    }

    @SubscribeEvent
    public static void doClientStuff(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> PROXY.onClientInit());
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("citadel").versioned("2.7.0").optional();
        registrar.playToServer(PropertiesMessage.TYPE, PropertiesMessage.CODEC, PropertiesMessage::handle);
        registrar.playToClient(AnimationMessage.TYPE, AnimationMessage.CODEC, AnimationMessage::handle);
        registrar.playBidirectional(
                DanceJukeboxMessage.TYPE,
                DanceJukeboxMessage.CODEC,
                DanceJukeboxMessage::handle,
                DanceJukeboxMessage::handle);
        registrar.playToClient(SyncePathMessage.TYPE, SyncePathMessage.CODEC, SyncePathMessage::handle);
        registrar.playToClient(SyncPathReachedMessage.TYPE, SyncPathReachedMessage.CODEC, SyncPathReachedMessage::handle);
        registrar.playToClient(SyncClientTickRateMessage.TYPE, SyncClientTickRateMessage.CODEC, SyncClientTickRateMessage::handle);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        RegistryAccess registryAccess = event.getServer().registryAccess();
        VillageHouseManager.addAllHouses(registryAccess);
        // Initialize surface rules after biome sources are ready
        SurfaceRuleInitializer.initializeOnServerStart(event.getServer());
    }

    private static <T> T unsafeRunForDist(Supplier<Supplier<T>> clientTarget, Supplier<Supplier<T>> serverTarget) {
        return switch (FMLEnvironment.getDist()) {
            case CLIENT -> clientTarget.get().get();
            case DEDICATED_SERVER -> serverTarget.get().get();
        };
    }
}