package com.inmobi.adserve.channels.server;

import java.io.BufferedWriter;
import java.io.FileWriter;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.SegmentFactory;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.messaging.publisher.AbstractMessagePublisher;
import com.inmobi.phoenix.exception.RepositoryException;
import static org.easymock.classextension.EasyMock.replay;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

public class SegmentFactoryTest extends TestCase {
  Configuration adaptorConfig;
  
  public void prepareConfig() throws Exception {
    FileWriter fstream = new FileWriter("/tmp/SegmentFactoryChannel-server.properties");
    BufferedWriter out = new BufferedWriter(fstream);
    out.write("log4j.logger.app = DEBUG, channel\n");
    out.write("log4j.additivity.app = false\n");
    out.write("log4j.appender.channel=org.apache.log4j.DailyRollingFileAppender\n");
    out.write("log4j.appender.channel.layout=org.apache.log4j.PatternLayout\n");
    out.write("log4j.appender.channel.DatePattern='.'yyyy-MM-dd-HH\n");
    String channelFile = "/tmp/channel.log." + System.currentTimeMillis();
    out.write("log4j.appender.channel.File=" + channelFile + "\n");
    out.write("log4j.logger.app = DEBUG, rr\n");
    out.write("log4j.additivity.rr.app = false\n");
    out.write("log4j.appender.rr=org.apache.log4j.DailyRollingFileAppender\n");
    out.write("log4j.appender.rr.layout=org.apache.log4j.PatternLayout\n");
    out.write("log4j.appender.rr.DatePattern='.'yyyy-MM-dd-HH\n");
    String rrFile = "/tmp/rr.log." + System.currentTimeMillis();
    System.out.println("here rr file name is " + rrFile);    
    out.write("log4j.logger.app = DEBUG, debug\n");
    out.write("log4j.additivity.rr.app = false\n");
    out.write("log4j.appender.rr=org.apache.log4j.DailyRollingFileAppender\n");
    out.write("log4j.appender.rr.layout=org.apache.log4j.PatternLayout\n");
    out.write("log4j.appender.rr.DatePattern='.'yyyy-MM-dd-HH\n");
    String debugFile = "/tmp/debug.log." + System.currentTimeMillis();
    System.out.println("here debug file name is " + debugFile);    
    out.write("log4j.appender.rr.File = " + rrFile + "\n");
    out.write("log4j.category.debug = DEBUG,debug\n");
    out.write("log4j.category.rr = DEBUG,rr\n");
    out.write("log4j.category.channel = DEBUG,channel\n");
    out.write("server.percentRollout=100 \nserver.siteType=PERFORMANCE,FAMILYSAFE,MATURE\n");
    out.write("server.enableDatabusLogging = true\n");
    out.write("server.enableFileLogging = true \n");
    out.write("server.sampledadvertisercount = 2");
    out.write("\nserver.maxconnections=100\n");
    out.write("logger.rr = rr \n");
    out.write("logger.channel = channel");
    out.write("\n logger.debug = debug \n");
    out.write("logger.advertiser = advertiser\n");
    out.write("logger.sampledadvertiser = sampledadvertiser");
    out.write("\nlogger.loggerConf = /opt/mkhoj/conf/cas/channel-server.properties\n");
    out.write("\nrtb.RTBreadtimeoutMillis = 200");
    out.write("\nrtb.isRtbEnabled = true");
    out.write("\nrtb.bidFloor = 0.1");
    out.write("\nrtb.maxconnections = 2");
    out.write("\nadapter.rtbAdvertiserName.status = on");
    out.write("\nadapter.rtbAdvertiserName.advertiserId = advertiserId");
    out.write("\nadapter.rtbAdvertiserName.urlBase = http://localhost:10005");
    out.write("\nadapter.rtbAdvertiserName.urlArg = query");
    out.write("\nadapter.rtbAdvertiserName.rtbMethod = post");
    out.write("\nadapter.rtbAdvertiserName.wnUrlback = http://localhost:10005");
    out.write("\nadapter.rtbAdvertiserName.accountId = ");
    out.write("\nadapter.rtbAdvertiserName.isWnRequired = true");
    out.write("\nadapter.rtbAdvertiserName.isWinFromClient = false");
    out.close();
  }
  
