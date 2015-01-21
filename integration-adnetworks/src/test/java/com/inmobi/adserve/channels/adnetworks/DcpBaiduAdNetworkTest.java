package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.baidu.DCPBaiduAdNetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;


public class DcpBaiduAdNetworkTest extends TestCase {
    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";
    private final Bootstrap clientBootstrap = null;

    private DCPBaiduAdNetwork dcpBaiduAdNetwork;
    private final String baiduHost =
            "http://mobads.baidu.com/cpro/ui/mads.php?u=default&ie=1&n=1&tm=512&cm=512&md=1&at=3&v=api_inmobi&tpl=2";
    private final String baiduStatus = "on";
    private final String baiduAdvId = "baiduadv1";
    private final String baiduTest = "1";

    private final String baiduFormat = "xml";
    private RepositoryHelper repositoryHelper;

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("baidu.host")).andReturn(baiduHost).anyTimes();
        expect(mockConfig.getString("baidu.status")).andReturn(baiduStatus).anyTimes();
        expect(mockConfig.getString("baidu.test")).andReturn(baiduTest).anyTimes();
        expect(mockConfig.getString("baidu.advertiserId")).andReturn(baiduAdvId).anyTimes();

        expect(mockConfig.getString("baidu.format")).andReturn(baiduFormat).anyTimes();
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
        dcpBaiduAdNetwork = new DCPBaiduAdNetwork(mockConfig, clientBootstrap, base, serverChannel);
        
        
        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);
        
        dcpBaiduAdNetwork.setHost(baiduHost);
    }

    @Test
    public void testDCPbaiduConfigureParameterBlankUid() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setOsId(HandSetOS.iOS.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");

        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "debug";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(baiduAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpBaiduAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPbaiduConfigureParameterSuccess() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setOsId(HandSetOS.iOS.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");

        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(baiduAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertTrue(dcpBaiduAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPbaiduConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(baiduAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpBaiduAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPbaiduConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(baiduAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpBaiduAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPbaiduRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");

        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSiteIncId(6575868);
        sasParams.setOsId(HandSetOS.Android.getValue());
        final String externalKey = "debug";

        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                        + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(baiduAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpBaiduAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, beaconUrl, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpBaiduAdNetwork.getRequestUri().toString();

            final String expectedUrl =
                    "http://mobads.baidu.com/cpro/ui/mads.php?u=default&ie=1&n=1&tm=512&cm=512&md=1&at=3&v=api_inmobi&tpl=2&appid=debug&os=android&w=50&h=320&ip=206.29.182.240&impt=http%3A%2F%2Fc2.w.inmobi.com%2Fc.asm%2F4%2Fb%2Fbx5%2Fyaz%2F2%2Fb%2Fa5%2Fm%2F0%2F0%2F0%2F202cb962ac59075b964b07152d234b70%2F4f8d98e2-4bbd-40bc-87e5-22da170600f9%2F-1%2F1%2F9cddca11%3Fbeacon%3Dtrue&clkt=http%3A%2F%2Fc2.w.inmobi.com%2Fc.asm%2F4%2Fb%2Fbx5%2Fyaz%2F2%2Fb%2Fa5%2Fm%2F0%2F0%2F0%2F202cb962ac59075b964b07152d234b70%2F4f8d98e2-4bbd-40bc-87e5-22da170600f9%2F-1%2F1%2F9cddca11%3Fds%3D1&sn=202cb962ac59075b964b07152d234b70&q=debug_cpr&act=LP%2CPH%2CDL%2CMAP%2CSMS%2CMAI%2CVD%2CRM";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPbaiduRequestUriWithLatLong() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final List<Long> category = new ArrayList<Long>();
        category.add(1l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSiteIncId(6575868);
        sasParams.setOsId(HandSetOS.Android.getValue());
        final String externalKey = "debug";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(baiduAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpBaiduAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpBaiduAdNetwork.getRequestUri().toString();
            final String urlWithOutGeo = actualUrl.substring(0, actualUrl.indexOf("&g="));
            final String expectedUrl =
                    "http://mobads.baidu.com/cpro/ui/mads.php?u=default&ie=1&n=1&tm=512&cm=512&md=1&at=3&v=api_inmobi&tpl=2&appid=debug&os=android&w=50&h=320&ip=206.29.182.240&impt=&clkt=&sn=202cb962ac59075b964b07152d234b70&q=debug_cpr&act=LP%2CPH%2CDL%2CMAP%2CSMS%2CMAI%2CVD%2CRM";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(urlWithOutGeo).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(urlWithOutGeo).getPath());        }
    }

    @Test
    public void testDCPbaiduParseAd() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setOsId(HandSetOS.Android.getValue());
        casInternalRequestParameters.setUid("23e2ewq445545saasw232323");
        final String externalKey = "19100";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                        + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(baiduAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpBaiduAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);
        final String response =
                "<html lang=\"en\"><head>    <meta charset=\"UTF-8\">    <meta name=\"viewport\" content=\"width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0\">    <title>mobads</title>    <script type=\"text/javascript\">        function mobadsAdClicked(){            new Image().src = \"http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1\";        }    </script>    </head>    <body style=\"margin:0;\">        <div class=\"mobads-api-container\" style=\"max-width:100%;font-size:100%;\">            <div class=\"mobads-api-ad\" style='margin:0;border:1px solid #ebebeb;position:relative;overflow:hidden;padding:0.2em 0 0.2em 1.3em;'>    <a target=\"_blank\" href=\"http://mobads.baidu.com/ad.html?url=http%3A%2F%2Fpage.baidu.com%2Fwww.webinternational.com.cn%2F3bq4k_3pi1_i.html%3F__mobads_clickid%3DuANBIyIxnHDvP1m1g1csnhPBrHmzmyndrH0LPyc4PWwBnjTkPHFbnWnYmWTs%26__mobads_charge%3DnHDvP1m1r1fznHTzr1RdrjR4nWfenHcdPWDePH0snj_zr1DeuANBIyIxmLKzrvw-mMNMr1csnhPBrHmzmyndrH0LPyc4PWwBnjTkPHFbnWnYmWTsr1_hFW0hFW0hFBmhnamhnamh%26__mobads_ta%3DmLwzrWmYnik9uAGdTLfln0%26__mobads_qk%3D5329763a47869774f9b5f55a%26__mobads_curl_check%3D1302967414&v=api2&sn=&clk=1\" onclick=\"mobadsAdClicked();\" style=\"text-decoration: none;line-height:0;\">        <span style=\"white-space:nowrap;font-family:Microsoft YaHei;font-size:150%;\">            <span class=\"tit\" style=\"font-weight:bold;color:#003399;line-height:1.2em;\">YES YES</span><br>            <span class=\"desc\" style=\"color:#999;font-size:63%;line-height:1.2em;\">Come on</span>        </span>    </a>    <img src=\"http://mobads.baidu.com/ads/img/logo_bottom_left.png\" style=\"position:absolute;bottom:0;left:0;width:7.4em;height:1.2em;margin-left:-5.6em;\"></div>        <img src=\"http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true\" style=\"display:none;\"><img src=\"http://cq01-testing-mobads06.vm.baidu.com:8092/ad.log?url2=nHDvP1m1QjfznHTzQjRdrjR4nWf_nHcdPWD_PH0snaszQaYzPHmdnj0zP1f_uANBIyIxmLKzQAw-mMNMQjcsnhPBrHmzmyndrH0LPyc4PWwBnjTkPHFbnWnYmWTsQashFW0hFW0hFBmhnamhnamh&__mobads_ta=mLwzrWmYnik9uAGdTLfln000&__mobads_qk=5329763a47869774f9b5f55a&v=api2&extra2=nj0snjDsnj0snj0snj0sniskrHTsnjDsnH0snj0sn0Cb\" style=\"display:none;\">        </div>    </body></html>";
        dcpBaiduAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpBaiduAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><html lang=\"en\"><head>    <meta charset=\"UTF-8\">    <meta name=\"viewport\" content=\"width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0\">    <title>mobads</title>    <script type=\"text/javascript\">        function mobadsAdClicked(){            new Image().src = \"http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1\";        }    </script>    </head>    <body style=\"margin:0;\">        <div class=\"mobads-api-container\" style=\"max-width:100%;font-size:100%;\">            <div class=\"mobads-api-ad\" style='margin:0;border:1px solid #ebebeb;position:relative;overflow:hidden;padding:0.2em 0 0.2em 1.3em;'>    <a target=\"_blank\" href=\"http://mobads.baidu.com/ad.html?url=http%3A%2F%2Fpage.baidu.com%2Fwww.webinternational.com.cn%2F3bq4k_3pi1_i.html%3F__mobads_clickid%3DuANBIyIxnHDvP1m1g1csnhPBrHmzmyndrH0LPyc4PWwBnjTkPHFbnWnYmWTs%26__mobads_charge%3DnHDvP1m1r1fznHTzr1RdrjR4nWfenHcdPWDePH0snj_zr1DeuANBIyIxmLKzrvw-mMNMr1csnhPBrHmzmyndrH0LPyc4PWwBnjTkPHFbnWnYmWTsr1_hFW0hFW0hFBmhnamhnamh%26__mobads_ta%3DmLwzrWmYnik9uAGdTLfln0%26__mobads_qk%3D5329763a47869774f9b5f55a%26__mobads_curl_check%3D1302967414&v=api2&sn=&clk=1\" onclick=\"mobadsAdClicked();\" style=\"text-decoration: none;line-height:0;\">        <span style=\"white-space:nowrap;font-family:Microsoft YaHei;font-size:150%;\">            <span class=\"tit\" style=\"font-weight:bold;color:#003399;line-height:1.2em;\">YES YES</span><br>            <span class=\"desc\" style=\"color:#999;font-size:63%;line-height:1.2em;\">Come on</span>        </span>    </a>    <img src=\"http://mobads.baidu.com/ads/img/logo_bottom_left.png\" style=\"position:absolute;bottom:0;left:0;width:7.4em;height:1.2em;margin-left:-5.6em;\"></div>        <img src=\"http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true\" style=\"display:none;\"><img src=\"http://cq01-testing-mobads06.vm.baidu.com:8092/ad.log?url2=nHDvP1m1QjfznHTzQjRdrjR4nWf_nHcdPWD_PH0snaszQaYzPHmdnj0zP1f_uANBIyIxmLKzQAw-mMNMQjcsnhPBrHmzmyndrH0LPyc4PWwBnjTkPHFbnWnYmWTsQashFW0hFW0hFBmhnamhnamh&__mobads_ta=mLwzrWmYnik9uAGdTLfln000&__mobads_qk=5329763a47869774f9b5f55a&v=api2&extra2=nj0snjDsnj0snj0snj0sniskrHTsnjDsnH0snj0sn0Cb\" style=\"display:none;\">        </div>    </body></html></body></html>",
                dcpBaiduAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPbaiduParseNoAd() throws Exception {
        final String response = "";
        dcpBaiduAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpBaiduAdNetwork.getHttpResponseStatusCode());
    }

    @Test
    public void testDCPbaiduParseEmptyResponseCode() throws Exception {
        final String response = "";
        dcpBaiduAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpBaiduAdNetwork.getHttpResponseStatusCode());
        assertEquals("", dcpBaiduAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPbaiduGetId() throws Exception {
        assertEquals(baiduAdvId, dcpBaiduAdNetwork.getId());
    }

    @Test
    public void testDCPbaiduGetImpressionId() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                        + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(baiduAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpBaiduAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 11, repositoryHelper);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpBaiduAdNetwork.getImpressionId());
    }

    @Test
    public void testDCPbaiduGetName() throws Exception {
        assertEquals("baidu", dcpBaiduAdNetwork.getName());
    }

    @Test
    public void testDCPbaiduIsClickUrlReq() throws Exception {
        assertEquals(true, dcpBaiduAdNetwork.isClickUrlRequired());
    }

}
