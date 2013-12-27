package com.inmobi.adserve.channels.adnetworks.wapstart;

import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.IABCountriesInterface;
import com.inmobi.adserve.channels.util.IABCountriesMap;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;


public class DCPWapStartAdNetwork extends BaseAdNetworkImpl {
    private final Configuration           config;
    private String                        latitude      = null;
    private String                        longitude     = null;
    private int                           width;
    private int                           height;
    private static IABCountriesInterface  iABCountries;
    private static DocumentBuilderFactory factory;
    private static DocumentBuilder        builder;
    private static final String           latlongFormat = "%s,%s";
    private Request                       ningRequest;

    static {
        iABCountries = new IABCountriesMap();
    }

    public DCPWapStartAdNetwork(final DebugLogger logger, final Configuration config,
            final ClientBootstrap clientBootstrap, final HttpRequestHandlerBase baseRequestHandler,
            final MessageEvent serverEvent) {
        super(baseRequestHandler, serverEvent, logger);
        this.config = config;
        this.logger = logger;
        this.clientBootstrap = clientBootstrap;
        factory = DocumentBuilderFactory.newInstance();
        try {
            builder = factory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            logger.error("XML Parser Builder initialization failed");
        }
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            logger.debug("mandatory parameters missing for wapstart so exiting adapter");
            return false;
        }
        host = config.getString("wapstart.host");

        if (null != sasParams.getSlot()
                && SlotSizeMapping.getDimension((long)sasParams.getSlot()) != null) {
            Dimension dim = SlotSizeMapping.getDimension((long)sasParams.getSlot());
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }
        else {
            logger.debug("mandate parameters missing for WapStart, so returning from adapter");
            return false;
        }

