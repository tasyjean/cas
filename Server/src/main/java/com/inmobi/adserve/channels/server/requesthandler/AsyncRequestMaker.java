package com.inmobi.adserve.channels.server.requesthandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.ChannelServer;
import com.inmobi.adserve.channels.server.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.server.SegmentFactory;
import com.inmobi.adserve.channels.server.annotations.DcpClientBoostrap;
import com.inmobi.adserve.channels.server.annotations.RtbClientBoostrap;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.phoenix.batteries.util.WilburyUUID;
import com.ning.http.client.AsyncHttpClient;


public class AsyncRequestMaker {
    private static final Logger   LOG = LoggerFactory.getLogger(AsyncRequestMaker.class);

    private final Bootstrap       dcpClientBootstrap;

    private final Bootstrap       rtbClientBootstrap;

    private final AsyncHttpClient asyncHttpClient;

    private final SegmentFactory  segmentFactory;

    @Inject
    public AsyncRequestMaker(@DcpClientBoostrap final Bootstrap dcpClientBootstrap,
            @RtbClientBoostrap final Bootstrap rtbClientBootstrap, final AsyncHttpClient asyncHttpClient,
            final SegmentFactory segmentFactory) {
        this.dcpClientBootstrap = dcpClientBootstrap;
        this.rtbClientBootstrap = rtbClientBootstrap;
        this.asyncHttpClient = asyncHttpClient;
        this.segmentFactory = segmentFactory;
    }

    public AsyncHttpClient getAsyncHttpClient() {
        return asyncHttpClient;
    }

    /**
     * For each channel we configure the parameters and make the async request if the async request is successful we add
     * it to segment list else we drop it
     */
    public List<ChannelSegment> prepareForAsyncRequest(final List<ChannelSegment> rows, final Configuration config,
            final Configuration rtbConfig, final Configuration adapterConfig, final HttpRequestHandlerBase base,
            final Set<String> advertiserSet, final Channel channel, final RepositoryHelper repositoryHelper,
            final JSONObject jObject, final SASRequestParameters sasParams,
            final CasInternalRequestParameters casInternalRequestParameterGlobal, final List<ChannelSegment> rtbSegments)
            throws Exception {

        List<ChannelSegment> segments = new ArrayList<ChannelSegment>();

        LOG.debug("Total channels available for sending requests {}", rows.size());
        boolean isRtbEnabled = rtbConfig.getBoolean("isRtbEnabled", false);
        int rtbMaxTimeOut = rtbConfig.getInt("RTBreadtimeoutMillis", 200);
        LOG.debug("isRtbEnabled is {}  and rtbMaxTimeout is {}", isRtbEnabled, rtbMaxTimeOut);

        for (ChannelSegment row : rows) {
            ChannelSegmentEntity channelSegmentEntity = row.getChannelSegmentEntity();
            AdNetworkInterface network = segmentFactory.getChannel(channelSegmentEntity.getAdvertiserId(), row
                    .getChannelSegmentEntity().getChannelId(), adapterConfig, dcpClientBootstrap, rtbClientBootstrap,
                    base, channel, advertiserSet, isRtbEnabled, rtbMaxTimeOut, sasParams.getDst(), repositoryHelper);
            if (null == network) {
                LOG.debug("No adapter found for adGroup: {}", channelSegmentEntity.getAdgroupId());
                continue;
            }
            LOG.debug("adapter found for adGroup: {} advertiserid is {} is {}", channelSegmentEntity.getAdgroupId(),
                    row.getChannelSegmentEntity().getAdvertiserId(), network.getName());
            if (null == repositoryHelper.queryChannelRepository(channelSegmentEntity.getChannelId())) {
                LOG.debug("No channel entity found for channel id: {}", channelSegmentEntity.getChannelId());
                continue;
            }

            String clickUrl = null;
            String beaconUrl = null;
            sasParams.setImpressionId(getImpressionId(channelSegmentEntity.getIncId()));
            CasInternalRequestParameters casInternalRequestParameters = getCasInternalRequestParameters(sasParams,
                    casInternalRequestParameterGlobal);
            controlEnrichment(casInternalRequestParameters, channelSegmentEntity);
            sasParams.setAdIncId(channelSegmentEntity.getIncId());
            LOG.debug("impression id is {}", sasParams.getImpressionId());

            if ((network.isClickUrlRequired() || network.isBeaconUrlRequired()) && null != sasParams.getImpressionId()) {
                boolean isCpc = false;
                if (null != channelSegmentEntity.getPricingModel()
                        && channelSegmentEntity.getPricingModel().equalsIgnoreCase("cpc")) {
                    isCpc = true;
                }
                ClickUrlMakerV6 clickUrlMakerV6 = setClickParams(isCpc, config, sasParams, jObject);
                Map<String, String> clickGetParams = new HashMap<String, String>();
                clickGetParams.put("ds", "1");
                Map<String, String> beaconGetParams = new HashMap<String, String>();
                beaconGetParams.put("ds", "1");
                beaconGetParams.put("event", "beacon");
                clickUrlMakerV6.createClickUrls();
                clickUrl = clickUrlMakerV6.getClickUrl(clickGetParams);
                beaconUrl = clickUrlMakerV6.getBeaconUrl(beaconGetParams);
                LOG.debug("click url : {}", clickUrl);
                LOG.debug("beacon url : {}", beaconUrl);
            }

            LOG.debug("Sending request to Channel of advsertiserId {}", channelSegmentEntity.getAdvertiserId());
            LOG.debug("external site key is {}", channelSegmentEntity.getExternalSiteKey());

            if (network.configureParameters(sasParams, casInternalRequestParameters, channelSegmentEntity, clickUrl,
                    beaconUrl)) {
                InspectorStats.incrementStatCount(network.getName(), InspectorStrings.successfulConfigure);
                row.setAdNetworkInterface(network);
                if (network.isRtbPartner()) {
                    rtbSegments.add(row);
                    LOG.debug("{} is a rtb partner so adding this network to rtb ranklist", network.getName());
                }
                else {
                    segments.add(row);
                }
            }
        }
        return segments;
    }

