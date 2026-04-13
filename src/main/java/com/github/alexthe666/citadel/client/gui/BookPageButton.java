package com.github.alexthe666.citadel.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.sounds.SoundEvents;

public class BookPageButton extends Button {
    private final boolean isForward;
    private final boolean playTurnSound;
    private GuiBasicBook bookGUI;

    public BookPageButton(GuiBasicBook bookGUI, int p_i51079_1_, int p_i51079_2_, boolean p_i51079_3_, OnPress p_i51079_4_, boolean p_i51079_5_) {
        super(p_i51079_1_, p_i51079_2_, 23, 13, CommonComponents.EMPTY, p_i51079_4_, DEFAULT_NARRATION);
        this.isForward = p_i51079_3_;
        this.playTurnSound = p_i51079_5_;
        this.bookGUI = bookGUI;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        int uOffset = 0;
        int vOffset = 0;
        if (this.isHovered) {
            uOffset += 23;
        }
        if (!this.isForward) {
            vOffset += 13;
        }
        if (this.isHovered) {
            int color = bookGUI.getWidgetColor();
            int r = (color & 0xFF0000) >> 16;
            int g = (color & 0xFF00) >> 8;
            int b = color & 0xFF;
            BookBlit.blitBookArrow(guiGraphics, bookGUI.getBookWidgetTexture(), this.getX(), this.getY(), uOffset, vOffset, r, g, b, 255);
        } else {
            BookBlit.blitBookArrow(guiGraphics, bookGUI.getBookWidgetTexture(), this.getX(), this.getY(), uOffset, vOffset, 255, 255, 255, 255);
        }
    }

    public void playDownSound(SoundManager p_230988_1_) {
        if (this.playTurnSound) {
            p_230988_1_.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
    }
}
