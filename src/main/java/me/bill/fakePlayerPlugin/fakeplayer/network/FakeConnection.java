package me.bill.fakePlayerPlugin.fakeplayer.network;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public final class FakeConnection extends Connection {

    public FakeConnection(InetAddress address) {
        super(PacketFlow.SERVERBOUND);
        try {
            Field channelField = Connection.class.getDeclaredField("channel");
            channelField.setAccessible(true);
            channelField.set(this, new FakeChannel(null, address));

            Field addressField = Connection.class.getDeclaredField("address");
            addressField.setAccessible(true);
            addressField.set(this, new InetSocketAddress(address, 25565));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize FakeConnection", e);
        }
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void send(@NotNull Packet<?> packet) {
    }

    @Override
    public void send(@NotNull Packet<?> packet, @Nullable PacketSendListener listener) {
    }

    @Override
    public void send(@NotNull Packet<?> packet, @Nullable PacketSendListener listener, boolean flush) {
    }
}
