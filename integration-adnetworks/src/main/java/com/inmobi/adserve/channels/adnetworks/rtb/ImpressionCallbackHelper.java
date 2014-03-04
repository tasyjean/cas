package com.inmobi.adserve.channels.adnetworks.rtb;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.HttpRequest;

import java.net.InetSocketAddress;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ImpressionCallbackHelper {
    private final static Logger LOG = LoggerFactory.getLogger(ImpressionCallbackHelper.class);

    public boolean writeResponse(final Bootstrap clientBootstrap, final URI uriCallBack,
            final HttpRequest callBackRequest) {
        ChannelFuture channelFuture = clientBootstrap.connect(new InetSocketAddress(uriCallBack.getHost(), uriCallBack
                .getPort() == -1 ? 80 : uriCallBack.getPort()));
        ChannelFuture futureCallBack = null;
        try {
            if (channelFuture.channel().isWritable()) {
                futureCallBack = channelFuture.channel().write(callBackRequest);
            }
        }
        catch (Exception e) {
            LOG.info("Error in making callback request {}", e);
        }
        if (null == futureCallBack) {
            LOG.debug("Could not make callback connection ");
            return false;
        }
        futureCallBack.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(final ChannelFuture future) throws Exception {

                if (!future.isSuccess()) {
                    LOG.info("error sending callback");
                    return;
                }
                LOG.debug("CallBack is sent");
                return;
            }
        });
        futureCallBack.addListener(ChannelFutureListener.CLOSE);
        LOG.debug("Callback channel is closed");
        return true;
    }
}
