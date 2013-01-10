package com.inmobi.adserve.channels.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.inmobi.adserve.channels.api.*;
import java.util.concurrent.ConcurrentHashMap;

import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.casthrift.AdResponse;
import com.inmobi.casthrift.CasChannelLog;
import com.inmobi.casthrift.RequestParams;
import com.inmobi.casthrift.RequestTpan;
import com.inmobi.casthrift.SiteParams;
import com.inmobi.messaging.Message;
import com.inmobi.messaging.publisher.AbstractMessagePublisher;
import com.inmobi.casthrift.Ad;
import com.inmobi.casthrift.AdIdChain;
import com.inmobi.casthrift.AdMeta;
import com.inmobi.casthrift.ContentRating;
import com.inmobi.casthrift.Gender;
import com.inmobi.casthrift.Geo;
import com.inmobi.casthrift.HandsetMeta;
import com.inmobi.casthrift.InventoryType;
import com.inmobi.casthrift.PricingModel;
import com.inmobi.casthrift.AdRR;
import com.inmobi.casthrift.Impression;
import com.inmobi.casthrift.Request;
import com.inmobi.casthrift.User;

import com.inmobi.casthrift.CasAdvertisementLog;

import com.inmobi.adserve.channels.util.DebugLogger;

public class Logging {

  private static AbstractMessagePublisher dataBusPublisher;
  private static String rrLogKey;
  private static String channelLogKey;
  private static String sampledAdvertisementLogKey;
  private static boolean enableFileLogging;
  private static boolean enableDatabusLogging;
  public static ConcurrentHashMap<String, String> sampledAdvertiserLogNos = new ConcurrentHashMap<String, String>(2000);
  private static int totalCount;

  public static void init(AbstractMessagePublisher dataBusPublisher, String rrLogKey, String channelLogKey,
      String advertisementLogKey, Configuration config) {
    Logging.dataBusPublisher = dataBusPublisher;
    Logging.rrLogKey = rrLogKey;
    Logging.channelLogKey = channelLogKey;
    Logging.sampledAdvertisementLogKey = advertisementLogKey;
    enableFileLogging = config.getBoolean("enableFileLogging");
    enableDatabusLogging = config.getBoolean("enableDatabusLogging");
    totalCount = config.getInt("sampledadvertisercount");
  }

  public static JSONArray getCarrier(JSONObject jObject) {
    try {
      return (jObject.getJSONArray("carrier"));
    } catch (JSONException e) {
      return null;
    } catch (NullPointerException exception) {
      return null;
    }
  }

  public static JSONArray getHandset(JSONObject jObject) {
    try {
      return (jObject.getJSONArray("handset"));
    } catch (JSONException e) {
      return null;
    } catch (NullPointerException exception) {
      return null;
    }
  }

