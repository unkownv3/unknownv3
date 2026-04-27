package me.bill.fpp.fakeplayer;

import me.bill.fpp.util.FppLogger;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.MobEntity;

import java.util.*;
import java.util.concurrent.*;

public class PathfindingService {
    private final FakePlayerManager manager;
    private final Map<UUID, NavigationTask> activeTasks = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public PathfindingService(FakePlayerManager manager) {
        this.manager = manager;
    }

    public void navigateTo(FakePlayer fp, Vec3d target, Runnable onComplete) {
        cancelNavigation(fp);
        NavigationTask task = new NavigationTask(fp, target, onComplete);
        activeTasks.put(fp.getUuid(), task);
    }

    public void cancelNavigation(FakePlayer fp) {
        NavigationTask task = activeTasks.remove(fp.getUuid());
        if (task != null) {
            task.cancel();
        }
    }

    public boolean isNavigating(FakePlayer fp) {
        return activeTasks.containsKey(fp.getUuid());
    }

    public void tick() {
        Iterator<Map.Entry<UUID, NavigationTask>> it = activeTasks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, NavigationTask> entry = it.next();
            NavigationTask task = entry.getValue();
            if (task.isCancelled() || task.isComplete()) {
                it.remove();
                continue;
            }
            task.tick();
        }
    }

    public void shutdown() {
        executor.shutdown();
        activeTasks.clear();
    }

    public static class NavigationTask {
        private final FakePlayer fp;
        private final Vec3d target;
        private final Runnable onComplete;
        private boolean cancelled = false;
        private boolean complete = false;
        private int tickCount = 0;
        private static final double REACH_DISTANCE = 1.5;

        public NavigationTask(FakePlayer fp, Vec3d target, Runnable onComplete) {
            this.fp = fp;
            this.target = target;
            this.onComplete = onComplete;
        }

        public void tick() {
            if (cancelled || complete) return;
            ServerPlayerEntity entity = fp.getPlayerEntity();
            if (entity == null) {
                cancel();
                return;
            }

            tickCount++;
            Vec3d current = entity.getPos();
            double dist = current.distanceTo(target);

            if (dist < REACH_DISTANCE) {
                complete = true;
                if (onComplete != null) onComplete.run();
                return;
            }

            // Simple movement toward target
            Vec3d direction = target.subtract(current).normalize();
            double speed = 0.2;
            Vec3d newPos = current.add(direction.multiply(speed));

            entity.setPosition(newPos.x, newPos.y, newPos.z);
            entity.setVelocity(direction.multiply(speed));

            // Timeout
            if (tickCount > 20 * 60) {
                cancel();
            }
        }

        public void cancel() { cancelled = true; }
        public boolean isCancelled() { return cancelled; }
        public boolean isComplete() { return complete; }
    }
}
