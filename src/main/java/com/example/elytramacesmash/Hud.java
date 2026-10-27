package com.example.elytramacesmash;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.awt.Color;

public class Hud {
    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.of("elytramacesmash", "hud"),
                (ctx, tickCounter) -> render(ctx));
    }

    private static int rainbow(double offset) {
        float hue = (float) (((System.currentTimeMillis() % 3000L) / 3000.0 + offset) % 1.0);
        return Color.HSBtoRGB(hue, 0.9f, 1.0f) | 0xFF000000;
    }

    private static void rgbText(DrawContext ctx, TextRenderer tr, String s, int x, int y) {
        int cx = x;
        for (int i = 0; i < s.length(); i++) {
            String ch = String.valueOf(s.charAt(i));
            ctx.drawTextWithShadow(tr, ch, cx, y, rainbow(i * 0.04));
            cx += tr.getWidth(ch);
        }
    }

    private static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        TextRenderer tr = mc.textRenderer;

        String title = ElytraMaceSmashClient.isActive() ? "ElytraMaceSmash: ON" : "ElytraMaceSmash: OFF";
        int x = 6, y = 6;
        if (ElytraMaceSmashClient.isActive()) {
            rgbText(ctx, tr, title, x, y);
            PlayerEntity t = ElytraMaceSmashClient.getTarget();
            if (t != null) {
                String info = "Target: " + t.getName().getString()
                        + "  |  " + String.format("%.1f", mc.player.distanceTo(t)) + "m"
                        + "  |  HP " + String.format("%.1f", t.getHealth());
                rgbText(ctx, tr, info, x, y + 12);
            }
        } else {
            ctx.drawTextWithShadow(tr, title, x, y, 0xFF888888);
        }
    }
}
