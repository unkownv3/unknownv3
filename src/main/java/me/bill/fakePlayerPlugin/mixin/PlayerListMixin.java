package me.bill.fakePlayerPlugin.mixin;

import me.bill.fakePlayerPlugin.config.Config;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Inject(method = "getPlayerCount", at = @At("RETURN"), cancellable = true)
    private void modifyPlayerCount(CallbackInfoReturnable<Integer> cir) {
        if (Config.serverListCountBots()) {
            // Include bot count in player count (default behavior)
            return;
        }
        // Subtract fake players from the count
        cir.setReturnValue(cir.getReturnValue() - FakePlayerManager.count());
    }
}
