package com.inmobi.adserve.channels.server;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.entity.*;
import com.inmobi.adserve.channels.repository.*;
import com.inmobi.adserve.channels.util.DebugLogger;
import junit.framework.TestCase;

public class FilterTest extends TestCase {

  Configuration mockConfig;
  Configuration mockAdapterConfig;
  private DebugLogger logger;
  private ChannelEntity cE1;
  private ChannelEntity cE2;
  private ChannelEntity cE3;
  private ChannelFeedbackEntity cFE1;
  private ChannelFeedbackEntity cFE2;
  private ChannelFeedbackEntity cFE3;
  private ChannelSegmentFeedbackEntity cSFE1;
  private ChannelSegmentFeedbackEntity cSFE2;
  private ChannelSegmentFeedbackEntity cSFE3;
  private ChannelSegmentFeedbackEntity cSFE4;
  private ChannelSegmentFeedbackEntity cSFE5;
  private ChannelSegmentFeedbackEntity cSFE6;

  public void setUp() throws IOException {
    mockConfig = createMock(Configuration.class);
    mockAdapterConfig = createMock(Configuration.class);

    HashMap<String, String> temp = new HashMap<String, String>();
    temp.put("openx.advertiserId", "");
    temp.put("openx.partnerSegmentNo", "");
    temp.put("atnt.advertiserId", "");
    temp.put("atnt.partnerSegmentNo", "");
    temp.put("tapit.advertiserId", "");
    temp.put("tapit.partnerSegmentNo", "");
    temp.put("mullahmedia.advertiserId", "");
    Iterator<String> itr = temp.keySet().iterator();

    cE1 = new ChannelEntity();
    cE1.setId("advertiserId1").setPriority(1).setImpressionCeil(90).setName("name1");
    cE2 = new ChannelEntity();
    cE2.setId("advertiserId2").setPriority(1).setImpressionCeil(90).setName("name2");
    cE3 = new ChannelEntity();
    cE3.setId("advertiserId3").setPriority(1).setImpressionCeil(90).setName("name3");
    cFE1 = new ChannelFeedbackEntity("advertiserId1", 100.0, 50.0, 50.0, 100, 95, 1.0, 4.0);
    cFE2 = new ChannelFeedbackEntity("advertiserId2", 100.0, 95.0, 5.0, 100, 55, 2.0, 0.6);
    cFE3 = new ChannelFeedbackEntity("advertiserId2", 100.0, 50.0, 50.0, 100, 55, 1.0, 4.0);
    cSFE1 = new ChannelSegmentFeedbackEntity("advertiserId1", "adgroupId1", 0.29, 0.1, 0, 0, 0, 0);
    cSFE2 = new ChannelSegmentFeedbackEntity("advertiserId1", "adgroupId2", 0.9, 0.1, 0, 0, 0, 0);
    cSFE3 = new ChannelSegmentFeedbackEntity("advertiserId1", "adgroupId3", 0.4, 0.1, 0, 0, 0, 0);
    cSFE4 = new ChannelSegmentFeedbackEntity("advertiserId2", "adgroupId4", 0.2, 0.1, 0, 0, 0, 0);
    cSFE5 = new ChannelSegmentFeedbackEntity("advertiserId2", "adgroupId5", 0.5, 0.1, 0, 0, 0, 0);
    cSFE6 = new ChannelSegmentFeedbackEntity("advertiserId3", "adgroupId6", 0.7, 0.1, 0, 0, 0, 0);

    expect(mockAdapterConfig.getKeys()).andReturn(itr).anyTimes();
    expect(mockAdapterConfig.getString("openx.advertiserId")).andReturn("advertiserId1").anyTimes();
    expect(mockAdapterConfig.getString("atnt.advertiserId")).andReturn("advertiserId2").anyTimes();
    expect(mockAdapterConfig.getString("tapit.advertiserId")).andReturn("advertiserId3").anyTimes();
    expect(mockAdapterConfig.getString("mullahmedia.advertiserId")).andReturn("advertiserId4").anyTimes();
    expect(mockAdapterConfig.getInt("openx.partnerSegmentNo", 2)).andReturn(2).anyTimes();
    expect(mockAdapterConfig.getInt("atnt.partnerSegmentNo", 2)).andReturn(2).anyTimes();
    expect(mockAdapterConfig.getInt("tapit.partnerSegmentNo", 2)).andReturn(2).anyTimes();
    expect(mockAdapterConfig.getString("openx.whiteListedSites")).andReturn("").anyTimes();
    expect(mockAdapterConfig.getString("atnt.whiteListedSites")).andReturn("").anyTimes();
    expect(mockAdapterConfig.getString("tapit.whiteListedSites")).andReturn("").anyTimes();
    expect(mockAdapterConfig.getString("mullahmedia.whiteListedSites")).andReturn("123,321").anyTimes();
    replay(mockAdapterConfig);

    expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
    expect(mockConfig.getString("loggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties").anyTimes();
    // expect(mockConfig.getInt("partnerSegmentNo", 3)).andReturn(2).anyTimes();
    expect(mockConfig.getInt("totalSegmentNo")).andReturn(5).anyTimes();
    expect(mockConfig.getDouble("revenueWindow", 0.33)).andReturn(10.0).anyTimes();
    expect(mockConfig.getDouble("ecpmShift", 0.1)).andReturn(0.0).anyTimes();
    expect(mockConfig.getDouble("feedbackPower", 2.0)).andReturn(1.0).anyTimes();
    expect(mockConfig.getInt("partnerSegmentNo", 2)).andReturn(2).anyTimes();
    expect(mockConfig.getInt("whiteListedSitesRefreshtime", 1000 * 300)).andReturn(0).anyTimes();
    replay(mockConfig);
    DebugLogger.init(mockConfig);
    logger = new DebugLogger();
    Filters.init(mockAdapterConfig);
  }

  @Test
  public void testImpressionBurnFilter() {
    Long[] rcList = null;
    Long[] tags = null;
    Timestamp modified_on = null;
    Long[] slotIds = null;
    Integer[] siteRatings = null;
    ChannelSegmentEntity channelSegmentEntity1 = new ChannelSegmentEntity("advertiserId1", "adgroupId1", "adId",
        "channelId1", (long) 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds,
        (long) 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false);
    ChannelSegmentEntity channelSegmentEntity2 = new ChannelSegmentEntity("advertiserId1", "adgroupId2", "adId",
        "channelId1", (long) 0, rcList, tags, false, true, "externalSiteKey", modified_on, "campaignId", slotIds,
        (long) 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false);
    ChannelSegmentEntity channelSegmentEntity3 = new ChannelSegmentEntity("advertiserId1", "adgroupId3", "adId",
        "channelId1", (long) 1, rcList, tags, false, false, "externalSiteKey", modified_on, "campaignId", slotIds,
        (long) 0, false, "pricingModel", siteRatings, 0, null, false, false, false, false);
    ChannelSegmentEntity channelSegmentEntity4 = new ChannelSegmentEntity("advertiserId2", "adgroupId4", "adId",
        "channelId2", (long) 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds,
        (long) 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false);
    ChannelSegmentEntity channelSegmentEntity5 = new ChannelSegmentEntity("advertiserId2", "adgroupId5", "adId",
        "channelId2", (long) 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds,
        (long) 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false);
    ChannelSegmentEntity channelSegmentEntity6 = new ChannelSegmentEntity("advertiserId3", "adgroupId5", "adId",
        "channelId3", (long) 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds,
        (long) 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false);

    HashMap<String, HashMap<String, ChannelSegment>> matchedSegments = new HashMap<String, HashMap<String, ChannelSegment>>();
    HashMap<String, ChannelSegment> adv1 = new HashMap<String, ChannelSegment>();
    HashMap<String, ChannelSegment> adv2 = new HashMap<String, ChannelSegment>();
    HashMap<String, ChannelSegment> adv3 = new HashMap<String, ChannelSegment>();

    adv1.put(channelSegmentEntity1.getAdgroupId(), new ChannelSegment(channelSegmentEntity1, cE1, cFE1, cSFE1, null,
        null, 0));
    adv1.put(channelSegmentEntity2.getAdgroupId(), new ChannelSegment(channelSegmentEntity2, cE1, cFE1, cSFE2, null,
        null, 0));
    adv1.put(channelSegmentEntity3.getAdgroupId(), new ChannelSegment(channelSegmentEntity3, cE1, cFE1, cSFE3, null,
        null, 0));
    adv2.put(channelSegmentEntity4.getAdgroupId(), new ChannelSegment(channelSegmentEntity4, cE2, cFE2, cSFE4, null,
        null, 0));
    adv2.put(channelSegmentEntity5.getAdgroupId(), new ChannelSegment(channelSegmentEntity5, cE2, cFE2, cSFE5, null,
        null, 0));
    adv3.put(channelSegmentEntity6.getAdgroupId(), new ChannelSegment(channelSegmentEntity6, cE3, cFE3, cSFE6, null,
        null, 0));
    matchedSegments.put(channelSegmentEntity1.getAdvertiserId(), adv1);
    matchedSegments.put(channelSegmentEntity4.getAdvertiserId(), adv2);
    matchedSegments.put(channelSegmentEntity6.getAdvertiserId(), adv3);

    matchedSegments = Filters.impressionBurnFilter(matchedSegments, logger, mockConfig, "null");

    assertEquals(false, matchedSegments.containsKey(channelSegmentEntity1.getAdvertiserId()));
    assertEquals(false, matchedSegments.containsKey(channelSegmentEntity4.getAdvertiserId()));
    assertEquals(true, matchedSegments.containsKey(channelSegmentEntity6.getAdvertiserId()));
  }

  @Test
  public void testPartnerSegmentCountFilter() {
    Long[] rcList = null;
    Long[] tags = null;
    Timestamp modified_on = null;
    Long[] slotIds = null;
    Integer[] siteRatings = null;
    ChannelSegmentEntity channelSegmentEntity1 = new ChannelSegmentEntity("advertiserId1", "adgroupId1", "adId",
        "channelId1", (long) 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds,
        (long) 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false);
    ChannelSegmentEntity channelSegmentEntity2 = new ChannelSegmentEntity("advertiserId1", "adgroupId2", "adId",
        "channelId1", (long) 0, rcList, tags, false, true, "externalSiteKey", modified_on, "campaignId", slotIds,
        (long) 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false);
    ChannelSegmentEntity channelSegmentEntity3 = new ChannelSegmentEntity("advertiserId1", "adgroupId3", "adId",
        "channelId1", (long) 1, rcList, tags, false, false, "externalSiteKey", modified_on, "campaignId", slotIds,
        (long) 0, false, "pricingModel", siteRatings, 0, null, false, false, false, false);
    ChannelSegmentEntity channelSegmentEntity4 = new ChannelSegmentEntity("advertiserId2", "adgroupId4", "adId",
        "channelId2", (long) 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds,
        (long) 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false);
    ChannelSegmentEntity channelSegmentEntity5 = new ChannelSegmentEntity("advertiserId2", "adgroupId5", "adId",
        "channelId2", (long) 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds,
        (long) 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false);
    ChannelSegmentEntity channelSegmentEntity6 = new ChannelSegmentEntity("advertiserId3", "adgroupId5", "adId",
        "channelId3", (long) 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds,
        (long) 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false);

    HashMap<String, HashMap<String, ChannelSegment>> matchedSegments = new HashMap<String, HashMap<String, ChannelSegment>>();
    HashMap<String, ChannelSegment> adv1 = new HashMap<String, ChannelSegment>();
    HashMap<String, ChannelSegment> adv2 = new HashMap<String, ChannelSegment>();
    HashMap<String, ChannelSegment> adv3 = new HashMap<String, ChannelSegment>();

    adv1.put(channelSegmentEntity1.getAdgroupId(), new ChannelSegment(channelSegmentEntity1, cE1, cFE1, cSFE1, null,
        null, 0));
    adv1.put(channelSegmentEntity2.getAdgroupId(), new ChannelSegment(channelSegmentEntity2, cE1, cFE1, cSFE2, null,
        null, 0));
    adv1.put(channelSegmentEntity3.getAdgroupId(), new ChannelSegment(channelSegmentEntity3, cE1, cFE1, cSFE3, null,
        null, 0));
    adv2.put(channelSegmentEntity4.getAdgroupId(), new ChannelSegment(channelSegmentEntity4, cE2, cFE2, cSFE4, null,
        null, 0));
    adv2.put(channelSegmentEntity5.getAdgroupId(), new ChannelSegment(channelSegmentEntity5, cE2, cFE2, cSFE5, null,
        null, 0));
    adv3.put(channelSegmentEntity6.getAdgroupId(), new ChannelSegment(channelSegmentEntity6, cE3, cFE3, cSFE6, null,
        null, 0));
    matchedSegments.put(channelSegmentEntity1.getAdvertiserId(), adv1);
    matchedSegments.put(channelSegmentEntity4.getAdvertiserId(), adv2);
    matchedSegments.put(channelSegmentEntity6.getAdvertiserId(), adv3);

    matchedSegments = Filters.partnerSegmentCountFilter(matchedSegments, 0.3, logger, mockConfig, mockAdapterConfig);

    assertEquals(false, matchedSegments.get("advertiserId1").containsKey("adgroupId1"));
    assertEquals(2, matchedSegments.get("advertiserId1").size());
    assertEquals(1, matchedSegments.get("advertiserId2").size());
    assertEquals(1, matchedSegments.get("advertiserId3").size());
    assertEquals(false, matchedSegments.get("advertiserId1").containsKey("adgroupId1"));
    assertEquals(false, matchedSegments.get("advertiserId2").containsKey("adgroupId4"));
  }

  @Test
  public void testSegmentsPerRequestFilter() {

    Long[] rcList = null;
    Long[] tags = null;
    Timestamp modified_on = null;
    Long[] slotIds = null;
    Integer[] siteRatings = null;
    ChannelSegmentEntity channelSegmentEntity1 = new ChannelSegmentEntity("advertiserId1", "adgroupId1", "adId",
        "channelId1", (long) 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds,
        (long) 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false);
    ChannelSegmentEntity channelSegmentEntity2 = new ChannelSegmentEntity("advertiserId1", "adgroupId2", "adId",
        "channelId1", (long) 0, rcList, tags, false, true, "externalSiteKey", modified_on, "campaignId", slotIds,
        (long) 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false);
    ChannelSegmentEntity channelSegmentEntity3 = new ChannelSegmentEntity("advertiserId1", "adgroupId3", "adId",
        "channelId1", (long) 1, rcList, tags, false, false, "externalSiteKey", modified_on, "campaignId", slotIds,
        (long) 0, false, "pricingModel", siteRatings, 0, null, false, false, false, false);
    ChannelSegmentEntity channelSegmentEntity4 = new ChannelSegmentEntity("advertiserId2", "adgroupId4", "adId",
        "channelId2", (long) 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds,
        (long) 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false);
    ChannelSegmentEntity channelSegmentEntity5 = new ChannelSegmentEntity("advertiserId2", "adgroupId5", "adId",
        "channelId2", (long) 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds,
        (long) 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false);
    ChannelSegmentEntity channelSegmentEntity6 = new ChannelSegmentEntity("advertiserId3", "adgroupId5", "adId",
        "channelId3", (long) 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds,
        (long) 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false);

    HashMap<String, HashMap<String, ChannelSegment>> matchedSegments = new HashMap<String, HashMap<String, ChannelSegment>>();
    HashMap<String, ChannelSegment> adv1 = new HashMap<String, ChannelSegment>();
    HashMap<String, ChannelSegment> adv2 = new HashMap<String, ChannelSegment>();
    HashMap<String, ChannelSegment> adv3 = new HashMap<String, ChannelSegment>();

    adv1.put(channelSegmentEntity1.getAdgroupId(), new ChannelSegment(channelSegmentEntity1, cE1, cFE1, cSFE1, null,
        null, cSFE1.geteCPM()));
    adv1.put(channelSegmentEntity2.getAdgroupId(), new ChannelSegment(channelSegmentEntity2, cE1, cFE1, cSFE2, null,
        null, cSFE2.geteCPM()));
    adv1.put(channelSegmentEntity3.getAdgroupId(), new ChannelSegment(channelSegmentEntity3, cE1, cFE1, cSFE3, null,
        null, cSFE3.geteCPM()));
    adv2.put(channelSegmentEntity4.getAdgroupId(), new ChannelSegment(channelSegmentEntity4, cE2, cFE2, cSFE4, null,
        null, cSFE4.geteCPM()));
    adv2.put(channelSegmentEntity5.getAdgroupId(), new ChannelSegment(channelSegmentEntity5, cE2, cFE2, cSFE5, null,
        null, cSFE5.geteCPM()));
    adv3.put(channelSegmentEntity6.getAdgroupId(), new ChannelSegment(channelSegmentEntity6, cE3, cFE3, cSFE6, null,
        null, cSFE6.geteCPM()));
    matchedSegments.put(channelSegmentEntity1.getAdvertiserId(), adv1);
    matchedSegments.put(channelSegmentEntity4.getAdvertiserId(), adv2);
    matchedSegments.put(channelSegmentEntity6.getAdvertiserId(), adv3);

    List<ChannelSegment> rows = new ArrayList<ChannelSegment>();
    for (String advertiserId : matchedSegments.keySet()) {
      for (String adgroupId : matchedSegments.get(advertiserId).keySet()) {
        rows.add(matchedSegments.get(advertiserId).get(adgroupId));
      }
    }
    List<ChannelSegment> finalRow = Filters.segmentsPerRequestFilter(matchedSegments, rows, logger, mockConfig);

    assertEquals(5, finalRow.size());
    assertEquals("adgroupId2", finalRow.get(0).getChannelSegmentEntity().getAdgroupId());
    assertEquals("adgroupId1", finalRow.get(4).getChannelSegmentEntity().getAdgroupId());
  }

  @Test
  public void testSiteWhiteListingLoading() {
    assertEquals(false, Filters.whiteListedSites.containsKey("advertiserId1"));
    assertEquals(false, Filters.whiteListedSites.containsKey("advertiserId2"));
    assertEquals(false, Filters.whiteListedSites.containsKey("advertiserId3"));
    assertEquals(true, Filters.whiteListedSites.containsKey("advertiserId4"));
    assertEquals(true, Filters.whiteListedSites.get("advertiserId4").contains("123"));
    assertEquals(true, Filters.whiteListedSites.get("advertiserId4").contains("321"));
    assertEquals(false, Filters.whiteListedSites.get("advertiserId4").contains("78"));

    Configuration newConfig = createMock(Configuration.class);

    HashMap<String, String> temp = new HashMap<String, String>();
    temp.put("openx.advertiserId", "");
    temp.put("openx.partnerSegmentNo", "");
    temp.put("atnt.advertiserId", "");
    temp.put("atnt.partnerSegmentNo", "");
    temp.put("tapit.advertiserId", "");
    temp.put("tapit.partnerSegmentNo", "");
    temp.put("mullahmedia.advertiserId", "");
    Iterator<String> itr = temp.keySet().iterator();

    expect(newConfig.getKeys()).andReturn(itr).anyTimes();
    expect(newConfig.getString("openx.advertiserId")).andReturn("advertiserId1").anyTimes();
    expect(newConfig.getString("atnt.advertiserId")).andReturn("advertiserId2").anyTimes();
    expect(newConfig.getString("tapit.advertiserId")).andReturn("advertiserId3").anyTimes();
    expect(newConfig.getString("mullahmedia.advertiserId")).andReturn("advertiserId4").anyTimes();
    expect(newConfig.getInt("openx.partnerSegmentNo", 2)).andReturn(2).anyTimes();
    expect(newConfig.getInt("atnt.partnerSegmentNo", 2)).andReturn(2).anyTimes();
    expect(newConfig.getInt("tapit.partnerSegmentNo", 2)).andReturn(2).anyTimes();
    expect(newConfig.getString("openx.whiteListedSites")).andReturn("").anyTimes();
    expect(newConfig.getString("atnt.whiteListedSites")).andReturn("").anyTimes();
    expect(newConfig.getString("tapit.whiteListedSites")).andReturn("123,321").anyTimes();
    expect(newConfig.getString("mullahmedia.whiteListedSites")).andReturn("").anyTimes();
    expect(newConfig.getInt("whiteListedSitesRefreshtime", 1000 * 300)).andReturn(0).anyTimes();
    replay(newConfig);

    Filters.refreshWhiteListedSites(mockConfig, newConfig, new DebugLogger());
    assertEquals(false, Filters.whiteListedSites.containsKey("advertiserId4"));
    assertEquals(true, Filters.whiteListedSites.containsKey("advertiserId3"));
    assertEquals(true, Filters.whiteListedSites.get("advertiserId3").contains("123"));
    assertEquals(true, Filters.whiteListedSites.get("advertiserId3").contains("321"));
    assertEquals(false, Filters.whiteListedSites.get("advertiserId3").contains("78"));
  }

}
