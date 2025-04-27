package com.example.respawnprotection;

import net.fabricmc.fabric.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class RespawnProtectionClient implements ClientModInitializer {
    private static int ticksLeft = 0;

    @Override
    public void onInitializeClient() {
        // receive server‐side ticks
        ClientPlayNetworking.registerGlobalReceiver(
                NetworkConstants.PACKET_ID,
                (client, handler, buf, sender) -> client.execute(() -> ticksLeft = buf.readInt())
        );

        // decrement locally
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ticksLeft > 0) ticksLeft--;
        });

        // HUD overlay
        HudRenderCallback.EVENT.register(this::renderOverlay);
    }

    private void renderOverlay(MatrixStack matrices, float tickDelta) {
        if (ticksLeft <= 0) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer tr = mc.textRenderer;
        int seconds = Math.max(1, (int)Math.ceil(ticksLeft / 20.0));
        String msg = "Respawn Protection: " + seconds + "s";
        int x = (mc.getWindow().getScaledWidth() - tr.getWidth(msg)) / 2;
        int y = mc.getWindow().getScaledHeight() - 40;
        tr.drawWithShadow(matrices, msg, x, y, 0xFF5555);
    }
}
