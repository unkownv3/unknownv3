package me.bill.fpp.api.event;

import me.bill.fpp.fakeplayer.FakePlayer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface FppBotSpawnEvent {
    Event<FppBotSpawnEvent> EVENT = EventFactory.createArrayBacked(FppBotSpawnEvent.class,
        listeners -> (bot, cancelled) -> {
            for (FppBotSpawnEvent listener : listeners) {
                cancelled = listener.onBotSpawn(bot, cancelled);
            }
            return cancelled;
        });

    boolean onBotSpawn(FakePlayer bot, boolean cancelled);
}
