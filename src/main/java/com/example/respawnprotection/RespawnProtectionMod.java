// File: RespawnProtectionMod.java
package com.example.respawnprotection;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class RespawnProtectionMod implements ModInitializer {

    private static int PROTECTION_TICKS; // 10 seconds protection
    private static final Map<UUID, Integer> protectedPlayers = new HashMap<>();
    public static RespawnProtectionConfig config;

    @Override
    public void onInitialize() {

        config = RespawnProtectionConfig.load();
        PROTECTION_TICKS = 20 * config.protectionTimeSeconds;
        System.out.println("[RespawnProtection] Protection time (seconds): " + config.protectionTimeSeconds);

        // When a player respawns, give them protection
        ServerEntityEvents.ENTITY_LOAD.register((entity, serverWorld) -> {
            if (entity instanceof ServerPlayerEntity player) {
                protectedPlayers.put(player.getUuid(), PROTECTION_TICKS);
            }
        });

        // Cancel incoming damage if the player is protected
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!protectedPlayers.isEmpty()) {
                Iterator<Map.Entry<UUID, Integer>> iterator = protectedPlayers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<UUID, Integer> entry = iterator.next();
                    UUID uuid = entry.getKey();
                    int ticksLeft = entry.getValue() - 1;

                    ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid); // <-- THIS

                    if (player != null) {
                        if (ticksLeft > 0) {
                            player.setInvulnerable(true);

                            if (!player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.GLOWING)) {
                                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                                        net.minecraft.entity.effect.StatusEffects.GLOWING,
                                        40,
                                        0,
                                        false,
                                        false,
                                        true
                                ));
                            }
                            protectedPlayers.put(uuid, ticksLeft);
                        } else {
                            player.setInvulnerable(false);
                            player.removeStatusEffect(net.minecraft.entity.effect.StatusEffects.GLOWING);
                            iterator.remove();
                        }
                    } else {
                        // Player is not online anymore, remove them
                        iterator.remove();
                    }
                }
            }
        });
    }
}
