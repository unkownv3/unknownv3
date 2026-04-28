package me.bill.fakePlayerPlugin.fakeplayer.network;

import me.bill.fakePlayerPlugin.FakePlayerPluginFabric;
import me.bill.fakePlayerPlugin.config.Config;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayer;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import me.bill.fakePlayerPlugin.util.FppLogger;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FakeServerGamePacketListenerImpl extends ServerGamePacketListenerImpl {

    public FakeServerGamePacketListenerImpl(MinecraftServer server, Connection connection,
                                            ServerPlayer player, CommonListenerCookie cookie) {
        super(server, connection, player, cookie);
    }

    @Override
    public void send(@NotNull Packet<?> packet) {
        if (packet instanceof ClientboundSetEntityMotionPacket motionPacket) {
            handleKnockbackPacket(motionPacket);
        }
    }

    @Override
    public void send(@NotNull Packet<?> packet, @Nullable PacketSendListener listener) {
        this.send(packet);
    }

    private void handleKnockbackPacket(ClientboundSetEntityMotionPacket packet) {
        if (!Config.bodyKnockback()) return;

        try {
            int entityId = packet.getId();
            if (entityId != this.player.getId()) return;

            FakePlayer fp = FakePlayerManager.getByServerPlayer(this.player);
            if (fp == null || fp.isFrozen()) return;

            double kbX = packet.getXa() / 8000.0;
            double kbY = packet.getYa() / 8000.0;
            double kbZ = packet.getZa() / 8000.0;

            this.player.setDeltaMovement(new Vec3(kbX, kbY, kbZ));
            this.player.hurtMarked = true;
        } catch (Exception e) {
            FppLogger.debug("KB packet handling error: " + e.getMessage());
        }
    }

    @Override
    public void onDisconnect(@NotNull DisconnectionDetails details) {
    }

    @Override
    public boolean isAcceptingMessages() {
        return true;
    }
}
