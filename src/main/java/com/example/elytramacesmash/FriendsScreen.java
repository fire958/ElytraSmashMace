package com.example.elytramacesmash;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class FriendsScreen extends Screen {
    private TextFieldWidget input;

    public FriendsScreen() { super(Text.literal("ElytraMaceSmash Friends")); }

    @Override
    protected void init() {
        clearChildren();
        int cx = width / 2;
        input = new TextFieldWidget(textRenderer, cx - 100, 30, 140, 20, Text.literal("Name"));
        input.setMaxLength(16);
        addDrawableChild(input);
        addDrawableChild(ButtonWidget.builder(Text.literal("Add"), b -> {
            Friends.add(input.getText());
            init();
        }).dimensions(cx + 45, 30, 55, 20).build());

        int y = 60;
        for (String name : Friends.all()) {
            final String n = name;
            addDrawableChild(ButtonWidget.builder(Text.literal(n + "  [remove]"), b -> {
                Friends.remove(n);
                init();
            }).dimensions(cx - 100, y, 200, 20).build());
            y += 22;
            if (y > height - 30) break;
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, "Friends (inpe attack nahi hoga)", width / 2, 12, 0xFFFFFFFF);
    }
}
