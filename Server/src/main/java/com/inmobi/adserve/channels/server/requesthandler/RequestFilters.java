package com.inmobi.adserve.channels.server.requesthandler;

import static com.inmobi.adserve.channels.util.InspectorStrings.BANNER_NOT_ALLOWED;
import static com.inmobi.adserve.channels.util.InspectorStrings.CHINA_MOBILE_TARGETING;
import static com.inmobi.adserve.channels.util.InspectorStrings.INCOMPATIBLE_SITE_TYPE;
import static com.inmobi.adserve.channels.util.InspectorStrings.INVALID_SLOT_REQUEST;
import static com.inmobi.adserve.channels.util.InspectorStrings.JSON_PARSING_ERROR;
import static com.inmobi.adserve.channels.util.InspectorStrings.LOW_SDK_VERSION;
import static com.inmobi.adserve.channels.util.InspectorStrings.MISSING_CATEGORY;
import static com.inmobi.adserve.channels.util.InspectorStrings.MISSING_SITE_ID;
import static com.inmobi.adserve.channels.util.InspectorStrings.NO_SAS_PARAMS;
import static com.inmobi.adserve.channels.util.InspectorStrings.NO_SUPPORTED_SLOTS;
import static com.inmobi.adserve.channels.util.InspectorStrings.TERMINATED_REQUESTS;
import static com.inmobi.adserve.channels.util.InspectorStrings.THRIFT_PARSING_ERROR;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.casthrift.DemandSourceType;


public class RequestFilters {
    private static final Logger LOG = LoggerFactory.getLogger(RequestFilters.class);
    protected static final Long CHINA = 164L;
    protected static final Integer CHINA_MOBILE = 787;


    public boolean isDroppedInRequestFilters(final HttpRequestHandler hrh) {
        final String termReason = hrh.getTerminationReason();
        if (null != termReason) {
            LOG.info("Request not being served because of the termination reason {}", termReason);
            final String statName = CasConfigUtil.JSON_PARSING_ERROR.equalsIgnoreCase(termReason)
                    ? JSON_PARSING_ERROR
                    : THRIFT_PARSING_ERROR;
            InspectorStats.incrementStatCount(TERMINATED_REQUESTS, statName);
            return true;
        }

        final SASRequestParameters sasParams = hrh.responseSender.getSasParams();
        if (null == sasParams) {
            LOG.info("Terminating request as sasParam is null");
            hrh.setTerminationReason(CasConfigUtil.NO_SAS_PARAMS);
            InspectorStats.incrementStatCount(TERMINATED_REQUESTS, NO_SAS_PARAMS);
            return true;
        }

        final DemandSourceType dst = DemandSourceType.findByValue(sasParams.getDst());
        final String dstName = dst != null ? "-" + dst.name() : "-UNKNOWN_DST";

        if (CollectionUtils.isEmpty(sasParams.getCategories())) {
            LOG.info("Category field is not present in the request so sending noad");
            sasParams.setCategories(new ArrayList<>());
            hrh.setTerminationReason(CasConfigUtil.MISSING_CATEGORY);
            InspectorStats.incrementStatCount(TERMINATED_REQUESTS, MISSING_CATEGORY + dstName);
            return true;
        }

        if (null == sasParams.getSiteId()) {
            LOG.info("Terminating request as site id was missing");
            hrh.setTerminationReason(CasConfigUtil.MISSING_SITE_ID);
            InspectorStats.incrementStatCount(TERMINATED_REQUESTS, MISSING_SITE_ID + dstName);
            return true;
        }

        if (!sasParams.getAllowBannerAds()) {
            LOG.info("Request not being served because of banner not allowed.");
            hrh.setTerminationReason(CasConfigUtil.BANNER_NOT_ALLOWED);
            InspectorStats.incrementStatCount(TERMINATED_REQUESTS, BANNER_NOT_ALLOWED + dstName);
            return true;
        }

        if (sasParams.getSiteContentType() != null
                && !CasConfigUtil.allowedSiteTypes.contains(sasParams.getSiteContentType().name())) {
            LOG.info("Terminating request as incompatible content type");
            hrh.setTerminationReason(CasConfigUtil.INCOMPATIBLE_SITE_TYPE);
            InspectorStats.incrementStatCount(TERMINATED_REQUESTS, INCOMPATIBLE_SITE_TYPE + dstName);
            return true;
        }

        final String tempSdkVersion = sasParams.getSdkVersion();
        LOG.debug("sdk-version : {}", tempSdkVersion);
        if (null != tempSdkVersion) {
            try {
                final String ia = tempSdkVersion.substring(0, 1);
                if (("i".equalsIgnoreCase(ia) || "a".equalsIgnoreCase(ia))
                        && Integer.parseInt(tempSdkVersion.substring(1, 2)) < 3) {
                    LOG.info("Terminating request as sdkVersion is less than 3");
                    hrh.setTerminationReason(CasConfigUtil.LOW_SDK_VERSION);
                    InspectorStats.incrementStatCount(TERMINATED_REQUESTS, LOW_SDK_VERSION + dstName);
                    return true;
                }
            } catch (final Exception exception) {
                LOG.info("Invalid sdk-version, Exception raised {}", exception);
            }
        }

        if (DemandSourceType.IX == dst &&
                dropInChinaMobileTargetingFilter(sasParams.getCountryId(), sasParams.getCarrierId())) {
            // Drop Request
            LOG.info("Request dropped since the China request is not from China Mobile or Test Carrier");
            hrh.setTerminationReason(CasConfigUtil.CHINA_MOBILE_TARGETING);
            InspectorStats.incrementStatCount(TERMINATED_REQUESTS, CHINA_MOBILE_TARGETING);
            return true;
        }

        if (CollectionUtils.isEmpty(sasParams.getProcessedMkSlot())) {
            /* Commenting to reduce stats, un-comment on need basis */
            // incrementStats(sasParams);
            LOG.info(
                    "Request dropped since no slot in the list RqMkSlot has a mapping to InMobi slots/IX supported slots");
            hrh.setTerminationReason(CasConfigUtil.NO_SUPPORTED_SLOTS);
            InspectorStats.incrementStatCount(TERMINATED_REQUESTS, NO_SUPPORTED_SLOTS + dstName);
            return true;
        }

        return false;
    }

    // China Mobile hack. TODO: Need to enable targeting at segment level
    protected static boolean dropInChinaMobileTargetingFilter(final Long countryId, final Integer carrierId) {
        return CHINA == countryId && CHINA_MOBILE != carrierId;
    }

    /**
     * Increment stats for DST Level and also for all slots that were requested from UMP
     *
     * @param sasParams
     */
    protected void incrementStats(final SASRequestParameters sasParams) {
        final DemandSourceType dst = DemandSourceType.findByValue(sasParams.getDst());
        final StringBuilder buildDst = new StringBuilder(dst != null ? dst.name() : String.valueOf(sasParams.getDst()))
                .append("-").append(INVALID_SLOT_REQUEST);
        // Increment stats for DST
        InspectorStats.incrementStatCount(buildDst.toString());
        // Increment stats all slots that were requested from UMP
        final List<Short> requestedSlots = sasParams.getRqMkSlot();
        if (CollectionUtils.isNotEmpty(requestedSlots)) {
            for (final Short slotId : requestedSlots) {
                final StringBuilder buildslots = new StringBuilder(buildDst);
                buildslots.append("-").append(slotId);
                InspectorStats.incrementStatCount(buildslots.toString());
            }
        }
    }

}
