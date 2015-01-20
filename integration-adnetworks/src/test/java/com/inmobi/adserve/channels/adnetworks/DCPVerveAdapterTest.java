package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.verve.DCPVerveAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;

public class DCPVerveAdapterTest extends TestCase {
    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";
    private DCPVerveAdNetwork dcpVerveAdnetwork;
    private final String verveHost = "http://adcel.vrvm.com/htmlad";
    private final String verveStatus = "on";
    private final String verveAdvId = "verveadv1";
    private RepositoryHelper repositoryHelper;

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("verve.host")).andReturn(verveHost).anyTimes();
        expect(mockConfig.getString("verve.status")).andReturn(verveStatus).anyTimes();
        expect(mockConfig.getString("verve.advertiserId")).andReturn(verveAdvId).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
    }

    @Override
    public void setUp() throws Exception {
        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        final Channel serverChannel = createMock(Channel.class);
        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        Formatter.init();
        final SlotSizeMapEntity slotSizeMapEntityFor4 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor9.getDimension()).andReturn(new Dimension(320, 48)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity slotSizeMapEntityFor10 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor10.getDimension()).andReturn(new Dimension(300, 250)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor10);
        final SlotSizeMapEntity slotSizeMapEntityFor11 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor11.getDimension()).andReturn(new Dimension(728, 90)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor11);
        final SlotSizeMapEntity slotSizeMapEntityFor14 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor14.getDimension()).andReturn(new Dimension(320, 480)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor14);
        final SlotSizeMapEntity slotSizeMapEntityFor15 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor15.getDimension()).andReturn(new Dimension(320, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor15);
        repositoryHelper = EasyMock.createMock(RepositoryHelper.class);
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)4))
                .andReturn(slotSizeMapEntityFor4).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)9))
                .andReturn(slotSizeMapEntityFor9).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)11))
                .andReturn(slotSizeMapEntityFor11).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        EasyMock.replay(repositoryHelper);
        dcpVerveAdnetwork = new DCPVerveAdNetwork(mockConfig, null, base, serverChannel);
    }

    @Test
    public void testDCPVerveConfigureParameters() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setOsId(HandSetOS.iOS.getValue());
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(verveAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertTrue(dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9, repositoryHelper));
    }

    @Test
    public void testDCPVerveConfigureParametersIPOnlySet() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setOsId(HandSetOS.iOS.getValue());
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(verveAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"trueLatLongOnly\":\"false\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        assertTrue(dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9, repositoryHelper));
    }

    @Test
    public void testDCPVerveConfigureParametersTrueLatLongSet() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setOsId(HandSetOS.iOS.getValue());
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(verveAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"trueLatLongOnly\":\"true\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        assertTrue(dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9, repositoryHelper));
    }

    @Test
    public void testDCPVerveConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setSource("iphone");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(verveAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15, repositoryHelper));
    }

    @Test
    public void testVerveConfigureParametersBlankExtKey() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setSource("iphone");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(verveAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPVerveConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        sasParams.setSource("iphone");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(verveAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPVerveConfigureParametersBlockAndroid() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("mozilla");
        sasParams.setSource("android");
        sasParams.setSdkVersion("a354");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(verveAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPVerveConfigureParametersUnblockAndroidVersion() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("mozilla");
        sasParams.setOsId(HandSetOS.Android.getValue());
        sasParams.setSdkVersion("a360");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(verveAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertTrue(dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPVerveRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setOsId(HandSetOS.iOS.getValue());
        sasParams.setLocSrc("true-lat-lon");
        final String externalKey = "1324";
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(verveAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"trueLatLongOnly\":\"true\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpVerveAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://adcel.vrvm.com/htmlad?ip=206.29.182.240&p=iphn&b=1324&site=00000000-0000-0000-0000-000000000000&ua=Mozilla&lat=37.4429&long=-122.1514&uis=v&ui=202cb962ac59075b964b07152d234b70&c=97&size=320x48&adunit=mma";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPVerveRequestUriWithIPOnly() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setOsId(HandSetOS.iOS.getValue());
        sasParams.setLocSrc("derived-lat-lon");
        final String externalKey = "1324";
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(verveAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"trueLatLongOnly\":\"false\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        if (dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 10, repositoryHelper)) {
            final String actualUrl = dcpVerveAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://adcel.vrvm.com/htmlad?ip=206.29.182.240&p=iphn&b=1324&site=00000000-0000-0020-0000-000000000000&ua=Mozilla&uis=v&ui=202cb962ac59075b964b07152d234b70&c=97&size=300x250&adunit=inter";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPVerveRequestUriBlankLatLong() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong(",-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setOsId(HandSetOS.Android.getValue());
        final String externalKey = "1324";
        sasParams.setLocSrc("true-lat-lon");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(verveAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"trueLatLongOnly\":\"true\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        if (dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 11, repositoryHelper)) {
            final String actualUrl = dcpVerveAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://adcel.vrvm.com/htmlad?ip=206.29.182.240&p=iphn&b=1324&site=00000000-0000-0020-0000-000000000000&ua=Mozilla&uis=v&ui=202cb962ac59075b964b07152d234b70&c=97&adunit=320x48";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPVerveRequestUriBannerType() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSource("wap");
        final String externalKey = "1324";
        sasParams.setLocSrc("true-lat-lon");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(verveAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"trueLatLongOnly\":\"true\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 11, repositoryHelper)) {
            final String actualUrl = dcpVerveAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://adcel.vrvm.com/htmlad?ip=206.29.182.240&p=ptnr&b=1324&site=00000000-0000-0000-0000-000000000000&ua=Mozilla&lat=37.4429&long=-122.1514&c=97&size=728x90&adunit=banner";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPVerveParseResponseAd() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        final String externalKey = "19100";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";

        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(verveAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4, repositoryHelper);
        final String response =
                "<a href=\"http://c.ypcdn.com/2/c/rtd?rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&vrid=6318744211295763877&&lsrc=SP&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&ptsid=imtest&tl=1933&dest=http%3A%2F%2Fapi.yp.com%2Fdisplay%2Fv1%2Fmip%3Fappid%3Dnchx22pyyt%26requestid%3D3258f028-8ab0-4b04-86ed-d7935132def1%26category%3DMedical%2520Clinics%26visitorid%3D6318744211295763877%26srid%3Dcffe8661-a6ba-4a45-817b-41a68eca9e84_%26listings%3D171643024_%26uselid%3D1%26distance%3D171643024%3A25.8_%26numListings%3D1%26city%3DPalo%2BAlto%252C%2BCA%252C%2B94301%26searchLat%3D37.444324%26searchLon%3D-122.14969%26product%3Dimage_snapshot%26selected%3D0%26selectedLat%3D37.80506134033203%26selectedLon%3D-122.27301788330078%26time%3D1355818336208\" target=\"_blank\"><img src=\"http://display-img.g.atti.com/image-api/adimage?x=eNoVxkEKwjAQAMDXuBeppGmr8bAHEbwJBV-wTVJdXJOSpGB_bzzNJHbY6cHMSpvG0KSaflJ9Y47eNe507oa2087PLVgqePeOLclOq6twYJvrgqdUGUli5SIlwrRmvNGHZduPQiFweO4fi7dMwrlkcJxRDwcDiYpFWF4IuVBCsHGtlfifoKP0hm0RVOC_BX9v6Tat\"/></a><img src=\"http://c.ypcdn.com/2/i/rtd?vrid=6318744211295763877&rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&srid=cffe8661-a6ba-4a45-817b-41a68eca9e84&lsrc=SP&cp=_&ptsid=imtest\" style=\"display:none;\"/><img src=\"http://b.scorecardresearch.com/p?c1=3&amp;c2=6035991&amp;c14=1\" height=\"1\" width=\"1\" alt=\"*\"><img src=\"http://go.vrvm.com/t?poid=4&adnet=33&r=6405519111883133169&e=AdImpInternal&paid=5721\" width=\"1\" height=\"1\" style=\"display:none;\"/>";
        dcpVerveAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpVerveAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href=\"http://c.ypcdn.com/2/c/rtd?rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&vrid=6318744211295763877&&lsrc=SP&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&ptsid=imtest&tl=1933&dest=http%3A%2F%2Fapi.yp.com%2Fdisplay%2Fv1%2Fmip%3Fappid%3Dnchx22pyyt%26requestid%3D3258f028-8ab0-4b04-86ed-d7935132def1%26category%3DMedical%2520Clinics%26visitorid%3D6318744211295763877%26srid%3Dcffe8661-a6ba-4a45-817b-41a68eca9e84_%26listings%3D171643024_%26uselid%3D1%26distance%3D171643024%3A25.8_%26numListings%3D1%26city%3DPalo%2BAlto%252C%2BCA%252C%2B94301%26searchLat%3D37.444324%26searchLon%3D-122.14969%26product%3Dimage_snapshot%26selected%3D0%26selectedLat%3D37.80506134033203%26selectedLon%3D-122.27301788330078%26time%3D1355818336208\" target=\"_blank\"><img src=\"http://display-img.g.atti.com/image-api/adimage?x=eNoVxkEKwjAQAMDXuBeppGmr8bAHEbwJBV-wTVJdXJOSpGB_bzzNJHbY6cHMSpvG0KSaflJ9Y47eNe507oa2087PLVgqePeOLclOq6twYJvrgqdUGUli5SIlwrRmvNGHZduPQiFweO4fi7dMwrlkcJxRDwcDiYpFWF4IuVBCsHGtlfifoKP0hm0RVOC_BX9v6Tat\"/></a><img src=\"http://c.ypcdn.com/2/i/rtd?vrid=6318744211295763877&rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&srid=cffe8661-a6ba-4a45-817b-41a68eca9e84&lsrc=SP&cp=_&ptsid=imtest\" style=\"display:none;\"/><img src=\"http://b.scorecardresearch.com/p?c1=3&amp;c2=6035991&amp;c14=1\" height=\"1\" width=\"1\" alt=\"*\"><img src=\"http://go.vrvm.com/t?poid=4&adnet=33&r=6405519111883133169&e=AdImpInternal&paid=5721\" width=\"1\" height=\"1\" style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(dcpVerveAdnetwork.getHttpResponseContent(), expectedResponse);
    }

    @Test
    public void testDCPVerveParseResponseAdWap() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("wap");
        final String externalKey = "19100";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(verveAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4, repositoryHelper);

        final String response =
                "<a href=\"http://c.ypcdn.com/2/c/rtd?rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&vrid=6318744211295763877&&lsrc=SP&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&ptsid=imtest&tl=1933&dest=http%3A%2F%2Fapi.yp.com%2Fdisplay%2Fv1%2Fmip%3Fappid%3Dnchx22pyyt%26requestid%3D3258f028-8ab0-4b04-86ed-d7935132def1%26category%3DMedical%2520Clinics%26visitorid%3D6318744211295763877%26srid%3Dcffe8661-a6ba-4a45-817b-41a68eca9e84_%26listings%3D171643024_%26uselid%3D1%26distance%3D171643024%3A25.8_%26numListings%3D1%26city%3DPalo%2BAlto%252C%2BCA%252C%2B94301%26searchLat%3D37.444324%26searchLon%3D-122.14969%26product%3Dimage_snapshot%26selected%3D0%26selectedLat%3D37.80506134033203%26selectedLon%3D-122.27301788330078%26time%3D1355818336208\" target=\"_blank\"><img src=\"http://display-img.g.atti.com/image-api/adimage?x=eNoVxkEKwjAQAMDXuBeppGmr8bAHEbwJBV-wTVJdXJOSpGB_bzzNJHbY6cHMSpvG0KSaflJ9Y47eNe507oa2087PLVgqePeOLclOq6twYJvrgqdUGUli5SIlwrRmvNGHZduPQiFweO4fi7dMwrlkcJxRDwcDiYpFWF4IuVBCsHGtlfifoKP0hm0RVOC_BX9v6Tat\"/></a><img src=\"http://c.ypcdn.com/2/i/rtd?vrid=6318744211295763877&rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&srid=cffe8661-a6ba-4a45-817b-41a68eca9e84&lsrc=SP&cp=_&ptsid=imtest\" style=\"display:none;\"/><img src=\"http://b.scorecardresearch.com/p?c1=3&amp;c2=6035991&amp;c14=1\" height=\"1\" width=\"1\" alt=\"*\"><img src=\"http://go.vrvm.com/t?poid=4&adnet=33&r=6405519111883133169&e=AdImpInternal&paid=5721\" width=\"1\" height=\"1\" style=\"display:none;\"/>";
        dcpVerveAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpVerveAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href=\"http://c.ypcdn.com/2/c/rtd?rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&vrid=6318744211295763877&&lsrc=SP&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&ptsid=imtest&tl=1933&dest=http%3A%2F%2Fapi.yp.com%2Fdisplay%2Fv1%2Fmip%3Fappid%3Dnchx22pyyt%26requestid%3D3258f028-8ab0-4b04-86ed-d7935132def1%26category%3DMedical%2520Clinics%26visitorid%3D6318744211295763877%26srid%3Dcffe8661-a6ba-4a45-817b-41a68eca9e84_%26listings%3D171643024_%26uselid%3D1%26distance%3D171643024%3A25.8_%26numListings%3D1%26city%3DPalo%2BAlto%252C%2BCA%252C%2B94301%26searchLat%3D37.444324%26searchLon%3D-122.14969%26product%3Dimage_snapshot%26selected%3D0%26selectedLat%3D37.80506134033203%26selectedLon%3D-122.27301788330078%26time%3D1355818336208\" target=\"_blank\"><img src=\"http://display-img.g.atti.com/image-api/adimage?x=eNoVxkEKwjAQAMDXuBeppGmr8bAHEbwJBV-wTVJdXJOSpGB_bzzNJHbY6cHMSpvG0KSaflJ9Y47eNe507oa2087PLVgqePeOLclOq6twYJvrgqdUGUli5SIlwrRmvNGHZduPQiFweO4fi7dMwrlkcJxRDwcDiYpFWF4IuVBCsHGtlfifoKP0hm0RVOC_BX9v6Tat\"/></a><img src=\"http://c.ypcdn.com/2/i/rtd?vrid=6318744211295763877&rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&srid=cffe8661-a6ba-4a45-817b-41a68eca9e84&lsrc=SP&cp=_&ptsid=imtest\" style=\"display:none;\"/><img src=\"http://b.scorecardresearch.com/p?c1=3&amp;c2=6035991&amp;c14=1\" height=\"1\" width=\"1\" alt=\"*\"><img src=\"http://go.vrvm.com/t?poid=4&adnet=33&r=6405519111883133169&e=AdImpInternal&paid=5721\" width=\"1\" height=\"1\" style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(dcpVerveAdnetwork.getHttpResponseContent(), expectedResponse);
    }

    @Test
    public void testDCPVerveParseResponseAdApp() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("app");
        final String externalKey = "19100";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                        + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11"
                        + "?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(verveAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4, repositoryHelper);

        final String response =
                "<a href=\"http://c.ypcdn.com/2/c/rtd?rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&vrid=6318744211295763877&&lsrc=SP&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&ptsid=imtest&tl=1933&dest=http%3A%2F%2Fapi.yp.com%2Fdisplay%2Fv1%2Fmip%3Fappid%3Dnchx22pyyt%26requestid%3D3258f028-8ab0-4b04-86ed-d7935132def1%26category%3DMedical%2520Clinics%26visitorid%3D6318744211295763877%26srid%3Dcffe8661-a6ba-4a45-817b-41a68eca9e84_%26listings%3D171643024_%26uselid%3D1%26distance%3D171643024%3A25.8_%26numListings%3D1%26city%3DPalo%2BAlto%252C%2BCA%252C%2B94301%26searchLat%3D37.444324%26searchLon%3D-122.14969%26product%3Dimage_snapshot%26selected%3D0%26selectedLat%3D37.80506134033203%26selectedLon%3D-122.27301788330078%26time%3D1355818336208\" target=\"_blank\"><img src=\"http://display-img.g.atti.com/image-api/adimage?x=eNoVxkEKwjAQAMDXuBeppGmr8bAHEbwJBV-wTVJdXJOSpGB_bzzNJHbY6cHMSpvG0KSaflJ9Y47eNe507oa2087PLVgqePeOLclOq6twYJvrgqdUGUli5SIlwrRmvNGHZduPQiFweO4fi7dMwrlkcJxRDwcDiYpFWF4IuVBCsHGtlfifoKP0hm0RVOC_BX9v6Tat\"/></a><img src=\"http://c.ypcdn.com/2/i/rtd?vrid=6318744211295763877&rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&srid=cffe8661-a6ba-4a45-817b-41a68eca9e84&lsrc=SP&cp=_&ptsid=imtest\" style=\"display:none;\"/><img src=\"http://b.scorecardresearch.com/p?c1=3&amp;c2=6035991&amp;c14=1\" height=\"1\" width=\"1\" alt=\"*\"><img src=\"http://go.vrvm.com/t?poid=4&adnet=33&r=6405519111883133169&e=AdImpInternal&paid=5721\" width=\"1\" height=\"1\" style=\"display:none;\"/>";
        dcpVerveAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpVerveAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href=\"http://c.ypcdn.com/2/c/rtd?rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&vrid=6318744211295763877&&lsrc=SP&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&ptsid=imtest&tl=1933&dest=http%3A%2F%2Fapi.yp.com%2Fdisplay%2Fv1%2Fmip%3Fappid%3Dnchx22pyyt%26requestid%3D3258f028-8ab0-4b04-86ed-d7935132def1%26category%3DMedical%2520Clinics%26visitorid%3D6318744211295763877%26srid%3Dcffe8661-a6ba-4a45-817b-41a68eca9e84_%26listings%3D171643024_%26uselid%3D1%26distance%3D171643024%3A25.8_%26numListings%3D1%26city%3DPalo%2BAlto%252C%2BCA%252C%2B94301%26searchLat%3D37.444324%26searchLon%3D-122.14969%26product%3Dimage_snapshot%26selected%3D0%26selectedLat%3D37.80506134033203%26selectedLon%3D-122.27301788330078%26time%3D1355818336208\" target=\"_blank\"><img src=\"http://display-img.g.atti.com/image-api/adimage?x=eNoVxkEKwjAQAMDXuBeppGmr8bAHEbwJBV-wTVJdXJOSpGB_bzzNJHbY6cHMSpvG0KSaflJ9Y47eNe507oa2087PLVgqePeOLclOq6twYJvrgqdUGUli5SIlwrRmvNGHZduPQiFweO4fi7dMwrlkcJxRDwcDiYpFWF4IuVBCsHGtlfifoKP0hm0RVOC_BX9v6Tat\"/></a><img src=\"http://c.ypcdn.com/2/i/rtd?vrid=6318744211295763877&rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&srid=cffe8661-a6ba-4a45-817b-41a68eca9e84&lsrc=SP&cp=_&ptsid=imtest\" style=\"display:none;\"/><img src=\"http://b.scorecardresearch.com/p?c1=3&amp;c2=6035991&amp;c14=1\" height=\"1\" width=\"1\" alt=\"*\"><img src=\"http://go.vrvm.com/t?poid=4&adnet=33&r=6405519111883133169&e=AdImpInternal&paid=5721\" width=\"1\" height=\"1\" style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(dcpVerveAdnetwork.getHttpResponseContent(), expectedResponse);
    }

    @Test
    public void testDCPVerveParseResponseAdAppIMAI() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("app");
        sasParams.setSdkVersion("a371");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        final String externalKey = "19100";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(verveAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4, repositoryHelper);

        final String response =
                "<a href=\"http://c.ypcdn.com/2/c/rtd?rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&vrid=6318744211295763877&&lsrc=SP&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&ptsid=imtest&tl=1933&dest=http%3A%2F%2Fapi.yp.com%2Fdisplay%2Fv1%2Fmip%3Fappid%3Dnchx22pyyt%26requestid%3D3258f028-8ab0-4b04-86ed-d7935132def1%26category%3DMedical%2520Clinics%26visitorid%3D6318744211295763877%26srid%3Dcffe8661-a6ba-4a45-817b-41a68eca9e84_%26listings%3D171643024_%26uselid%3D1%26distance%3D171643024%3A25.8_%26numListings%3D1%26city%3DPalo%2BAlto%252C%2BCA%252C%2B94301%26searchLat%3D37.444324%26searchLon%3D-122.14969%26product%3Dimage_snapshot%26selected%3D0%26selectedLat%3D37.80506134033203%26selectedLon%3D-122.27301788330078%26time%3D1355818336208\" target=\"_blank\"><img src=\"http://display-img.g.atti.com/image-api/adimage?x=eNoVxkEKwjAQAMDXuBeppGmr8bAHEbwJBV-wTVJdXJOSpGB_bzzNJHbY6cHMSpvG0KSaflJ9Y47eNe507oa2087PLVgqePeOLclOq6twYJvrgqdUGUli5SIlwrRmvNGHZduPQiFweO4fi7dMwrlkcJxRDwcDiYpFWF4IuVBCsHGtlfifoKP0hm0RVOC_BX9v6Tat\"/></a><img src=\"http://c.ypcdn.com/2/i/rtd?vrid=6318744211295763877&rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&srid=cffe8661-a6ba-4a45-817b-41a68eca9e84&lsrc=SP&cp=_&ptsid=imtest\" style=\"display:none;\"/><img src=\"http://b.scorecardresearch.com/p?c1=3&amp;c2=6035991&amp;c14=1\" height=\"1\" width=\"1\" alt=\"*\"><img src=\"http://go.vrvm.com/t?poid=4&adnet=33&r=6405519111883133169&e=AdImpInternal&paid=5721\" width=\"1\" height=\"1\" style=\"display:none;\"/>";
        dcpVerveAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpVerveAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a href=\"http://c.ypcdn.com/2/c/rtd?rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&vrid=6318744211295763877&&lsrc=SP&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&ptsid=imtest&tl=1933&dest=http%3A%2F%2Fapi.yp.com%2Fdisplay%2Fv1%2Fmip%3Fappid%3Dnchx22pyyt%26requestid%3D3258f028-8ab0-4b04-86ed-d7935132def1%26category%3DMedical%2520Clinics%26visitorid%3D6318744211295763877%26srid%3Dcffe8661-a6ba-4a45-817b-41a68eca9e84_%26listings%3D171643024_%26uselid%3D1%26distance%3D171643024%3A25.8_%26numListings%3D1%26city%3DPalo%2BAlto%252C%2BCA%252C%2B94301%26searchLat%3D37.444324%26searchLon%3D-122.14969%26product%3Dimage_snapshot%26selected%3D0%26selectedLat%3D37.80506134033203%26selectedLon%3D-122.27301788330078%26time%3D1355818336208\" target=\"_blank\"><img src=\"http://display-img.g.atti.com/image-api/adimage?x=eNoVxkEKwjAQAMDXuBeppGmr8bAHEbwJBV-wTVJdXJOSpGB_bzzNJHbY6cHMSpvG0KSaflJ9Y47eNe507oa2087PLVgqePeOLclOq6twYJvrgqdUGUli5SIlwrRmvNGHZduPQiFweO4fi7dMwrlkcJxRDwcDiYpFWF4IuVBCsHGtlfifoKP0hm0RVOC_BX9v6Tat\"/></a><img src=\"http://c.ypcdn.com/2/i/rtd?vrid=6318744211295763877&rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&srid=cffe8661-a6ba-4a45-817b-41a68eca9e84&lsrc=SP&cp=_&ptsid=imtest\" style=\"display:none;\"/><img src=\"http://b.scorecardresearch.com/p?c1=3&amp;c2=6035991&amp;c14=1\" height=\"1\" width=\"1\" alt=\"*\"><img src=\"http://go.vrvm.com/t?poid=4&adnet=33&r=6405519111883133169&e=AdImpInternal&paid=5721\" width=\"1\" height=\"1\" style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(dcpVerveAdnetwork.getHttpResponseContent(), expectedResponse);
    }

    @Test
    public void testDCPVerveParseNoAd() throws Exception {
        final String response = "";
        dcpVerveAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpVerveAdnetwork.getHttpResponseStatusCode(), 500);
    }

    @Test
    public void testDCPVerveParseEmptyResponseCode() throws Exception {
        final String response = "";
        dcpVerveAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpVerveAdnetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpVerveAdnetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPVerveGetId() throws Exception {
        assertEquals(dcpVerveAdnetwork.getId(), "verveadv1");
    }

    @Test
    public void testDCPVerveGetImpressionId() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29"
                + "+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(verveAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15, repositoryHelper);
        assertEquals(dcpVerveAdnetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @Test
    public void testDCPVerveGetName() throws Exception {
        assertEquals(dcpVerveAdnetwork.getName(), "verve");
    }

    @Test
    public void testDCPVerveIsClickUrlReq() throws Exception {
        assertEquals(dcpVerveAdnetwork.isClickUrlRequired(), false);
    }
}
