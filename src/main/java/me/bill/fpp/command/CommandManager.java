package me.bill.fpp.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.FakePlayerMod;
import me.bill.fpp.util.FppLogger;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CommandManager {
    private final FakePlayerMod mod;
    private final List<FppCommand> commands = new ArrayList<>();
    private final Map<String, FppCommand> byName = new LinkedHashMap<>();

    public CommandManager(FakePlayerMod mod) {
        this.mod = mod;
        registerAllCommands();
        CommandRegistrationCallback.EVENT.register(this::registerBrigadier);
    }

    private void registerAllCommands() {
        var fpm = mod.getFakePlayerManager();
        var config = mod.getConfig();

        register(new HelpCommand(this));
        register(new SpawnCommand(fpm));
        register(new DespawnCommand(fpm));
        register(new ListCommand(fpm));
        register(new TpCommand(fpm));
        register(new TphCommand(fpm));
        register(new InfoCommand(fpm, mod.getDatabaseManager()));
        register(new ChatCommand(mod));
        register(new ReloadCommand(mod));
        register(new FreezeCommand(fpm));
        register(new MoveCommand(fpm, mod.getPathfindingService()));
        register(new MineCommand(fpm));
        register(new PlaceCommand(fpm));
        register(new AttackCommand(fpm));
        register(new FollowCommand(fpm));
        register(new SleepCommand(fpm));
        register(new StopCommand(fpm));
        register(new RenameCommand(fpm));
        register(new PersonalityCommand(mod));
        register(new InventoryCommand(fpm));
        register(new SwapCommand(fpm));
        register(new RankCommand(fpm));
        register(new StatsCommand(fpm, mod.getDatabaseManager()));
        register(new SettingCommand(fpm));
        register(new SaveCommand(mod));
        register(new FindCommand(fpm));
        register(new UseCommand(fpm));
        register(new XpCommand(fpm));
        register(new AlertCommand());
        register(new PeaksCommand(mod));
        register(new BadwordCommand(fpm));
        register(new WaypointCommand(fpm));
        register(new StorageCommand(fpm));
        register(new CmdCommand(fpm));
        register(new SetOwnerCommand(fpm));
        register(new SyncCommand(mod));
        register(new BotGroupCommand(fpm));
        register(new BotSelectCommand(fpm));
    }

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

    private void registerBrigadier(CommandDispatcher<ServerCommandSource> dispatcher,
                                    net.minecraft.command.CommandRegistryAccess registryAccess,
                                    RegistrationEnvironment environment) {
        LiteralArgumentBuilder<ServerCommandSource> fpp = literal("fpp")
            .executes(ctx -> {
                sendHelp(ctx.getSource());
                return 1;
            })
            .then(argument("args", StringArgumentType.greedyString())
                .suggests((ctx, builder) -> {
                    String input = builder.getRemaining().toLowerCase();
                    for (FppCommand cmd : commands) {
                        if (cmd.getName().toLowerCase().startsWith(input)) {
                            builder.suggest(cmd.getName());
                        }
                    }
                    return builder.buildFuture();
                })
                .executes(ctx -> {
                    String argsStr = StringArgumentType.getString(ctx, "args");
                    String[] parts = argsStr.split("\\s+");
                    String subCmd = parts[0].toLowerCase();
                    String[] subArgs = parts.length > 1 ?
                        Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

                    FppCommand command = byName.get(subCmd);
                    if (command == null) {
                        ctx.getSource().sendError(Text.literal("Unknown sub-command: " + subCmd)
                            .formatted(Formatting.RED));
                        return 0;
                    }

                    return command.execute(ctx, subArgs);
                })
            );

        // Register aliases
        dispatcher.register(fpp);
        dispatcher.register(literal("fakeplayer").redirect(dispatcher.getRoot().getChild("fpp")));
        dispatcher.register(literal("fp").redirect(dispatcher.getRoot().getChild("fpp")));
    }

    private void sendHelp(ServerCommandSource source) {
        source.sendMessage(Text.literal("═══════════════════════════════════════").formatted(Formatting.BLUE));
        source.sendMessage(Text.literal("  FakePlayerPlugin (Fabric) v" + FakePlayerMod.VERSION).formatted(Formatting.AQUA));
        source.sendMessage(Text.literal("═══════════════════════════════════════").formatted(Formatting.BLUE));
        for (FppCommand cmd : commands) {
            source.sendMessage(Text.literal("  /fpp " + cmd.getName()).formatted(Formatting.YELLOW)
                .append(Text.literal(" - " + cmd.getDescription()).formatted(Formatting.GRAY)));
        }
    }
}
