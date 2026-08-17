package com.github.alexthe666.citadel.mixin.client;

import com.github.alexthe666.citadel.client.event.EventLivingRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    @Shadow
    protected M model;

    @Inject(method = "setupRotations", at = @At("RETURN"))
    protected void citadel_setupRotations(S state, PoseStack poseStack, float bodyRot, float entityScale, CallbackInfo ci) {
        LivingEntity entity = getLivingEntity(state);
        if (entity != null) {
            EventLivingRenderer.SetupRotations event = new EventLivingRenderer.SetupRotations(entity, model, poseStack, bodyRot, state.partialTick);
            NeoForge.EVENT_BUS.post(event);
        }
    }

    @Inject(
            method = "submit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Ljava/lang/Object;)V",
                    shift = At.Shift.BEFORE
            )
    )
    protected void citadel_render_setupAnim_before(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        LivingEntity entity = getLivingEntity(state);
        if (entity != null) {
            EventLivingRenderer.PreSetupAnimations event = new EventLivingRenderer.PreSetupAnimations(entity, model, poseStack, state.yRot, state.partialTick, submitNodeCollector, state.lightCoords);
            NeoForge.EVENT_BUS.post(event);
        }
    }

    @Inject(
            method = "submit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Ljava/lang/Object;)V",
                    shift = At.Shift.AFTER
            )
    )
    protected void citadel_render_setupAnim_after(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        LivingEntity entity = getLivingEntity(state);
        if (entity != null) {
            EventLivingRenderer.PostSetupAnimations event = new EventLivingRenderer.PostSetupAnimations(entity, model, poseStack, state.yRot, state.partialTick, submitNodeCollector, state.lightCoords);
            NeoForge.EVENT_BUS.post(event);
        }
    }

    @Inject(method = "submit", at = @At(value = "RETURN"))
    protected void citadel_render_renderToBuffer(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        LivingEntity entity = getLivingEntity(state);
        if (entity != null) {
            EventLivingRenderer.PostRenderModel event = new EventLivingRenderer.PostRenderModel(entity, model, poseStack, state.yRot, state.partialTick, submitNodeCollector, state.lightCoords);
            NeoForge.EVENT_BUS.post(event);
        }
    }

    private static LivingEntity getLivingEntity(LivingEntityRenderState state) {
        if (Minecraft.getInstance().level == null) {
            return null;
        }
        if (state instanceof AvatarRenderState avatarRenderState) {
            Entity entity = Minecraft.getInstance().level.getEntity(avatarRenderState.id);
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity;
            }
        }
        for (Entity entity : Minecraft.getInstance().level.entitiesForRendering()) {
            if (entity instanceof LivingEntity livingEntity
                    && entity.getType() == state.entityType
                    && Math.abs(entity.getX() - state.x) < 0.01D
                    && Math.abs(entity.getY() - state.y) < 0.01D
                    && Math.abs(entity.getZ() - state.z) < 0.01D) {
                return livingEntity;
            }
        }
        return null;
    }
}
