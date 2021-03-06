package com.inmobi.adserve.channels.adnetworks.amobeeplatform;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.util.CategoryList;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;


public class DCPAmobeePlatformAdnetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DCPAmobeePlatformAdnetwork.class);

    private static final String EXT_SITE_KEY = "as";
    private static final String IP_ADDR = "i";
    private static final String DEVICE_ID = "uid";
    private static final String TIME = "t";
    private static final String CATEGORIES = "kw";
    private static final String WIDTH = "adw";
    private static final String HEIGHT = "adh";
    private static final String IDFA = "ifa";
    private static final String UDID = "udid";
    private static final String ANDROIDID = "androidid";
    private static final String GENDER = "ge";
    private static final String AGE = "age";
    private static final String COUNTRY_CODE = "co";
    private static final String NEGATIVE_KEYWORD = "nk";
    private int width;
    private int height;
    private String latitude;
    private String longitude;
    private int client = 0;
    private String name;

    /**
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverChannel
     */
    public DCPAmobeePlatformAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        host = config.getString(name + ".host");
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for {} so exiting adapter", name);
            LOG.info("Configure parameters inside {} returned false", name);
            return false;
        }

        if (casInternalRequestParameters.getLatLong() != null
                && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
            final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        if (null != slotSizeMapEntity) {
            final Dimension dim = slotSizeMapEntity.getDimension();
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        } else {
            LOG.debug("mandatory parameters missing for {} so exiting adapter", name);
            LOG.info("Configure parameters inside {} returned false", name);
            return false;
        }
        if (sasParams.getOsId() == HandSetOS.Android.getValue()) { // android
            client = 1;

        } else if (sasParams.getOsId() == HandSetOS.iOS.getValue()) { // iPhone
            client = 2;
        }
        return true;
    }

    @Override
    public String getName() {
        return name + DCP_KEY;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            final StringBuilder url = new StringBuilder(host);
            appendQueryParam(url, EXT_SITE_KEY, externalSiteId, false);
            appendQueryParam(url, UA, getURLEncode(sasParams.getUserAgent(), format), false);
            appendQueryParam(url, IP_ADDR, sasParams.getRemoteHostIp(), false);
            appendQueryParam(url, DEVICE_ID, getUid(true), false);
            appendQueryParam(url, TIME, System.currentTimeMillis(), false);
            appendQueryParam(url, CATEGORIES, getURLEncode(getCategories(',', true), format), false);
            if (width != 0 && height != 0) {
                appendQueryParam(url, WIDTH, width, false);
                appendQueryParam(url, HEIGHT, height, false);
            }

            if (client == 2) {
                final String ifa = getUidIFA(true);
                if (StringUtils.isNotBlank(ifa)) {
                    appendQueryParam(url, IDFA, ifa, false);
                }
                if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIDUS1())) {
                    appendQueryParam(url, UDID, casInternalRequestParameters.getUidIDUS1(), false);
                } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUidMd5())) {
                    appendQueryParam(url, UDID, casInternalRequestParameters.getUidMd5(), false);
                }
            } else if (client == 1) {
                if (StringUtils.isNotBlank(casInternalRequestParameters.getUidMd5())) {
                    appendQueryParam(url, ANDROIDID, casInternalRequestParameters.getUidMd5(), false);
                } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUidO1())) {
                    appendQueryParam(url, ANDROIDID, casInternalRequestParameters.getUidMd5(), false);
                }
            }

            if (!StringUtils.isEmpty(latitude)) {
                appendQueryParam(url, LAT, latitude, false);
            }
            if (!StringUtils.isEmpty(longitude)) {
                appendQueryParam(url, LONG, longitude, false);
            }
            if (sasParams.getGender() != null) {
                appendQueryParam(url, GENDER, sasParams.getGender(), false);
            }
            if (sasParams.getAge() != null) {
                appendQueryParam(url, AGE, sasParams.getAge(), false);
            }
            if (sasParams.getCountryCode() != null) {
                appendQueryParam(url, COUNTRY_CODE, sasParams.getCountryCode(), false);
            }
            if (sasParams.getPostalCode() != null) {
                appendQueryParam(url, ZIP, sasParams.getPostalCode(), false);
            }
            if (ContentType.PERFORMANCE == sasParams.getSiteContentType()) {
                appendQueryParam(url, NEGATIVE_KEYWORD,
                        getURLEncode(CategoryList.getBlockedCategoryForPerformance(), format), false);
            } else {
                appendQueryParam(url, NEGATIVE_KEYWORD,
                        getURLEncode(CategoryList.getBlockedCategoryForPerformance(), format), false);
            }

            LOG.debug("{} url is {}", name, url);

            return new URI(url.toString());
        } catch (final URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.info("{}", exception);
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
            responseContent = DEFAULT_EMPTY_STRING;
            return;
        } else {
            final VelocityContext context = new VelocityContext();
            context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, response.trim());
            buildInmobiAdTracker();

            try {
                responseContent =
                        Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, getBeaconUrl());
                adStatus = AD_STRING;
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from {}: {}", response, name, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
                return;
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return config.getString(name + ".advertiserId");
    }

}
