package com.inmobi.adserve.channels.server.requesthandler;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.MetricsManager;
import com.inmobi.casthrift.*;
import com.inmobi.messaging.Message;
import com.inmobi.messaging.publisher.AbstractMessagePublisher;
import org.apache.commons.configuration.Configuration;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class Logging {

    private static AbstractMessagePublisher                dataBusPublisher;
    private static String                                  rrLogKey;
    private static String                                  sampledAdvertisementLogKey;
    private static boolean                                 enableFileLogging;
    private static boolean                                 enableDatabusLogging;
    private final static ConcurrentHashMap<String, String> sampledAdvertiserLogNos = new ConcurrentHashMap<String, String>(
                                                                                           2000);

    public static ConcurrentHashMap<String, String> getSampledadvertiserlognos() {
        return sampledAdvertiserLogNos;
    }

    private static int totalCount;

    public static void init(AbstractMessagePublisher dataBusPublisher, String rrLogKey, String advertisementLogKey,
            Configuration config) {
        Logging.dataBusPublisher = dataBusPublisher;
        Logging.rrLogKey = rrLogKey;
        Logging.sampledAdvertisementLogKey = advertisementLogKey;
        enableFileLogging = config.getBoolean("enableFileLogging");
        enableDatabusLogging = config.getBoolean("enableDatabusLogging");
        totalCount = config.getInt("sampledadvertisercount");
    }

    // Writing rrlogs
    public static void rrLogging(ChannelSegment channelSegment, List<ChannelSegment> rankList, DebugLogger logger,
            SASRequestParameters sasParams, String terminationReason, long totalTime) throws JSONException, TException {
        InspectorStats.incrementStatCount(InspectorStrings.latency, totalTime);
        boolean isTerminated = false;
        if (terminationReason.equalsIgnoreCase("no")) {
            isTerminated = true;
        }
        short adsServed = 0;
        String host;
        try {
            InetAddress addr = InetAddress.getLocalHost();
            host = addr.getHostName();
            if (host == null) {
                logger.info("host cant be empty, abandoning rr logging");
                return;
            }
        }
        catch (UnknownHostException ex) {
            logger.info("could not resolve host inside rr logging, so abandoning response");
            return;
        }
        InventoryType inventory = getInventoryType(sasParams);
        String timestamp = ReportTime.getTTime();
        AdIdChain adChain;
        AdMeta adMeta;
        Ad ad;
        Impression impression = null;
        boolean isServerImpression = false;
        String advertiserId = null;
        if (channelSegment != null) {
            InspectorStats.incrementStatCount(channelSegment.getAdNetworkInterface().getName(),
                InspectorStrings.serverImpression);
            isServerImpression = true;
            advertiserId = channelSegment.getChannelEntity().getAccountId();
            adsServed = 1;
            ChannelSegmentEntity channelSegmentEntity = channelSegment.getChannelSegmentEntity();
            adChain = new AdIdChain(channelSegmentEntity.getAdId(), channelSegmentEntity.getAdgroupId(),
                    channelSegmentEntity.getCampaignId(), channelSegmentEntity.getAdvertiserId(),
                    channelSegmentEntity.getExternalSiteKey());
            ContentRating contentRating = getContentRating(sasParams);
            PricingModel pricingModel = getPricingModel(channelSegmentEntity.getPricingModel());
            adMeta = new AdMeta(contentRating, pricingModel, "BANNER");
            ad = new Ad(adChain, adMeta);
            impression = new Impression(channelSegment.getAdNetworkInterface().getImpressionId(), ad);
            impression.setAdChain(createCasAdChain(channelSegment));
            double winBid = channelSegment.getAdNetworkInterface().getSecondBidPriceInUsd();
            if (winBid != -1) {
                ad.setWinBid(winBid);
            }
        }
        JSONArray handset = null;
        JSONArray carrier = null;
        String requestSlot = null;
        String slotServed = null;
        if (null != sasParams) {
            handset = sasParams.getHandset();
            carrier = sasParams.getCarrier();
            requestSlot = sasParams.getRqMkSlot();
            slotServed = sasParams.getSlot();
        }
        HandsetMeta handsetMeta = new HandsetMeta();
        if (null != handset && handset.length() > 0) {
            handsetMeta.setId(handset.getInt(0));
        }
        if (null != sasParams && sasParams.getOsId() != 0) {
            handsetMeta.setOsId(sasParams.getOsId());
        }
        Geo geo = null;
        if (null != carrier) {
            geo = new Geo(carrier.getInt(0), Integer.valueOf(carrier.getInt(1)).shortValue());
            if (carrier.length() >= 4 && carrier.get(3) != null) {
                geo.setRegion(carrier.getInt(3));
            }
            if (carrier.length() >= 5 && carrier.get(4) != null) {
                geo.setCity(carrier.getInt(4));
            }
        }

        short slotRequested = -1;
        if (null != requestSlot) {
            if (requestSlot.matches("^\\d+$")) {
                slotRequested = Integer.valueOf(requestSlot).shortValue();
            }
            else {
                logger.info("wrong value for request slot is", requestSlot);
            }
        }

        User user = new User();
        if (null != sasParams) {
            if (null != sasParams.getAge()) {
                if (sasParams.getAge().matches("^\\d+$")) {
                    try {
                        user.setAge(Short.valueOf(sasParams.getAge()));
                    }
                    catch (NumberFormatException e) {
                        logger.info("Exception in casting age from string to Short", e);
                    }
                }
            }
            if (null != sasParams.getGender()) {
                user.setGender(getGender(sasParams));
            }
            if (null != sasParams.getUid()) {
                user.setId(sasParams.getUid());
            }
        }
        short adRequested = 1;
        Request request = new Request(adRequested, adsServed, sasParams == null ? null : sasParams.getSiteId(),
                sasParams == null ? null : sasParams.getTid());
        if (slotServed != null) {
            request.setSlot_served(Integer.valueOf(slotServed).shortValue());
        }
        request.setIP(geo);
        request.setHandset(handsetMeta);
        request.setInventory(inventory);
        request.setUser(user);
        if (requestSlot != null) {
            request.setSlot_requested(slotRequested);
        }
        if (null != sasParams && null != sasParams.getSiteSegmentId()) {
            request.setSegmentId(sasParams.getSiteSegmentId());
        }

        List<Impression> impressions = null;
        if (null != impression) {
            impressions = new ArrayList<Impression>();
            impressions.add(impression);
        }
        AdRR adRR = new AdRR(host, timestamp, request, impressions, isTerminated, terminationReason);
        List<Channel> channels = createChannelsLog(rankList);
        adRR.setChannels(channels);
        if (enableDatabusLogging) {
            TSerializer tSerializer = new TSerializer(new TBinaryProtocol.Factory());
            Message msg = new Message(tSerializer.serialize(adRR));
            dataBusPublisher.publish(rrLogKey, msg);
            logger.debug("ADRR is: ", adRR);
        }
        // Logging real time stats for graphite
        String osName = "";
        try {
            if (null != sasParams && null != advertiserId && null != impression && null != impression.getAd()) {
                Integer sasParamsOsId = sasParams.getOsId();
                if (sasParamsOsId > 0 && sasParamsOsId < 21) {
                    osName = HandSetOS.values()[sasParamsOsId - 1].toString();
                }
                MetricsManager.updateStats(Integer.parseInt(sasParams.getCountryStr()), sasParams.getCountry(),
                    sasParams.getOsId(), osName, Filters.getAdvertiserIdToNameMapping().get(advertiserId), false,
                    false, isServerImpression, 0.0, (long) 0.0, impression.getAd().getWinBid());
            }
        }
        catch (Exception e) {
            logger.info("error while writing to graphite in rrLog", e);
        }
    }

    public static List<Channel> createChannelsLog(List<ChannelSegment> rankList) {
        if (null == rankList) {
            return new ArrayList<Channel>();
        }
        List<Channel> channels = new ArrayList<Channel>();
        for (ChannelSegment channelSegment : rankList) {
            Channel channel = new Channel();
            channel.setAdStatus(getAdStatus(channelSegment.getAdNetworkInterface().getAdStatus()));
            channel.setLatency(channelSegment.getAdNetworkInterface().getLatency());
            channel.setAdChain(createCasAdChain(channelSegment));
            double bid = channelSegment.getAdNetworkInterface().getBidPriceInUsd();
            if (bid > 0) {
                channel.setBid(bid);
            }
            channels.add(channel);
            // Incrementing inspectors
            AdNetworkInterface adNetwork = channelSegment.getAdNetworkInterface();
            ThirdPartyAdResponse adResponse = adNetwork.getResponseStruct();
            InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.totalRequests);
            InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.latency, adResponse.latency);
            InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.connectionLatency,
                adNetwork.getConnectionLatency());
            if ("AD".equals(adResponse.adStatus)) {
                InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.totalFills);
            }
            else if ("NO_AD".equals(adResponse.adStatus)) {
                InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.totalNoFills);
            }
            else if ("TIME_OUT".equals(adResponse.adStatus)) {
                InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.totalTimeout);
            }
            else {
                InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.totalTerminate);
            }
        }
        return channels;
    }

    public static CasAdChain createCasAdChain(ChannelSegment channelSegment) {
        CasAdChain casAdChain = new CasAdChain();
        casAdChain.setAdvertiserId(channelSegment.getChannelEntity().getAccountId());
        casAdChain.setCampaign_inc_id(channelSegment.getChannelSegmentEntity().getCampaignIncId());
        casAdChain.setAdgroup_inc_id(channelSegment.getChannelSegmentEntity().getAdgroupIncId());
        casAdChain.setExternalSiteKey(channelSegment.getChannelSegmentEntity().getExternalSiteKey());
        casAdChain.setDst(DemandSourceType.findByValue(channelSegment.getChannelSegmentEntity().getDst()));
        return casAdChain;
    }

    public static AdStatus getAdStatus(String adStatus) {
        if ("AD".equalsIgnoreCase(adStatus)) {
            return AdStatus.AD;
        }
        else if ("NO_AD".equals(adStatus)) {
            return AdStatus.NO_AD;
        }
        else if ("TIME_OUT".equals(adStatus)) {
            return AdStatus.TIME_OUT;
        }
        return AdStatus.DROPPED;
    }

    public static void advertiserLogging(List<ChannelSegment> rankList, DebugLogger logger, Configuration config) {
        logger.debug("came inside advertiser log");
        Logger advertiserLogger = LoggerFactory.getLogger(config.getString("advertiser"));
        if (!advertiserLogger.isDebugEnabled()) {
            return;
        }
        char sep = 0x01;
        StringBuilder log = new StringBuilder();
        logger.debug("got logger handle for advertiser logs");
        for (int index = 0; rankList != null && index < rankList.size(); index++) {
            AdNetworkInterface adNetworkInterface = rankList.get(index).getAdNetworkInterface();
            ThirdPartyAdResponse adResponse = adNetworkInterface.getResponseStruct();
            String partnerName = adNetworkInterface.getName();
            log.append(partnerName);
            log.append(sep).append(adResponse.adStatus);
            String response = "";
            String requestUrl = "";
            if (adResponse.adStatus.equalsIgnoreCase("AD")) {
                response = adNetworkInterface.getHttpResponseContent();
                log.append(sep).append(response);
            }
            if (!adNetworkInterface.getRequestUrl().equals("")) {
                requestUrl = adNetworkInterface.getRequestUrl();
                log.append(sep).append(requestUrl);
            }
            if (index != rankList.size() - 1) {
                log.append("\n");
            }
        }
        if (enableFileLogging && log.length() > 0) {
            advertiserLogger.debug(log.toString());
            logger.debug("done with advertiser logging");
        }
    }

    public static void sampledAdvertiserLogging(List<ChannelSegment> rankList, DebugLogger logger, Configuration config) {
        logger.debug("came inside sampledAdvertiser log");
        Logger sampledAdvertiserLogger = LoggerFactory.getLogger(config.getString("sampledadvertiser"));
        if (!sampledAdvertiserLogger.isDebugEnabled()) {
            return;
        }
        char sep = 0x01;
        StringBuilder log = new StringBuilder();
        logger.debug("got logger handle for sampledAdvertiser logs");

        for (int index = 0; rankList != null && index < rankList.size(); index++) {
            AdNetworkInterface adNetworkInterface = ((ChannelSegment) rankList.get(index)).getAdNetworkInterface();
            ThirdPartyAdResponse adResponse = adNetworkInterface.getResponseStruct();
            String adstatus = adResponse.adStatus;
            if (!adstatus.equalsIgnoreCase("AD")) {
                continue;
            }
            String partnerName = adNetworkInterface.getName();
            String extsiteKey = rankList.get(index).getChannelSegmentEntity().getExternalSiteKey();
            String advertiserId = rankList.get(index).getChannelSegmentEntity().getAdvertiserId();
            String requestUrl = "";
            String response = "";
            if (sampledAdvertiserLogNos.get(partnerName + extsiteKey) == null) {
                String value = System.currentTimeMillis() + "_" + 0;
                sampledAdvertiserLogNos.put(partnerName + extsiteKey, value);
            }
            Long time = Long.parseLong(sampledAdvertiserLogNos.get(partnerName + extsiteKey).split("_")[0]);
            Integer count = Integer.parseInt(sampledAdvertiserLogNos.get(partnerName + extsiteKey).split("_")[1]);

            if (System.currentTimeMillis() - time < 3600000) {
                if (count < totalCount) {
                    requestUrl = adNetworkInterface.getRequestUrl();
                    response = adNetworkInterface.getHttpResponseContent();
                    if (requestUrl.equals("") || response.equals("")) {
                        continue;
                    }
                    if (index > 0 && partnerName.length() > 0 && log.length() > 0) {
                        log.append("\n");
                    }
                    log.append(partnerName)
                                .append(sep)
                                .append(rankList.get(index).getChannelSegmentEntity().getExternalSiteKey());
                    log.append(sep).append(requestUrl).append(sep).append(adResponse.adStatus);
                    log.append(sep).append(response).append(sep).append(advertiserId);
                    count++;
                    sampledAdvertiserLogNos.put(partnerName + extsiteKey, time + "_" + count);
                }
            }
            else {
                logger.debug("creating new sampledadvertiser logs");
                count = 0;
                sampledAdvertiserLogNos.put(partnerName + extsiteKey, System.currentTimeMillis() + "_" + 0);
                time = Long.parseLong(sampledAdvertiserLogNos.get(partnerName + extsiteKey).split("_")[0]);
                count = Integer.parseInt(sampledAdvertiserLogNos.get(partnerName + extsiteKey).split("_")[1]);
                requestUrl = adNetworkInterface.getRequestUrl();
                response = adNetworkInterface.getHttpResponseContent();
                if (requestUrl.equals("") || response.equals("")) {
                    continue;
                }
                if (index > 0 && partnerName.length() > 0 && log.length() > 0) {
                    log.append("\n");
                }
                log.append(partnerName)
                            .append(sep)
                            .append(rankList.get(index).getChannelSegmentEntity().getExternalSiteKey());
                log.append(sep).append(requestUrl).append(sep).append(adResponse.adStatus);
                log.append(sep).append(response).append(sep).append(advertiserId);
                count++;
                sampledAdvertiserLogNos.put(partnerName + extsiteKey, time + "_" + count);
            }
            if (enableDatabusLogging) {
                if (count >= totalCount) {
                    continue;
                }
                CasAdvertisementLog casAdvertisementLog = new CasAdvertisementLog(partnerName, requestUrl, response,
                        adstatus, extsiteKey, advertiserId);
                Message msg = null;
                try {
                    TSerializer tSerializer = new TSerializer(new TBinaryProtocol.Factory());
                    msg = new Message(tSerializer.serialize(casAdvertisementLog));
                }
                catch (TException e) {
                    logger.debug("Error while creating sampledAdvertiser logs for databus ");
                }
                if (null != msg) {
                    dataBusPublisher.publish(sampledAdvertisementLogKey, msg);
                }
            }
        }
        if (enableFileLogging && log.length() > 0) {
            sampledAdvertiserLogger.debug(log.toString());
            logger.debug("done with sampledAdvertiser logging");
        }
    }

    public static ContentRating getContentRating(SASRequestParameters sasParams) {
        if (sasParams == null || null == sasParams.getSiteType()) {
            return null;
        }
        else if (sasParams.getSiteType().equalsIgnoreCase("performance")) {
            return ContentRating.PERFORMANCE;
        }
        else if (sasParams.getSiteType().equalsIgnoreCase("FAMILY_SAFE")) {
            return ContentRating.FAMILY_SAFE;
        }
        else if (sasParams.getSiteType().equalsIgnoreCase("MATURE")) {
            return ContentRating.MATURE;
        }
        return null;
    }

    public static PricingModel getPricingModel(String pricingModel) {
        if (pricingModel == null) {
            return null;
        }
        else if (pricingModel.equalsIgnoreCase("cpc")) {
            return PricingModel.CPC;
        }
        else if (pricingModel.equalsIgnoreCase("cpm")) {
            return PricingModel.CPM;
        }
        return null;
    }

    public static InventoryType getInventoryType(SASRequestParameters sasParams) {
        if (null != sasParams && sasParams.getSdkVersion() != null && sasParams.getSdkVersion().equalsIgnoreCase("0")) {
            return InventoryType.BROWSER;
        }
        return InventoryType.APP;
    }

    public static Gender getGender(SASRequestParameters sasParams) {
        if (sasParams == null) {
            return null;
        }
        else if (sasParams.getGender().equalsIgnoreCase("m")) {
            return Gender.MALE;
        }
        return Gender.FEMALE;
    }
}