        if (casInternalRequestParameters.latLong != null
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];

        }

        logger.info("Configure parameters inside wapstart returned true");
        return true;
    }

    @Override
    public String getName() {
        return "wapstart";
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            StringBuilder url = new StringBuilder(host);
            url.append("?version=2&encoding=1&area=viewBannerXml&ip=").append(sasParams.getRemoteHostIp());
            url.append("&id=").append(externalSiteId);
            String bsiteId = StringUtils.replace(blindedSiteId, "-", "");
            url.append("&pageId=00000000").append(bsiteId);
            url.append("&kws=").append(getURLEncode(getCategories(';'), format));

            // if (sasParams.getGender() != null) {
            // url.append("&sex=").append(sasParams.getGender());
            // }
            if (sasParams.getAge() != null) {
                url.append("&age=").append(sasParams.getAge());
            }
            if (sasParams.getCountryCode() != null) {
                url.append("&countryCode=").append(iABCountries.getIabCountry(sasParams.getCountryCode()));
            }
            if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude)) {
                url.append("&location=")
                            .append(getURLEncode(String.format(latlongFormat, latitude, longitude), format));
            }

            logger.debug("WapStart url is", url);

            return (new URI(url.toString()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            logger.info(exception.getMessage());
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public boolean makeAsyncRequest() {
        logger.debug("In PayPal async");
        try {
            String uri = getRequestUri().toString();
            requestUrl = uri;
            setNingRequest(requestUrl);
            logger.debug("Nexage uri :", uri);
            startTime = System.currentTimeMillis();
            baseRequestHandler.getAsyncClient().executeRequest(ningRequest, new AsyncCompletionHandler() {
                @Override
                public Response onCompleted(final Response response) throws Exception {
                    if (!isRequestCompleted()) {
                        logger.debug("Operation complete for channel partner: ", getName());
                        latency = System.currentTimeMillis() - startTime;
                        logger.debug(getName(), "operation complete latency", latency);
                        String responseStr = response.getResponseBody();
                        HttpResponseStatus httpResponseStatus = HttpResponseStatus.valueOf(response.getStatusCode());
                        parseResponse(responseStr, httpResponseStatus);
                        processResponse();
                    }
                    return response;
                }

                @Override
                public void onThrowable(final Throwable t) {
                    if (isRequestComplete) {
                        return;
                    }

                    if (t instanceof java.util.concurrent.TimeoutException) {
                        latency = System.currentTimeMillis() - startTime;
                        logger.debug(getName(), "timeout latency ", latency);
                        adStatus = "TIME_OUT";
                        processResponse();
                        return;
                    }

                    logger.debug(getName(), "error latency ", latency);
                    adStatus = "TERM";
                    logger.info("error while fetching response from:", getName(), t.getMessage());
                    processResponse();
                    return;
                }
            });
        }
        catch (Exception e) {
            logger.debug("Exception in", getName(), "makeAsyncRequest :", e.getMessage());
        }
        logger.debug(getName(), "returning from make NingRequest");
        return true;
    }

    private void setNingRequest(final String requestUrl) {
        ningRequest = new RequestBuilder()
                .setUrl(requestUrl)
                    .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                    .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
                    .setHeader(HttpHeaders.Names.REFERER, requestUrl)
                    .setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE)
                    .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                    .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp())
                    .build();
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        logger.debug("response is", response);

        if (null == response || status.getCode() != 200 || response.trim().isEmpty()) {
            statusCode = status.getCode();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            try {
                VelocityContext context = new VelocityContext();
                TemplateType t = null;
                Document doc = builder.parse(new InputSource(new java.io.StringReader(response)));
                doc.getDocumentElement().normalize();
                NodeList reportNodes = doc.getElementsByTagName("banner");

                Node rootNode = reportNodes.item(0);
                if (rootNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element rootElement = (Element) rootNode;

                    Element partnerClickUrl = (Element) rootElement.getElementsByTagName("link").item(0);
                    Element partnerBeaconElement = (Element) rootElement
                            .getElementsByTagName("cookieSetterUrl")
                                .item(0);

                    String partnerBeacon = partnerBeaconElement.getTextContent();
                    if (StringUtils.isNotEmpty(partnerBeacon)) {
                        context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, partnerBeacon);
                    }

                    context.put(VelocityTemplateFieldConstants.PartnerClickUrl, partnerClickUrl.getTextContent());
                    context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);

                    Element pictureUrl = (Element) rootElement.getElementsByTagName("pictureUrl").item(0);
                    String pictureUrlTxt = pictureUrl.getTextContent();
                    if (StringUtils.isNotEmpty(pictureUrlTxt)) {
                        context.put(VelocityTemplateFieldConstants.PartnerImgUrl, pictureUrlTxt);
                        t = TemplateType.IMAGE;
                    }
                    else {
                        Element title = (Element) rootElement.getElementsByTagName("title").item(0);
                        Element description = (Element) rootElement.getElementsByTagName("content").item(0);
                        context.put(VelocityTemplateFieldConstants.AdText, title.getTextContent());
                        context.put(VelocityTemplateFieldConstants.Description, description.getTextContent());
                        String vmTemplate = Formatter.getRichTextTemplateForSlot(slot.toString());
                        if (!StringUtils.isEmpty(vmTemplate)) {
                            context.put(VelocityTemplateFieldConstants.Template, vmTemplate);
                            t = TemplateType.RICH;
                        }
                        else {
                            t = TemplateType.PLAIN;
                        }
                    }
                }

                statusCode = status.getCode();
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl, logger);
                adStatus = "AD";
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from WapStart :", exception);
                logger.info("Response from WapStart:", response);
            }
        }
    }

    @Override
    public String getId() {
        return (config.getString("wapstart.advertiserId"));
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

    // form httprequest
    @Override
    public HttpRequest getHttpRequest() throws Exception {
        try {
            URI uri = getRequestUri();
            requestUrl = uri.toString();
            request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.toASCIIString());
            logger.debug("host name is ", uri.getHost());
            request.setHeader(HttpHeaders.Names.HOST, uri.getHost());
            logger.debug("got the host");
            request.setHeader("x-display-metrics", String.format("%sx%s", width, height));
            request.setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent());
            request.setHeader("xplus1-user-agent", sasParams.getUserAgent());
            request.setHeader(HttpHeaders.Names.REFERER, uri.toString());
            request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES);
            request.setHeader("x-plus1-remote-addr", sasParams.getRemoteHostIp());
            request.setHeader("X-Forwarded-For", sasParams.getRemoteHostIp());
        }
        catch (Exception ex) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.HTTPREQUEST_ERROR;
            logger.info("Error in making http request ", ex.getMessage());
        }
        return request;
    }
}
