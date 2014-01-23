package com.inmobi.adserve.channels.server;

import com.inmobi.adserve.adpool.AdPoolRequest;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.api.ServletFactory;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.Logging;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.server.servlet.ServletInvalid;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.apache.thrift.TException;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.jboss.netty.util.CharsetUtil;
import org.json.JSONException;
import org.json.JSONObject;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class HttpRequestHandler extends IdleStateAwareChannelUpstreamHandler {

    public String         terminationReason;
    public JSONObject     jObject           = null;
    public AdPoolRequest  tObject;
    public DebugLogger    logger            = null;
    public ResponseSender responseSender;
    public boolean        isTraceRequest;

    public void setTerminationReason(String terminationReason) {
        this.terminationReason = terminationReason;
    }

    public HttpRequestHandler() {
        logger = new DebugLogger();
        responseSender = new ResponseSender(this, logger);
    }

    /**
     * Invoked when an exception occurs whenever channel throws closedchannelexception increment the totalterminate
     * means channel is closed by party who requested for the ad
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {

        String exceptionString = e.getClass().getSimpleName();
        InspectorStats.incrementStatCount(InspectorStrings.channelException, exceptionString);
        InspectorStats.incrementStatCount(InspectorStrings.channelException, InspectorStrings.count);
        if (logger == null) {
            logger = new DebugLogger();
        }
        if (exceptionString.equalsIgnoreCase(ServletHandler.CLOSED_CHANNEL_EXCEPTION)
                || exceptionString.equalsIgnoreCase(ServletHandler.CONNECTION_RESET_PEER)) {
            InspectorStats.incrementStatCount(InspectorStrings.totalTerminate);
            logger.debug("Channel is terminated", ctx.getChannel().getId());
        }
        e.getCause().printStackTrace();
        logger.info("Getting netty error in HttpRequestHandler:", e.getCause().getMessage());
        if (e.getChannel().isOpen()) {
            responseSender.sendNoAdResponse(e);
        }
    }

    // Invoked when request timeout.
    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) {
        if (e.getChannel().isOpen()) {
            logger.debug("Channel is open in channelIdle handler");
            if (responseSender.getRankList() != null) {
                for (ChannelSegment channelSegment : responseSender.getRankList()) {
                    if (channelSegment.getAdNetworkInterface().getAdStatus().equals("AD")) {
                        logger.debug("Got Ad from", channelSegment.getAdNetworkInterface().getName(), "Top Rank was",
                            responseSender.getRankList().get(0).getAdNetworkInterface().getName());
                        responseSender.sendAdResponse(channelSegment.getAdNetworkInterface(), e);
                        return;
                    }
                }
            }
            responseSender.sendNoAdResponse(e);
        }
        // Whenever channel is Write_idle, increment the totalTimeout. It means
        // server
        // could not write the response with in 800 ms
        logger.debug("inside channel idle event handler for Request channel ID: " + e.getChannel().getId());
        if (e.getState().toString().equalsIgnoreCase("ALL_IDLE")
                || e.getState().toString().equalsIgnoreCase("WRITE_IDLE")) {
            InspectorStats.incrementStatCount(InspectorStrings.totalTimeout);
            logger.debug("server timeout");
        }
    }

    // Invoked when message is received over the connection
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        try {
            HttpRequest request = (HttpRequest) e.getMessage();
            QueryStringDecoder queryStringDecoder;
            String content = request.getContent().toString(CharsetUtil.UTF_8);
            if (request.getMethod() == HttpMethod.POST) {
                //logger.debug("post : ", content);
                queryStringDecoder = new QueryStringDecoder(request.getUri() + "?post=" + content);
                //logger.debug("URI is : " + request.getUri() + "?" + content);
            }
            else {
                //logger.debug("get : ", content);
                queryStringDecoder = new QueryStringDecoder(request.getUri());
                //logger.debug("URI is : " + request.getUri());
            }
            String path = queryStringDecoder.getPath();
            logger.debug("Servlet path is " + path);
            ServletFactory servletFactory = ServletHandler.servletMap.get(path);
            Servlet servlet;
            if (servletFactory == null) {
                servlet = new ServletInvalid();
            }
            else {
                servlet = servletFactory.getServlet();
            }
            logger.debug("Got the servlet " + servlet.getName());
            if ("/trace".equals(path)) {
                logger.setTrace();
                isTraceRequest = true;
            }
            servlet.handleRequest(this, queryStringDecoder, e, logger);
        }
        catch (Exception exception) {
            terminationReason = ServletHandler.processingError;
            InspectorStats.incrementStatCount(InspectorStrings.processingError, InspectorStrings.count);
            responseSender.sendNoAdResponse(e);
            String exceptionClass = exception.getClass().getSimpleName();
            // incrementing the count of the number of exceptions thrown in the
            // server code
            InspectorStats.incrementStatCount(exceptionClass, InspectorStrings.count);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            logger.info("stack trace is", sw.toString());
            if (logger.isDebugEnabled()) {
                sendMail(exception.getMessage(), sw.toString());
            }
        }
    }

    public void writeLogs(ResponseSender responseSender, DebugLogger logger) {
        List<ChannelSegment> list = new ArrayList<ChannelSegment>();
        if (null != responseSender.getRankList()) {
            list.addAll(responseSender.getRankList());
        }
        if (null != responseSender.getAuctionEngine().getRtbSegments()) {
            list.addAll(responseSender.getAuctionEngine().getRtbSegments());
        }
        long totalTime = responseSender.getTotalTime();
        if (totalTime > 2000) {
            totalTime = 0;
        }
        try {
            ChannelSegment adResponseChannelSegment = null;
            if (null != responseSender.getRtbResponse()) {
                adResponseChannelSegment = responseSender.getRtbResponse();
            }
            else if (null != responseSender.getAdResponse()) {
                adResponseChannelSegment = responseSender.getRankList().get(responseSender.getSelectedAdIndex());
            }
            Logging.rrLogging(adResponseChannelSegment, list, logger, responseSender.sasParams, terminationReason,
                totalTime);
            Logging.advertiserLogging(list, logger, ServletHandler.getLoggerConfig());
            Logging.sampledAdvertiserLogging(list, logger, ServletHandler.getLoggerConfig());
        }
        catch (JSONException exception) {
            logger.debug(ChannelServer.getMyStackTrace(exception));
        }
        catch (TException exception) {
            logger.debug(ChannelServer.getMyStackTrace(exception));
        }
        logger.debug("done with logging");
    }

    // send Mail if channel server crashes
    public static void sendMail(String errorMessage, String stackTrace) {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", ServletHandler.getServerConfig().getString("smtpServer"));
        Session session = Session.getDefaultInstance(properties);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(ServletHandler.getServerConfig().getString("sender")));
            List recipients = ServletHandler.getServerConfig().getList("recipients");
            javax.mail.internet.InternetAddress[] addressTo = new javax.mail.internet.InternetAddress[recipients.size()];

            for (int index = 0; index < recipients.size(); index++) {
                addressTo[index] = new javax.mail.internet.InternetAddress((String) recipients.get(index));
            }

            message.setRecipients(Message.RecipientType.TO, addressTo);
            InetAddress addr = InetAddress.getLocalHost();
            message.setSubject("Channel Ad Server Crashed on Host " + addr.getHostName());
            message.setText(errorMessage + stackTrace);
            Transport.send(message);
        }
        catch (MessagingException mex) {
            mex.printStackTrace();
        }
        catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }

}