  // Writing rrlogs
  public static void rrLogging(ChannelSegment channelSegment, DebugLogger logger, Configuration config,
      SASRequestParameters sasParams, String terminationReason) throws JSONException, TException {
    Logger rrLogger = Logger.getLogger(config.getString("rr"));
    boolean isTerminated = false;
    if(terminationReason.equalsIgnoreCase("no"))
      isTerminated = true;
    logger.info("Obtained the handle to rr logger");
    char separator = 0x01;
    String tempParam = "";
    StringBuilder log = new StringBuilder();
    short adsServed = 0;
    String host = null;
    try {
      InetAddress addr = InetAddress.getLocalHost();
      host = addr.getHostName();

      if(host == null) {
        logger.error("host cant be empty, abandoning rr logging");
        return;
      }
      log.append("host=\"" + host + "\"");
    } catch (UnknownHostException ex) {
      logger.error("could not resolve host inside rr logging, so abandoning response");
      return;
    }

    log.append(separator + "terminated=\"" + terminationReason + "\"");
    if(logger.isDebugEnabled())
      logger.debug("is sas params null here " + (sasParams == null));

    if(null != sasParams && null != sasParams.siteId)
      log.append(separator + "rq-mk-siteid=\"" + sasParams.siteId + "\"");
    if(null != sasParams && null != sasParams.rqMkAdcount)
      log.append(separator + "rq-mk-adcount=\"" + tempParam + "\"");
    if(null != sasParams && null != sasParams.tid)
      log.append(separator + "tid=\"" + tempParam + "\"");

    String taskId = tempParam;
    InventoryType inventory = getInventoryType(sasParams);
    String timestamp = ReportTime.getTTime();
    log.append(separator + "ttime=\"" + timestamp + "\"");
    log.append(separator + "rq-src=[\"uk\",\"uk\",\"uk\",\"uk\",");
    if(null != sasParams && null != sasParams.tp)
      log.append("\"" + sasParams.tp + "\"]");
    else
      log.append("\"dir\"]");

    log.append(separator + "selectedads=[");
    AdIdChain adChain = null;
    AdMeta adMeta = null;
    Ad ad = null;
    Impression impression = null;
    if(channelSegment != null) {
      adsServed = 1;
      log.append("{\"ad\":[");
      log.append(channelSegment.channelSegmentEntity.getIncId()).append(",");
      log.append("\"\",\"");
      adChain = new AdIdChain(channelSegment.channelSegmentEntity.getAdId(),
          channelSegment.channelSegmentEntity.getAdgroupId(), channelSegment.channelSegmentEntity.getCampaignId(),
          channelSegment.channelSegmentEntity.getId(), channelSegment.channelSegmentEntity.getExternalSiteKey());
      log.append(channelSegment.channelSegmentEntity.getAdId()).append("\",\"");
      log.append(channelSegment.channelSegmentEntity.getAdgroupId()).append("\",\"");
      log.append(channelSegment.channelSegmentEntity.getCampaignId()).append("\",\"");
      log.append(channelSegment.channelSegmentEntity.getId()).append("\",\"");
      ContentRating contentRating = getContentRating(sasParams);
      PricingModel pricingModel = getPricingModel(channelSegment.channelSegmentEntity.getPricingModel());
      adMeta = new AdMeta(contentRating, pricingModel, "BANNER");
      ad = new Ad(adChain, adMeta);
      impression = new Impression(channelSegment.adNetworkInterface.getImpressionId(), ad);
      log.append(channelSegment.channelSegmentEntity.getPricingModel()).append("\",\"BANNER\", \"");
      log.append(channelSegment.channelSegmentEntity.getExternalSiteKey()).append("\"],\"impid\":\"");
      log.append(channelSegment.adNetworkInterface.getImpressionId()).append("\"");
      double winBid = channelSegment.adNetworkInterface.getBidprice();
      if(winBid != -1) {
        log.append(",\"" + "winBid" + "\":\"" + winBid + "\"");
        ad.setWinBid(winBid);
      }
      log.append("}");
    }
    log.append("]");

    JSONArray handset = null;
    JSONArray carrier = null;
    String requestSlot = null;
    String slotServed = null;
    if(null != sasParams) {
      handset = sasParams.handset;
      carrier = sasParams.carrier;
      requestSlot = sasParams.rqMkSlot;
      slotServed = sasParams.slot;
    }
    HandsetMeta handsetMeta = new HandsetMeta();
    if(null != handset)
      log.append(separator).append("handset=").append(handset);
    if(null != handset && handset.length() > 3) {
      handsetMeta.setId(handset.getInt(3));
      handsetMeta.setManufacturer(handset.getInt(2));
    } else if(null != sasParams && sasParams.osId != 0)
      handsetMeta.setOsId(sasParams.osId);
    Geo geo = null;
    if(null != carrier) {
      log.append(separator).append("carrier=").append(carrier);
      geo = new Geo(carrier.getInt(0), Integer.valueOf(carrier.getInt(1)).shortValue());
      if(carrier.length() >= 4 && carrier.get(3) != null)
        geo.setRegion(carrier.getInt(3));
      if(carrier.length() >= 5 && carrier.get(4) != null)
        geo.setCity(carrier.getInt(4));
    }

    short slotRequested = -1;
    if(null != requestSlot) {
      log.append(separator).append("rq-mk-ad-slot=\"").append(requestSlot).append("\"");
      if(requestSlot.matches("^\\d+$"))
        slotRequested = Integer.valueOf(requestSlot).shortValue();
      else
        logger.error("wrong value for request slot is " + requestSlot);
    }

    if(null != slotServed) {
      log.append(separator).append("slot-served=").append(slotServed);
    }

    User user = new User();
    log.append(separator + "uparams={");
    if(null != sasParams) {
      if(null != sasParams.age) {
        log.append("\"u-age\":\"").append(sasParams.age).append("\",");
        if(sasParams.age.matches("^\\d+$"))
          user.setAge(Short.valueOf(sasParams.age));
      }
      if(null != sasParams.gender) {
        log.append("\"u-gender\":\"").append(sasParams.gender).append("\",");
        user.setGender(getGender(sasParams));
      }
      if(null != sasParams.genderOrig)
        log.append("\"u-gender-orig\":\"").append(sasParams.genderOrig).append("\",");
      if(null != sasParams.uid) {
        log.append("\"u-id\":\"").append(sasParams.uid).append("\",");
        user.setId(sasParams.uid);
      }
      if(null != sasParams.userLocation)
        log.append("\"u-location\":\"").append(sasParams.userLocation).append("\",");
      if(null != sasParams.postalCode)
        log.append("\"u-postalcode\":\"").append(sasParams.postalCode).append("\"");
    }
    if(log.charAt(log.length() - 1) == ',')
      log.deleteCharAt(log.length() - 1);
    log.append("}").append(separator).append("u-id-params=");
    if(null != sasParams && null != sasParams.uidParams)
      log.append(sasParams.uidParams);
    else
      log.append("{}");
    
    if (null != sasParams && null != sasParams.siteSegmentId)
      log.append(separator).append("sel_seg_id=\"").append(sasParams.siteSegmentId).append("\"");
    
    if (logger.isDebugEnabled())
      logger.debug("finally writing to rr log" + log.toString());
    
    if(enableFileLogging)
      rrLogger.info(log.toString());
    else
      logger.debug("file logging is not enabled");
    short adRequested = 1;
    Request request = new Request(adRequested, adsServed, sasParams == null ? null : sasParams.siteId, taskId);
    if(slotServed != null)
      request.setSlot_served(Integer.valueOf(slotServed).shortValue());
    request.setIP(geo);
    request.setHandset(handsetMeta);
    request.setInventory(inventory);
    request.setUser(user);
    if(requestSlot != null)
      request.setSlot_requested(slotRequested);
    if (null != sasParams && null != sasParams.siteSegmentId)
      request.setSegmentId(sasParams.siteSegmentId);
    
    List<Impression> impressions = null;
    if(null != impression) {
      impressions = new ArrayList<Impression>();
      impressions.add(impression);
    }
    AdRR adRR = new AdRR(host, timestamp, request, impressions, isTerminated, terminationReason);
    if(enableDatabusLogging) {
      TSerializer tSerializer = new TSerializer(new TBinaryProtocol.Factory());
      Message msg = new Message(tSerializer.serialize(adRR));
      dataBusPublisher.publish(rrLogKey, msg);
    }
  }

