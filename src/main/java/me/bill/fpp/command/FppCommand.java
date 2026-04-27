package me.bill.fpp.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collections;
import java.util.List;

public abstract class FppCommand {
    private final String name;
    private final String description;
    private final String permission;
    private final List<String> aliases;

    public FppCommand(String name, String description, String permission) {
        this(name, description, permission, Collections.emptyList());
    }

    public FppCommand(String name, String description, String permission, List<String> aliases) {
        this.name = name;
        this.description = description;
        this.permission = permission;
        this.aliases = aliases;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPermission() { return permission; }
    public List<String> getAliases() { return aliases; }

    public abstract int execute(CommandContext<ServerCommandSource> ctx, String[] args);

    public List<String> tabComplete(CommandContext<ServerCommandSource> ctx, String[] args) {
        return Collections.emptyList();
    }
}
