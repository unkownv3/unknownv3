package me.bill.fpp.api.event;

import me.bill.fpp.fakeplayer.FakePlayer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface FppBotDespawnEvent {
    Event<FppBotDespawnEvent> EVENT = EventFactory.createArrayBacked(FppBotDespawnEvent.class,
        listeners -> (bot, reason) -> {
            for (FppBotDespawnEvent listener : listeners) {
                listener.onBotDespawn(bot, reason);
            }
        });

    void onBotDespawn(FakePlayer bot, String reason);
}
