package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.timeout.IdleStateEvent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.thrift.TException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.Logging;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.util.CommonUtils;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


public class HttpRequestHandler extends ChannelDuplexHandler {

    private static final Logger LOG               = LoggerFactory.getLogger(HttpRequestHandler.class);

    public String               terminationReason = "NO";
    public JSONObject           jObject           = null;
    public ResponseSender       responseSender;

    private Provider<Marker>    traceMarkerProvider;
    private Marker              traceMarker;

    private Provider<Servlet>   servletProvider;

    private HttpRequest         httpRequest;

    public String getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(final String terminationReason) {
        this.terminationReason = terminationReason;
    }

    HttpRequestHandler() {
        responseSender = new ResponseSender(this);
    }

    @Inject
    HttpRequestHandler(final Provider<Marker> traceMarkerProvider, final Provider<Servlet> servletProvider) {
        this.traceMarkerProvider = traceMarkerProvider;
        this.servletProvider = servletProvider;
        responseSender = new ResponseSender(this);
    }

    // Invoked when request timeout.
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        // MDC.put("requestId", e.getChannel().getId().toString());

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (ctx.channel().isOpen()) {
                LOG.debug(traceMarker, "Channel is open in channelIdle handler");
                if (responseSender.getRankList() != null) {
                    for (ChannelSegment channelSegment : responseSender.getRankList()) {
                        if (channelSegment.getAdNetworkInterface().getAdStatus().equals("AD")) {
                            LOG.debug(traceMarker, "Got Ad from {} Top Rank was {}", channelSegment
                                    .getAdNetworkInterface().getName(), responseSender.getRankList().get(0)
                                    .getAdNetworkInterface().getName());
                            responseSender.sendAdResponse(channelSegment.getAdNetworkInterface(), ctx.channel());
                            return;
                        }
                    }
                }
                responseSender.sendNoAdResponse(ctx.channel());
            }
            // Whenever channel is Write_idle, increment the totalTimeout. It means
            // server
            // could not write the response with in 800 ms
            LOG.debug(traceMarker, "inside channel idle event handler for Request channel ID: {}", ctx.channel());
            InspectorStats.incrementStatCount(InspectorStrings.totalTimeout);
            LOG.debug(traceMarker, "server timeout");
        }
    }

    /**
     * Invoked when an exception occurs whenever channel throws closedchannelexception increment the totalterminate
     * means channel is closed by party who requested for the ad
     */
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        MDC.put("requestId", String.valueOf(ctx.channel().hashCode()));

        String exceptionString = cause.getClass().getSimpleName();
        InspectorStats.incrementStatCount(InspectorStrings.channelException, exceptionString);
        InspectorStats.incrementStatCount(InspectorStrings.channelException, InspectorStrings.count);
        if (exceptionString.equalsIgnoreCase(ServletHandler.CLOSED_CHANNEL_EXCEPTION)
                || exceptionString.equalsIgnoreCase(ServletHandler.CONNECTION_RESET_PEER)) {
            InspectorStats.incrementStatCount(InspectorStrings.totalTerminate);
            LOG.debug(traceMarker, "Channel is terminated {}", ctx.channel());
        }
        LOG.info(traceMarker, "Getting netty error in HttpRequestHandler: {}", cause);
        if (ctx.channel().isOpen()) {
            responseSender.sendNoAdResponse(ctx.channel());
        }

        ctx.channel().close();
    }

    // Invoked when message is received over the connection
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        try {
            httpRequest = (HttpRequest) msg;

            traceMarker = traceMarkerProvider.get();

            Servlet servlet = servletProvider.get();

            LOG.debug(traceMarker, "Got the servlet {} , uri {}", servlet.getName(), httpRequest.getUri());

            servlet.handleRequest(this, new QueryStringDecoder(httpRequest.getUri()), ctx.channel());
            return;
        }
        catch (Exception exception) {
            terminationReason = ServletHandler.processingError;
            InspectorStats.incrementStatCount(InspectorStrings.processingError, InspectorStrings.count);
            responseSender.sendNoAdResponse(ctx.channel());
            String exceptionClass = exception.getClass().getSimpleName();
            // incrementing the count of the number of exceptions thrown in the
            // server code
            InspectorStats.incrementStatCount(exceptionClass, InspectorStrings.count);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            LOG.info(traceMarker, "stack trace is {}", sw);
            if (LOG.isDebugEnabled()) {
                sendMail(exception.getMessage(), sw.toString());
            }
        }
    }

    public boolean isRequestFromLocalHost() {
        String host = CommonUtils.getHost(httpRequest);

        if (host != null && host.startsWith("localhost")) {
            return true;
        }

        return false;
    }

    /**
     * @return the httpRequest
     */
    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public void writeLogs(final ResponseSender responseSender) {
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
            if (responseSender.getAdResponse() == null) {
                Logging.channelLogline(list, null, ServletHandler.getLoggerConfig(), responseSender.sasParams,
                        totalTime);
                Logging.rrLogging(null, list, ServletHandler.getLoggerConfig(), responseSender.sasParams,
                        terminationReason);
                Logging.advertiserLogging(list, ServletHandler.getLoggerConfig());
                Logging.sampledAdvertiserLogging(list, ServletHandler.getLoggerConfig());
            }
            else {
                Logging.channelLogline(list, responseSender.getAdResponse().clickUrl, ServletHandler.getLoggerConfig(),
                        responseSender.sasParams, totalTime);
                if (responseSender.getRtbResponse() == null) {
                    LOG.debug(traceMarker, "rtb response is null so logging dcp response in rr");
                    Logging.rrLogging(responseSender.getRankList().get(responseSender.getSelectedAdIndex()), list,
                            ServletHandler.getLoggerConfig(), responseSender.sasParams, terminationReason);
                }
                else {
                    LOG.debug(traceMarker, "rtb response is not null so logging rtb response in rr");
                    Logging.rrLogging(responseSender.getRtbResponse(), list, ServletHandler.getLoggerConfig(),
                            responseSender.sasParams, terminationReason);
                }
                Logging.advertiserLogging(list, ServletHandler.getLoggerConfig());
                Logging.sampledAdvertiserLogging(list, ServletHandler.getLoggerConfig());
            }
        }
        catch (JSONException exception) {
            LOG.debug(traceMarker, "{}", exception);
            return;
        }
        catch (TException exception) {
            LOG.debug(traceMarker, "{}", exception);
            return;
        }
        LOG.debug(traceMarker, "done with logging");
    }

    // send Mail if channel server crashes
    public static void sendMail(final String errorMessage, final String stackTrace) {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", ServletHandler.getServerConfig().getString("smtpServer"));
        Session session = Session.getDefaultInstance(properties);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(ServletHandler.getServerConfig().getString("sender")));
            List<String> recipients = ServletHandler.getServerConfig().getList("recipients");
            javax.mail.internet.InternetAddress[] addressTo = new javax.mail.internet.InternetAddress[recipients.size()];

            for (int index = 0; index < recipients.size(); index++) {
                addressTo[index] = new javax.mail.internet.InternetAddress(recipients.get(index));
            }

            message.setRecipients(Message.RecipientType.TO, addressTo);
            InetAddress addr = InetAddress.getLocalHost();
            message.setSubject("Channel Ad Server Crashed on Host " + addr.getHostName());
            message.setText(errorMessage + stackTrace);
            Transport.send(message);
        }
        catch (MessagingException mex) {
            // logger.info("Error while sending mail");
            mex.printStackTrace();
        }
        catch (UnknownHostException ex) {
            // logger.debug("could not resolve host inside send mail");
            ex.printStackTrace();
        }
    }

}