package me.bill.fakePlayerPlugin.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.List;

public abstract class FppCommand {
    private final String name;
    private final String description;
    private final String permission;
    private final List<String> aliases;

    protected FppCommand(String name, String description, String permission, String... aliases) {
        this.name = name;
        this.description = description;
        this.permission = permission;
        this.aliases = aliases.length > 0 ? List.of(aliases) : Collections.emptyList();
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPermission() { return permission; }
    public List<String> getAliases() { return aliases; }

    public abstract int execute(CommandContext<CommandSourceStack> context);

    public LiteralArgumentBuilder<CommandSourceStack> buildNode() {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(name);
        buildArguments(node);
        return node;
    }

    protected void buildArguments(LiteralArgumentBuilder<CommandSourceStack> node) {
        node.executes(this::execute);
    }

    protected ServerPlayer getPlayer(CommandContext<CommandSourceStack> context) {
        try {
            return context.getSource().getPlayerOrException();
        } catch (Exception e) {
            return null;
        }
    }

    protected void sendMessage(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSystemMessage(
            net.minecraft.network.chat.Component.literal(message)
        );
    }

    protected void sendError(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendFailure(
            net.minecraft.network.chat.Component.literal("\u00a7c" + message)
        );
    }

    protected void sendSuccess(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSystemMessage(
            net.minecraft.network.chat.Component.literal("\u00a7a" + message)
        );
    }
}
