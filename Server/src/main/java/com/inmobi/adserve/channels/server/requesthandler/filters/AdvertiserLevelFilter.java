package com.inmobi.adserve.channels.server.requesthandler.filters;

import java.util.List;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.requesthandler.beans.AdvertiserMatchedSegmentDetail;


/**
 * @author abhishek.parwal
 * 
 */
public interface AdvertiserLevelFilter extends ChannelSegmentFilter {

    public void filter(final List<AdvertiserMatchedSegmentDetail> matchedSegmentDetails,
            final SASRequestParameters sasParams);

}
