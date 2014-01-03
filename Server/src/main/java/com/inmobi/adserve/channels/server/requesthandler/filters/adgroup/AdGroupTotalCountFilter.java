package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.config.ServerConfig;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdGroupTotalCountFilter implements AdGroupLevelFilter {

    private static final Logger    LOG = LoggerFactory.getLogger(AbstractAdGroupLevelFilter.class);

    private final Provider<Marker> traceMarkerProvider;

    private final ServerConfig     serverConfig;

    @Inject
    AdGroupTotalCountFilter(final Provider<Marker> traceMarkerProvider, final ServerConfig serverConfig) {
        this.traceMarkerProvider = traceMarkerProvider;
        this.serverConfig = serverConfig;
    }

    @Override
    public void filter(final List<ChannelSegment> channelSegments, final SASRequestParameters sasParams,
            final CasContext casContext) {

        Marker traceMarker = traceMarkerProvider.get();

        int maxSegmentSelectionCount = serverConfig.getMaxSegmentSelectionCount();

        if (maxSegmentSelectionCount == -1) {
            return;
        }

        int selectedSegmentCount = 0;

        for (Iterator<ChannelSegment> iterator = channelSegments.listIterator(); iterator.hasNext();) {
            ChannelSegment channelSegment = iterator.next();

            boolean result = failedInFilter(maxSegmentSelectionCount, selectedSegmentCount);

            if (result) {
                // TODO: we can optimize if we don't need these inspector stats , then we can shorten our iteration
                iterator.remove();
                LOG.debug(traceMarker, "Failed in filter {}  , adgroup {}", this.getClass().getName(), channelSegment
                        .getChannelSegmentFeedbackEntity().getId());
                incrementStats(channelSegment);
            }
            else {
                selectedSegmentCount++;
                LOG.debug(traceMarker, "Passed in filter {} ,  adgroup {}", this.getClass().getName(), channelSegment
                        .getChannelSegmentFeedbackEntity().getId());
            }
        }
    }

    /**
     * @param maxSegmentSelectionCount
     * @param selectedSegmentCount
     * @return
     */
    private boolean failedInFilter(final int maxSegmentSelectionCount, final int selectedSegmentCount) {
        return selectedSegmentCount >= maxSegmentSelectionCount;
    }

    /**
     * @param channelSegment
     */
    protected void incrementStats(final ChannelSegment channelSegment) {
        channelSegment.incrementInspectorStats(InspectorStrings.droppedInSegmentPerRequestFilter);
    }
}
