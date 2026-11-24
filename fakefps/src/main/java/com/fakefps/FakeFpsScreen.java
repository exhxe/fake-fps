package com.fakefps;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/** 設定 + ドラッグでHUD位置調整(吸着つき) */
public class FakeFpsScreen extends Screen {
    private static final int SNAP = 6;
    private static final int MARGIN = 4;
    private static final int ROW = 22;
    private static final int PANEL_W = 250;

    private final Screen parent;

    private boolean dragging = false;
    private double grabDX, grabDY;
    private int guideX = -1, guideY = -1;
    private int panelTop;

    public FakeFpsScreen(Screen parent) {
        super(Text.literal("Fake FPS"));
        this.parent = parent;
    }

    private int panelHeight() {
        return ROW * 8 + 12;
    }

    @Override
    protected void init() {
        FakeFpsConfig c = FakeFpsConfig.INSTANCE;
        int cx = this.width / 2;
        panelTop = Math.max(10, this.height / 2 - panelHeight() / 2 + 20);
        int fx = cx + 5;
        int fw = 105;
        int y = panelTop + 8;

        addField(fx, y, fw, String.valueOf(c.minFps), s -> c.minFps = parseInt(s, c.minFps));
        y += ROW;
        addField(fx, y, fw, String.valueOf(c.maxFps), s -> c.maxFps = parseInt(s, c.maxFps));
        y += ROW;
        addField(fx, y, fw, String.valueOf(c.intervalMs), s -> c.intervalMs = parseInt(s, c.intervalMs));
        y += ROW;
        TextFieldWidget fmt = addField(fx, y, fw, c.format, s -> c.format = s);
        fmt.setMaxLength(64);
        y += ROW;
        addField(fx, y, fw, String.format("%06X", c.color & 0xFFFFFF), s -> {
            try {
                if (s.length() == 6) c.color = Integer.parseInt(s, 16);
            } catch (NumberFormatException ignored) {
            }
        });
        y += ROW;
        addField(fx, y, fw, String.valueOf(c.scale), s -> {
            try {
                float v = Float.parseFloat(s);
                if (v >= 0.5f && v <= 4f) c.scale = v;
            } catch (NumberFormatException ignored) {
            }
        });
        y += ROW;

        this.addDrawableChild(ButtonWidget.builder(enabledText(), b -> {
            c.enabled = !c.enabled;
            b.setMessage(enabledText());
        }).dimensions(cx - PANEL_W / 2 + 10, y, 110, 20).build());
        this.addDrawableChild(ButtonWidget.builder(shadowText(), b -> {
            c.shadow = !c.shadow;
            b.setMessage(shadowText());
        }).dimensions(cx + 5, y, 110, 20).build());
        y += ROW;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("完了"), b -> close())
                .dimensions(cx - 50, y, 100, 20).build());
    }

    private TextFieldWidget addField(int x, int y, int w, String value, Consumer<String> onChange) {
        TextFieldWidget f = new TextFieldWidget(this.textRenderer, x, y, w, 18, Text.empty());
        f.setMaxLength(32);
        f.setText(value);
        f.setChangedListener(onChange);
        this.addDrawableChild(f);
        return f;
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Math.max(0, Integer.parseInt(s.trim()));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private Text enabledText() {
        return Text.literal("表示: " + (FakeFpsConfig.INSTANCE.enabled ? "ON" : "OFF"));
    }

    private Text shadowText() {
        return Text.literal("影: " + (FakeFpsConfig.INSTANCE.shadow ? "ON" : "OFF"));
    }

    /** 設定パネルは、背景の上・ウィジェットの下に描く。 */
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderBackground(context, mouseX, mouseY, deltaTicks);
        int cx = this.width / 2;
        context.fill(cx - PANEL_W / 2, panelTop, cx + PANEL_W / 2, panelTop + panelHeight(), 0xB0000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int cx = this.width / 2;

        int lx = cx - PANEL_W / 2 + 10;
        int ty = panelTop + 8 + 5;
        String[] labels = {"最小FPS", "最大FPS", "更新間隔(ms)", "表示形式 ({fps})", "色 (HEX)", "サイズ (0.5-4)"};
        for (int i = 0; i < labels.length; i++) {
            context.drawTextWithShadow(this.textRenderer, labels[i], lx, ty + i * ROW, 0xFFFFFFFF);
        }
        String hint = "プレビューをドラッグして位置を移動";
        context.drawTextWithShadow(this.textRenderer, hint, cx - this.textRenderer.getWidth(hint) / 2, panelTop - 12, 0xFFAAAAAA);

        // HUDプレビュー
        FakeFpsConfig c = FakeFpsConfig.INSTANCE;
        String text = FakeFpsHud.text(FakeFpsState.get());
        int w = FakeFpsHud.width(this.textRenderer, text);
        int h = FakeFpsHud.height(this.textRenderer);
        c.x = Math.max(0, Math.min(this.width - w, c.x));
        c.y = Math.max(0, Math.min(this.height - h, c.y));

        if (dragging) {
            if (guideX >= 0) context.fill(guideX, 0, guideX + 1, this.height, 0xAAFFFF55);
            if (guideY >= 0) context.fill(0, guideY, this.width, guideY + 1, 0xAAFFFF55);
        }
        context.fill(c.x - 2, c.y - 2, c.x + w + 2, c.y + h + 2, dragging ? 0x6055AAFF : 0x40FFFFFF);
        FakeFpsHud.draw(context, this.textRenderer, text, c.x, c.y);
    }

    private boolean overHud(double mx, double my) {
        FakeFpsConfig c = FakeFpsConfig.INSTANCE;
        String text = FakeFpsHud.text(FakeFpsState.get());
        int w = FakeFpsHud.width(this.textRenderer, text);
        int h = FakeFpsHud.height(this.textRenderer);
        return mx >= c.x - 2 && mx <= c.x + w + 2 && my >= c.y - 2 && my <= c.y + h + 2;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        if (click.button() == 0 && overHud(click.x(), click.y())) {
            dragging = true;
            grabDX = click.x() - FakeFpsConfig.INSTANCE.x;
            grabDY = click.y() - FakeFpsConfig.INSTANCE.y;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging) {
            FakeFpsConfig c = FakeFpsConfig.INSTANCE;
            String text = FakeFpsHud.text(FakeFpsState.get());
            int w = FakeFpsHud.width(this.textRenderer, text);
            int h = FakeFpsHud.height(this.textRenderer);
            int nx = (int) Math.round(click.x() - grabDX);
            int ny = (int) Math.round(click.y() - grabDY);

            guideX = -1;
            guideY = -1;
            int[] xs = {MARGIN, (this.width - w) / 2, this.width - w - MARGIN};
            int[] xGuides = {MARGIN, this.width / 2, this.width - MARGIN};
            for (int i = 0; i < xs.length; i++) {
                if (Math.abs(nx - xs[i]) <= SNAP) {
                    nx = xs[i];
                    guideX = xGuides[i];
                    break;
                }
            }
            int[] ys = {MARGIN, (this.height - h) / 2, this.height - h - MARGIN};
            int[] yGuides = {MARGIN, this.height / 2, this.height - MARGIN};
            for (int i = 0; i < ys.length; i++) {
                if (Math.abs(ny - ys[i]) <= SNAP) {
                    ny = ys[i];
                    guideY = yGuides[i];
                    break;
                }
            }
            c.x = Math.max(0, Math.min(this.width - w, nx));
            c.y = Math.max(0, Math.min(this.height - h, ny));
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging) {
            dragging = false;
            guideX = -1;
            guideY = -1;
            FakeFpsConfig.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public void close() {
        FakeFpsConfig.save();
        this.client.setScreen(parent);
    }
}