  // Write Channel Logs
  public static void channelLogline(List<ChannelSegment> rankList, String clickUrl, DebugLogger logger,
      Configuration config, SASRequestParameters sasParams, long totalTime) throws JSONException, TException {
    logger.debug("came inside channel log line");
    Logger debugLogger = Logger.getLogger(config.getString("channel"));
    logger.debug("got logger handle for cas logs");
    char sep = 0x01;
    StringBuilder log = new StringBuilder();
    log.append("trtt=").append(totalTime);
    InspectorStats.incrementStatCount(InspectorStrings.latency, totalTime);
    if(null != sasParams && sasParams.siteId != null)
      log.append(sep + "rq-mk-siteid=\"").append(sasParams.siteId).append("\"");

    String timestamp = ReportTime.getUTCTimestamp();
    log.append(sep).append("ttime=\"").append(timestamp).append("\"");
    if(null != sasParams && sasParams.tid != null)
      log.append(sep).append("tid=\"").append(sasParams.tid).append("\"");
    if(clickUrl != null)
      log.append(sep + "clurl=\"" + clickUrl + "\"");
    log.append(sep).append("rq-tpan=[");
    logger.debug("sasparams not null here");

    List<AdResponse> responseList = new ArrayList<AdResponse>();

    // Writing inspector stats and getting log line from adapters
    for (int index = 0; rankList != null && index < rankList.size(); index++) {
      JSONObject logLine = null;
      ThirdPartyAdResponse adResponse = ((ChannelSegment) rankList.get(index)).adNetworkInterface.getResponseStruct();
      try {
        InspectorStats.incrementStatCount(((ChannelSegment) rankList.get(index)).adNetworkInterface.getName(),
            InspectorStrings.totalRequests);
        InspectorStats.incrementStatCount(((ChannelSegment) rankList.get(index)).adNetworkInterface.getName(),
            InspectorStrings.latency, adResponse.latency);
        if(adResponse.adStatus.equals("AD"))
          InspectorStats.incrementStatCount(((ChannelSegment) rankList.get(index)).adNetworkInterface.getName(),
              InspectorStrings.totalFills);
        else if(adResponse.adStatus.equals("NO_AD"))
          InspectorStats.incrementStatCount(((ChannelSegment) rankList.get(index)).adNetworkInterface.getName(),
              InspectorStrings.totalNoFills);
        else if(adResponse.adStatus.equals("TIME_OUT"))
          InspectorStats.incrementStatCount(((ChannelSegment) rankList.get(index)).adNetworkInterface.getName(),
              InspectorStrings.totalTimeout);
        else
          InspectorStats.incrementStatCount(((ChannelSegment) rankList.get(index)).adNetworkInterface.getName(),
              InspectorStrings.totalTerminate);
        logLine = new JSONObject();
        String advertiserId = ((ChannelSegment) rankList.get(index)).adNetworkInterface.getId();
        String externalSiteKey = ((ChannelSegment) rankList.get(index)).channelSegmentEntity.getExternalSiteKey();
        double bid = ((ChannelSegment) rankList.get(index)).adNetworkInterface.getBidprice();
        String resp = adResponse.adStatus;
        long latency = adResponse.latency;
        logLine.put("adv", advertiserId);
        logLine.put("3psiteid", externalSiteKey);
        logLine.put("resp", resp);
        logLine.put("latency", adResponse.latency);
        AdResponse response = new AdResponse(advertiserId, externalSiteKey, resp, latency);
        if(bid != -1) {
          logLine.put("bid", bid);
          response.setBid(bid);
        }
        responseList.add(response);

      } catch (JSONException exception) {
        logger.error("error reading channel log line from the adapters");
      } catch (NullPointerException exception) {
        logger.error("error reading channel log line from the adapters " + exception.getMessage());
      }

      if(logLine != null) {
        log.append(logLine);
        if(index != rankList.size() - 1)
          log.append(",");
      }
      if(index == rankList.size() - 1)
        log.append("]").append(sep);
    }
    if(rankList == null || rankList.size() == 0)
      log.append("]").append(sep);

    // Type collectionType = new TypeToken<Collection<Integer>>(){}.getType();
    // Gson gson = new Gson();
    // List<Integer> category = gson.fromJson(getCategories(jObject, logger),
    // ArrayList.class);
    ContentRating siteType = getContentRating(sasParams);

    List<Integer> categ = null;
    if(null != sasParams && sasParams.categories != null) {
      categ = new ArrayList<Integer>();
      for (long cat : sasParams.categories) {
        categ.add((int) cat);
      }
    }

    SiteParams siteParams = new SiteParams(categ, siteType);
    RequestParams requestParams = (sasParams == null? null : new RequestParams(sasParams.remoteHostIp, sasParams.source, sasParams.userAgent));

    if(null != sasParams && null != sasParams.remoteHostIp)
      log.append("rq-params={\"host\":\"").append(sasParams.remoteHostIp).append("\"");
    JSONArray carrier = null;
    if(null != sasParams ) {
    if(sasParams.source != null)
      log.append(",\"src\":\"").append(sasParams.source).append("\"");
    log.append("}").append(sep).append("rq-h-user-agent=\"");
    log.append(sasParams.userAgent).append("\"").append(sep).append("rq-site-params=[{\"categ\":");
    log.append(sasParams.categories.toString()).append("},{\"type\":\"" + sasParams.siteType + "\"}]");
    carrier = sasParams.carrier;
    }
     
    Geo geo = null;
    if(null != carrier) {
      log.append(sep).append("carrier=").append(carrier);
      geo = new Geo(carrier.getInt(0), Integer.valueOf(carrier.getInt(1)).shortValue());
      if(carrier.length() >= 4 && carrier.get(3) != null)
        geo.setRegion(carrier.getInt(3));
      if(carrier.length() >= 5 && carrier.get(4) != null)
        geo.setCity(carrier.getInt(4));
    }

    logger.debug("finished writing cas logs");
    logger.debug(log.toString());
    if(enableFileLogging)
      debugLogger.info(log.toString());
    CasChannelLog channelLog = new CasChannelLog(totalTime, clickUrl, sasParams == null ? null : sasParams.siteId, new RequestTpan(responseList),
        siteParams, requestParams, timestamp);
    if(null != geo)
      channelLog.setIP(geo);
    if(enableDatabusLogging) {
      TSerializer tSerializer = new TSerializer(new TBinaryProtocol.Factory());
      Message msg = new Message(tSerializer.serialize(channelLog));
      dataBusPublisher.publish(channelLogKey, msg);
    }
  }