    private CasInternalRequestParameters getCasInternalRequestParameters(final SASRequestParameters sasParams,
            final CasInternalRequestParameters casInternalRequestParameterGlobal) {
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

    private void controlEnrichment(final CasInternalRequestParameters casInternalRequestParameters,
            final ChannelSegmentEntity channelSegmentEntity) {
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

    public List<ChannelSegment> makeAsyncRequests(final List<ChannelSegment> rankList, final Channel channel,
            final List<ChannelSegment> rtbSegments) {
        Iterator<ChannelSegment> itr = rankList.iterator();
        while (itr.hasNext()) {
            ChannelSegment channelSegment = itr.next();
            InspectorStats.incrementStatCount(channelSegment.getAdNetworkInterface().getName(),
                    InspectorStrings.totalInvocations);
            if (channelSegment.getAdNetworkInterface().makeAsyncRequest()) {
                LOG.debug("Successfully sent request to channel of  advertiser id {} and channel id {}", channelSegment
                        .getChannelSegmentEntity().getId(), channelSegment.getChannelSegmentEntity().getChannelId());
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
                LOG.debug("Successfully sent request to rtb channel of  advertiser id {} and channel id {}",
                        channelSegment.getChannelSegmentEntity().getId(), channelSegment.getChannelSegmentEntity()
                                .getChannelId());
            }
            else {
                rtbItr.remove();
            }
        }
        return rankList;
    }

    public String getImpressionId(final long adId) {
        String uuidIntKey = (WilburyUUID.setIntKey(WilburyUUID.getUUID().toString(), (int) adId)).toString();
        String uuidMachineKey = (WilburyUUID.setMachineId(uuidIntKey, ChannelServer.hostIdCode)).toString();
        return (WilburyUUID.setDataCenterId(uuidMachineKey, ChannelServer.dataCenterIdCode)).toString();
    }

    private ClickUrlMakerV6 setClickParams(final boolean pricingModel, final Configuration config,
            final SASRequestParameters sasParams, final JSONObject jObject) {
        Set<String> unhashable = new HashSet<String>();
        unhashable.addAll(Arrays.asList(config.getStringArray("clickmaker.unhashable")));
        ClickUrlMakerV6 clickUrlMakerV6 = new ClickUrlMakerV6(unhashable);
        try {
            if (null != sasParams.getAge()) {
                clickUrlMakerV6.setAge(Integer.parseInt(sasParams.getAge()));
            }
        }
        catch (NumberFormatException e) {
            LOG.debug("Wrong format for Age {}", e);
        }
        if (null != sasParams.getGender()) {
            clickUrlMakerV6.setGender(sasParams.getGender());
        }
        clickUrlMakerV6.setCPC(pricingModel);
        Integer carrierId = null;
        if (null != sasParams.getCarrier()) {
            try {
                carrierId = sasParams.getCarrier().getInt(0);
            }
            catch (JSONException e) {
                LOG.debug("carrierId is not present in the request");
            }
        }
        if (null != carrierId) {
            clickUrlMakerV6.setCarrierId(carrierId);
        }
        try {
            if (null != sasParams.getCountryStr()) {
                clickUrlMakerV6.setCountryId(Integer.parseInt(sasParams.getCountryStr()));
            }
        }
        catch (NumberFormatException e) {
            LOG.debug("Wrong format for CountryString {}", e);
        }
        try {
            if (null != sasParams.getHandset()) {
                clickUrlMakerV6.setHandsetInternalId(Long.parseLong(sasParams.getHandset().get(0).toString()));
            }
        }
        catch (NumberFormatException e1) {
            LOG.debug("NumberFormatException while parsing handset");
        }
        catch (JSONException e1) {
            LOG.debug("CountryId is not present in the sasParams");
        }

        if (null == sasParams.getImpressionId()) {
            LOG.debug("impression id is null");
        }
        else {
            clickUrlMakerV6.setImpressionId(sasParams.getImpressionId());
        }
        clickUrlMakerV6.setIpFileVersion(sasParams.getIpFileVersion().longValue());
        clickUrlMakerV6.setIsBillableDemog(false);
        try {
            if (null != sasParams.getArea()) {
                clickUrlMakerV6.setLocation(Integer.parseInt(sasParams.getArea()));
            }
        }
        catch (NumberFormatException e) {
            LOG.debug("Wrong format for Area {}", e);
        }
        if (null != sasParams.getSiteSegmentId()) {
            clickUrlMakerV6.setSegmentId(sasParams.getSiteSegmentId());
        }
        clickUrlMakerV6.setSiteIncId(sasParams.getSiteIncId());
        Map<String, String> uidMap = new HashMap<String, String>();
        JSONObject userIdMap = null;
        try {
            userIdMap = (JSONObject) jObject.get("u-id-params");
        }
        catch (JSONException e) {
            LOG.debug("u-id-params is not present in the request");
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
                    LOG.debug("value corresponding to uid key is not present in the uidMap");
                }
                if (null != value) {
                    uidMap.put(key.toUpperCase(Locale.ENGLISH), value);
                }
            }
        }
        clickUrlMakerV6.setUdIdVal(uidMap);
        clickUrlMakerV6.setCryptoSecretKey(config.getString("clickmaker.key.1.value"));
        clickUrlMakerV6.setTestCryptoSecretKey(config.getString("clickmaker.key.2.value"));
        clickUrlMakerV6.setImageBeaconFlag(true);// true/false
        clickUrlMakerV6.setBeaconEnabledOnSite(true);// do not know
        clickUrlMakerV6.setTestMode(false);
        clickUrlMakerV6.setRmAd(jObject.optBoolean("rich-media", false));
        clickUrlMakerV6.setRmBeaconURLPrefix(config.getString("clickmaker.beaconURLPrefix"));
        clickUrlMakerV6.setClickURLPrefix(config.getString("clickmaker.clickURLPrefix"));
        clickUrlMakerV6.setImageBeaconURLPrefix(config.getString("clickmaker.beaconURLPrefix"));
        return clickUrlMakerV6;
    }
}
