package com.inmobi.adserve.channels.adnetworks.rubicon;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.adpool.NetworkType;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;

public class DCPRubiconAdnetwork extends AbstractDCPAdNetworkImpl {

	private static final Logger LOG = LoggerFactory
			.getLogger(DCPRubiconAdnetwork.class);

	private String latitude;
	private String longitude;
	private String zoneId;
	private int width;
	private int height;

	private static final String APP_BUNDLE = "app.bundle";
	private static final String B_SITE_ID = "site_id";
	private static final String ZONE_ID = "zone_id";
	private static final String SIZE_ID = "size_id";
	private static final String SIZE = "screen_res";
	private static final String UA = "ua";
	private static final String CLIENT_IP = "ip";
	private static final String DEVICE_OS = "device.os";
	private static final String OS_VERSION = "device.osv";
	private static final String DEVICE_ID = "device.dpid";
	private static final String SHA1_DEVICE_ID = "device.dpidsha1";
	private static final String MD5_DEVICE_ID = "device.dpidmd5";
	private static final String DEVICE_ID_TYPE = "device.dpid_type";
	private static final String CONNECTION_TYPE = "device.connectiontype";
	private static final String LAT = "geo.latitude";
	private static final String LONG = "geo.longitude";
	private static final String AD_SENSITIVE  = "i.aq_sensitivity";
	protected static final String KEYWORDS = "kw";
	protected static final String FLOOR_PRICE = "rp_floor";

	private static final String SIZE_FORMAT = "%dx%d";
	private static final String SENSITIVITY_LOW = "low";
	private static final String SENSITIVITY_HIGH = "high";
	
	private final String userName;
	private final String password;
	
	private boolean isApp;

	private static Map<Short, Integer> slotIdMap;
	static {
		slotIdMap = new HashMap<Short, Integer>();
		slotIdMap.put((short) 4, 44);
		//Mapping 320x48 to 320x50
		slotIdMap.put((short)9, 43);
		slotIdMap.put((short)10, 15);
		slotIdMap.put((short)11, 2);
		slotIdMap.put((short)12, 1);
		slotIdMap.put((short)13, 8);
		slotIdMap.put((short)15, 43);
		slotIdMap.put((short)18, 9);
		slotIdMap.put((short)19, 50);
		slotIdMap.put((short)21, 45);
		slotIdMap.put((short)23, 46);
	}

	
	
		/**
	 * @param config
	 * @param clientBootstrap
	 * @param baseRequestHandler
	 * @param serverEvent
	 */
	public DCPRubiconAdnetwork(final Configuration config,
			final Bootstrap clientBootstrap,
			final HttpRequestHandlerBase baseRequestHandler,
			final Channel serverChannel) {
		super(config, clientBootstrap, baseRequestHandler, serverChannel);
		userName = config.getString("rubicon.username");
		password = config.getString("rubicon.password");

	}

	@Override
	public boolean configureParameters() {
		if (StringUtils.isBlank(sasParams.getRemoteHostIp())
				|| StringUtils.isBlank(sasParams.getUserAgent())
				|| StringUtils.isBlank(externalSiteId)) {
			LOG.debug("mandatory parameters missing for rubicon so exiting adapter");
			return false;
		}
		host = config.getString("rubicon.host");
		if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
				&& StringUtils.countMatches(
						casInternalRequestParameters.latLong, ",") > 0) {
			String[] latlong = casInternalRequestParameters.latLong.split(",");
			latitude = latlong[0];
			longitude = latlong[1];
		}
		if (null != sasParams.getSlot()
				&& SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
			Dimension dim=null;
			if((long) sasParams.getSlot() == 9){
				dim = SlotSizeMapping.getDimension(15l);
			}
			else{
				dim = SlotSizeMapping.getDimension((long) sasParams.getSlot());
			}
					
			width = (int) Math.ceil(dim.getWidth());
			height = (int) Math.ceil(dim.getHeight());
			if (!slotIdMap.containsKey(sasParams.getSlot())) {
				LOG.debug("Size not allowed for rubicon so exiting adapter");
				return false;
			}
		} else {
			LOG.debug("mandatory parameter size missing for rubicon so exiting adapter");
			return false;
		}
		
