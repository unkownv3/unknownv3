package me.bill.fakePlayerPlugin.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayer;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import me.bill.fakePlayerPlugin.util.FppLogger;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.*;
import java.util.stream.Collectors;

public class CommandManager {
    private final List<FppCommand> commands = new ArrayList<>();
    private final Map<String, FppCommand> byName = new LinkedHashMap<>();

    public void register(FppCommand command) {
        if (!byName.containsKey(command.getName().toLowerCase())) {
            commands.add(command);
            byName.put(command.getName().toLowerCase(), command);
            for (String alias : command.getAliases()) {
                byName.putIfAbsent(alias.toLowerCase(), command);
            }
        }
    }

    public List<FppCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    public void registerAll(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("fpp");

        // Register each subcommand
        for (FppCommand cmd : commands) {
            root.then(cmd.buildNode());
        }

        // fpp help
        root.executes(ctx -> {
            sendHelp(ctx);
            return 1;
        });

        dispatcher.register(root);

        // Aliases
        dispatcher.register(Commands.literal("fakeplayer").redirect(dispatcher.getRoot().getChild("fpp")));
        dispatcher.register(Commands.literal("fp").redirect(dispatcher.getRoot().getChild("fpp")));

        FppLogger.info("Registered " + commands.size() + " commands");
    }

    private void sendHelp(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSystemMessage(
            net.minecraft.network.chat.Component.literal("\u00a7b\u00a7l[FPP] \u00a7fFake Player Plugin Commands:")
        );
        for (FppCommand cmd : commands) {
            ctx.getSource().sendSystemMessage(
                net.minecraft.network.chat.Component.literal(
                    "\u00a7b /fpp " + cmd.getName() + " \u00a77- " + cmd.getDescription()
                )
            );
        }
    }

    public static SuggestionProvider<CommandSourceStack> suggestBotNames() {
        return (ctx, builder) -> {
            FakePlayerManager.getAll().forEach(fp -> builder.suggest(fp.getName()));
            return builder.buildFuture();
        };
    }

    public static SuggestionProvider<CommandSourceStack> suggestOnlinePlayers() {
        return (ctx, builder) -> {
            var server = ctx.getSource().getServer();
            server.getPlayerList().getPlayers().forEach(p -> {
                if (!FakePlayerManager.isFakePlayer(p)) {
                    builder.suggest(p.getGameProfile().getName());
                }
            });
            return builder.buildFuture();
        };
    }
}
