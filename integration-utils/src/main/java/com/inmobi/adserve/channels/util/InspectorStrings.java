package com.inmobi.adserve.channels.util;

public class InspectorStrings {

    private InspectorStrings(){
        //dummy private constructor to avoid instantiation from other classes
    }

    public static final String TOTAL_REQUESTS                            = "TotalRequests";
    public static final String RULE_ENGINE_REQUESTS                      = "RuleEngineRequests";
    public static final String BACK_FILL_REQUESTS                        = "BackFillRequests";
    public static final String IX_REQUESTS                               = "IXRequests";
    public static final String NATIVE_REQUESTS                           = "NativeRequests";
    public static final String NON_AD_REQUESTS                           = "NonAdRequests";
    public static final String TOTAL_INVOCATIONS                         = "TotalInvocations";
    public static final String SUCCESSFUL_CONFIGURE                      = "SuccessfulConfigure";
    public static final String TOTAL_NO_FILLS                            = "TotalNoFills";
    public static final String TOTAL_FILLS                               = "TotalFills";
    public static final String RULE_ENGINE_FILLS                         = "RuleEngineFills";
    public static final String IX_FILLS                                  = "IXFills";
    public static final String DCP_FILLS                                 = "DCPFills";
    public static final String LATENCY                                   = "Latency";
    public static final String CONNECTION_LATENCY                        = "ConnectionLatency";
    public static final String COUNT                                     = "Count";
    public static final String TOTAL_TIMEOUT                             = "TotalTimeout";
    public static final String CONNECTION_TIMEOUT                        = "ConnectionTimeout";
    public static final String TOTAL_TERMINATE                           = "TotalTerminate";
    public static final String SUCCESSFUL_REQUESTS                       = "success";
    public static final String JSON_PARSING_ERROR                        = "Terminated_JsonError";
    public static final String THRIFT_PARSING_ERROR                      = "Terminated_ThriftError";
    public static final String PROCESSING_ERROR                          = "Terminated_ServerError";
    public static final String MISSING_SITE_ID                           = "Terminated_NoSite";
    public static final String CLIENT_TIMER_LATENCY                      = "ClientTimerLatency";
    public static final String timerLatency                              = "timerLatency";
    public static final String timeoutException                          = "TimeoutException";
    public static final String INCOMPATIBLE_SITE_TYPE                    = "Termninated_IncompatibleSite";
    public static final String PERCENT_ROLL_OUT                          = "PercentRollout";
    public static final String NO_MATCH_SEGMENT_LATENCY                  = "NoMatchSegmentLatency";
    public static final String NO_MATCH_SEGMENT_COUNT                    = "NoMatchSegmentCount";
    public static final String DROPPED_IN_IMPRESSION_FILTER              = "DroppedInImpressionFilter";
    public static final String DROPPED_IN_PROPERTY_VIOLATION_FILTER      = "DroppedInPropertyViolationFilter";
    public static final String DROPPED_IN_BURN_FILTER                    = "DroppedInBurnFilter";
    public static final String DROPPED_IN_REQUEST_CAP_FILTER             = "DroppedInRequestCapFilter";
    public static final String DROPPED_IN_SEGMENT_PER_REQUEST_FILTER     = "DroppedInSegmentPerRequestFilter";
    public static final String TOTAL_MATCHED_SEGMENTS                    = "TotalMatchedSegments";
    public static final String TOTAL_SELECTED_SEGMENTS                   = "TotalSelectedSegments";
    public static final String LOW_SDK_VERSION                           = "LowSdkVersion";
    public static final String SERVER_IMPRESSION                         = "Impression";
    public static final String CHANNEL_EXCEPTION                         = "ChannelException";
    public static final String DROPPED_IN_UDID_FILTER                    = "DroppedInUdidFilter";
    public static final String DROPPED_IN_LAT_LONG_FILTER                = "DroppedInLatLongFilter";
    public static final String DROPPED_IN_ZIPCODE_FILTER                 = "DroppedInZipcodeFilter";
    public static final String DROPPED_IN_RICH_MEDIA_FILTER              = "DroppedInRichMediaFilter";
    public static final String DROPPED_IN_ONLY_INTERSTITIAL_FILTER       = "DroppedInOnlyInterstitialFilter";
    public static final String DROPPED_IN_ONLY_NON_INTERSTITIAL_FILTER   = "DroppedInOnlyNonInterstitialFilter";
    public static final String DROPPED_IN_ADVERTISER_EXCLUSION_FILTER    = "DroppedinAdvertiserExclusionFilter";
    public static final String DROPPED_IN_SITE_EXCLUSION_FILTER          = "DroppedinSiteExclusionFilter";
    public static final String DROPPED_IN_HANDSET_TARGETING_FILTER       = "DroppedinHandsetTargetingFilter";
    public static final String DROPPED_IN_PRICING_ENGINE_FILTER          = "DroppedinPricingEngineFilter";
    public static final String DROPPED_IN_TOD_FILTER                     = "DroppedInTODFilter";
    public static final String SITE_FEEDBACK_CACHE_HIT                   = "SiteFeedbackCacheHit";
    public static final String SITE_FEEDBACK_CACHE_MISS                  = "SiteFeedbackCacheMiss";
    public static final String SITE_FEEDBACK_LATENCY                     = "SiteFeedbackLatency";
    public static final String MISSING_CATEGORY                          = "MissingCategory";
    public final static String DROPPED_IN_RTB_BALANCE_FILTER             = "DroppedInRtbBalanceFilter";
    public final static String DROPPED_IN_RTB_BID_FLOOR_FILTER           = "DroppedInRtbBidFloorFilter";
    public final static String DROPPED_IN_RTB_AUCTION_ID_MIS_MATCH_FILTER = "DroppedInRtbAuctionIdMisMatchFilter";
    public final static String DROPPED_IN_RTB_SEATID_MIS_MATCH_FILTER    = "DroppedInRtbSeatidMisMatchFilter";
    public final static String DROPPED_IN_RTB_IMPRESSION_ID_MIS_MATCH_FILTER = "DroppedInRtbImpressionIdMisMatchFilter";
    public final static String DROPPED_IN_CREATIVE_ID_MISSING_FILTER     = "DroppedInCreativeIdMissingFilter";
    public final static String DROPPED_IN_SAMPLE_IMAGE_URL_MISSING_FILTER = "DroppedInSampleImageUrlMissingFilter";
    public final static String DROPPED_IN_ADVERTISER_DOMAINS_MISSING_FILTER      = "DroppedInAdvertiserDomainsFilter";
    public final static String DROPPED_IN_CREATIVE_ATTRIBUTES_MISSING_FILTER = "DroppedInCreativeAttributesFilter";
    public final static String DROPPED_IN_CREATIVE_VALIDATOR_FILTER      = "DroppedInCreativeValidatorFilter";
    public static final String siteFeedbackRequestsToAerospike           = "SiteFeedbackRequestsToAerospike";
    public static final String siteFeedbackFailedToLoadFromAerospike     = "SiteFeedbackFailedToLoadFromAerospike";
    public final static String droppedInAuctionIxImpressionIdFilter      = "DroppedInAuctionIxImpressionIdFilter";
    public final static String DROPPED_IN_ACCOUNT_SEGMENT_FILTER         = "DroppedInAccountSegmentFilter";
    public static final String DROPPED_IN_SUPPLY_DEMAND_CLASSIFICATION_FILTER = "DroppedInSupplyDemandClassificationFilter";
    public static final String DROPPED_IN_RTB_CURRENCY_NOT_SUPPORTED_FILTER = "DroppedInRtbCurrencyNotSupportedFilter";
    public static final String DROPPED_IN_INVALID_DETAILS_FILTER         = "DroppedInInvalidDetailsFilter";
    public static final String DROPPED_IN_BANNER_NOT_ALLOWED_FILTER      = "DroppedInBannerNotAllowedFilter";
    public static final String DROPPED_IN_PARTNER_COUNT_FILTER           = "DroppedInPartnerCountFilter";
    public static final String DROPPED_IN_DAILY_IMP_COUNT_FILTER         = "DroppedInDailyImpressionCountFilter";
    public static final String IX_SENT_AS_TRANSPARENT                    = "IXSentAsTransparent";
    public static final String IX_SENT_AS_BLIND                          = "IXSentAsBlind";
    public static final String IX_ZONE_ID_NOT_PRESENT                    = "IXZoneIdNotPresent";
    public static final String IX_SITE_ID_NOT_PRESENT                    = "IXSiteIdNotPresent";
}
