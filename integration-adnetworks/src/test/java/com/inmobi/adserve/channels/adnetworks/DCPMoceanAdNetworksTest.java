package com.inmobi.adserve.channels.adnetworks;

import static junit.framework.TestCase.assertEquals;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.adnetworks.mocean.DCPMoceanAdNetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;


/**
 * @author deepak
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
public class DCPMoceanAdNetworksTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final String moceanHost = "http://ads.mocean.com/ad";
    private static final String moceanStatus = "on";
    private static final String moceanAdvId = "huntadv1";
    private static final String moceanTest = "1";
    private static Configuration mockConfig = null;
    private static DCPMoceanAdNetwork dcpMoceanAdNetwork;
    private static RepositoryHelper repositoryHelper;

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("mocean.host")).andReturn(moceanHost).anyTimes();
        expect(mockConfig.getString("mocean.status")).andReturn(moceanStatus).anyTimes();
        expect(mockConfig.getString("mocean.test")).andReturn(moceanTest).anyTimes();
        expect(mockConfig.getString("mocean.advertiserId")).andReturn(moceanAdvId).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
    }

    @Before
    public void setUp() throws Exception {
        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        Formatter.init();
        final Channel serverChannel = createMock(Channel.class);
        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        final SlotSizeMapEntity slotSizeMapEntityFor4 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor9.getDimension()).andReturn(new Dimension(320, 48)).anyTimes();
        replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity slotSizeMapEntityFor10 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor10.getDimension()).andReturn(new Dimension(300, 250)).anyTimes();
        replay(slotSizeMapEntityFor10);
        final SlotSizeMapEntity slotSizeMapEntityFor11 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor11.getDimension()).andReturn(new Dimension(728, 90)).anyTimes();
        replay(slotSizeMapEntityFor11);
        final SlotSizeMapEntity slotSizeMapEntityFor12 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor12.getDimension()).andReturn(new Dimension(468, 60)).anyTimes();
        replay(slotSizeMapEntityFor12);
        final SlotSizeMapEntity slotSizeMapEntityFor14 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor14.getDimension()).andReturn(new Dimension(320, 480)).anyTimes();
        replay(slotSizeMapEntityFor14);
        final SlotSizeMapEntity slotSizeMapEntityFor15 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor15.getDimension()).andReturn(new Dimension(320, 50)).anyTimes();
        replay(slotSizeMapEntityFor15);
        repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.querySlotSizeMapRepository((short) 4))
                .andReturn(slotSizeMapEntityFor4).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 9))
                .andReturn(slotSizeMapEntityFor9).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 11))
                .andReturn(slotSizeMapEntityFor11).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 12))
                .andReturn(slotSizeMapEntityFor12).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository(Short.MAX_VALUE))
                .andReturn(null).anyTimes();
        replay(repositoryHelper);
        dcpMoceanAdNetwork = new DCPMoceanAdNetwork(mockConfig, null, base, serverChannel);

        dcpMoceanAdNetwork.setName("mocean");
        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        dcpMoceanAdNetwork.setHost(moceanHost);
    }

    @Test
    public void testDCPMoceanConfigureParameters() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(moceanAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(
                dcpMoceanAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper),
                true);
    }

    @Test
    public void testDCPMoceanConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(moceanAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(
                dcpMoceanAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper),
                false);
    }

    @Test
    public void testDCPMoceanConfigureParametersBlankExtKey() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(moceanAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(
                dcpMoceanAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper),
                false);
    }

    @Test
    public void testDCPMoceanConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(moceanAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(
                dcpMoceanAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper),
                false);
    }

    @Test
    public void testDCPMoceanRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSource("APP");
        sasParams.setOsId(5);
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        final String externalKey = "1324";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(moceanAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (dcpMoceanAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpMoceanAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ads.mocean.com/ad?ip=206.29.182.240&track=1&timeout=500&rmtype=none&key=6&type=3&over_18=0&zone=1324&ua=Mozilla&test=1&lat=37.4429&long=-122.1514&isapp=yes&isweb=no&udidtype=custom&udid=202cb962ac59075b964b07152d234b70&pubsiteid=00000000-0000-0020-0000-000000000000&min_size_x=288&min_size_y=45&size_x=320&size_y=50&keywords=Food+%26+Drink%2CAdventure%2CWord";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPMoceanRequestUriWithSpecialFormat() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setCountryCode("US");
        sasParams.setSource("WAP");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        final String externalKey = "1324";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(moceanAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (dcpMoceanAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 12, repositoryHelper)) {
            final String actualUrl = dcpMoceanAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ads.mocean.com/ad?ip=206.29.182.240&track=1&timeout=500&rmtype=none&key=6&type=3&over_18=0&zone=1324&ua=Mozilla&test=1&lat=37.4429&long=-122.1514&isapp=no&isweb=yes&udidtype=custom&udid=202cb962ac59075b964b07152d234b70&pubsiteid=00000000-0000-0020-0000-000000000000&country=US&min_size_x=421&min_size_y=54&size_x=468&size_y=60&format=468x60&keywords=Food+%26+Drink%2CAdventure%2CWord";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPMoceanRequestUriBlankLatLong() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong(" ,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        casInternalRequestParameters.setUidMd5("111a222b333c444d555e666f777");
        casInternalRequestParameters.setUidIFA("12q12q12q12q12q12q12q12q12q12q12q");
        casInternalRequestParameters.setTrackingAllowed(true);
        sasParams.setSource("APP");
        sasParams.setOsId(3);
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        final String externalKey = "1324";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(moceanAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (dcpMoceanAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpMoceanAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ads.mocean.com/ad?ip=206.29.182.240&track=1&timeout=500&rmtype=none&key=6&type=3&over_18=0&zone=1324&ua=Mozilla&test=1&isapp=yes&isweb=no&androidid=111a222b333c444d555e666f777&udidtype=custom&udid=202cb962ac59075b964b07152d234b70&udidtype=custom&udid=202cb962ac59075b964b07152d234b70&pubsiteid=00000000-0000-0020-0000-000000000000&min_size_x=288&min_size_y=45&size_x=320&size_y=50&keywords=Food+%26+Drink%2CAdventure%2CWord";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPMoceanRequestUriBlankSlot() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        sasParams.setOsId(5);
        final String externalKey = "1324";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(moceanAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (dcpMoceanAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, Short.MAX_VALUE, repositoryHelper)) {
            final String actualUrl = dcpMoceanAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ads.mocean.com/ad?ip=206.29.182.240&track=1&timeout=500&rmtype=none&key=6&type=3&over_18=0&zone=1324&ua=Mozilla&test=1&lat=37.4429&long=-122.1514&isapp=no&isweb=yes&udidtype=custom&udid=202cb962ac59075b964b07152d234b70&pubsiteid=00000000-0000-0020-0000-000000000000&keywords=Food+%26+Drink%2CAdventure%2CWord";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPMoceanParseResponseImg() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(moceanAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpMoceanAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper);
        final String response =
                "[{\"url\" : \"http://ads.mocean.com/4/redir/1b2c8f91-fbf5-11e1-bd3e-a0369f06db33/0/139494\",\"img\" : \"http://img.ads.mocean.com/img/4f6/345/c8115c2/image_320x50.gif\",\"type\": \"image/gif\",\"track\" : \"null\"}];";
        dcpMoceanAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpMoceanAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href='http://ads.mocean.com/4/redir/1b2c8f91-fbf5-11e1-bd3e-a0369f06db33/0/139494' onclick=\"document.getElementById('click').src='clickUrl';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://img.ads.mocean.com/img/4f6/345/c8115c2/image_320x50.gif'  /></a><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                dcpMoceanAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPMoceanParseResponseImgAppSDK360() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSource("app");
        sasParams.setSdkVersion("i360");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        sasParams.setUserAgent("Mozilla");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(moceanAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpMoceanAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper);
        final String response =
                "[{\"url\" : \"http://ads.mocean.com/4/redir/1b2c8f91-fbf5-11e1-bd3e-a0369f06db33/0/139494\",\"img\" : \"http://img.ads.mocean.com/img/4f6/345/c8115c2/image_320x50.gif\",\"type\": \"image/gif\",\"track\" : \"null\"}];";
        dcpMoceanAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpMoceanAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a href='http://ads.mocean.com/4/redir/1b2c8f91-fbf5-11e1-bd3e-a0369f06db33/0/139494' onclick=\"document.getElementById('click').src='clickUrl';imraid.openExternal('http://ads.mocean.com/4/redir/1b2c8f91-fbf5-11e1-bd3e-a0369f06db33/0/139494'); return false;\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://img.ads.mocean.com/img/4f6/345/c8115c2/image_320x50.gif'  /></a><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                dcpMoceanAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPMoceanParseResponseTextAdWAP() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("wap");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(moceanAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpMoceanAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);

        final String response =
                "[{\"url\" : \"http://ads.mocean.com/1/redir/6b5f4d87-fbf6-11e1-b228-001b21ccdb21/0/139491\",\"text\" : \"Test Campaign for Integration Testing\", \"track\" : \"\"}];";
        dcpMoceanAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpMoceanAdNetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\" content=\"text/html; charset=utf-8\"/></head><style>body, html {margin: 0; padding: 0; overflow: hidden;}.template_120_20 { width: 120px; height: 19px; }.template_168_28 { width: 168px; height: 27px; }.template_216_36 { width: 216px; height: 35px;}.template_300_50 {width: 300px; height: 49px;}.template_320_50 {width: 320px; height: 49px;}.template_320_48 {width: 320px; height: 47px;}.template_468_60 {width: 468px; height: 59px;}.template_728_90 {width: 728px; height: 89px;}.container { overflow: hidden; position: relative;}.adbg { border-top: 1px solid #00577b; background: #003229;background: -moz-linear-gradient(top, #003e57 0%, #003229 100%);background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #003e57), color-stop(100%, #003229));background: -webkit-linear-gradient(top, #003e57 0%, #003229 100%);background: -o-linear-gradient(top, #003e57 0%, #003229 100%);background: -ms-linear-gradient(top, #003e57 0%, #003229 100%);background: linear-gradient(top, #003e57 0%, #003229 100%); }.right-image { position: absolute; top: 0; right: 5px; display: table-cell; vertical-align: middle}.right-image-cell { padding-left:4px!important;white-space: nowrap; vertical-align: middle; height: 100%; width:40px;background:url(http://r.edge.inmobicdn.net/IphoneActionsData/line.png) repeat-y left top; } .adtable,.adtable td{border: 0; padding: 0; margin:0;}.adtable {padding:5px;}.adtext-cell {width:100%}.adtext-cell-div {font-family:helvetica;color:#fff;float:left;v-allign:middle}.template_120_20 .adtable{padding-top:0px;}.template_120_20 .adtable .adtext-cell div{font-size: 0.42em!important;}.template_168_28 .adtable{padding-top: 2px;}.template_168_28 .adtable .adtext-cell div{font-size: 0.5em!important;}.template_168_28 .adtable .adimg{width:18px;height:18px; }.template_216_36 .adtable{padding-top: 3px;}.template_216_36 .adtable .adtext-cell div{font-size: 0.6em!important;padding-left:3px;}.template_216_36 .adtable .adimg{width:25px;height:25px;}.template_300_50 .adtable{padding-top: 7px;}.template_300_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_300_50 .adtable .adimg{width:30px;height:30px;}.template_320_48 .adtable{padding-top: 6px;}.template_320_48 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_48 .adtable .adimg{width:30px;height:30px;}.template_320_50 .adtable{padding-top: 7px;}.template_320_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_50 .adtable .adimg{width:30px;height:30px;}.template_468_60 .adtable{padding-top:10px;}.template_468_60 .adtable .adtext-cell div{font-size: 1.1em!important;padding-left:8px;}.template_468_60 .adtable .adimg{width:35px;height:35px;}.template_728_90 .adtable{padding-top:19px;}.template_728_90 .adtable .adtext-cell div{font-size: 1.4em!important;padding-left:10px;}.template_728_90 .adtable .adimg{width:50px;height:50px;}</style><body style=\"margin:0;padding:0;overflow: hidden;\"><a style=\"text-decoration:none; \" href=\"http://ads.mocean.com/1/redir/6b5f4d87-fbf6-11e1-b228-001b21ccdb21/0/139491\" onclick=\"document.getElementById('click').src='clickUrl';\" target=\"_blank\"><div class=\"container template_300_50 adbg\"><table class=\"adtable\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td class=\"adtext-cell\"><div class=\"adtext-cell-div\" style=\"font-weight:bold;\">Test Campaign for Integration Testing</div></div></td><td class=\"right-image-cell\">&nbsp<img width=\"28\" height=\"28\" class=\"adimg\" border=\"0\" src=\"http://r.edge.inmobicdn.net/IphoneActionsData/web.png\" /></td></tr></table></div></a><img src='beaconUrl' height=1 width=1 border=0 \"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpMoceanAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPMoceanParseResponseTextAdAapSDK360() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSdkVersion("i360");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        sasParams.setSource("app");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(moceanAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpMoceanAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);

        final String response =
                "[{\"url\" : \"http://ads.mocean.com/1/redir/6b5f4d87-fbf6-11e1-b228-001b21ccdb21/0/139491\",\"text\" : \"Test Campaign for Integration Testing\", \"track\" : \"\"}];";
        dcpMoceanAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpMoceanAdNetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\" content=\"text/html; charset=utf-8\"/></head><style>body, html {margin: 0; padding: 0; overflow: hidden;}.template_120_20 { width: 120px; height: 19px; }.template_168_28 { width: 168px; height: 27px; }.template_216_36 { width: 216px; height: 35px;}.template_300_50 {width: 300px; height: 49px;}.template_320_50 {width: 320px; height: 49px;}.template_320_48 {width: 320px; height: 47px;}.template_468_60 {width: 468px; height: 59px;}.template_728_90 {width: 728px; height: 89px;}.container { overflow: hidden; position: relative;}.adbg { border-top: 1px solid #00577b; background: #003229;background: -moz-linear-gradient(top, #003e57 0%, #003229 100%);background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #003e57), color-stop(100%, #003229));background: -webkit-linear-gradient(top, #003e57 0%, #003229 100%);background: -o-linear-gradient(top, #003e57 0%, #003229 100%);background: -ms-linear-gradient(top, #003e57 0%, #003229 100%);background: linear-gradient(top, #003e57 0%, #003229 100%); }.right-image { position: absolute; top: 0; right: 5px; display: table-cell; vertical-align: middle}.right-image-cell { padding-left:4px!important;white-space: nowrap; vertical-align: middle; height: 100%; width:40px;background:url(http://r.edge.inmobicdn.net/IphoneActionsData/line.png) repeat-y left top; } .adtable,.adtable td{border: 0; padding: 0; margin:0;}.adtable {padding:5px;}.adtext-cell {width:100%}.adtext-cell-div {font-family:helvetica;color:#fff;float:left;v-allign:middle}.template_120_20 .adtable{padding-top:0px;}.template_120_20 .adtable .adtext-cell div{font-size: 0.42em!important;}.template_168_28 .adtable{padding-top: 2px;}.template_168_28 .adtable .adtext-cell div{font-size: 0.5em!important;}.template_168_28 .adtable .adimg{width:18px;height:18px; }.template_216_36 .adtable{padding-top: 3px;}.template_216_36 .adtable .adtext-cell div{font-size: 0.6em!important;padding-left:3px;}.template_216_36 .adtable .adimg{width:25px;height:25px;}.template_300_50 .adtable{padding-top: 7px;}.template_300_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_300_50 .adtable .adimg{width:30px;height:30px;}.template_320_48 .adtable{padding-top: 6px;}.template_320_48 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_48 .adtable .adimg{width:30px;height:30px;}.template_320_50 .adtable{padding-top: 7px;}.template_320_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_50 .adtable .adimg{width:30px;height:30px;}.template_468_60 .adtable{padding-top:10px;}.template_468_60 .adtable .adtext-cell div{font-size: 1.1em!important;padding-left:8px;}.template_468_60 .adtable .adimg{width:35px;height:35px;}.template_728_90 .adtable{padding-top:19px;}.template_728_90 .adtable .adtext-cell div{font-size: 1.4em!important;padding-left:10px;}.template_728_90 .adtable .adimg{width:50px;height:50px;}</style><body style=\"margin:0;padding:0;overflow: hidden;\"><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a style=\"text-decoration:none; \" href=\"http://ads.mocean.com/1/redir/6b5f4d87-fbf6-11e1-b228-001b21ccdb21/0/139491\" onclick=\"document.getElementById('click').src='clickUrl';imraid.openExternal('http://ads.mocean.com/1/redir/6b5f4d87-fbf6-11e1-b228-001b21ccdb21/0/139491'); return false;\" target=\"_blank\"><div class=\"container template_300_50 adbg\"><table class=\"adtable\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td class=\"adtext-cell\"><div class=\"adtext-cell-div\" style=\"font-weight:bold;\">Test Campaign for Integration Testing</div></div></td><td class=\"right-image-cell\">&nbsp<img width=\"28\" height=\"28\" class=\"adimg\" border=\"0\" src=\"http://r.edge.inmobicdn.net/IphoneActionsData/web.png\" /></td></tr></table></div></a><img src='beaconUrl' height=1 width=1 border=0 \"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpMoceanAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPMoceanParseResponseWithContent() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSdkVersion("i360");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        sasParams.setSource("app");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(moceanAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpMoceanAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);

        final String response =
                "[{\"url\" : \"http://ads.mocean.com/6/redir/1bacab71-a839-11e3-92f1-a0369f0a5201/0/550667\", \"text\" : \"\", \"img\" : \"http://admarvel.s3.amazonaws.com/ads/c195800/13934438747851_300x50.png\", \"track\" : \"http://ads.mocean.com/6/img/1bacab71-a839-11e3-92f1-a0369f0a5201?redir=http%3A%2F%2F184.73.151.140%2Ffam%2Fview.php%3Fp%3D__pid%3D80a39249e06655a3__sid%3D75833__bid%3D951143__cb%3Dc0442f5b13__h%3D1394444898__uid%3D8d7e3052bf2d166aeb6605e97903f71432012870__tp%3D19352aca746c090f32e6edae176f5e39__os%3DAndroid__s%3D197ce8c07776458ff77e7a4d51cf5d06\", \"content\" : \"<a href='http://ads.mocean.com/6/redir/1bacab71-a839-11e3-92f1-a0369f0a5201/0/550667' target='_self'><img src='http://admarvel.s3.amazonaws.com/ads/c195800/13934438747851_300x50.png' width='300' height='50' alt='' title='' border='0' /></a><img src=\\\"http://184.73.151.140/fam/view.php?p=__pid=80a39249e06655a3__sid=75833__bid=951143__cb=c0442f5b13__h=1394444898__uid=8d7e3052bf2d166aeb6605e97903f71432012870__tp=19352aca746c090f32e6edae176f5e39__os=Android__s=197ce8c07776458ff77e7a4d51cf5d06\\\" alt=\\\"\\\" width=\\\"1\\\" height=\\\"1\\\" /><img src=\\\"http://ads.mocean.com/6/img/1bacab71-a839-11e3-92f1-a0369f0a5201\\\" width=\\\"1\\\" height=\\\"1\\\" alt=\\\"\\\"/>\", \"response\" : \"<a href='http://184.73.151.140/fam/ck.php?p=__pid=80a39249e06655a3__sid=75833__bid=951143__cb=17757dc446__h=1394444898__uid=8d7e3052bf2d166aeb6605e97903f71432012870__tp=19352aca746c090f32e6edae176f5e39__os=Android__s=51684c5491c2dc651ecbd604f853b143' target='_self'><img src='http://admarvel.s3.amazonaws.com/ads/c195800/13934438747851_300x50.png' width='300' height='50' alt='' title='' border='0' /></a><img src=\\\"http://184.73.151.140/fam/view.php?p=__pid=80a39249e06655a3__sid=75833__bid=951143__cb=c0442f5b13__h=1394444898__uid=8d7e3052bf2d166aeb6605e97903f71432012870__tp=19352aca746c090f32e6edae176f5e39__os=Android__s=197ce8c07776458ff77e7a4d51cf5d06\\\" alt=\\\"\\\" width=\\\"1\\\" height=\\\"1\\\" />\" }]";

        dcpMoceanAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpMoceanAdNetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a href='http://ads.mocean.com/6/redir/1bacab71-a839-11e3-92f1-a0369f0a5201/0/550667' target='_self'><img src='http://admarvel.s3.amazonaws.com/ads/c195800/13934438747851_300x50.png' width='300' height='50' alt='' title='' border='0' /></a><img src=\"http://184.73.151.140/fam/view.php?p=__pid=80a39249e06655a3__sid=75833__bid=951143__cb=c0442f5b13__h=1394444898__uid=8d7e3052bf2d166aeb6605e97903f71432012870__tp=19352aca746c090f32e6edae176f5e39__os=Android__s=197ce8c07776458ff77e7a4d51cf5d06\" alt=\"\" width=\"1\" height=\"1\" /><img src=\"http://ads.mocean.com/6/img/1bacab71-a839-11e3-92f1-a0369f0a5201\" width=\"1\" height=\"1\" alt=\"\"/><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpMoceanAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPMoceanParseInvalidResponse() throws Exception {
        final String response =
                "HTTP/1.1 20a OK\nServer=Netscape-Enterprise/4.1\n\n[{\"error\" : \"No ads available\"}];";
        dcpMoceanAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpMoceanAdNetwork.getHttpResponseStatusCode(), 500);
    }

    @Test
    public void testDCPMoceanParseNoAd() throws Exception {
        final String response = "[{\"error\" : \"No ads available\"}];";
        dcpMoceanAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpMoceanAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpMoceanAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPMoceanParseEmptyResponseCode() throws Exception {
        final String response = "";
        dcpMoceanAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpMoceanAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpMoceanAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPMoceanGetId() throws Exception {
        assertEquals(dcpMoceanAdNetwork.getId(), "huntadv1");
    }

    @Test
    public void testDCPMoceanGetImpressionId() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(moceanAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpMoceanAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper);
        assertEquals(dcpMoceanAdNetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @Test
    public void testDCPMoceanGetName() throws Exception {
        assertEquals(dcpMoceanAdNetwork.getName(), "moceanDCP");
    }

}