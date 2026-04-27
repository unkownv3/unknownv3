package me.bill.fpp.mixin;

import me.bill.fpp.FakePlayerMod;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Inject(method = "getCurrentPlayerCount", at = @At("RETURN"), cancellable = true)
    private void modifyPlayerCount(CallbackInfoReturnable<Integer> cir) {
        FakePlayerMod mod = FakePlayerMod.getInstance();
        if (mod != null && mod.getConfig() != null && !mod.getConfig().serverListCountBots()) {
            if (mod.getFakePlayerManager() != null) {
                cir.setReturnValue(cir.getReturnValue() - mod.getFakePlayerManager().getBotCount());
            }
        }
    }
}
