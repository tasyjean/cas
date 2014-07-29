package com.inmobi.adserve.channels.server.servlet;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.io.File;


@Singleton
@Path("/testResponse")
public class ServletTestResponse implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletStat.class);

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
                              final Channel serverChannel) throws Exception {
        LOG.debug("Inside testTResponse servlet");
        String fileName = "/opt/mkhoj/test/cas/testResponse.txt";
        String testResponse = Files.toString(new File(fileName), Charsets.UTF_8);
        hrh.responseSender.sendResponse(testResponse, serverChannel);
    }

    @Override
    public String getName() {
        return "stat";
    }
}
