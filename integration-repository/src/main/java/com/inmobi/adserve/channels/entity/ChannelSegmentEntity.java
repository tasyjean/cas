package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.inmobi.adtemplate.platform.CreativeType;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

import lombok.Getter;
import lombok.Setter;


@Getter
public class ChannelSegmentEntity implements IdentifiableEntity<String> {
    private final static Logger LOG = LoggerFactory.getLogger(ChannelSegmentEntity.class);
    private static final long serialVersionUID = 1L;
    private final static Gson GSON = new Gson();
    private final String advertiserId;
    private final String adgroupId;
    private final String[] adIds;
    private final String channelId;
    private final long platformTargeting;
    private final Long[] rcList;
    private final Long[] tags;
    private final boolean status;
    private final boolean isTestMode;
    private final String externalSiteKey;
    private final Timestamp modified_on;
    private final String campaignId;
    private final Long[] slotIds;
    private final SecondaryAdFormatConstraints secondaryAdFormatConstraints;
    private final Long[] incIds;
    private final long adgroupIncId;
    private final boolean allTags;
    private final String pricingModel;
    private final ArrayList<Integer> targetingPlatform;
    private final Integer[] siteRatings;
    private final List<Integer> osIds;
    private final boolean udIdRequired;
    private final boolean zipCodeRequired;
    private final boolean latlongRequired;
    private final boolean restrictedToRichMediaOnly;
    private final boolean appUrlEnabled;
    private final boolean interstitialOnly;
    private final boolean nonInterstitialOnly;
    private final boolean stripUdId;
    private final boolean stripZipCode;
    private final boolean stripLatlong;
    private final boolean secure;
    private final JSONObject additionalParams;
    private final Long[] categoryTaxonomy;
    private final Set<String> sitesIE;
    private final boolean isSiteInclusion;
    private final long impressionCeil;
    private final List<Long> manufModelTargetingList;
    private final double ecpmBoost;
    private final Date ecpmBoostExpiryDate;
    private final Long[] tod;
    private final int dst; // Classify rtbd, ix and dcp ad groups
    private final long campaignIncId;
    private final Integer[] adFormatIds;
    private final String automationTestId;

    public ChannelSegmentEntity(final Builder builder) {
        advertiserId = builder.advertiserId;
        adgroupId = builder.adgroupId;
        adIds = builder.adIds;
        channelId = builder.channelId;
        platformTargeting = builder.platformTargeting;
        rcList = builder.rcList;
        tags = builder.tags;
        status = builder.status;
        isTestMode = builder.isTestMode;
        externalSiteKey = builder.externalSiteKey;
        modified_on = builder.modified_on;
        campaignId = builder.campaignId;
        slotIds = builder.slotIds;
        secondaryAdFormatConstraints = builder.secondaryAdFormatConstraints;
        incIds = builder.incIds;
        adgroupIncId = builder.adgroupIncId;
        allTags = builder.allTags;
        pricingModel = builder.pricingModel;
        final ArrayList<Integer> targetingPlatform = new ArrayList<>();
        if (builder.targetingPlatform == 1 || builder.targetingPlatform > 2) {
            targetingPlatform.add(1);
        }
        if (builder.targetingPlatform >= 2) {
            targetingPlatform.add(2);
        }
        this.targetingPlatform = targetingPlatform;
        siteRatings = builder.siteRatings;
        osIds = builder.osIds;
        udIdRequired = builder.udIdRequired;
        zipCodeRequired = builder.zipCodeRequired;
        latlongRequired = builder.latlongRequired;
        restrictedToRichMediaOnly = builder.restrictedToRichMediaOnly;
        appUrlEnabled = builder.appUrlEnabled;
        interstitialOnly = builder.interstitialOnly;
        nonInterstitialOnly = builder.nonInterstitialOnly;
        stripUdId = builder.stripUdId;
        stripZipCode = builder.stripZipCode;
        stripLatlong = builder.stripLatlong;
        secure = builder.secure;
        additionalParams = builder.additionalParams;
        categoryTaxonomy = builder.categoryTaxonomy;
        sitesIE = builder.sitesIE;
        isSiteInclusion = builder.isSiteInclusion;
        impressionCeil = builder.impressionCeil;
        manufModelTargetingList = builder.manufModelTargetingList;
        ecpmBoost = builder.ecpmBoost;
        ecpmBoostExpiryDate = builder.ecpmBoostExpiryDate;
        tod = builder.tod;
        dst = builder.dst;
        campaignIncId = builder.campaignIncId;
        adFormatIds = builder.adFormatIds;
        automationTestId = builder.automationTestId;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private String advertiserId;
        private String adgroupId;
        private String[] adIds;
        private String channelId;
        private long platformTargeting;
        private Long[] rcList;
        private Long[] tags;
        private boolean status;
        private boolean isTestMode;
        private String externalSiteKey;
        private Timestamp modified_on;
        private String campaignId;
        private Long[] slotIds;
        private SecondaryAdFormatConstraints secondaryAdFormatConstraints;
        private Long[] incIds;
        private long adgroupIncId;
        private boolean allTags;
        private String pricingModel;
        private int targetingPlatform;
        private Integer[] siteRatings;
        private List<Integer> osIds;
        private boolean udIdRequired;
        private boolean zipCodeRequired;
        private boolean latlongRequired;
        private boolean restrictedToRichMediaOnly;
        private boolean appUrlEnabled;
        private boolean interstitialOnly;
        private boolean nonInterstitialOnly;
        private boolean stripUdId;
        private boolean stripZipCode;
        private boolean stripLatlong;
        private boolean secure;
        private JSONObject additionalParams;
        private Long[] categoryTaxonomy;
        private Set<String> sitesIE;
        private boolean isSiteInclusion;
        private long impressionCeil;
        private List<Long> manufModelTargetingList;
        private double ecpmBoost;
        private Date ecpmBoostExpiryDate;
        private Long[] tod;
        private int dst;
        private long campaignIncId;
        private Integer[] adFormatIds;
        private String automationTestId;

        public ChannelSegmentEntity build() {
            return new ChannelSegmentEntity(this);
        }
    }

