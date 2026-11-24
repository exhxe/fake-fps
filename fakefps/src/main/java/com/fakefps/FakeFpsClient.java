package com.fakefps;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class FakeFpsClient implements ClientModInitializer {
    public static KeyBinding openKey;
    private static boolean pendingOpen = false;

    @Override
    public void onInitializeClient() {
        FakeFpsConfig.load();

        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("fakefps", "main"));
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fakefps.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new FakeFpsScreen(null));
                }
            }
            // /fakefps: チャットが閉じた後(currentScreen == null)に開く
            if (pendingOpen && client.currentScreen == null) {
                pendingOpen = false;
                client.setScreen(new FakeFpsScreen(null));
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("fakefps").executes(ctx -> {
                    pendingOpen = true;
                    return 1;
                })));

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CHAT,
                Identifier.of("fakefps", "hud"),
                (context, tickCounter) -> {
                    FakeFpsConfig c = FakeFpsConfig.INSTANCE;
                    MinecraftClient mc = MinecraftClient.getInstance();
                    if (!c.enabled || mc.currentScreen instanceof FakeFpsScreen) return;
                    String text = FakeFpsHud.text(FakeFpsState.get());
                    int w = FakeFpsHud.width(mc.textRenderer, text);
                    int h = FakeFpsHud.height(mc.textRenderer);
                    int sw = mc.getWindow().getScaledWidth();
                    int sh = mc.getWindow().getScaledHeight();
                    int x = Math.max(0, Math.min(sw - w, c.x));
                    int y = Math.max(0, Math.min(sh - h, c.y));
                    FakeFpsHud.draw(context, mc.textRenderer, text, x, y);
                });
    }
}
