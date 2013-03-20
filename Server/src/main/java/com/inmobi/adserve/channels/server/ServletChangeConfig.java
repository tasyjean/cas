package com.inmobi.adserve.channels.server;

import java.util.Iterator;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

public class ServletChangeConfig implements Servlet{
  
  @Override
  public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
      DebugLogger logger) throws Exception{
    HttpRequest request = (HttpRequest) e.getMessage();
    JSONObject jObject = null;
    try {
      jObject = new JSONObject(request.getContent().toString(CharsetUtil.UTF_8));
    } catch (JSONException exeption) {
      logger.debug("Encountered Json Error while creating json object inside servlet"); 
      hrh.setTerminationReason(ServletHandler.jsonParsingError);
      InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
      hrh.responseSender.sendResponse("Incorrect Json", e);
      return;
    }
    
    logger.debug("Successfully got json for config change");
    try {
      StringBuilder updates = new StringBuilder();
      updates.append("Successfully changed Config!!!!!!!!!!!!!!!!!\n").append("The changes are\n");
      Iterator<String> itr = jObject.keys();
      while (itr.hasNext()) {
        String configKey = itr.next().toString();
        if(configKey.startsWith("adapter")
            && ServletHandler.getAdapterConfig().containsKey(configKey.replace("adapter.", ""))) {
          ServletHandler.getAdapterConfig().setProperty(configKey.replace("adapter.", ""), jObject.getString(configKey));
          updates.append(configKey).append("=")
              .append(ServletHandler.getAdapterConfig().getString(configKey.replace("adapter.", ""))).append("\n");
        }
        if(configKey.startsWith("server") && ServletHandler.getServerConfig().containsKey(configKey.replace("server.", ""))) {
          ServletHandler.getServerConfig().setProperty(configKey.replace("server.", ""), jObject.getString(configKey));
          if(configKey.replace("server.", "").equals("maxconnections")) {
            BootstrapCreation.setMaxConnectionLimit(ServletHandler.getServerConfig().getInt(configKey.replace("server.", "")));
          }
          updates.append(configKey).append("=")
              .append(ServletHandler.getServerConfig().getString(configKey.replace("server.", ""))).append("\n");
        }
      }
      hrh.responseSender.sendResponse(updates.toString(), e);
    } catch (JSONException ex) {
      logger.debug("Encountered Json Error while creating json object inside HttpRequest Handler for config change");
      hrh.terminationReason = ServletHandler.jsonParsingError;
    }            
  }
  @Override
  public String getName() {
    return "configchange";
  }

}
