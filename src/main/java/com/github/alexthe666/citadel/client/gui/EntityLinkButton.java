package com.github.alexthe666.citadel.client.gui;

import com.github.alexthe666.citadel.client.gui.data.EntityLinkData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class EntityLinkButton extends Button {

    private static final Map<String, Entity> renderedEntites = new HashMap<>();
    private static final Quaternionf ENTITY_ROTATION = new Quaternionf().rotationXYZ((float) Math.toRadians(30), (float) Math.toRadians(130), (float) Math.PI);
    private final EntityLinkData data;
    private final GuiBasicBook bookGUI;

    public EntityLinkButton(GuiBasicBook bookGUI, EntityLinkData linkData, int k, int l, Button.OnPress o) {
        super(k + linkData.getX() - 12, l + linkData.getY(), (int) (24 * linkData.getScale()), (int) (24 * linkData.getScale()), CommonComponents.EMPTY, o, DEFAULT_NARRATION);
        this.data = linkData;
        this.bookGUI = bookGUI;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        int v = 30;
        float f = (float) data.getScale();
        var pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate(this.getX(), this.getY());
        pose.scale(f, f);
        this.drawBtn(false, guiGraphics, 0, 0, 0, v, 24, 24);
        pose.popMatrix();

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(Identifier.parse(data.getEntity())).map(Holder.Reference::value).orElse(null);
        Entity model = type != null ? renderedEntites.computeIfAbsent(data.getEntity(), k -> type.create(Minecraft.getInstance().level, EntitySpawnReason.LOAD)) : null;

        int sx1 = this.getX() + Math.round(f * 4);
        int sy1 = this.getY() + Math.round(f * 4);
        int sx2 = this.getX() + Math.round(f * 20);
        int sy2 = this.getY() + Math.round(f * 20);
        guiGraphics.enableScissor(sx1, sy1, sx2, sy2);
        if (model != null) {
            model.tickCount = Minecraft.getInstance().player.tickCount;
            float renderScale = (float) (data.getEntityScale() * f * 10);
            int cx = this.getX() + Math.round(f * (11.0F + (float) (data.getOffset_x() * data.getEntityScale())));
            int cy = this.getY() + Math.round(f * (22.0F + (float) (data.getOffset_y() * data.getEntityScale())));
            renderEntityPreview(guiGraphics, model, partialTick, cx, cy, renderScale, ENTITY_ROTATION, sx1, sy1, sx2, sy2);
        }
        guiGraphics.disableScissor();
        if (this.isHovered) {
            bookGUI.setEntityTooltip(this.data.getHoverText());
        }
        int u = this.isHovered ? 48 : 24;
        pose.pushMatrix();
        pose.translate(this.getX(), this.getY());
        pose.scale(f, f);
        this.drawBtn(!this.isHovered, guiGraphics, 0, 0, u, v, 24, 24);
        pose.popMatrix();
    }

    private static void renderEntityPreview(GuiGraphicsExtractor guiGraphics, Entity entity, float partialTick, int centerX, int centerY, float scale, Quaternionf rotation, int vx1, int vy1, int vx2, int vy2) {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderState state = dispatcher.extractEntity(entity, partialTick);
        if (state instanceof LivingEntityRenderState livingState) {
            livingState.boundingBoxWidth /= livingState.scale;
            livingState.boundingBoxHeight /= livingState.scale;
            livingState.scale = 1.0F;
        }
        Quaternionf q1 = new Quaternionf().rotateZ((float) Math.PI).mul(rotation);
        Quaternionf q2 = new Quaternionf();
        Vector3f translate = new Vector3f(0.0F, state.boundingBoxHeight / 2.0F + scale, 0.0F);
        int half = Math.max(16, (int) (scale * 2.0F));
        guiGraphics.entity(state, partialTick, translate, q1, q2, centerX - half, centerY - half, centerX + half, centerY + half);
    }

    public void drawBtn(boolean color, GuiGraphicsExtractor guiGraphics, int destX, int destY, int srcU, int srcV, int destW, int destH) {
        if (color) {
            int widgetColor = bookGUI.getWidgetColor();
            int r = (widgetColor & 0xFF0000) >> 16;
            int g = (widgetColor & 0xFF00) >> 8;
            int b = widgetColor & 0xFF;
            BookBlit.blitWithColor(guiGraphics, bookGUI.getBookWidgetTexture(), destX, destY, srcU, srcV, destW, destH, 256, 256, r, g, b, 255);
        } else {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, bookGUI.getBookWidgetTexture(), destX, destY, (float) srcU, (float) srcV, destW, destH, 256, 256);
        }
    }
}
