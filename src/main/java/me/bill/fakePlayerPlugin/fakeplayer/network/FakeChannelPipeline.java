package me.bill.fakePlayerPlugin.fakeplayer.network;

import io.netty.channel.*;

public final class FakeChannelPipeline extends DefaultChannelPipeline {
    public FakeChannelPipeline(Channel channel) {
        super(channel);
    }
}
