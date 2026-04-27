package me.bill.fpp.listener;

import me.bill.fpp.FakePlayerMod;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import me.bill.fpp.util.FppLogger;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

public class FppEventListeners {

    public static void register() {
        // Player join/leave
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // When a real player joins, existing bots can greet them
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            // Cleanup if needed
        });

        // Chat events - for AI responses
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            FakePlayerMod mod = FakePlayerMod.getInstance();
            if (mod == null || mod.getFakePlayerManager() == null) return;
            if (mod.getFakePlayerManager().isFakePlayer(sender)) return;

            String text = message.getContent().getString();
            if (mod.getBotChatAI() != null) {
                mod.getBotChatAI().onPlayerChat(sender, text);
            }
        });

        // Entity damage - handle bot death
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;
            FakePlayerMod mod = FakePlayerMod.getInstance();
            if (mod == null || mod.getFakePlayerManager() == null) return;

            FakePlayer fp = mod.getFakePlayerManager().getBot(player.getUuid());
            if (fp == null) return;

            fp.incrementDeathCount();
            fp.setAlive(false);

            if (mod.getConfig().bodyRespawn()) {
                mod.getFakePlayerManager().respawnBot(fp);
            } else {
                mod.getFakePlayerManager().despawnBot(fp, "death");
            }
        });

        // Entity damage tracking
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return true;
            FakePlayerMod mod = FakePlayerMod.getInstance();
            if (mod == null || mod.getFakePlayerManager() == null) return true;

            FakePlayer fp = mod.getFakePlayerManager().getBot(player.getUuid());
            if (fp != null) {
                fp.addDamageTaken(amount);
            }
            return true;
        });

        // Right-click on bot
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(entity instanceof ServerPlayerEntity target)) return ActionResult.PASS;

            FakePlayerMod mod = FakePlayerMod.getInstance();
            if (mod == null || mod.getFakePlayerManager() == null) return ActionResult.PASS;

            FakePlayer fp = mod.getFakePlayerManager().getBot(target.getUuid());
            if (fp == null) return ActionResult.PASS;

            String cmd = fp.getRightClickCommand();
            if (cmd != null && !cmd.isEmpty()) {
                String processed = cmd.replace("{player}", player.getName().getString())
                    .replace("{bot}", fp.getName());
                player.getServer().getCommandManager().executeWithPrefix(
                    player.getServer().getCommandSource(), processed);
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        });

        FppLogger.info("Event listeners registered.");
    }
}
