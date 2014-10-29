package com.inmobi.adserve.channels.server.servlet;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.util.List;


@Singleton
@Path("/changerollout")
public class ServletChangeRollout implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletChangeRollout.class);

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        Integer percentRollout;
        try {
            final List<String> rollout = queryStringDecoder.parameters().get("percentRollout");
            percentRollout = Integer.parseInt(rollout.get(0));
        } catch (final NumberFormatException ex) {
            LOG.error("invalid attempt to change rollout percentage {}", ex);
            hrh.responseSender.sendResponse("INVALIDPERCENT", serverChannel);
            return;
        }

        InspectorStats.incrementStatCount(InspectorStrings.PERCENT_ROLL_OUT, Long.valueOf(percentRollout));
        LOG.debug("new roll out percentage is {}", percentRollout);
        hrh.responseSender.sendResponse("OK", serverChannel);
    }

    @Override
    public String getName() {
        return "changerollout";
    }
}
