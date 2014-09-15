package com.inmobi.adserve.channels.server.auction;

import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.AuctionEngineInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.IXAccountMapEntity;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.requesthandler.AsyncRequestMaker;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.annotations.AdvertiserIdNameMap;
import com.inmobi.casthrift.ADCreativeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/***
 * Auction Engine to run different types of auctions in rtbd and ix.
 * 
 * @author Devi Chand(devi.chand@inmobi.com)
 */
public class AuctionEngine implements AuctionEngineInterface {
    private static final Logger         LOG             = LoggerFactory.getLogger(AuctionEngine.class);

    private boolean                     auctionComplete = false;
    private ChannelSegment              auctionResponse;
    private double                      secondBidPrice;
    public SASRequestParameters         sasParams;
    public CasInternalRequestParameters casInternalRequestParameters;
    private List<ChannelSegment>        unfilteredChannelSegmentList;

    @AdvertiserIdNameMap
    @Inject
    private static Map<String, String>  advertiserIdNameMap;

    @Inject
    private static AsyncRequestMaker asyncRequestMaker;

    @Inject
    private static AuctionFilterApplier auctionFilterApplier;

    public AuctionEngine() {}

    /***
     * RunRtbSecondPriceAuctionEngine returns the adnetwork selected after auctioning If no of rtb segments selected
     * after filtering is zero it returns the null If no of rtb segments selected after filtering is one it returns the
     * rtb adapter for the segment BidFloor is maximum of lowestEcpm and siteFloor If only 2 rtb are selected, highest
     * bid will win and would be charged the secondHighest price If only 1 rtb is selected, it will be selected for
     * sending response and will be charged the highest of secondHighest price or 90% of bidFloor
     */
    /*
        Runs a second price auction for RTBD and a first price auction for IX
     */

    @Override
    public synchronized AdNetworkInterface runAuctionEngine() {
        // Do not run auction 2 times.

        if (auctionComplete) {
            return auctionResponse == null ? null : auctionResponse.getAdNetworkInterface();
        }
        auctionComplete = true;
        List<ChannelSegment> filteredChannelSegmentList;

        if(unfilteredChannelSegmentList.size() >= 1) {
            LOG.debug("Inside {} auction engine", getDSTName());
            // Apply filtration only when we have at least 1 channelSegment
            filteredChannelSegmentList = auctionFilterApplier.applyFilters(new ArrayList<>(unfilteredChannelSegmentList), casInternalRequestParameters);
        } else {
            filteredChannelSegmentList = new ArrayList<>();
        }

        LOG.debug("No. of filtered {} segments are {}", getDSTName(), filteredChannelSegmentList.size());

        // Send auction response as null in case of 0 rtb/ix responses.
        if (filteredChannelSegmentList.size() == 0) {
            auctionResponse = null;
            LOG.debug("Returning from auction engine , winner is none");
            return null;
        }
        else if (filteredChannelSegmentList.size() == 1) {
            auctionResponse = filteredChannelSegmentList.get(0);
            // Take minimum of auctionFloor+0.01 and bid as secondBidprice if no. of auction
            // response are 1.
            if (6 == auctionResponse.getAdNetworkInterface().getDst()) {
                // For RTBD
                secondBidPrice = Math.min(casInternalRequestParameters.auctionBidFloor, auctionResponse
                        .getAdNetworkInterface().getBidPriceInUsd());
            } else {
                // For IX,
                // we run a first price auction, but the value is still stored in secondBidPrice
                secondBidPrice = auctionResponse.getAdNetworkInterface().getBidPriceInUsd();

            }
            LOG.debug("Completed auction, winner is {} and firstBidPrice is {}", filteredChannelSegmentList.get(0)
                    .getAdNetworkInterface().getName(), secondBidPrice);

            // Set encrypted bid price.
            auctionResponse.getAdNetworkInterface().setEncryptedBid(getEncryptedBid(secondBidPrice));
            auctionResponse.getAdNetworkInterface().setSecondBidPrice(secondBidPrice);

            // Return as there is no need to iterate over the list.
            return filteredChannelSegmentList.get(0).getAdNetworkInterface();
        }

        // Multiple IX bids from RP should not be currently possible
        if (unfilteredChannelSegmentList.get(0).getAdNetworkInterface().getDst() == 8) {
            return null;
        }

        // Sort the list by their bid prices.
        for (int i = 0; i < filteredChannelSegmentList.size(); i++) {
            for (int j = i + 1; j < filteredChannelSegmentList.size(); j++) {
                if (filteredChannelSegmentList.get(i).getAdNetworkInterface().getBidPriceInUsd() < filteredChannelSegmentList.get(j).getAdNetworkInterface()
                        .getBidPriceInUsd()) {
                    ChannelSegment channelSegment = filteredChannelSegmentList.get(i);
                    filteredChannelSegmentList.set(i, filteredChannelSegmentList.get(j));
                    filteredChannelSegmentList.set(j, channelSegment);
                }
            }
        }

        // Calculates the max price of all auction responses.
        double maxPrice = filteredChannelSegmentList.get(0).getAdNetworkInterface().getBidPriceInUsd();
        int secondHighestBid = 1;// Keep secondHighestBidPrice number from auction response list.
        int lowestLatencyBid = 0;// Keep winner number from auction response list.
        for (int i = 1; i < filteredChannelSegmentList.size(); i++) {
            if (filteredChannelSegmentList.get(i).getAdNetworkInterface().getBidPriceInUsd() < maxPrice) {
                secondHighestBid = i;
                break;
            }
            else if (filteredChannelSegmentList.get(i).getAdNetworkInterface().getLatency() < filteredChannelSegmentList.get(lowestLatencyBid)
                    .getAdNetworkInterface().getLatency()) {
                lowestLatencyBid = i;
            }
        }

        // Set auction response for the auction run.
        auctionResponse = filteredChannelSegmentList.get(lowestLatencyBid);

        // Calculates the secondHighestBidPrice if no of auction responses are more than 1.
        secondBidPrice = filteredChannelSegmentList.get(secondHighestBid).getAdNetworkInterface().getBidPriceInUsd();
        double winnerBid = filteredChannelSegmentList.get(lowestLatencyBid).getAdNetworkInterface().getBidPriceInUsd();
        if (winnerBid == secondBidPrice) {
            secondBidPrice = casInternalRequestParameters.auctionBidFloor;
        }

        // Ensure secondHighestBidPrice never crosses response bid.
        secondBidPrice = Math.min(secondBidPrice, auctionResponse.getAdNetworkInterface().getBidPriceInUsd());
        auctionResponse.getAdNetworkInterface().setEncryptedBid(getEncryptedBid(secondBidPrice));
        auctionResponse.getAdNetworkInterface().setSecondBidPrice(secondBidPrice);
        LOG.debug("Completed auction, winner is {} and secondBidPrice is {}", filteredChannelSegmentList.get(lowestLatencyBid)
                .getAdNetworkInterface().getName(), secondBidPrice);
        return filteredChannelSegmentList.get(lowestLatencyBid).getAdNetworkInterface();
    }