    @Override
    public String getId() {
        return adgroupId;
    }

    @Override
    public String getJSON() {
        return GSON.toJson(this);
    }

    /**
     * Get the inc_id corresponding to an ad format in the ad_group.
     */
    public long getIncId(final ADCreativeType creativeType) {
        final long notFound = -1L;

        if (null == getAdFormatIds()) {
            return notFound;
        }

        final List<Integer> requestedAdFormatIdList = getAdFormatIdList(creativeType);
        try {
            for (int i = 0; i < getAdFormatIds().length; i++) {
                if (requestedAdFormatIdList.contains(getAdFormatIds()[i])) {
                    return getIncIds()[i];
                }
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            LOG.debug("Exception raised in ChannelSegmentEntity {}", e);
            return notFound;
        }
        // This would happen only with inconsistent data.
        return notFound;
    }

    /**
     * Get the AdId corresponding to an ad format in the ad_group.
     */
    public String getAdId(final ADCreativeType creativeType) {
        final String notFound = StringUtils.EMPTY;

        if (null == getAdFormatIds()) {
            return notFound;
        }

        final List<Integer> requestedAdFormatIdList = getAdFormatIdList(creativeType);
        try {
            for (int i = 0; i < getAdFormatIds().length; i++) {
                if (requestedAdFormatIdList.contains(getAdFormatIds()[i])) {
                    return getAdIds()[i];
                }
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            LOG.debug("Exception raised in ChannelSegmentEntity {}", e);
            return notFound;
        }
        // This would happen only with inconsistent data.
        return notFound;
    }

    /**
     * Get Ad format ids corresponding to a creative type in the ad group.
     */
    private List<Integer> getAdFormatIdList(final ADCreativeType creativeType) {
        List<Integer> adFormatIdList = new ArrayList<>(2);
        if (null == creativeType) {
            adFormatIdList.add(-1);
            return adFormatIdList;
        }

        switch (creativeType) {
            case NATIVE:
                adFormatIdList.add(CreativeType.META.getValue()); // NATIVE META_JSON
                break;
            case INTERSTITIAL_VIDEO:
                adFormatIdList.add(CreativeType.VIDEO.getValue()); // VIDEO
                adFormatIdList.add(CreativeType.VIDEO_VAST_URL.getValue()); // VAST
                break;
            case BANNER:
                adFormatIdList.add(CreativeType.TEXT.getValue()); //BANNER
                break;
            default:
                adFormatIdList.add(-1);

        }
        return adFormatIdList;
    }
}
