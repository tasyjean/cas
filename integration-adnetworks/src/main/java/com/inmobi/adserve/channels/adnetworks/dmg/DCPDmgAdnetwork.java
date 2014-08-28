package com.inmobi.adserve.channels.adnetworks.dmg;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.CategoryList;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DCPDmgAdnetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG    = LoggerFactory.getLogger(DCPDmgAdnetwork.class);

    private int                 width;
    private int                 height;
    private String              latitude;
    private String              longitude;
    private String              adType;
    private int                 adTypeId;
    private int                 client = 0;

    /**
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverEvent
     */
    public DCPDmgAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for dmg so exiting adapter");
            return false;
        }

        if (casInternalRequestParameters.latLong != null
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        if (null != sasParams.getSlot() && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
            Dimension dim = SlotSizeMapping.getDimension((long) sasParams.getSlot());
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());

            Integer slot = Integer.valueOf(sasParams.getSlot());
            if (10 == slot // 300X250
                    || 14 == slot // 320X480
                    || 16 == slot) /* 768X1024 */{
                adType = "FullScreen";
                adTypeId = 3;
            }
            else {
                adType = "MobileBanner";
                adTypeId = 4;
            }
        }
        else {
            LOG.debug("mandatory parameters missing for dmg so exiting adapter");
            return false;
        }
        if (sasParams.getOsId() == HandSetOS.Android.getValue()) { // android
            client = 1;

        }
        else if (sasParams.getOsId() == HandSetOS.iOS.getValue()) { // iPhone
            client = 2;
        }
        LOG.info("Configure parameters inside dmg returned true");
        return true;
    }

    @Override
    public String getName() {
        return "dmg";
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            String host = config.getString("dmg.host");
            String adNetworkId = config.getString("dmg.adnetworkId");
            StringBuilder url = new StringBuilder(host);
            url.append("?acc=").append(adNetworkId).append("&as=").append(blindedSiteId).append("&ua=")
                    .append(getURLEncode(sasParams.getUserAgent(), format)).append("&i=")
                    .append(sasParams.getRemoteHostIp()).append("&f=").append(adType).append("&uid=").append(getUid())
                    .append("&t=").append(System.currentTimeMillis()).append("&tp=").append(adTypeId).append("&kw=")
                    .append(getURLEncode(getCategories(',', true), format));
            
            if (width != 0 && height != 0) {
                url.append("&adw=").append(width).append("&adh=").append(height);
            }
            if (client == 2) {
                if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFA)) {
                    url.append("&ifa=").append(casInternalRequestParameters.uidIFA);
                }
                if (StringUtils.isNotBlank(casInternalRequestParameters.uidSO1)) {
                    url.append("&udid=").append(casInternalRequestParameters.uidMd5);
                }
            }
            else if (client == 1) {
                if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
                    url.append("&androidid=").append(casInternalRequestParameters.uidMd5);
                }
            }

            if (!StringUtils.isEmpty(latitude)) {
                url.append("&lat=").append(latitude);
            }
            if (!StringUtils.isEmpty(longitude)) {
                url.append("&long=").append(longitude);
            }
            if (sasParams.getGender() != null) {
                url.append("&ge=").append(sasParams.getGender());
            }
            if (sasParams.getAge() != null) {
                url.append("&age=").append(sasParams.getAge());
            }
            if (sasParams.getCountryCode() != null) {
                url.append("&co=").append(sasParams.getCountryCode());
            }
            if (sasParams.getPostalCode() != null) {
                url.append("&zip=").append(sasParams.getPostalCode());
            }
            if (SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
                url.append("&nk=").append(getURLEncode(CategoryList.getBlockedCategoryForPerformance(), format));
            }
            else {
                url.append("&nk=").append(getURLEncode(CategoryList.getBlockedCategoryForFamilySafe(), format));
            }

            LOG.debug("dmg url is {}", url);

            return (new URI(url.toString()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.error("{}", exception);
        }
        return null;
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);
        statusCode = status.code();
        if (null == response || status.code() != 200 || response.trim().isEmpty()) {
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            VelocityContext context = new VelocityContext();
            context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, response.trim());
            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl);
                adStatus = "AD";
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.error("Error parsing response from dmg : {}", exception);
                LOG.error("Response from dmg: {}", response);
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return (config.getString("dmg.advertiserId"));
    }

}