    @Override
    // This function sets the parameters contained in the AdIdChain from the buyer field in the ix response
    public boolean updateDSPAccountInfo(RepositoryHelper repositoryHelper, String buyer) {
        // Get Inmobi account id for the DSP on Rubicon side
        IXAccountMapEntity ixAccountMapEntity = repositoryHelper.queryIXAccountMapRepository(Long.parseLong(buyer));
        if(null == ixAccountMapEntity) {
            LOG.error("Invalid Rubicon DSP id: DSP id:{}", buyer);
            return false;
        }
        String accountId = ixAccountMapEntity.getInmobiAccountId();

        // Get collection of Channel Segment Entities for the particular Inmobi account id
        ChannelAdGroupRepository channelAdGroupRepository = repositoryHelper.getChannelAdGroupRepository();
        if (null == channelAdGroupRepository) {
            LOG.error("Channel AdGroup Repository is null.");
            return false;
        }

        Collection<ChannelSegmentEntity> adGroupMap = channelAdGroupRepository.getEntities(accountId);

        if(adGroupMap.isEmpty()) {
            // If collection is empty
            LOG.error("Channel Segment Entity collection for Rubicon DSP is empty: DSP id:{}, inmobi account id:{}", buyer, accountId);
            return false;
        } else {
            // Else picking up the first channel segment entity and assuming that to be the correct entity
            ChannelSegmentEntity dspChannelSegmentEntity = adGroupMap.iterator().next();

            // Create a new ChannelSegment with DSP information. So that, all the logging happens on DSP Id.
            this.auctionResponse = new ChannelSegment(dspChannelSegmentEntity, null, null, null, null,
                    auctionResponse.getAdNetworkInterface(), -1L);

            // Get response creative type and get the incId for the respective response creative type
            ADCreativeType responseCreativeType =  auctionResponse.getAdNetworkInterface().getCreativeType();
            long incId = auctionResponse.getChannelSegmentEntity().getIncId(responseCreativeType);

            // Generating new impression id
            String newImpressionId = asyncRequestMaker.getImpressionId(incId);
            ((IXAdNetwork)auctionResponse.getAdNetworkInterface()).setImpressionId(newImpressionId);
            return true;
        }
    }

    private String getDSTName() {
        return sasParams.getDst()==6?"RTBD":"IX";
    }


    @Override
    public boolean isAuctionComplete() {
        return auctionComplete;
    }

    public ChannelSegment getAuctionResponse() {
        return auctionResponse;
    }

    @Override
    public double getSecondBidPrice() {
        return secondBidPrice;
    }

    @Override
    public boolean areAllChannelSegmentRequestsComplete() {
        if (unfilteredChannelSegmentList == null) {
            return false;
        }
        if (unfilteredChannelSegmentList.size() == 0) {
            return true;
        }
        for (ChannelSegment channelSegment : unfilteredChannelSegmentList) {
            if (!channelSegment.getAdNetworkInterface().isRequestCompleted()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isAuctionResponseNull() {
        return auctionResponse == null;
    }

    public List<ChannelSegment> getUnfilteredChannelSegmentList() {
        return unfilteredChannelSegmentList;
    }

    public void setUnfilteredChannelSegmentList(final List<ChannelSegment> unfilteredChannelSegmentList) {
        this.unfilteredChannelSegmentList = unfilteredChannelSegmentList;
    }

    public String getEncryptedBid(final Double bid) {
        long winBid = (long) (bid * Math.pow(10, 6));
        return asyncRequestMaker.getImpressionId(winBid);
    }

    public double calculateAuctionFloor(final double siteFloor, final double highestEcpm, final double segmentFloor,
                                        final double countryFloor, final double networkSiteEcpm) {
        double auctionFloor;
        auctionFloor = Math.max(siteFloor, highestEcpm);
        auctionFloor = Math.max(auctionFloor, segmentFloor);
        auctionFloor = Math.max(auctionFloor, countryFloor);
        auctionFloor = Math.max(auctionFloor, networkSiteEcpm);
        return auctionFloor;
    }
}