		isApp = (StringUtils.isBlank(sasParams.getSource()) || "WAP"
				.equalsIgnoreCase(sasParams.getSource())) ? false : true;
		if(isApp && StringUtils.isEmpty(getUid())){
			LOG.debug("mandatory parameter udid is missing for APP traffic in rubicon so exiting adapter");
			return false;
		}
		
		JSONObject additionalParams = entity.getAdditionalParams();
		zoneId = getZoneId(additionalParams);
		if(null == zoneId){
			LOG.debug("Zone Id is not configured in rubicon so exiting adapter");
			return false;
	
		}
		
		LOG.info("Configure parameters inside Rubicon returned true");
		return true;
	}

	@Override
	public String getName() {
		return "rubicon";
	}

	@Override
	public URI getRequestUri() throws Exception {
		StringBuilder url = new StringBuilder(host);
		appendQueryParam(url, ZONE_ID, zoneId, false);
		if(isApp){
			appendQueryParam(url, APP_BUNDLE, blindedSiteId, false);
		}
		appendQueryParam(url, UA,
				getURLEncode(sasParams.getUserAgent(), format), false);
		appendQueryParam(url, CLIENT_IP, sasParams.getRemoteHostIp(), false);
		appendQueryParam(url, B_SITE_ID, externalSiteId, false);
		Integer sasParamsOsId = sasParams.getOsId();
		if (sasParamsOsId > 0 && sasParamsOsId < 21) {
			appendQueryParam(url, DEVICE_OS,
					HandSetOS.values()[sasParamsOsId - 1].toString(), false);
		}

		if (StringUtils.isNotBlank(sasParams.getOsMajorVersion())) {
			appendQueryParam(url, OS_VERSION, sasParams.getOsMajorVersion(),
					false);
		}
		appendQueryParam(url, SIZE, String.format(SIZE_FORMAT, width, height),
				false);
		appendQueryParam(url, SIZE_ID, slotIdMap.get(sasParams.getSlot()),
				false);
		if (StringUtils.isNotBlank(latitude)
				&& StringUtils.isNotBlank(longitude)) {
			appendQueryParam(url, LAT, latitude, false);
			appendQueryParam(url, LONG, longitude, false);
		}
		if (NetworkType.WIFI == sasParams.getNetworkType()) {
			appendQueryParam(url, CONNECTION_TYPE, 2, false);
		} else {
			appendQueryParam(url, CONNECTION_TYPE, 0, false);
		}
		if (SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
			appendQueryParam(url, AD_SENSITIVE, SENSITIVITY_LOW, false);
        }
        else {
        	appendQueryParam(url, AD_SENSITIVE, SENSITIVITY_HIGH, false);
        }
		if(casInternalRequestParameters.rtbBidFloor > 0){
			appendQueryParam(url, FLOOR_PRICE,casInternalRequestParameters.rtbBidFloor,false );
		}
		appendDeviceIds(url);
		
		appendQueryParam(url, KEYWORDS,
				blindedSiteId, false);
		LOG.debug("Rubicon url is {}", url);
		return new URI(url.toString());
	}

	private void appendDeviceIds(StringBuilder url) {
		//Device id type 1 (IDFA), 2 (OpenUDID), 3 (Apple UDID), 4 (Android device ID)
		if (sasParams.getOsId() == HandSetOS.Android.getValue()) {
			if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
				appendQueryParam(url, MD5_DEVICE_ID, casInternalRequestParameters.uidMd5,
						false);
				appendQueryParam(url, DEVICE_ID_TYPE, 4,
						false);
			}
			else if (StringUtils.isNotBlank(casInternalRequestParameters.uidIDUS1)) {
				appendQueryParam(url, SHA1_DEVICE_ID, casInternalRequestParameters.uidIDUS1,
						false);
				appendQueryParam(url, DEVICE_ID_TYPE, 4,
						false);

			}
			else if (StringUtils.isNotBlank(casInternalRequestParameters.uid)){
				appendQueryParam(url, MD5_DEVICE_ID, casInternalRequestParameters.uid,
						false);
				appendQueryParam(url, DEVICE_ID_TYPE, 2,
						false);
			}
			
		}
		else if(sasParams.getOsId() == HandSetOS.iOS.getValue()) {
			if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFA)) {
				appendQueryParam(url, DEVICE_ID, casInternalRequestParameters.uidIFA,
						false);
				appendQueryParam(url, DEVICE_ID_TYPE, 1,
						false);
			} else if (StringUtils.isNotBlank(casInternalRequestParameters.uidSO1)) {
				appendQueryParam(url, SHA1_DEVICE_ID,
						casInternalRequestParameters.uidSO1, false);
				appendQueryParam(url, DEVICE_ID_TYPE, 3,
						false);
			} else if (StringUtils.isNotBlank(casInternalRequestParameters.uidO1)) {
				appendQueryParam(url, SHA1_DEVICE_ID, casInternalRequestParameters.uidO1,
						false);
				appendQueryParam(url, DEVICE_ID_TYPE, 3,
						false);
			} else if (StringUtils.isNotBlank(casInternalRequestParameters.uid)){
				appendQueryParam(url, MD5_DEVICE_ID, casInternalRequestParameters.uid,
						false);
				appendQueryParam(url, DEVICE_ID_TYPE, 2,
						false);
			}
			
		}
	}
	
	@Override
	public Request getNingRequest() throws Exception {

	        URI uri = getRequestUri();
	        if (uri.getPort() == -1) {
	            uri = new URIBuilder(uri).setPort(80).build();
	        }
	        String authStr = userName + ":" + password;
	        String authEncoded = new String(Base64.encodeBase64(authStr.getBytes()));
	        return new RequestBuilder().setURI(uri).setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
	                .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
	                .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
	                .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp())
	                .setHeader("Authorization", "Basic " + authEncoded)
	                .setHeader(HttpHeaders.Names.HOST, uri.getHost()).build();
	   
	}

	@Override
	public void parseResponse(final String response,
			final HttpResponseStatus status) {
		LOG.debug("response is {}", response);

		if (null == response || status.code() != 200
				|| response.trim().isEmpty()) {
			statusCode = status.code();
			if (200 == statusCode) {
				statusCode = 500;
			}
			responseContent = "";
			return;
		} else {
			statusCode = status.code();
			VelocityContext context = new VelocityContext();
			try {
				JSONObject adResponse = new JSONObject(response);
				if(adResponse.getString("status").equalsIgnoreCase("ok")){
					JSONObject ad = adResponse.getJSONArray("ads").getJSONObject(0);
					String htmlContent = ad.getString("script");
					if(ad.has("impression_url")){
						String partnerBeacon = ad.getString("impression_url");
						context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, partnerBeacon);
					}
					
					if (StringUtils.isBlank(htmlContent)) {
						adStatus = "NO_AD";
						statusCode = 500;
						responseContent = "";
						return;
					}
					context.put(VelocityTemplateFieldConstants.PartnerHtmlCode,
							htmlContent);
	
					responseContent = Formatter.getResponseFromTemplate(
							TemplateType.HTML, context, sasParams, beaconUrl);
					adStatus = "AD";
				}
				else{
					adStatus = "NO_AD";
					return;
				}
			} catch (Exception exception) {
				adStatus = "NO_AD";
				LOG.info("Error parsing response from Rubicon");
				LOG.info("Response from Rubicon {}", response);
			}
		}
	}

	@Override
	public String getId() {
		return (config.getString("rubicon.advertiserId"));
	}
	
	 public String getZoneId(JSONObject additionalParams) {
	        Long[] segmentCategories = entity.getTags();
	        try{
		        if (segmentCategories != null && segmentCategories[0] != 1) {
		            for (int index = 0; index < segmentCategories.length; index++) {
		                String category = additionalParams.getString(segmentCategories[index].toString());
		                LOG.debug("segment category is {}", category);
		                if (category != null) {
		                    return category;
		                }
		            }
		        }
		        else if (sasParams.getCategories() != null) {
		            for (int index = 0; index < sasParams.getCategories().size(); index++) {
		                String category = additionalParams.getString(sasParams.getCategories().get(index).toString());
		                LOG.debug("category is {}", category);
		                if (category != null) {
		                    return category;
		                }
		            }
		        }
	        }
	        catch(JSONException exception)
	        {
	        	LOG.equals("Unable to get zone_id for Rubicon");
	        }
	        return null;
	    }


	}
