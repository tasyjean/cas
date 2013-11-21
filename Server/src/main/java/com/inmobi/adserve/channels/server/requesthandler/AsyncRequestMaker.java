package com.inmobi.adserve.channels.server.requesthandler;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.ChannelServer;
import com.inmobi.adserve.channels.server.SegmentFactory;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.phoenix.batteries.util.WilburyUUID;
import com.ning.http.client.AsyncHttpClient;
import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;


public class AsyncRequestMaker {

    private static ClientBootstrap clientBootstrap;
    private static ClientBootstrap rtbClientBootstrap;
    private static AsyncHttpClient asyncHttpClient;

    public static AsyncHttpClient getAsyncHttpClient() {
        return asyncHttpClient;
    }

    public static void init(ClientBootstrap clientBootstrap, ClientBootstrap rtbClientBootstrap,
            AsyncHttpClient asyncHttpClient) {
        AsyncRequestMaker.clientBootstrap = clientBootstrap;
        AsyncRequestMaker.rtbClientBootstrap = rtbClientBootstrap;
        AsyncRequestMaker.asyncHttpClient = asyncHttpClient;
    }

    /**
     * For each channel we configure the parameters and make the async request if the async request is successful we add
     * it to segment list else we drop it
     */
    public static List<ChannelSegment> prepareForAsyncRequest(List<ChannelSegment> rows, DebugLogger logger,
            Configuration config, Configuration rtbConfig, Configuration adapterConfig, HttpRequestHandlerBase base,
            Set<String> advertiserSet, MessageEvent e, RepositoryHelper repositoryHelper, JSONObject jObject,
            SASRequestParameters sasParams, CasInternalRequestParameters casInternalRequestParameterGlobal,
            List<ChannelSegment> rtbSegments) throws Exception {

        List<ChannelSegment> segments = new ArrayList<ChannelSegment>();

        logger.debug("Total channels available for sending requests", rows.size() + "");
        boolean isRtbEnabled = rtbConfig.getBoolean("isRtbEnabled", false);
        int rtbMaxTimeOut = rtbConfig.getInt("RTBreadtimeoutMillis", 200);
        logger.debug("isRtbEnabled is", isRtbEnabled, " and rtbMaxTimeout is", rtbMaxTimeOut);

        for (ChannelSegment row : rows) {
            ChannelSegmentEntity channelSegmentEntity = row.getChannelSegmentEntity();
            AdNetworkInterface network = SegmentFactory.getChannel(channelSegmentEntity.getAdvertiserId(), row
                    .getChannelSegmentEntity()
                        .getChannelId(), adapterConfig, clientBootstrap, rtbClientBootstrap, base, e, advertiserSet,
                logger, isRtbEnabled, rtbMaxTimeOut, sasParams.getDst(), repositoryHelper);
            if (null == network) {
                logger.debug("No adapter found for adGroup:", channelSegmentEntity.getAdgroupId());
                continue;
            }
            logger.debug("adapter found for adGroup:", channelSegmentEntity.getAdgroupId(), "advertiserid is", row
                    .getChannelSegmentEntity()
                        .getAdvertiserId(), "is", network.getName());
            if (null == repositoryHelper.queryChannelRepository(channelSegmentEntity.getChannelId())) {
                logger.debug("No channel entity found for channel id:", channelSegmentEntity.getChannelId());
                continue;
            }

            String clickUrl = null;
            String beaconUrl = null;
            sasParams.setImpressionId(getImpressionId(channelSegmentEntity.getIncId()));
            CasInternalRequestParameters casInternalRequestParameters = getCasInternalRequestParameters(sasParams,
                casInternalRequestParameterGlobal);
            controlEnrichment(casInternalRequestParameters, channelSegmentEntity);
            sasParams.setAdIncId(channelSegmentEntity.getIncId());
            logger.debug("impression id is", sasParams.getImpressionId());

            if ((network.isClickUrlRequired() || network.isBeaconUrlRequired()) && null != sasParams.getImpressionId()) {
                boolean isCpc = false;
                if (null != channelSegmentEntity.getPricingModel()
                        && channelSegmentEntity.getPricingModel().equalsIgnoreCase("cpc")) {
                    isCpc = true;
                }
                ClickUrlMakerV6 clickUrlMakerV6 = setClickParams(logger, isCpc, config, sasParams, jObject);
                clickUrl = clickUrlMakerV6.getClickUrl();
                beaconUrl = clickUrlMakerV6.getBeaconUrl();
                logger.debug("click url :", clickUrl);
                logger.debug("beacon url :", beaconUrl);
            }

            logger.debug("Sending request to Channel of advsertiserId", channelSegmentEntity.getAdvertiserId());
            logger.debug("external site key is", channelSegmentEntity.getExternalSiteKey());

            if (network.configureParameters(sasParams, casInternalRequestParameters, channelSegmentEntity, clickUrl,
                beaconUrl)) {
                InspectorStats.incrementStatCount(network.getName(), InspectorStrings.successfulConfigure);
                row.setAdNetworkInterface(network);
                if (network.isRtbPartner()) {
                    rtbSegments.add(row);
                    logger.debug(network.getName(), "is a rtb partner so adding this network to rtb ranklist");
                }
                else {
                    segments.add(row);
                }
            }
        }
        return segments;
    }

