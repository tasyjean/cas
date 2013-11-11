package com.inmobi.adserve.channels.entity;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;
import lombok.Getter;
import lombok.Setter;


@Getter
public class ChannelSegmentFeedbackEntity implements IdentifiableEntity<String> {

    private static final long serialVersionUID = 1L;

    private final String      advertiserId;
    private final String      adGroupId;
    private final double      eCPM;
    private final double      fillRatio;
    private final double      lastHourLatency;
    private final int         todayRequests;
    private final int         beacons;
    private final int         clicks;
    private final int         todayImpressions;

    public ChannelSegmentFeedbackEntity(Builder builder) {
        this.advertiserId = builder.advertiserId;
        this.adGroupId = builder.adGroupId;
        this.eCPM = builder.eCPM;
        this.fillRatio = builder.fillRatio;
        this.lastHourLatency = builder.lastHourLatency;
        this.todayRequests = builder.todayRequests;
        this.beacons = builder.beacons;
        this.clicks = builder.clicks;
        this.todayImpressions = builder.todayImpressions;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Getter
    @Setter
    public static class Builder {
        private String advertiserId;
        private String adGroupId;
        private double eCPM;
        private double fillRatio;
        private double lastHourLatency;
        private int    todayRequests;
        private int    beacons;
        private int    clicks;
        private int    todayImpressions;

        public ChannelSegmentFeedbackEntity build() {
            return new ChannelSegmentFeedbackEntity(this);
        }
    }

    @Override
    public String getId() {
        return adGroupId;
    }

    @Override
    public String getJSON() {
        return null;
    }

}