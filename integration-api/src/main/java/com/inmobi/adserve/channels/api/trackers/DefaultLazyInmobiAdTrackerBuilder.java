package com.inmobi.adserve.channels.api.trackers;

import static com.inmobi.adserve.channels.util.config.GlobalConstant.ZERO;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.inmobi.adserve.channels.api.SASRequestParameters;

/**
 * Created by ishanbhatnagar on 14/5/15.
 */


/**
 * Builder for the DefaultLazyInmobiAdTracker
 */
public class DefaultLazyInmobiAdTrackerBuilder extends InmobiAdTrackerBuilder {
    private final DefaultLazyInmobiAdTracker.Builder builder;

    // Config Constants
    private static String testCryptoSecretKey;
    private static String cryptoSecretKey;
    private static String rmBeaconURLPrefix;
    private static String imageBeaconURLPrefix;
    private static String clickURLPrefix;

    // Constants
    private static final Boolean IS_BILLABLE_DEMOG = false;
    private static final boolean TEST_MODE = false;
    private static final boolean IS_BEACON_ENABLED_ON_SITE = true;
    private static final boolean IMAGE_BEACON_FLAG = true;
    private static final boolean IS_TEST_REQUEST = false;
    private static final String BUDGET_BUCKET_ID = "101";
    private static final String TIER_INFO = "-1";

    @Inject
    public DefaultLazyInmobiAdTrackerBuilder(@Assisted final SASRequestParameters sasParams,
                                             @Assisted final String impressionId,
                                             @Assisted final boolean isCpc) {
        super(sasParams, impressionId, isCpc);
        builder = DefaultLazyInmobiAdTracker.newBuilder();
        buildHelper();
    }

    public static void init(Configuration clickmakerConfig) {
        cryptoSecretKey      = clickmakerConfig.getString("key.1.value");
        testCryptoSecretKey  = clickmakerConfig.getString("key.2.value");
        rmBeaconURLPrefix    = clickmakerConfig.getString("beaconURLPrefix");
        clickURLPrefix       = clickmakerConfig.getString("clickURLPrefix");
        imageBeaconURLPrefix = rmBeaconURLPrefix;
    }

    private final void buildHelper() {
        builder.impressionId(this.impressionId);
        builder.age(null != sasParams.getAge() ? Math.max(sasParams.getAge().intValue(), 0) : 0);
        builder.countryId(null != sasParams.getCountryId() ? sasParams.getCountryId().intValue() : 0);
        builder.location(null != sasParams.getState() ? sasParams.getState() : 0);
        builder.siteSegmentId(sasParams.getSiteSegmentId());
        builder.placementSegmentId(sasParams.getPlacementSegmentId());
        builder.gender(null != sasParams.getGender() ? sasParams.getGender() : "u");
        builder.isCPC(isCpc);
        builder.carrierId(sasParams.getCarrierId());
        builder.handsetInternalId(sasParams.getHandsetInternalId());
        builder.ipFileVersion(sasParams.getIpFileVersion().longValue());
        builder.siteIncId(sasParams.getSiteIncId());
        builder.udIdVal(sasParams.getTUidParams());
        builder.isRmAd(sasParams.isRichMedia());
        builder.latlonval(StringUtils.isEmpty(sasParams.getLatLong()) ? "x" : sasParams.getLatLong());
        builder.isRtbSite(sasParams.getSst() != 0);
        builder.dst(String.valueOf(sasParams.getDst() - 1));    // Dst-1
        builder.placementId(sasParams.getPlacementId());
        builder.integrationDetails(sasParams.getIntegrationDetails());
        builder.appBundleId(sasParams.getAppBundleId());
        builder.normalizedUserId(sasParams.getNormalizedUserId());
        builder.requestedAdType(sasParams.getRequestedAdType());

        // Config Constants
        builder.cryptoSecretKey(cryptoSecretKey);
        builder.testCryptoSecretKey(testCryptoSecretKey);
        builder.rmBeaconURLPrefix(rmBeaconURLPrefix);
        builder.clickURLPrefix(clickURLPrefix);
        builder.imageBeaconURLPrefix(imageBeaconURLPrefix);

        // Constants
        builder.tierInfo(TIER_INFO);
        builder.creativeId(ZERO);
        builder.isBillableDemog(IS_BILLABLE_DEMOG);
        builder.imageBeaconFlag(IMAGE_BEACON_FLAG);
        builder.isBeaconEnabledOnSite(IS_BEACON_ENABLED_ON_SITE);
        builder.testMode(TEST_MODE);
        builder.isTestRequest(IS_TEST_REQUEST);
        builder.budgetBucketId(BUDGET_BUCKET_ID);
    }

    @Override
    public DefaultLazyInmobiAdTracker buildInmobiAdTracker() {
        return builder.build();
    }
}