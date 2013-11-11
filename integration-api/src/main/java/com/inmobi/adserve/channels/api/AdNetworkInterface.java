package com.inmobi.adserve.channels.api;

import java.net.URI;
import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpRequest;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;


public interface AdNetworkInterface {

    // Returns the Adstatus.
    String getAdStatus();

    // Return the latency.
    long getLatency();

    // Return the bid price for rtb, for other will return the -1.
    double getBidprice();

    // Sets the secondBid price after running the auction.
    void setSecondBidPrice(Double price);

    // Returns the second bid price after auctioning.
    double getSecondBidPrice();

    // Returns true for rtb partner, false otherwise.
    boolean isRtbPartner();

    // Returns auction id sent in the rtb response
    String getAuctionId();

    // Returns impression Id sent in the rtb response
    String getRtbImpressionId();

    // Returns seat id sent in the rtb response
    String getSeatId();

    // Returns the name of the third party ad network.
    String getName();

    // Returns the Channel Id for the TPAN as in our database.
    String getId();

    // Returns the channel id for adapter
    Integer getChannelId();

    // Updates the request parameters according to the Ad Network. Returns true on
    // success.
    boolean configureParameters(SASRequestParameters param, CasInternalRequestParameters casInternalRequestParameters,
            ChannelSegmentEntity entity, String clickUrl, String beaconUrl);

    // Makes asynchronous request to Ad Network server. Returns true on success.
    boolean makeAsyncRequest();

    // Constructs the request url
    URI getRequestUri() throws Exception;

    // Constructs the http request object.
    HttpRequest getHttpRequest() throws Exception;

    // whether click url is used by adapter
    boolean isBeaconUrlRequired();

    // whether click url is used by adapter
    boolean isClickUrlRequired();

    // Returns true if the adapter is an internal partner.
    boolean isInternal();

    // Called after the adapter is selected for impression.
    void impressionCallback();

    // Called after the adapter is not selected for impression.
    void noImpressionCallBack();

    // get click url
    String getClickUrl();

    // get Impression Id
    String getImpressionId();

    // Returns true if request is completed.
    boolean isRequestCompleted();

    // Constructs the response from status and content.
    ThirdPartyAdResponse getResponseAd();

    // get request url
    String getRequestUrl();

    // get Response content
    String getHttpResponseContent();

    Map getResponseHeaders();

    // Does the clean up for the channels and closes the port.
    void cleanUp();

    // return response Struct
    ThirdPartyAdResponse getResponseStruct();

    // return connection latency
    long getConnectionLatency();

    boolean useJsAdTag();

    void setEncryptedBid(String encryptedBid);

    void generateJsAdResponse();

}