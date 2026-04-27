package me.bill.fpp.command;
import com.mojang.brigadier.context.CommandContext;
import me.bill.fpp.fakeplayer.FakePlayer;
import me.bill.fpp.fakeplayer.FakePlayerManager;
import me.bill.fpp.fakeplayer.BotType;
import net.minecraft.block.Block;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import java.util.*;
public class MineCommand extends FppCommand {
    private final FakePlayerManager manager;
    private final Map<UUID, MiningTask> activeTasks = new HashMap<>();
    public MineCommand(FakePlayerManager manager) { super("mine", "Make bot mine blocks", "fpp.mine", List.of("dig", "break")); this.manager = manager; }
    @Override
    public int execute(CommandContext<ServerCommandSource> ctx, String[] args) {
        if (args.length < 2) { ctx.getSource().sendError(Text.literal("Usage: /fpp mine <bot> <start|stop|area>")); return 0; }
        FakePlayer fp = manager.getBot(args[0]);
        if (fp == null) { ctx.getSource().sendError(Text.literal("Bot not found")); return 0; }
        if ("stop".equalsIgnoreCase(args[1])) {
            activeTasks.remove(fp.getUuid()); fp.setBotType(BotType.AFK);
            ctx.getSource().sendMessage(Text.literal(fp.getName() + " stopped mining.").formatted(Formatting.GREEN)); return 1;
        }
        if ("start".equalsIgnoreCase(args[1])) {
            fp.setBotType(BotType.MINING);
            MiningTask task = new MiningTask(fp);
            activeTasks.put(fp.getUuid(), task);
            ctx.getSource().sendMessage(Text.literal(fp.getName() + " started mining nearby blocks.").formatted(Formatting.GREEN)); return 1;
        }
        return 1;
    }
    public static class MiningTask {
        private final FakePlayer fp;
        public MiningTask(FakePlayer fp) { this.fp = fp; }
        public void tick() {
            if (fp.getPlayerEntity() == null) return;
            ServerPlayerEntity entity = fp.getPlayerEntity();
            ServerWorld world = entity.getServerWorld();
            BlockPos pos = entity.getBlockPos();
            for (int dx = -2; dx <= 2; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -2; dz <= 2; dz++) {
                BlockPos target = pos.add(dx, dy, dz);
                if (!world.getBlockState(target).isAir()) { world.breakBlock(target, true, entity); return; }
            }
        }
    }
}
