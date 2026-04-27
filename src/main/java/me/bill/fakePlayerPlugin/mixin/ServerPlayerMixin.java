package me.bill.fakePlayerPlugin.mixin;

import me.bill.fakePlayerPlugin.fakeplayer.FakePlayer;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void onDeath(DamageSource source, CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer)(Object) this;
        FakePlayer fp = FakePlayerManager.getByServerPlayer(self);
        if (fp != null) {
            fp.incrementDeathCount();
            fp.setAlive(false);
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer self = (ServerPlayer)(Object) this;
        FakePlayer fp = FakePlayerManager.getByServerPlayer(self);
        if (fp != null) {
            fp.addDamageTaken(amount);
        }
    }
}
