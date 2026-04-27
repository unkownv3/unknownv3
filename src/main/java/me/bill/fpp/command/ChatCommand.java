package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.FakePlayerMod;
import me.bill.fpp.fakeplayer.FakePlayer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;
public class ChatCommand extends FppCommand {
    private final FakePlayerMod mod;
    public ChatCommand(FakePlayerMod mod) { super("chat", "Manage bot chat settings", "fpp.chat"); this.mod = mod; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        if (args.length < 2) { ctx.getSource().sendError(Text.literal("Usage: /fpp chat <bot> <on|off|say|tier|mute>")); return 0; }
        FakePlayer fp = mod.getFakePlayerManager().getBot(args[0]);
        if (fp == null) { ctx.getSource().sendError(Text.literal("Bot not found")); return 0; }
        String action = args[1].toLowerCase();
        switch (action) {
            case "on" -> { fp.setChatEnabled(true); ctx.getSource().sendMessage(Text.literal("Chat enabled for " + fp.getName()).formatted(Formatting.GREEN)); }
            case "off", "mute" -> { fp.setChatEnabled(false); ctx.getSource().sendMessage(Text.literal("Chat disabled for " + fp.getName()).formatted(Formatting.GREEN)); }
            case "say" -> {
                if (args.length < 3) { ctx.getSource().sendError(Text.literal("Usage: /fpp chat <bot> say <message>")); return 0; }
                String msg = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                if (fp.getPlayerEntity() != null) {
                    fp.getPlayerEntity().getServer().getPlayerManager().broadcast(Text.literal("<" + fp.getDisplayName() + "> " + msg), false);
                }
            }
            case "tier" -> {
                if (args.length < 3) { ctx.getSource().sendError(Text.literal("Usage: /fpp chat <bot> tier <name>")); return 0; }
                fp.setChatTier(args[2]); ctx.getSource().sendMessage(Text.literal("Chat tier set to " + args[2]).formatted(Formatting.GREEN));
            }
        }
        return 1;
    }
}
