package com.inmobi.adserve.channels.adnetworks.appier;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.awt.Dimension;
import java.net.URI;
import java.nio.charset.Charset;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DCPAppierAdNetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG          = LoggerFactory.getLogger(DCPAppierAdNetwork.class);

    private transient String    latitude;
    private transient String    longitude;
    private int                 width;
    private int                 height;
    private String              os           = null;
    private static final String sizeFormat   = "%dx%d";
    private String              sourceType;

    private static final String VERSION      = "version";
    private static final String REQID        = "reqid";
    private static final String SEGKEY       = "segkey";
    private static final String CLIENT_IP    = "clientip";
    private static final String B_SITE_ID    = "bsiteid";
    private static final String SITE_TYPE    = "sitetype";
    private static final String CATEGRORIES  = "categories";
    private static final String OS           = "os";
    private static final String IDFA_MD5     = "idfamd5";
    private static final String STD_ODIN1    = "stdodin1";
    private static final String INMOBI_ODIN1 = "inmobiodin1";
    private static final String UM5          = "um5";
    private static final String SITE_RATING  = "siterating";
    private static final String PERFORMANCE  = "Performance";
    private static final String FAMILY_SAFE  = "FAMILY_SAFE";

    /**
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverEvent
     */
    public DCPAppierAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for appier so exiting adapter");
            return false;
        }
        host = config.getString("appier.host");
        if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        if (!StringUtils.isBlank(sasParams.getSlot())
                && SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
            Dimension dim = SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot()));
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }

        if (sasParams.getOsId() == HandSetOS.Android.getValue()) { // android
            os = "android";
        }
        else if (sasParams.getOsId() == HandSetOS.iPhone_OS.getValue()) { // iPhone
            os = "ios";
        }
        sourceType = (StringUtils.isBlank(sasParams.getSource()) || "WAP".equalsIgnoreCase(sasParams.getSource())) ? "1"
                : "0";
        LOG.info("Configure parameters inside Appier returned true");
        return true;
    }

    @Override
    public String getName() {
        return "appier";
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

    @Override
    public HttpRequest getHttpRequest() throws Exception {
        try {
            URI uri = getRequestUri();
            requestUrl = uri.toString();
            ByteBuf buffer = Unpooled.copiedBuffer(getRequestParams(), Charset.defaultCharset());
            // TODO: make header validation false later
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.toASCIIString(), buffer,
                    true);
            LOG.debug("host name is {}", uri.getHost());
            HttpHeaders headers = request.headers();
            headers.set(HttpHeaders.Names.HOST, uri.getHost());
            headers.set(HttpHeaders.Names.REFERER, uri.toString());
            headers.set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            headers.set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES);
            headers.set("X-Forwarded-For", sasParams.getRemoteHostIp());
            headers.set(HttpHeaders.Names.CONTENT_LENGTH, buffer.readableBytes());
            headers.set(HttpHeaders.Names.CONTENT_TYPE, "application/x-www-form-urlencoded");
        }
        catch (Exception ex) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.HTTPREQUEST_ERROR;
            LOG.error("Error in making http request {}", ex);
        }
        return request;
    }

    @Override
    public URI getRequestUri() throws Exception {
        return new URI(host);
    }

    private String getRequestParams() throws Exception {

        StringBuilder url = new StringBuilder(VERSION).append("=1");
        appendQueryParam(url, REQID, impressionId, false);
        appendQueryParam(url, SEGKEY, externalSiteId, false);
        appendQueryParam(url, CLIENT_IP, sasParams.getRemoteHostIp(), false);
        appendQueryParam(url, B_SITE_ID, blindedSiteId, false);
        appendQueryParam(url, SITE_TYPE, sourceType, false);
        appendQueryParam(url, UA, getURLEncode(sasParams.getUserAgent(), format), false);
        appendQueryParam(url, CATEGRORIES, getURLEncode(getCategories(','), format), false);
        if (os != null) {
            appendQueryParam(url, OS, os, false);
        }
        if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude)) {
            appendQueryParam(url, LAT, latitude, false);
            appendQueryParam(url, LONG, longitude, false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.zipCode)) {
            appendQueryParam(url, ZIP, casInternalRequestParameters.zipCode, false);
        }
        if (StringUtils.isNotBlank(sasParams.getCountry())) {
            appendQueryParam(url, COUNTRY, sasParams.getCountry().toUpperCase(), false);
        }
        if (StringUtils.isNotBlank(sasParams.getGender())) {
            appendQueryParam(url, GENDER, sasParams.getGender(), false);
        }
        if (width != 0 && height != 0) {
            appendQueryParam(url, SIZE, String.format(sizeFormat, width, height), false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFA)) {
            appendQueryParam(url, IDFA_MD5, getHashedValue(casInternalRequestParameters.uidIFA, "MD5"), false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidSO1)) {
            appendQueryParam(url, STD_ODIN1, casInternalRequestParameters.uidSO1, false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidO1)) {
            appendQueryParam(url, INMOBI_ODIN1, casInternalRequestParameters.uidO1, false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
            appendQueryParam(url, UM5, casInternalRequestParameters.uidMd5, false);
        }
        else if (StringUtils.isNotBlank(casInternalRequestParameters.uid)) {
            appendQueryParam(url, UM5, casInternalRequestParameters.uid, false);
        }
        if (SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
            appendQueryParam(url, SITE_RATING, PERFORMANCE, false);
        }
        else {
            appendQueryParam(url, SITE_RATING, FAMILY_SAFE, false);
        }

        LOG.debug("Appier url is {}", url);

        return url.toString();
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);
        statusCode = status.code();
        if (null == response || status.code() != 200 || response.trim().isEmpty()) {
            if (200 == statusCode || 204 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else if (statusCode == 204) {
            statusCode = 500;
            adStatus = "NO_AD";
            return;
        }
        else {
            try {
                JSONObject adResponse = new JSONObject(response);
                statusCode = status.code();
                VelocityContext context = new VelocityContext();
                context.put(VelocityTemplateFieldConstants.PartnerClickUrl, adResponse.getString("click_landing"));
                String partnerBeacon = adResponse.getString("imp_beacon");
                if (StringUtils.isNotBlank(partnerBeacon) && !"null".equalsIgnoreCase(partnerBeacon)) {
                    context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, partnerBeacon);
                }
                context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);
                TemplateType t;
                String adType = adResponse.getString("type");
                if ("txt".equalsIgnoreCase(adType)) {
                    context.put(VelocityTemplateFieldConstants.AdText, adResponse.getString("text"));
                    String vmTemplate = Formatter.getRichTextTemplateForSlot(slot);
                    if (!StringUtils.isEmpty(vmTemplate)) {
                        context.put(VelocityTemplateFieldConstants.Template, vmTemplate);
                        t = TemplateType.RICH;
                    }
                    else {
                        t = TemplateType.PLAIN;
                    }
                }
                else {
                    context.put(VelocityTemplateFieldConstants.PartnerImgUrl, adResponse.getString("banner_url"));
                    t = TemplateType.IMAGE;
                }
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
                adStatus = "AD";
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.error("Error parsing response from Appier : {}", exception);
                LOG.error("Response from Appier: {}", response);
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return (config.getString("appier.advertiserId"));
    }

}