  public static void advertiserLogging(List<ChannelSegment> rankList, DebugLogger logger, Configuration config) {
    logger.debug("came inside advertiser log");
    Logger advertiserLogger = Logger.getLogger(config.getString("advertiser"));
    if(!advertiserLogger.isDebugEnabled())
      return;
    char sep = 0x01;
    StringBuilder log = new StringBuilder();
    logger.debug("got logger handle for advertiser logs");
    for (int index = 0; rankList != null && index < rankList.size(); index++) {
      AdNetworkInterface adNetworkInterface = ((ChannelSegment) rankList.get(index)).adNetworkInterface;
      ThirdPartyAdResponse adResponse = adNetworkInterface.getResponseStruct();
      String partnerName = adNetworkInterface.getName();
      log.append(partnerName);
      log.append(sep).append(adResponse.adStatus);
      String response = "";
      String requestUrl = "";
      if(adResponse.adStatus.equalsIgnoreCase("AD")) {
        response = adNetworkInterface.getHttpResponseContent();
        log.append(sep).append(response);
      }
      if(!adNetworkInterface.getRequestUrl().equals("")) {
        requestUrl = adNetworkInterface.getRequestUrl();
        log.append(sep).append(requestUrl);
      }
      if(index != rankList.size() - 1)
        log.append("\n");
    }
    if(enableFileLogging && log.length() > 0) {
      advertiserLogger.debug(log);
      logger.debug("done with advertiser logging");
    }
  }

