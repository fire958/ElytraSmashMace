package com.example.elytramacesmash;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class ElytraMaceSmashClient implements ClientModInitializer {
    private static KeyBinding toggleKey, guiKey;
    private static boolean active = false;
    private static PlayerEntity target = null;
    private static int fireworkCooldown = 0;

    @Override
    public void onInitializeClient() {
        Friends.load();
        // 1.21.9+ mein category ek object hai
        KeyBinding.Category cat = KeyBinding.Category.create(Identifier.of("elytramacesmash", "main"));
        toggleKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.elytramacesmash.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, cat));
        guiKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.elytramacesmash.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, cat));
        ClientTickEvents.END_CLIENT_TICK.register(ElytraMaceSmashClient::tick);
        Hud.register();
    }

    public static boolean isActive() { return active; }
    public static PlayerEntity getTarget() { return target; }

    private static void msg(MinecraftClient mc, String s) {
        if (mc.player != null) mc.player.sendMessage(Text.literal("[ElytraMaceSmash] " + s), true);
    }

    private static void tick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null) { active = false; return; }

        while (guiKey.wasPressed()) mc.setScreen(new FriendsScreen());

        while (toggleKey.wasPressed()) {
            if (active) { active = false; target = null; msg(mc, "OFF"); }
            else {
                target = findTarget(mc, p);
                if (target == null) msg(mc, "Koi target nahi mila");
                else { active = true; msg(mc, "LOCKED: " + target.getName().getString()); }
            }
        }
        if (!active) return;

        if (target == null || !target.isAlive() || target.isRemoved() || target.getHealth() <= 0) {
            // target mar gaya: agla dhoondo, warna band
            target = findTarget(mc, p);
            if (target == null) { active = false; msg(mc, "Target khatam - OFF"); return; }
            msg(mc, "Naya target: " + target.getName().getString());
        }

        // Elytra se uro
        if (!p.isGliding()) {
            if (p.isOnGround()) p.jump();
            else if (p.getVelocity().y < 0) {
                p.startGliding();
                p.networkHandler.sendPacket(new ClientCommandC2SPacket(p, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        }

        // Aim: door ho to thora upar se (mace smash ke liye height), qareeb ho to seedha
        double dist = p.distanceTo(target);
        Vec3d aim = target.getEyePos();
        if (dist > 8) aim = aim.add(0, Math.min(dist * 0.25, 10), 0);
        Vec3d d = aim.subtract(p.getEyePos());
        double hor = Math.sqrt(d.x * d.x + d.z * d.z);
        p.setYaw((float) (Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0));
        p.setPitch((float) -Math.toDegrees(Math.atan2(d.y, hor)));

        // Firework boost
        if (fireworkCooldown > 0) fireworkCooldown--;
        if (p.isGliding() && fireworkCooldown == 0 && p.getVelocity().length() < 0.9 && dist > 5) {
            int fw = findHotbar(p, Items.FIREWORK_ROCKET);
            if (fw != -1) {
                p.getInventory().setSelectedSlot(fw);
                mc.interactionManager.interactItem(p, Hand.MAIN_HAND);
                fireworkCooldown = 30;
            }
        }

        // Mace attack
        if (dist <= 3.5) {
            int mace = findHotbar(p, Items.MACE);
            if (mace != -1) {
                p.getInventory().setSelectedSlot(mace);
                if (p.getAttackCooldownProgress(0.5f) >= 0.95f) {
                    mc.interactionManager.attackEntity(p, target);
                    p.swingHand(Hand.MAIN_HAND);
                }
            }
        }
    }

    private static PlayerEntity findTarget(MinecraftClient mc, ClientPlayerEntity p) {
        PlayerEntity best = null;
        double bestD = 64 * 64;
        for (PlayerEntity o : mc.world.getPlayers()) {
            if (o == p || !o.isAlive() || o.isSpectator()) continue;
            if (Friends.isFriend(o.getName().getString())) continue;
            double dd = p.squaredDistanceTo(o);
            if (dd < bestD) { bestD = dd; best = o; }
        }
        return best;
    }

    private static int findHotbar(ClientPlayerEntity p, net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) if (p.getInventory().getStack(i).isOf(item)) return i;
        return -1;
    }
}