    private static CasInternalRequestParameters getCasInternalRequestParameters(SASRequestParameters sasParams,
            CasInternalRequestParameters casInternalRequestParameterGlobal) {
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.impressionId = sasParams.getImpressionId();
        casInternalRequestParameters.blockedCategories = casInternalRequestParameterGlobal.blockedCategories;
        casInternalRequestParameters.blockedAdvertisers = casInternalRequestParameterGlobal.blockedAdvertisers;
        casInternalRequestParameters.highestEcpm = casInternalRequestParameterGlobal.highestEcpm;
        casInternalRequestParameters.rtbBidFloor = casInternalRequestParameterGlobal.rtbBidFloor;
        casInternalRequestParameters.auctionId = casInternalRequestParameterGlobal.auctionId;
        casInternalRequestParameters.uid = casInternalRequestParameterGlobal.uid;
        casInternalRequestParameters.uidO1 = casInternalRequestParameterGlobal.uidO1;
        casInternalRequestParameters.uidIFA = casInternalRequestParameterGlobal.uidIFA;
        casInternalRequestParameters.uidIFV = casInternalRequestParameterGlobal.uidIFV;
        casInternalRequestParameters.uidSO1 = casInternalRequestParameterGlobal.uidSO1;
        casInternalRequestParameters.uidIDUS1 = casInternalRequestParameterGlobal.uidIDUS1;
        casInternalRequestParameters.uidMd5 = casInternalRequestParameterGlobal.uidMd5;
        casInternalRequestParameters.uidADT = casInternalRequestParameterGlobal.uidADT;
        casInternalRequestParameters.zipCode = sasParams.getPostalCode();
        casInternalRequestParameters.latLong = sasParams.getLatLong();
        casInternalRequestParameters.appUrl = sasParams.getAppUrl();
        return casInternalRequestParameters;
    }

    private static void controlEnrichment(CasInternalRequestParameters casInternalRequestParameters,
            ChannelSegmentEntity channelSegmentEntity) {
        if (channelSegmentEntity.isStripUdId()) {
            casInternalRequestParameters.uid = null;
            casInternalRequestParameters.uidO1 = null;
            casInternalRequestParameters.uidMd5 = null;
            casInternalRequestParameters.uidIFA = null;
            casInternalRequestParameters.uidIFV = null;
            casInternalRequestParameters.uidIDUS1 = null;
            casInternalRequestParameters.uidSO1 = null;
            casInternalRequestParameters.uidADT = null;
        }
        if (channelSegmentEntity.isStripLatlong()) {
            casInternalRequestParameters.zipCode = null;
        }
        if (channelSegmentEntity.isStripLatlong()) {
            casInternalRequestParameters.latLong = null;
        }
        if (!channelSegmentEntity.isAppUrlEnabled()) {
            casInternalRequestParameters.appUrl = null;
        }

    }

    public static List<ChannelSegment> makeAsyncRequests(List<ChannelSegment> rankList, DebugLogger logger,
            MessageEvent e, List<ChannelSegment> rtbSegments) {
        Iterator<ChannelSegment> itr = rankList.iterator();
        while (itr.hasNext()) {
            ChannelSegment channelSegment = itr.next();
            InspectorStats.incrementStatCount(channelSegment.getAdNetworkInterface().getName(),
                InspectorStrings.totalInvocations);
            if (channelSegment.getAdNetworkInterface().makeAsyncRequest()) {
                logger.debug("Successfully sent request to channel of  advertiser id", channelSegment
                        .getChannelSegmentEntity()
                            .getId(), "and channel id", channelSegment.getChannelSegmentEntity().getChannelId());
            }
            else {
                itr.remove();
            }
        }
        Iterator<ChannelSegment> rtbItr = rtbSegments.iterator();
        while (rtbItr.hasNext()) {
            ChannelSegment channelSegment = rtbItr.next();
            InspectorStats.incrementStatCount(channelSegment.getAdNetworkInterface().getName(),
                InspectorStrings.totalInvocations);
            if (channelSegment.getAdNetworkInterface().makeAsyncRequest()) {
                logger.debug("Successfully sent request to rtb channel of  advertiser id", channelSegment
                        .getChannelSegmentEntity()
                            .getId(), "and channel id", channelSegment.getChannelSegmentEntity().getChannelId());
            }
            else {
                rtbItr.remove();
            }
        }
        return rankList;
    }

