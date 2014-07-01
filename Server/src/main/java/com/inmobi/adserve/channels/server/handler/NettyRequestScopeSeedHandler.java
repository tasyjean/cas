package com.inmobi.adserve.channels.server.handler;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.scope.NettyRequestScope;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.server.servlet.ServletInvalid;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;

import javax.inject.Inject;
import java.util.Map;


/**
 * @author abhishek.parwal
 * 
 */
@Sharable
@Singleton
@Slf4j
public class NettyRequestScopeSeedHandler extends ChannelInboundHandlerAdapter {

    private final NettyRequestScope    scope;
    private final Map<String, Servlet> pathToServletMap;
    private final ServletInvalid       invalidServlet;
    private final Provider<Marker> traceMarkerProvider;


    @Inject
    public NettyRequestScopeSeedHandler(final NettyRequestScope scope, final Map<String, Servlet> pathToServletMap,
            final ServletInvalid invalidServlet, final Provider<Marker> traceMarkerProvider) {
        this.scope = scope;
        this.pathToServletMap = pathToServletMap;
        this.invalidServlet = invalidServlet;
        this.traceMarkerProvider = traceMarkerProvider;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        HttpRequest httpRequest = (HttpRequest) msg;
        boolean isTracer = Boolean.valueOf(httpRequest.headers().get("x-mkhoj-tracer"));
        Marker traceMarker = isTracer ? NettyRequestScope.TRACE_MAKER : null;
        scope.enter();
        try {
            scope.seed(Key.get(Marker.class), traceMarker);
            scope.seed(Key.get(ResponseSender.class), new ResponseSender(traceMarkerProvider));

            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(httpRequest.getUri());
            String path = queryStringDecoder.path();

            Servlet servlet = pathToServletMap.get(path);
            if (servlet == null) {
                servlet = invalidServlet;
            }

            log.debug("Request servlet is {} for path {}", servlet, path);
            scope.seed(Servlet.class, servlet);

            ctx.fireChannelRead(httpRequest);
        }
        finally {
            scope.exit();
        }
    }

}
