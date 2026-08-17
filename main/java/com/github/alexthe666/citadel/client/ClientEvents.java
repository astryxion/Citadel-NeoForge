package com.github.alexthe666.citadel.client;

import com.github.alexthe666.citadel.Citadel;
import com.github.alexthe666.citadel.client.render.CitadelLecternRenderer;
import com.github.alexthe666.citadel.client.shader.CitadelShaderRenderTypes;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = "citadel", value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Citadel.LECTERN_BE.get(), CitadelLecternRenderer::new);
    }

    @SubscribeEvent
    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(CitadelShaderRenderTypes.RAINBOW_AURA_PIPELINE);
    }

    @SubscribeEvent
    public static void registerItemModels(RegisterItemModelsEvent event) {
        event.register(Identifier.fromNamespaceAndPath("citadel", "custom_item_model"), CitadelItemstackRenderer.Unbaked.MAP_CODEC);
    }
}
