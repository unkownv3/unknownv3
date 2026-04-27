package me.bill.fpp.command;

import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;
import java.util.Set;

public class TphCommand extends FppCommand {
    private final FakePlayerManager manager;
    public TphCommand(FakePlayerManager manager) {
        super("tph", "Teleport yourself to a bot", "fpp.tp");
        this.manager = manager;
    }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        if (args.length == 0) { ctx.getSource().sendError(Text.literal("Usage: /fpp tph <bot>")); return 0; }
        FakePlayer fp = manager.getBot(args[0]);
        if (fp == null || fp.getPlayerEntity() == null) { ctx.getSource().sendError(Text.literal("Bot not found or offline")); return 0; }
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        var botEntity = fp.getPlayerEntity();
        player.teleport(botEntity.getServerWorld(), botEntity.getX(), botEntity.getY(), botEntity.getZ(), Set.of(), botEntity.getYaw(), botEntity.getPitch(), false);
        ctx.getSource().sendMessage(Text.literal("Teleported to " + fp.getName()).formatted(Formatting.GREEN));
        return 1;
    }
}
