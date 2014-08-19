package com.inmobi.adserve.channels.adnetworks.verve;

import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;

public class DCPVerveAdNetwork extends AbstractDCPAdNetworkImpl {

	private static final Logger LOG = LoggerFactory
			.getLogger(DCPVerveAdNetwork.class);

	private transient String latitude;
	private transient String longitude;
	private int width;
	private int height;
	private String portalKeyword;
	private String adUnit;
	private static final String IPHONE_KEYWORD = "iphn";
	private static final String ANDROID_KEYWORD = "anap";
	private static final String WAP_KEYWORD = "ptnr";
	private static final String WAP = "wap";
	private static final String DERIVED_LAT_LONG = "DERIVED_LAT_LON";
	private static final String TRUE_LAT_LONG_ONLY = "trueLatLongOnly";
	private static final String MMA = "mma";
	private static final String BANNER = "banner";
	private static final String INTER = "inter";
	private boolean sendTrueLatLongOnly;

	public DCPVerveAdNetwork(final Configuration config,
			final Bootstrap clientBootstrap,
			final HttpRequestHandlerBase baseRequestHandler,
			final Channel serverChannel) {
		super(config, clientBootstrap, baseRequestHandler, serverChannel);
	}

	@Override
	public boolean configureParameters() {
		if (StringUtils.isBlank(sasParams.getRemoteHostIp())
				|| StringUtils.isBlank(sasParams.getUserAgent())
				|| StringUtils.isBlank(externalSiteId)) {
			LOG.debug("mandatory parameters missing for verve so exiting adapter");
			return false;
		}
		host = config.getString("verve.host");

		try {
			// TRUE_LAT_LONG_ONLY is configured as the additional param in the
			// segment table
			sendTrueLatLongOnly = Boolean.parseBoolean(entity
					.getAdditionalParams().getString(TRUE_LAT_LONG_ONLY));
		} catch (JSONException e) {
			sendTrueLatLongOnly = false;
			LOG.info("trueLatLong is not configured for the segment:{} {}",
					entity.getExternalSiteKey(), this.getName());
		}

		if (sendTrueLatLongOnly) {
			if (DERIVED_LAT_LONG.equalsIgnoreCase(sasParams.getLocSrc())) {
				return false;
			} else if (casInternalRequestParameters.latLong != null
					&& StringUtils.countMatches(
							casInternalRequestParameters.latLong, ",") > 0) {
				String[] latlong = casInternalRequestParameters.latLong
						.split(",");
				latitude = latlong[0];
				longitude = latlong[1];
			} else {
				return false;
			}
			if (StringUtils.isBlank(latitude) || StringUtils.isBlank(longitude)) {
				return false;
			}
		} else if (!DERIVED_LAT_LONG.equalsIgnoreCase(sasParams.getLocSrc())
				&& StringUtils.isNotBlank(sasParams.getLocSrc())) { // request
																	// has true
																	// lat-long
			return false;
		}
		adUnit = MMA;
		if (null != sasParams.getSlot()
				&& SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
			Dimension dim = SlotSizeMapping.getDimension((long) sasParams
					.getSlot());
			width = (int) Math.ceil(dim.getWidth());
			height = (int) Math.ceil(dim.getHeight());
			if (sasParams.getSlot() == 11) {
				adUnit = BANNER;
			} else if (sasParams.getSlot() == 10 || sasParams.getSlot() == 14) {
				adUnit = INTER;
			}
		}

		if (WAP.equalsIgnoreCase(sasParams.getSource())) {
			portalKeyword = WAP_KEYWORD;
		} else if (sasParams.getOsId() == HandSetOS.iOS.getValue()) {
			portalKeyword = IPHONE_KEYWORD;
		} else if (sasParams.getOsId() == HandSetOS.Android.getValue()) {
			if (StringUtils.isBlank(sasParams.getSdkVersion())
					|| sasParams.getSdkVersion().toLowerCase()
							.startsWith("a35")) {
				LOG.info("Blocking traffic for 3.5.* android version");
				return false;
			}
			portalKeyword = ANDROID_KEYWORD;
		} else {
			LOG.info("param source {}", sasParams.getSource());
			LOG.info("Configure parameters inside verve returned false");
			return false;
		}

		LOG.info("Configure parameters inside verve returned true");
		return true;
	}

	@Override
	public String getName() {
		return "verve";
	}

