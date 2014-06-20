package com.inmobi.adserve.channels.adnetworks;
import com.inmobi.adserve.channels.adnetworks.rubicon.DCPRubiconAdnetwork;
import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import junit.framework.TestCase;
import org.apache.commons.configuration.Configuration;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;


public class DCPRubiconAdnetworkTest extends TestCase {
	private Configuration mockConfig = null;
	private final String debug = "debug";
	private final String loggerConf = "/tmp/channel-server.properties";
	private final Bootstrap clientBootstrap = null;

	private DCPRubiconAdnetwork dcpRubiconAdNetwork;
	private final String rubiconHost = "http://staged-by.rubiconproject.com/a/api/server.js?account_id=11726&rp_pmp_tier=2";
	private final String rubiconStatus = "on";
	private final String rubiconAdvId = "rubiconadv1";
	private final String rubiconTest = "1";
	private final String rubiconUser = "inmobi";
	private final String rubiconPassword = "test";

	public void prepareMockConfig() {
		mockConfig = createMock(Configuration.class);
		expect(mockConfig.getString("rubicon.host")).andReturn(rubiconHost)
		.anyTimes();
		expect(mockConfig.getString("rubicon.status")).andReturn(rubiconStatus)
		.anyTimes();
		expect(mockConfig.getString("rubicon.test")).andReturn(rubiconTest)
		.anyTimes();
		expect(mockConfig.getString("rubicon.password")).andReturn(
				rubiconPassword).anyTimes();
		expect(mockConfig.getString("rubicon.username")).andReturn(
				rubiconUser).anyTimes();
		expect(mockConfig.getString("rubicon.advertiserId")).andReturn(
				rubiconAdvId).anyTimes();
		expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
		expect(mockConfig.getString("slf4jLoggerConf")).andReturn(
				"/opt/mkhoj/conf/cas/logger.xml");
		expect(mockConfig.getString("log4jLoggerConf")).andReturn(
				"/opt/mkhoj/conf/cas/channel-server.properties");
		replay(mockConfig);
	}

	@Override
	public void setUp() throws Exception {
		File f;
		f = new File(loggerConf);
		if (!f.exists()) {
			f.createNewFile();
		}

		HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
		Channel serverChannel = createMock(Channel.class);
		prepareMockConfig();
		SlotSizeMapping.init();
		Formatter.init();
		dcpRubiconAdNetwork = new DCPRubiconAdnetwork(mockConfig,
				clientBootstrap, base, serverChannel);
		;
	}

