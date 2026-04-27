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

public class TpCommand extends FppCommand {
    private final FakePlayerManager manager;
    public TpCommand(FakePlayerManager manager) {
        super("tp", "Teleport a bot to you", "fpp.tp");
        this.manager = manager;
    }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        if (args.length == 0) { ctx.getSource().sendError(Text.literal("Usage: /fpp tp <bot>")); return 0; }
        FakePlayer fp = manager.getBot(args[0]);
        if (fp == null) { ctx.getSource().sendError(Text.literal("Bot not found: " + args[0])); return 0; }
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) { ctx.getSource().sendError(Text.literal("Must be a player")); return 0; }
        if (fp.getPlayerEntity() != null) {
            fp.getPlayerEntity().teleport(player.getServerWorld(), player.getX(), player.getY(), player.getZ(), Set.of(), player.getYaw(), player.getPitch(), false);
            ctx.getSource().sendMessage(Text.literal("Teleported " + fp.getName() + " to you.").formatted(Formatting.GREEN));
        }
        return 1;
    }
}
