package me.bill.fakePlayerPlugin.fakeplayer.network;

import io.netty.channel.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.function.Consumer;

public final class FakeChannel extends AbstractChannel {
    private static final EventLoop EVENT_LOOP = new DefaultEventLoop();
    private final ChannelConfig config = new DefaultChannelConfig(this);
    private final ChannelPipeline pipeline;
    private final InetAddress address;
    private volatile boolean open = true;
    private volatile boolean active = true;
    private volatile Consumer<Object> packetListener;

    public FakeChannel(Channel parent, InetAddress address) {
        super(parent);
        this.address = address;
        this.pipeline = new FakeChannelPipeline(this);
    }

    public FakeChannel(InetAddress address) {
        this(null, address);
    }

    public void setPacketListener(Consumer<Object> listener) {
        this.packetListener = listener;
    }

    public Consumer<Object> getPacketListener() {
        return this.packetListener;
    }

    @Override
    public ChannelConfig config() {
        this.config.setAutoRead(true);
        return this.config;
    }

    @Override
    public ChannelPipeline pipeline() {
        return this.pipeline;
    }

    @Override
    public boolean isOpen() {
        return this.open;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public ChannelMetadata metadata() {
        return new ChannelMetadata(true);
    }

    @Override
    public EventLoop eventLoop() {
        return EVENT_LOOP;
    }

    @Override
    protected SocketAddress localAddress0() {
        return new InetSocketAddress(this.address, 25565);
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return new InetSocketAddress(this.address, 25565);
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new AbstractUnsafe() {
            @Override
            public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
                safeSetSuccess(promise);
            }
        };
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return true;
    }

    @Override
    protected void doBeginRead() {}

    @Override
    protected void doBind(SocketAddress localAddress) {}

    @Override
    protected void doDisconnect() {
        this.active = false;
    }

    @Override
    protected void doClose() {
        this.active = false;
        this.open = false;
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) {
        Object msg;
        while ((msg = in.current()) != null) {
            in.remove();
        }
    }
}