	@Override
	public URI getRequestUri() throws Exception {
		try {
			StringBuilder url = new StringBuilder();
			url.append(host).append("?ip=").append(sasParams.getRemoteHostIp());
			url.append("&p=").append(portalKeyword);
			url.append("&b=").append(externalSiteId);
			url.append("&site=").append(blindedSiteId);
			if (!StringUtils.isEmpty(sasParams.getGender())) {
				url.append("&ei=gender=").append(
						sasParams.getGender().toLowerCase());
			}
			if (null != sasParams.getAge()) {
				url.append(";age=").append(sasParams.getAge());
			}
			url.append("&ua=").append(
					getURLEncode(sasParams.getUserAgent(), format));
			if (sendTrueLatLongOnly) {
				url.append("&lat=").append(latitude);
				url.append("&long=").append(longitude);
			}

			if (!"wap".equalsIgnoreCase(sasParams.getSource())) {
				if (sasParams.getOsId() == HandSetOS.iOS.getValue()) {
					if (casInternalRequestParameters.uidIFA != null) {
						url.append("&uis=a&ui=").append(
								casInternalRequestParameters.uidIFA);
					} else if (casInternalRequestParameters.uidSO1 != null) {
						url.append("&uis=us&ui=").append(
								casInternalRequestParameters.uidSO1);
					} else if (casInternalRequestParameters.uidO1 != null) {
						url.append("&uis=us&ui=").append(
								casInternalRequestParameters.uidO1);
					} else if (casInternalRequestParameters.uidMd5 != null) {
						url.append("&uis=u&ui=").append(
								casInternalRequestParameters.uidMd5);
					}else if (casInternalRequestParameters.uidIDUS1 != null) {
                        url.append("&uis=ds&ui=").append(
                                casInternalRequestParameters.uidIDUS1);
                    }else if (!StringUtils
							.isBlank(casInternalRequestParameters.uid)
							&& !casInternalRequestParameters.uid.equals("null")) {
						url.append("&uis=v&ui=").append(
								casInternalRequestParameters.uid);
					}
				} else if (sasParams.getOsId() == HandSetOS.Android.getValue()) {
					  if (casInternalRequestParameters.uidMd5 != null) {
						url.append("&uis=dm&ui=").append(
								casInternalRequestParameters.uidMd5);
					} else if (!StringUtils
							.isBlank(casInternalRequestParameters.uid)
							&& !casInternalRequestParameters.uid.equals("null")) {
						url.append("&uis=v&ui=").append(
								casInternalRequestParameters.uid);
					}
                    else {
                        String gpid = getGPID();
                        if (gpid != null) {
                            url.append("&g=").append(gpid);
                        }
                    }
                }
			}

			if (casInternalRequestParameters.zipCode != null) {
				url.append("&z=").append(casInternalRequestParameters.zipCode);
			}

			url.append("&c=97");// get category map

			if (width != 0 && height != 0) {
				url.append("&size=").append(width).append('x').append(height);
				url.append("&adunit=").append(adUnit);
			}

			LOG.debug("Verve url is {}", url);
			return (new URI(url.toString()));
		} catch (URISyntaxException exception) {
			errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
			LOG.error("{}", exception);
		}
		return null;
	}

	@Override
	public void parseResponse(final String response,
			final HttpResponseStatus status) {
		LOG.debug("response is {} and response length is {}", response,
				response.length());
		if (status.code() != 200 || StringUtils.isBlank(response)) {
			statusCode = status.code();
			if (200 == statusCode) {
				statusCode = 500;
			}
			responseContent = "";
			return;
		} else {
			statusCode = status.code();
			VelocityContext context = new VelocityContext();
			context.put(VelocityTemplateFieldConstants.IMBeaconUrl, beaconUrl);
			context.put(VelocityTemplateFieldConstants.PartnerHtmlCode,
					response.trim());
			try {
				responseContent = Formatter.getResponseFromTemplate(
						TemplateType.HTML, context, sasParams, beaconUrl);
				adStatus = "AD";
			} catch (Exception exception) {
				adStatus = "NO_AD";
				LOG.info("Error parsing response from verve : {}", exception);
				LOG.info("Response from verve : {}", response);
			}
		}
		LOG.debug("response length is {}", responseContent.length());
	}

	@Override
	public String getId() {
		return (config.getString("verve.advertiserId"));
	}
}