    public static String getImpressionId(long adId) {
        String uuidIntKey = (WilburyUUID.setIntKey(WilburyUUID.getUUID().toString(), (int) adId)).toString();
        String uuidMachineKey = (WilburyUUID.setMachineId(uuidIntKey, ChannelServer.hostIdCode)).toString();
        return (WilburyUUID.setDataCenterId(uuidMachineKey, ChannelServer.dataCenterIdCode)).toString();
    }

    private static ClickUrlMakerV6 setClickParams(DebugLogger logger, boolean pricingModel, Configuration config,
            SASRequestParameters sasParams, JSONObject jObject) {
        ClickUrlMakerV6.Builder builder = ClickUrlMakerV6.newBuilder();
        builder.setLogger(logger);
        try {
            if (null != sasParams.getAge()) {
                builder.setAge(Integer.parseInt(sasParams.getAge()));
            }
        }
        catch (NumberFormatException e) {
            logger.debug("Wrong format for Age", e.getMessage());
        }
        if (null != sasParams.getGender()) {
            builder.setGender(sasParams.getGender());
        }
        builder.setCPC(pricingModel);
        Integer carrierId = null;
        if (null != sasParams.getCarrier()) {
            try {
                carrierId = sasParams.getCarrier().getInt(0);
            }
            catch (JSONException e) {
                logger.debug("carrierId is not present in the request");
            }
        }
        if (null != carrierId) {
            builder.setCarrierId(carrierId);
        }
        try {
            if (null != sasParams.getCountryStr()) {
                builder.setCountryId(Integer.parseInt(sasParams.getCountryStr()));
            }
        }
        catch (NumberFormatException e) {
            logger.debug("Wrong format for CountryString", e.getMessage());
        }
        try {
            if (null != sasParams.getHandset()) {
                builder.setHandsetInternalId(Long.parseLong(sasParams.getHandset().get(0).toString()));
            }
        }
        catch (NumberFormatException e1) {
            logger.debug("NumberFormatException while parsing handset");
        }
        catch (JSONException e1) {
            logger.debug("CountryId is not present in the sasParams");
        }

        if (null == sasParams.getImpressionId()) {
            logger.debug("impression id is null");
        }
        else {
            builder.setImpressionId(sasParams.getImpressionId());
        }
        builder.setIpFileVersion(sasParams.getIpFileVersion().longValue());
        builder.setIsBillableDemog(false);
        try {
            if (null != sasParams.getArea()) {
                builder.setLocation(Integer.parseInt(sasParams.getArea()));
            }
        }
        catch (NumberFormatException e) {
            logger.debug("Wrong format for Area", e.getMessage());
        }
        if (null != sasParams.getSiteSegmentId()) {
            builder.setSegmentId(sasParams.getSiteSegmentId());
        }
        builder.setSiteIncId(sasParams.getSiteIncId());
        Map<String, String> uidMap = new HashMap<String, String>();
        JSONObject userIdMap = null;
        try {
            userIdMap = (JSONObject) jObject.get("u-id-params");
        }
        catch (JSONException e) {
            logger.debug("u-id-params is not present in the request");
        }

        if (null != userIdMap) {
            Iterator userMapIterator = userIdMap.keys();
            while (userMapIterator.hasNext()) {
                String key = (String) userMapIterator.next();
                String value = null;
                try {
                    value = (String) userIdMap.get(key);
                }
                catch (JSONException e) {
                    logger.debug("value corresponding to uid key is not present in the uidMap");
                }
                if (null != value) {
                    uidMap.put(key.toUpperCase(Locale.ENGLISH), value);
                }
            }
        }
        builder.setUdIdVal(uidMap);
        builder.setCryptoSecretKey(config.getString("clickmaker.key.1.value"));
        builder.setTestCryptoSecretKey(config.getString("clickmaker.key.2.value"));
        builder.setImageBeaconFlag(true);// true/false
        builder.setBeaconEnabledOnSite(true);// do not know
        builder.setTestMode(false);
        builder.setRmAd(jObject.optBoolean("rich-media", false));
        builder.setRmBeaconURLPrefix(config.getString("clickmaker.beaconURLPrefix"));
        builder.setClickURLPrefix(config.getString("clickmaker.clickURLPrefix"));
        builder.setImageBeaconURLPrefix(config.getString("clickmaker.beaconURLPrefix"));
        builder.setTestRequest(false);
        builder.setLatlonval(sasParams.getLatLong());
        builder.setRtbSite(false);
        builder.setDst(sasParams.getDst() + "");
        return new ClickUrlMakerV6(builder);
    }
}
