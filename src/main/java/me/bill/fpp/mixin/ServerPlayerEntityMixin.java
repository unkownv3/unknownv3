package me.bill.fpp.mixin;

import me.bill.fpp.FakePlayerMod;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        FakePlayerMod mod = FakePlayerMod.getInstance();
        if (mod == null || mod.getFakePlayerManager() == null) return;

        var fp = mod.getFakePlayerManager().getBot(self.getUuid());
        if (fp != null && fp.isFrozen()) {
            ci.cancel();
        }
    }
}