  public void setUp() throws Exception {
    prepareConfig();  
    adaptorConfig = createMock(Configuration.class);
    expect(adaptorConfig.getString("rtbAdvertiserName.advertiserId")).andReturn("advertiserId").anyTimes();
    expect(adaptorConfig.getString("rtbAdvertiserName.status")).andReturn("on").anyTimes();
    expect(adaptorConfig.getString("rtbAdvertiserName.urlArg")).andReturn("query").anyTimes();
    expect(adaptorConfig.getString("rtbAdvertiserName.wnUrlback")).andReturn("http://localhost:10005").anyTimes();
    expect(adaptorConfig.getString("rtbAdvertiserName.rtbMethod")).andReturn("post").anyTimes();
    expect(adaptorConfig.getString("rtbAdvertiserName.accountId")).andReturn("advertiserId").anyTimes();
    expect(adaptorConfig.getString("rtbAdvertiserName.rtbVer")).andReturn("2").anyTimes();
    expect(adaptorConfig.getBoolean("rtbAdvertiserName.isWnRequired")).andReturn(true).anyTimes();
    expect(adaptorConfig.getBoolean("rtbAdvertiserName.isWinFromClient")).andReturn(true).anyTimes();
    expect(adaptorConfig.getString("rtbAdvertiserName.host.default", null)).andReturn("http://localhost:10005").anyTimes();
    expect(adaptorConfig.getString("rtbAdvertiserName.host.null", "http://localhost:10005")).andReturn("http://localhost:10005").anyTimes();
    expect(adaptorConfig.getString("rtbAdvertiserName.targetingParams")).andReturn(null).anyTimes();
    replay(adaptorConfig);
  }
  
  @Test
  public void testgetRTBChannel() throws RepositoryException {
    String configFile = "/tmp/SegmentFactoryChannel-server.properties";
    ConfigurationLoader config = ConfigurationLoader.getInstance(configFile);
    Configuration mockConfig = createMock(Configuration.class);
    expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
    expect(mockConfig.getString("loggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties").anyTimes();
    replay(mockConfig);
    DebugLogger.init(mockConfig);
    DebugLogger logger = new DebugLogger();
    RepositoryHelper repoHelper = createMock(RepositoryHelper.class);
    ChannelEntity channelEntity = createMock(ChannelEntity.class);
    expect(channelEntity.isRtb()).andReturn(true).anyTimes();
    expect(channelEntity.getUrlBase()).andReturn("").anyTimes();
    expect(channelEntity.getUrlArg()).andReturn("").anyTimes();
    expect(channelEntity.getRtbMethod()).andReturn("").anyTimes();
    expect(channelEntity.getRtbVer()).andReturn("").anyTimes();
    expect(channelEntity.getWnUrl()).andReturn("").anyTimes();
    expect(channelEntity.isWnRequied()).andReturn(false).anyTimes();
    expect(channelEntity.isWnFromClient()).andReturn(false).anyTimes();
    expect(channelEntity.getAccountId()).andReturn("").anyTimes();
    replay(channelEntity);
    expect(repoHelper.queryChannelRepository("channelTest")).andReturn(channelEntity).anyTimes();
    replay(repoHelper);
    //HttpRequestHandler.init(config, null, null, null, null, null, null, null, null);
    Configuration rtbConfig = createMock(Configuration.class);
    expect(rtbConfig.getBoolean("isRtbEnabled")).andReturn(true).anyTimes();
    replay(rtbConfig);
    ServletHandler.rtbConfig = rtbConfig;
    SegmentFactory.setRepositoryHelper(repoHelper);
    AdNetworkInterface adNetworkInterface = SegmentFactory.getChannel("advertiserId", "channelTest", config.adapterConfiguration(), null, null, null, null,
        null, logger, false, 0.0);
    assertEquals(null, adNetworkInterface);
    AdNetworkInterface adNetworkInterface2 = SegmentFactory.getChannel("advertiserId", "channelTest", adaptorConfig, null, null, null, null,
        null, logger, true, 0.0);
    assertNotNull(adNetworkInterface2);
    assertEquals(true, adNetworkInterface2 instanceof RtbAdNetwork);
  }
}
