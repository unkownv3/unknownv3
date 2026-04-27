package me.bill.fpp.mixin;

import me.bill.fpp.FakePlayerMod;
import me.bill.fpp.fakeplayer.FakePlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(Entity target, CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        FakePlayerMod mod = FakePlayerMod.getInstance();
        if (mod == null || mod.getFakePlayerManager() == null) return;

        FakePlayer fp = mod.getFakePlayerManager().getBot(self.getUuid());
        if (fp != null && fp.isFrozen()) {
            ci.cancel();
        }
    }
}