  public static void sampledAdvertiserLogging(List<ChannelSegment> rankList, DebugLogger logger, Configuration config) {
    logger.debug("came inside sampledAdvertiser log");
    Logger sampledAdvertiserLogger = Logger.getLogger(config.getString("sampledadvertiser"));
    if(!sampledAdvertiserLogger.isDebugEnabled())
      return;
    char sep = 0x01;
    StringBuilder log = new StringBuilder();
    logger.debug("got logger handle for sampledAdvertiser logs");

    for (int index = 0; rankList != null && index < rankList.size(); index++) {
      AdNetworkInterface adNetworkInterface = ((ChannelSegment) rankList.get(index)).adNetworkInterface;
      ThirdPartyAdResponse adResponse = adNetworkInterface.getResponseStruct();
      String adstatus = adResponse.adStatus;
      if(!adstatus.equalsIgnoreCase("AD"))
        continue;
      String partnerName = adNetworkInterface.getName();
      String extsiteKey = rankList.get(index).channelSegmentEntity.getExternalSiteKey();
      String advertiserId = rankList.get(index).channelSegmentEntity.getId();
      String requestUrl = "";
      String response = "";
      if(sampledAdvertiserLogNos.get(partnerName + extsiteKey) == null) {
        sampledAdvertiserLogNos.put(partnerName + extsiteKey, System.currentTimeMillis() + "_" + 0);
      }
      Long time = Long.parseLong(sampledAdvertiserLogNos.get(partnerName + extsiteKey).split("_")[0]);
      Integer count = Integer.parseInt(sampledAdvertiserLogNos.get(partnerName + extsiteKey).split("_")[1]);

      if(System.currentTimeMillis() - time < 3600000) {
        if(count < totalCount) {
          requestUrl = adNetworkInterface.getRequestUrl();
          response = adNetworkInterface.getHttpResponseContent();
          if(requestUrl.equals("") || response.equals(""))
            continue;
          if(index > 0 && partnerName.length() > 0 && log.length() > 0)
            log.append("\n");
          log.append(partnerName).append(sep).append(rankList.get(index).channelSegmentEntity.getExternalSiteKey());
          log.append(sep).append(requestUrl).append(sep).append(adResponse.adStatus);
          log.append(sep).append(response).append(sep).append(advertiserId);
          count++;
          sampledAdvertiserLogNos.put(partnerName + extsiteKey, time + "_" + count);
        }
      } else {
        logger.debug("creating new sampledadvertiser logs");
        time = System.currentTimeMillis();
        count = 0;
        sampledAdvertiserLogNos.put(partnerName + extsiteKey, System.currentTimeMillis() + "_" + 0);
        time = Long.parseLong(sampledAdvertiserLogNos.get(partnerName + extsiteKey).split("_")[0]);
        count = Integer.parseInt(sampledAdvertiserLogNos.get(partnerName + extsiteKey).split("_")[1]);
        requestUrl = adNetworkInterface.getRequestUrl();
        response = adNetworkInterface.getHttpResponseContent();
        if(requestUrl.equals("") || response.equals("")) {
          continue;
        }
        if(index > 0 && partnerName.length() > 0 && log.length() > 0)
          log.append("\n");
        log.append(partnerName).append(sep).append(rankList.get(index).channelSegmentEntity.getExternalSiteKey());
        log.append(sep).append(requestUrl).append(sep).append(adResponse.adStatus);
        log.append(sep).append(response).append(sep).append(advertiserId);
        count++;
        sampledAdvertiserLogNos.put(partnerName + extsiteKey, time + "_" + count);
      }
      if(enableDatabusLogging) {
        if(count >= totalCount)
          continue;
        CasAdvertisementLog casAdvertisementLog = new CasAdvertisementLog(partnerName, requestUrl, response, adstatus,
            extsiteKey, advertiserId);
        Message msg = null;
        try {
          TSerializer tSerializer = new TSerializer(new TBinaryProtocol.Factory());
          msg = new Message(tSerializer.serialize(casAdvertisementLog));
        } catch (TException e) {
          logger.debug("Error while creating sampledAdvertiser logs for databus ");
          e.printStackTrace();
        }
        if(null != msg) {
          dataBusPublisher.publish(sampledAdvertisementLogKey, msg);
        } else {
          logger.debug("In sampledAdvertiser log: log msg is null");
        }
      } else {
        logger.debug("In sampledAdvertiser log: enableDatabusLogging is false ");
      }
    }
    if(enableFileLogging && log.length() > 0) {
      sampledAdvertiserLogger.debug(log);
      logger.debug("done with sampledAdvertiser logging");
    }
  }

