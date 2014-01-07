package com.inmobi.adserve.channels.server.servlet;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Path;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.RequestParser;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


@Singleton
@Path("/configChange")
public class ServletChangeConfig implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletChangeConfig.class);
    private final RequestParser requestParser;

    @Inject
    ServletChangeConfig(final RequestParser requestParser) {
        this.requestParser = requestParser;
    }

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final MessageEvent e) throws Exception {

        Map<String, List<String>> params = queryStringDecoder.getParameters();
        JSONObject jObject = null;
        try {
            jObject = requestParser.extractParams(params, "update");
        }
        catch (JSONException exeption) {
            LOG.debug("Encountered Json Error while creating json object inside servlet");
            hrh.setTerminationReason(ServletHandler.jsonParsingError);
            InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
            hrh.responseSender.sendResponse("Incorrect Json", e);
            return;
        }
        if (jObject == null) {
            LOG.debug("jobject is null so returning");
            hrh.setTerminationReason(ServletHandler.jsonParsingError);
            hrh.responseSender.sendResponse("Incorrect Json", e);
            return;
        }
        LOG.debug("Successfully got json for config change");
        try {
            StringBuilder updates = new StringBuilder();
            updates.append("Successfully changed Config!!!!!!!!!!!!!!!!!\n").append("The changes are\n");
            @SuppressWarnings("unchecked")
            Iterator<String> itr = jObject.keys();
            while (itr.hasNext()) {
                String configKey = itr.next().toString();
                if (configKey.startsWith("adapter")
                        && ServletHandler.getAdapterConfig().containsKey(configKey.replace("adapter.", ""))) {
                    ServletHandler.getAdapterConfig().setProperty(configKey.replace("adapter.", ""),
                        jObject.getString(configKey));
                    updates.append(configKey)
                                .append("=")
                                .append(ServletHandler.getAdapterConfig().getString(configKey.replace("adapter.", "")))
                                .append("\n");
                }
                if (configKey.startsWith("server")
                        && ServletHandler.getServerConfig().containsKey(configKey.replace("server.", ""))) {
                    ServletHandler.getServerConfig().setProperty(configKey.replace("server.", ""),
                        jObject.getString(configKey));
                    updates.append(configKey)
                                .append("=")
                                .append(ServletHandler.getServerConfig().getString(configKey.replace("server.", "")))
                                .append("\n");
                }
            }
            hrh.responseSender.sendResponse(updates.toString(), e);
        }
        catch (JSONException ex) {
            LOG.debug("Encountered Json Error while creating json object inside HttpRequest Handler for config change");
            hrh.terminationReason = ServletHandler.jsonParsingError;
        }
    }

    @Override
    public String getName() {
        return "configchange";
    }

}
