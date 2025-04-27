package com.example.respawnprotection;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.CustomPayload;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RespawnProtectionMod implements ModInitializer {
    public static RespawnProtectionConfig config;
    private static final Map<UUID, Integer> protectedPlayers = new HashMap<>();

    @Override
    public void onInitialize() {
        // load or create config
        config = RespawnProtectionConfig.load();
        System.out.println("[RespawnProtection] Seconds = " + config.protectionTimeSeconds);

        // /respawnprot set <seconds> /respawnprot reload
        CommandRegistrationCallback.EVENT.register((disp, access, env) ->
                disp.register(CommandManager.literal("respawnprot")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("seconds", IntegerArgumentType.integer(0, 300))
                                        .executes(this::setSeconds)))
                        .then(CommandManager.literal("reload")
                                .executes(this::reloadConfig))
                )
        );

        // when a player respawns, start the timer and send the packet
        ServerPlayerEvents.COPY_FROM.register((oldP, newP, alive) -> {
            int ticks = config.protectionTimeSeconds * 20;
            protectedPlayers.put(newP.getUuid(), ticks);

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(ticks);

            // create a CustomPayload and send it
            CustomPayload packet = ServerPlayNetworking.createS2CPacket(NetworkConstants.PACKET_ID, buf);
            ServerPlayNetworking.send(newP, packet);
        });
    }

    private int setSeconds(CommandContext<ServerCommandSource> ctx) {
        int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
        config.protectionTimeSeconds = seconds;
        config.save();
        protectedPlayers.replaceAll((uuid, t) -> seconds * 20);
        ctx.getSource().sendFeedback(() -> Text.literal("[RespawnProtection] time set to " + seconds + " s"), true);
        return 1;
    }

    private int reloadConfig(CommandContext<ServerCommandSource> ctx) {
        config = RespawnProtectionConfig.load();
        ctx.getSource().sendFeedback(() ->
                Text.literal("[RespawnProtection] config reloaded (" + config.protectionTimeSeconds + " s)"), true);
        return 1;
    }
}
