package me.bill.fakePlayerPlugin.command;

import com.mojang.brigadier.context.CommandContext;
import me.bill.fakePlayerPlugin.config.Config;
import net.minecraft.commands.CommandSourceStack;

public class ReloadCommand extends FppCommand {
    public ReloadCommand() {
        super("reload", "Reload the FPP config", "fpp.reload");
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) {
        Config.reload();
        sendSuccess(context, "FPP config reloaded!");
        return 1;
    }
}
