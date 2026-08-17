package com.github.alexthe666.citadel.client.event;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.minecraft.util.TriState;

public class EventRenderSplashText extends Event {
    private Component splashText;
    private GuiGraphicsExtractor guiGraphics;
    private float partialTicks;

    public EventRenderSplashText(Component splashText, GuiGraphicsExtractor guiGraphics, float partialTicks) {
        this.splashText = splashText;
        this.guiGraphics = guiGraphics;
        this.partialTicks = partialTicks;
    }

    public Component getSplashText() {
        return splashText;
    }

    public void setSplashText(Component splashText) {
        this.splashText = splashText;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public GuiGraphicsExtractor getGuiGraphics() {
        return guiGraphics;
    }

    public static class Pre extends EventRenderSplashText {
        private TriState result = TriState.DEFAULT;

        private int splashTextColor;

        public Pre(Component splashText, GuiGraphicsExtractor guiGraphics, float partialTicks, int splashTextColor) {
            super(splashText, guiGraphics, partialTicks);
            this.splashTextColor = splashTextColor;
        }

        public int getSplashTextColor() {
            return splashTextColor;
        }

        public void setSplashTextColor(int splashTextColor) {
            this.splashTextColor = splashTextColor;
        }

        public void setResult(TriState result) {
            this.result = result;
        }

        public TriState getResult() {
            return result;
        }
    }

    public static class Post extends EventRenderSplashText {

        public Post(Component splashText, GuiGraphicsExtractor guiGraphics, float partialTicks) {
            super(splashText, guiGraphics, partialTicks);
        }
    }
}
