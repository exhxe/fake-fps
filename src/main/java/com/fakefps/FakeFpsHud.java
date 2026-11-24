package com.fakefps;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class FakeFpsHud {
    public static String text(int fps) {
        return FakeFpsConfig.INSTANCE.format.replace("{fps}", String.valueOf(fps));
    }

    public static int width(TextRenderer font, String text) {
        return Math.round(font.getWidth(text) * FakeFpsConfig.INSTANCE.scale);
    }

    public static int height(TextRenderer font) {
        return Math.round(font.fontHeight * FakeFpsConfig.INSTANCE.scale);
    }

    public static void draw(DrawContext context, TextRenderer font, String text, int x, int y) {
        FakeFpsConfig c = FakeFpsConfig.INSTANCE;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate((float) x, (float) y);
        context.getMatrices().scale(c.scale, c.scale);
        context.drawText(font, text, 0, 0, 0xFF000000 | (c.color & 0xFFFFFF), c.shadow);
        context.getMatrices().popMatrix();
    }
}