	@Test
	public void testDCPrubiconConfigureParametersAppBlankUid() throws JSONException {
		SASRequestParameters sasParams = new SASRequestParameters();
		CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
		sasParams.setRemoteHostIp("206.29.182.240");
		sasParams.setSlot(Short.valueOf("11"));
		sasParams.setSource("APP");
		sasParams.setOsId(HandSetOS.Android.getValue());
		sasParams
		.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
		casInternalRequestParameters.latLong = "37.4429,-122.1514";
		String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
		sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
		String externalKey = "f6wqjq1r5v";
		ChannelSegmentEntity entity = new ChannelSegmentEntity(
				AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId,
						null, null, null, 0, null, null, true, true,
						externalKey, null, null, null, 0, true, null, null, 0,
						null, false, false, false, false, false, false, false,
						false, false, false, new JSONObject(
								"{\"3\":\"160212\",\"site\":\"191002\"}"), new ArrayList<Integer>(),
								0.0d, null, null, 32));
		assertEquals(false, dcpRubiconAdNetwork.configureParameters(sasParams,
				casInternalRequestParameters, entity, clurl, null));
	}
	public void testDCPrubiconConfigureParametersAppWithUid() throws JSONException {
		SASRequestParameters sasParams = new SASRequestParameters();
		CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
		sasParams.setRemoteHostIp("206.29.182.240");
		sasParams.setSlot(Short.valueOf("11"));
		sasParams.setOsId(HandSetOS.Android.getValue());
		sasParams.setSource("APP");
		List<Long> category = new ArrayList<Long>();
		category.add(3l);
		sasParams.setCategories(category);
		sasParams
		.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
		casInternalRequestParameters.latLong = "37.4429,-122.1514";
		casInternalRequestParameters.uid = "23e2ewq445545";
		String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
		sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
		String externalKey = "f6wqjq1r5v";
		ChannelSegmentEntity entity = new ChannelSegmentEntity(
				AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId,
						null, null, null, 0, null, null, true, true,
						externalKey, null, null, null, 0, true, null, null, 0,
						null, false, false, false, false, false, false, false,
						false, false, false, new JSONObject(
								"{\"3\":\"160212\",\"site\":\"191002\"}"), new ArrayList<Integer>(),
								0.0d, null, null, 32));
		assertEquals(true, dcpRubiconAdNetwork.configureParameters(sasParams,
				casInternalRequestParameters, entity, clurl, null));
	}




	@Test
	public void testDCPrubiconConfigureParametersIOS() throws JSONException {
		SASRequestParameters sasParams = new SASRequestParameters();
		CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
		sasParams.setRemoteHostIp("206.29.182.240");
		sasParams.setSlot(Short.valueOf("11"));
		sasParams.setOsId(HandSetOS.iOS.getValue());
		sasParams
		.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
		casInternalRequestParameters.latLong = "37.4429,-122.1514";
		casInternalRequestParameters.uidIFA = "23e2ewq445545";
		String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
		sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
		List<Long> category = new ArrayList<Long>();
		category.add(3l);
		sasParams.setCategories(category);
		String externalKey = "f6wqjq1r5v";
		ChannelSegmentEntity entity = new ChannelSegmentEntity(
				AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId,
						null, null, null, 0, null, null, true, true,
						externalKey, null, null, null, 0, true, null, null, 0,
						null, false, false, false, false, false, false, false,
						false, false, false, new JSONObject(
								"{\"3\":\"160212\",\"site\":\"191002\"}"), new ArrayList<Integer>(),
								0.0d, null, null, 32));
		assertEquals(true, dcpRubiconAdNetwork.configureParameters(sasParams,
				casInternalRequestParameters, entity, clurl, null));
	}

	@Test
	public void testDCPrubiconConfigureParametersWap() throws JSONException {
		SASRequestParameters sasParams = new SASRequestParameters();
		CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
		sasParams.setRemoteHostIp("206.29.182.240");
		sasParams.setSlot(Short.valueOf("11"));
		sasParams.setOsId(HandSetOS.webOS.getValue());
		List<Long> category = new ArrayList<Long>();
		category.add(3l);
		sasParams.setCategories(category);
		sasParams
		.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
		casInternalRequestParameters.latLong = "37.4429,-122.1514";
		String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
		sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
		String externalKey = "f6wqjq1r5v";
		ChannelSegmentEntity entity = new ChannelSegmentEntity(
				AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId,
						null, null, null, 0, null, null, true, true,
						externalKey, null, null, null, 0, true, null, null, 0,
						null, false, false, false, false, false, false, false,
						false, false, false, new JSONObject(
								"{\"3\":\"160212\",\"site\":\"191002\"}"), new ArrayList<Integer>(),
								0.0d, null, null, 32));
		assertEquals(true, dcpRubiconAdNetwork.configureParameters(sasParams,
				casInternalRequestParameters, entity, clurl, null));
	}

	@Test
	public void testDCPrubiconConfigureParametersBlankIP() throws JSONException {
		SASRequestParameters sasParams = new SASRequestParameters();
		CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
		sasParams.setRemoteHostIp(null);
		sasParams
		.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
		casInternalRequestParameters.latLong = "37.4429,-122.1514";
		String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
		sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
		String externalKey = "f6wqjq1r5v";
		ChannelSegmentEntity entity = new ChannelSegmentEntity(
				AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId,
						null, null, null, 0, null, null, true, true,
						externalKey, null, null, null, 0, true, null, null, 0,
						null, false, false, false, false, false, false, false,
						false, false, false, new JSONObject(
								"{\"3\":\"160212\",\"site\":\"191002\"}"), new ArrayList<Integer>(),
								0.0d, null, null, 32));
		assertEquals(false, dcpRubiconAdNetwork.configureParameters(sasParams,
				casInternalRequestParameters, entity, clurl, null));
	}

	@Test
	public void testDCPrubiconConfigureParametersBlankUA() throws JSONException {
		SASRequestParameters sasParams = new SASRequestParameters();
		CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
		sasParams.setRemoteHostIp("206.29.182.240");
		sasParams.setUserAgent(" ");
		casInternalRequestParameters.latLong = "37.4429,-122.1514";
		String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
		sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
		String externalKey = "f6wqjq1r5v";
		ChannelSegmentEntity entity = new ChannelSegmentEntity(
				AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId,
						null, null, null, 0, null, null, true, true,
						externalKey, null, null, null, 0, true, null, null, 0,
						null, false, false, false, false, false, false, false,
						false, false, false, new JSONObject(
								"{\"3\":\"160212\",\"site\":\"191002\"}"), new ArrayList<Integer>(),
								0.0d, null, null, 32));
		assertEquals(false, dcpRubiconAdNetwork.configureParameters(sasParams,
				casInternalRequestParameters, entity, clurl, null));
	}

	@Test
	public void testDCPrubiconRequestUri() throws Exception {
		SASRequestParameters sasParams = new SASRequestParameters();
		CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
		casInternalRequestParameters.blockedCategories = new ArrayList<Long>(
				Arrays.asList(new Long[] { 50l, 51l }));
		sasParams.setRemoteHostIp("206.29.182.240");
		sasParams.setUserAgent(URLEncoder.encode("Mozilla/5.0 (iPhone; CPU iPhone OS 7_0_5 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Mobile/11B601", "UTF-8"));
		sasParams.setSource("APP");
		casInternalRequestParameters.latLong = "37.4429,-122.1514";
		sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
		List<Long> category = new ArrayList<Long>();
		category.add(3l);
		sasParams.setCategories(category);
		casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
		sasParams.setSlot(Short.valueOf("15"));
		sasParams.setSiteIncId(6575868);
		sasParams.setOsId(HandSetOS.Android.getValue());
		String externalKey = "38132";
		SlotSizeMapping.init();
		ChannelSegmentEntity entity = new ChannelSegmentEntity(
				AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId,
						null, null, null, 0, null, null, true, true,
						externalKey, null, null, null, 0, true, null, null, 0,
						null, false, false, false, false, false, false, false,
						false, false, false, new JSONObject(
								"{\"3\":\"160212\",\"site\":\"38132\"}"), new ArrayList<Integer>(),
								0.0d, null, null, 0));

		dcpRubiconAdNetwork.configureParameters(sasParams,casInternalRequestParameters, entity, null, null);

		String actualUrl = dcpRubiconAdNetwork.getRequestUri().toString();
		String expectedUrl = "http://staged-by.rubiconproject.com/a/api/server.js?account_id=11726&rp_pmp_tier=2&zone_id=160212&app.bundle=com.inmobi-exchange.00000000-0000-0000-0000-0000006456fc&app.domain=com.inmobi-exchange&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+7_0_5+like+Mac+OS+X%29+AppleWebKit%2F537.51.1+%28KHTML%2C+like+Gecko%29+Mobile%2F11B601&ip=206.29.182.240&site_id=38132&device.os=Android&size_id=43&geo.latitude=37.4429&geo.longitude=-122.1514&device.connectiontype=0&i.aq_sensitivity=high&app.rating=4%2B&i.category=Business&i.iab=IAB19-15%2CIAB5-15%2CIAB3%2CIAB4&device.dpidmd5=202cb962ac59075b964b07152d234b70&device.dpid_type=open-udid&kw=38132";

		assertEquals(expectedUrl, actualUrl);
		dcpRubiconAdNetwork.getNingRequest();
	}

	@Test
	public void testDCPrubiconRequestUriWithSpecificSlot() throws Exception {
		SASRequestParameters sasParams = new SASRequestParameters();
		CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
		casInternalRequestParameters.blockedCategories = new ArrayList<Long>(
				Arrays.asList(new Long[] { 50l, 51l }));
		sasParams.setRemoteHostIp("206.29.182.240");
		sasParams.setUserAgent("Mozilla");
		sasParams.setSource("APP");
		casInternalRequestParameters.latLong = "37.4429,-122.1514";
		sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
		List<Long> category = new ArrayList<Long>();
		category.add(3l);
		sasParams.setCategories(category);

		casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
		sasParams.setSlot(Short.valueOf("9"));
		sasParams.setSiteIncId(6575868);
		sasParams.setSource("APP");
		sasParams.setOsId(HandSetOS.Android.getValue());
		String externalKey = "38132";
		SlotSizeMapping.init();
		ChannelSegmentEntity entity = new ChannelSegmentEntity(
				AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId,
						null, null, null, 0, null, null, true, true,
						externalKey, null, null, null, 0, true, null, null, 0,
						null, false, false, false, false, false, false, false,
						false, false, false, new JSONObject(
								"{\"3\":\"160212\",\"site\":\"38132\"}"), new ArrayList<Integer>(),
								0.0d, null, null, 0));
		dcpRubiconAdNetwork.configureParameters(sasParams,
				casInternalRequestParameters, entity, null, null);
		String actualUrl = dcpRubiconAdNetwork.getRequestUri().toString();
		String expectedUrl = "http://staged-by.rubiconproject.com/a/api/server.js?account_id=11726&rp_pmp_tier=2&zone_id=160212&app.bundle=com.inmobi-exchange.00000000-0000-0000-0000-0000006456fc&app.domain=com.inmobi-exchange&ua=Mozilla&ip=206.29.182.240&site_id=38132&device.os=Android&size_id=43&geo.latitude=37.4429&geo.longitude=-122.1514&device.connectiontype=0&i.aq_sensitivity=high&app.rating=4%2B&i.category=Business&i.iab=IAB19-15%2CIAB5-15%2CIAB3%2CIAB4&device.dpidmd5=202cb962ac59075b964b07152d234b70&device.dpid_type=open-udid&kw=38132";
		assertEquals(expectedUrl, actualUrl);

	}

	@Test
	public void testDCPrubiconParseAdWap() throws Exception {
		SASRequestParameters sasParams = new SASRequestParameters();
		CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
		casInternalRequestParameters.blockedCategories = new ArrayList<Long>(
				Arrays.asList(new Long[] { 50l, 51l }));
		sasParams.setRemoteHostIp("206.29.182.240");
		sasParams.setUserAgent("Mozilla");
		sasParams.setSlot(Short.valueOf("15"));
		casInternalRequestParameters.latLong = "37.4429,-122.1514";
		sasParams.setOsId(HandSetOS.Android.getValue());
		casInternalRequestParameters.uid = "23e2ewq445545saasw232323";
		String externalKey = "19100";
		String beaconUrl = "http://c2.w.inmobi.com/c"
				+ ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
				+ "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

		ChannelSegmentEntity entity = new ChannelSegmentEntity(
				AdNetworksTest
				.getChannelSegmentEntityBuilder(
						rubiconAdvId,
						null,
						null,
						null,
						0,
						null,
						null,
						true,
						true,
						externalKey,
						null,
						null,
						null,
						0,
						true,
						null,
						null,
						0,
						null,
						false,
						false,
						false,
						false,
						false,
						false,
						false,
						false,
						false,
						false,
						new JSONObject(
								"{\"3\":\"160212\",\"site\":\"19100\"}"),
								new ArrayList<Integer>(), 0.0d, null, null, 32));
		dcpRubiconAdNetwork.configureParameters(sasParams,
				casInternalRequestParameters, entity, null, beaconUrl);
		String response = "{\"status\" : \"ok\",\"tracking\" : \"affiliate-1234\",\"inventory\" : { \"deals\" : \"12345,98765\" },\"ads\" : [{\"status\" : \"ok\",\"impression_id\" : \"ed4122f3-f4ac-477b-9abd-89c44f252100\",\"size_id\" : \"2\",\"advertiser\" : 7,\"network\" : 123,\"seat\" : 456,\"deal\" : 789,\"type\" : \"MRAIDv2\",\"creativeapi\" : 1000,\"impression_url\" : \"http://ad.tracker/impression/ed4122f3-f4ac-477b-9abd-89c44f252100\",\"script\" :\"<div>testing rubicon</div>\"}]}";
		dcpRubiconAdNetwork.parseResponse(response, HttpResponseStatus.OK);
		assertEquals(200, dcpRubiconAdNetwork.getHttpResponseStatusCode());
		assertEquals(
				"<html><head><title></title><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script><div>testing rubicon</div></script><img src='http://ad.tracker/impression/ed4122f3-f4ac-477b-9abd-89c44f252100' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
				dcpRubiconAdNetwork.getHttpResponseContent());
	}

	@Test
	public void testDCPrubiconParseAdApp() throws Exception {
		SASRequestParameters sasParams = new SASRequestParameters();
		CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
		casInternalRequestParameters.blockedCategories = new ArrayList<Long>(
				Arrays.asList(new Long[] { 50l, 51l }));
		sasParams.setRemoteHostIp("206.29.182.240");
		sasParams.setUserAgent("Mozilla");
		sasParams.setSlot(Short.valueOf("15"));
		casInternalRequestParameters.latLong = "37.4429,-122.1514";
		sasParams.setOsId(HandSetOS.Android.getValue());
		sasParams.setSource("APP");
		casInternalRequestParameters.uid = "23e2ewq445545saasw232323";
		String externalKey = "19100";
		String beaconUrl = "http://c2.w.inmobi.com/c"
				+ ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
				+ "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

		ChannelSegmentEntity entity = new ChannelSegmentEntity(
				AdNetworksTest
				.getChannelSegmentEntityBuilder(
						rubiconAdvId,
						null,
						null,
						null,
						0,
						null,
						null,
						true,
						true,
						externalKey,
						null,
						null,
						null,
						0,
						true,
						null,
						null,
						0,
						null,
						false,
						false,
						false,
						false,
						false,
						false,
						false,
						false,
						false,
						false,
						new JSONObject(
								"{\"3\":\"160212\",\"site\":\"19100\"}"),
								new ArrayList<Integer>(), 0.0d, null, null, 32));
		dcpRubiconAdNetwork.configureParameters(sasParams,
				casInternalRequestParameters, entity, null, beaconUrl);
		String response = "{\"status\" : \"ok\",\"tracking\" : \"affiliate-1234\",\"inventory\" : { \"deals\" : \"12345,98765\" },\"ads\" : [{\"status\" : \"ok\",\"impression_id\" : \"ed4122f3-f4ac-477b-9abd-89c44f252100\",\"size_id\" : \"2\",\"advertiser\" : 7,\"network\" : 123,\"seat\" : 456,\"deal\" : 789,\"type\" : \"MRAIDv2\",\"creativeapi\" : 1000,\"impression_url\" : \"http://ad.tracker/impression/ed4122f3-f4ac-477b-9abd-89c44f252100\",\"script\" :\"<div>testing rubicon</div>\"}]}";
		dcpRubiconAdNetwork.parseResponse(response, HttpResponseStatus.OK);
		assertEquals(200, dcpRubiconAdNetwork.getHttpResponseStatusCode());
		assertEquals(
				"<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script><div>testing rubicon</div></script><img src='http://ad.tracker/impression/ed4122f3-f4ac-477b-9abd-89c44f252100' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
				dcpRubiconAdNetwork.getHttpResponseContent());
	}


	@Test
	public void testDCPrubiconParseNoAd() throws Exception {
		String response = "";
		dcpRubiconAdNetwork.parseResponse(response, HttpResponseStatus.OK);
		assertEquals(500, dcpRubiconAdNetwork.getHttpResponseStatusCode());
	}


	@Test
	public void testDCPrubiconGetId() throws Exception {
		assertEquals(rubiconAdvId, dcpRubiconAdNetwork.getId());
	}

	@Test
	public void testDCPrubiconGetImpressionId() throws Exception {
		SASRequestParameters sasParams = new SASRequestParameters();
		CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
		sasParams.setRemoteHostIp("206.29.182.240");
		sasParams
		.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
		casInternalRequestParameters.latLong = "37.4429,-122.1514";
		String clurl = "http://c2.w.inmobi.com/c"
				+ ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
				+ "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
		sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
		String externalKey = "f6wqjq1r5v";
		ChannelSegmentEntity entity = new ChannelSegmentEntity(
				AdNetworksTest
				.getChannelSegmentEntityBuilder(
						rubiconAdvId,
						null,
						null,
						null,
						0,
						null,
						null,
						true,
						true,
						externalKey,
						null,
						null,
						null,
						0,
						true,
						null,
						null,
						0,
						null,
						false,
						false,
						false,
						false,
						false,
						false,
						false,
						false,
						false,
						false,
						new JSONObject(
								"{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
								new ArrayList<Integer>(), 0.0d, null, null, 32));
		dcpRubiconAdNetwork.configureParameters(sasParams,
				casInternalRequestParameters, entity, clurl, null);
		assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9",
				dcpRubiconAdNetwork.getImpressionId());
	}

	@Test
	public void testDCPrubiconGetName() throws Exception {
		assertEquals("rubicon", dcpRubiconAdNetwork.getName());
	}

	@Test
	public void testDCPrubiconIsClickUrlReq() throws Exception {
		assertEquals(false, dcpRubiconAdNetwork.isClickUrlRequired());
	}


}
