package com.inmobi.adserve.channels.server.requesthandler.filters;

import com.inmobi.adserve.channels.server.constants.FilterOrder;


/**
 * @author abhishek.parwal
 * 
 */
public interface ChannelSegmentFilter {

    /**
     * 
     * @param priority
     */
    void setOrder(final FilterOrder priority);

    /**
     * 
     * @return
     */
    FilterOrder getOrder();
}