  public static ContentRating getContentRating(SASRequestParameters sasParams) {
    if(sasParams == null)
      return null;
    if(sasParams.siteType == null)
      return null;
    if(sasParams.siteType.equalsIgnoreCase("performance"))
      return ContentRating.PERFORMANCE;
    if(sasParams.siteType.equalsIgnoreCase("FAMILY_SAFE"))
      return ContentRating.FAMILY_SAFE;
    if(sasParams.siteType.equalsIgnoreCase("MATURE"))
      return ContentRating.MATURE;
    else
      return null;
  }

  public static PricingModel getPricingModel(String pricingModel) {
    if(pricingModel == null)
      return null;
    if(pricingModel.equalsIgnoreCase("cpc"))
      return PricingModel.CPC;
    if(pricingModel.equalsIgnoreCase("cpm"))
      return PricingModel.CPM;
    else
      return null;
  }

  public static InventoryType getInventoryType(SASRequestParameters sasParams) {
    if(null != sasParams && sasParams.sdkVersion != null && sasParams.sdkVersion.equalsIgnoreCase("0"))
      return InventoryType.BROWSER;
    return InventoryType.APP;
  }

  public static Gender getGender(SASRequestParameters sasParams) {
    if(sasParams ==  null) 
      return null;
    if(sasParams.gender.equalsIgnoreCase("m"))
      return Gender.MALE;
    else
      return Gender.FEMALE;
  }
}
