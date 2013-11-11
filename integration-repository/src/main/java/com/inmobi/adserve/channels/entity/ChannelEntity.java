package com.inmobi.adserve.channels.entity;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Set;


@Getter
public class ChannelEntity implements IdentifiableEntity<String> {

    private static final long serialVersionUID = 1L;

    private final String      channelId;
    private final String      name;
    private final String      username;
    private final String      password;
    private final String      accountId;
    private final String      reportingApiKey;
    private final String      reportingApiUrl;
    private final boolean     isActive;
    private final boolean     isTestMode;
    private final long        burstQps;
    private final long        impressionCeil;
    private final long        impressionFloor;
    private final long        requestCap;
    private final int         priority;
    private final int         demandSourceTypeId;
    private final Timestamp   modifiedOn;
    private final String      urlBase;
    private final String      urlArg;
    private final String      rtbVer;
    private final boolean     isRtb;
    private final String      rtbMethod;
    // used if the win notification is sent via client
    private final String      wnUrl;
    // if url is already a part of bid response then wnRequired=false otherwise
    // true
    private final boolean     wnRequied;
    // if wnUrl is placed in the final ad response to publisher, it is server
    // side wn and if explicit call is made through cas client(adaptor
    // connection) it is through client side.
    private final boolean     wnFromClient;
    private final String      status;
    private final Set<String> sitesIE;
    private final boolean     isSiteInclusion;
    /*
     * This field tells the type of account whether dso-brand or dso-performance or dso-programmatic
     */
    private final int         accountSegment;

    public ChannelEntity(Builder builder) {
        this.channelId = builder.channelId;
        this.name = builder.name;
        this.username = builder.username;
        this.password = builder.password;
        this.accountId = builder.accountId;
        this.reportingApiKey = builder.reportingApiKey;
        this.reportingApiUrl = builder.reportingApiUrl;
        this.isActive = builder.isActive;
        this.isTestMode = builder.isTestMode;
        this.burstQps = builder.burstQps;
        this.impressionCeil = builder.impressionCeil;
        this.impressionFloor = builder.impressionFloor;
        this.requestCap = builder.requestCap;
        this.priority = builder.priority;
        this.demandSourceTypeId = builder.demandSourceTypeId;
        this.modifiedOn = builder.modifiedOn;
        this.urlBase = builder.urlBase;
        this.urlArg = builder.urlArg;
        this.rtbVer = builder.rtbVer;
        this.isRtb = builder.isRtb;
        this.rtbMethod = builder.rtbMethod;
        this.wnUrl = builder.wnUrl;
        this.wnRequied = builder.wnRequied;
        this.wnFromClient = builder.wnFromClient;
        this.status = builder.status;
        this.sitesIE = builder.sitesIE;
        this.isSiteInclusion = builder.isSiteInclusion;
        this.accountSegment = builder.accountSegment;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private String      channelId;
        private String      name;
        private String      username;
        private String      password;
        private String      accountId;
        private String      reportingApiKey;
        private String      reportingApiUrl;
        private boolean     isActive;
        private boolean     isTestMode;
        private long        burstQps;
        private long        impressionCeil;
        private long        impressionFloor;
        private long        requestCap;
        private int         priority;
        private int         demandSourceTypeId;
        private Timestamp   modifiedOn;
        private String      urlBase;
        private String      urlArg;
        private String      rtbVer;
        private boolean     isRtb;
        private String      rtbMethod;
        // used if the win notification is sent via client
        private String      wnUrl;
        // if url is already a part of bid response then wnRequired=false otherwise
        // true
        private boolean     wnRequied;
        // if wnUrl is placed in the final ad response to publisher, it is server
        // side wn and if explicit call is made through cas client(adaptor
        // connection) it is through client side.
        private boolean     wnFromClient;
        private String      status;
        private Set<String> sitesIE;
        private boolean     isSiteInclusion;
        private int         accountSegment;

        public ChannelEntity build() {
            return new ChannelEntity(this);
        }
    }

    @Override
    public String getJSON() {
        return null;
    }

    @Override
    public String getId() {
        return channelId;
    }

}