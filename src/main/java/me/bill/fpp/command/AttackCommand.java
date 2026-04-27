package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import me.bill.fpp.fakeplayer.BotType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;
public class AttackCommand extends FppCommand {
    private final FakePlayerManager manager;
    public AttackCommand(FakePlayerManager manager) { super("attack", "Make bot attack entities", "fpp.attack", List.of("fight", "pve")); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        if (args.length < 2) { ctx.getSource().sendError(Text.literal("Usage: /fpp attack <bot> <start|stop|mob|player>")); return 0; }
        FakePlayer fp = manager.getBot(args[0]);
        if (fp == null) { ctx.getSource().sendError(Text.literal("Bot not found")); return 0; }
        if ("stop".equalsIgnoreCase(args[1])) { fp.setBotType(BotType.AFK); fp.setPveSmartAttackMode(FakePlayer.PveSmartAttackMode.OFF); ctx.getSource().sendMessage(Text.literal(fp.getName() + " stopped attacking.").formatted(Formatting.GREEN)); return 1; }
        fp.setBotType(BotType.ATTACKING);
        if (args.length >= 3) { fp.setPveSmartAttackMode("aggressive".equalsIgnoreCase(args[2]) ? FakePlayer.PveSmartAttackMode.AGGRESSIVE : FakePlayer.PveSmartAttackMode.DEFENSIVE); }
        else { fp.setPveSmartAttackMode(FakePlayer.PveSmartAttackMode.DEFENSIVE); }
        ctx.getSource().sendMessage(Text.literal(fp.getName() + " is now attacking nearby hostile mobs.").formatted(Formatting.GREEN));
        return 1;
    }
}
