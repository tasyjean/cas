package com.inmobi.adserve.channels.adnetworks.ix;

import com.googlecode.cqengine.resultset.ResultSet;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.IXPackageEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.segment.Segment;
import com.inmobi.segment.impl.CarrierId;
import com.inmobi.segment.impl.Country;
import com.inmobi.segment.impl.DeviceOs;
import com.inmobi.segment.impl.InventoryType;
import com.inmobi.segment.impl.InventoryTypeEnum;
import com.inmobi.segment.impl.LatlongPresent;
import com.inmobi.segment.impl.NetworkType;
import com.inmobi.segment.impl.SiteCategory;
import com.inmobi.segment.impl.SiteId;
import com.inmobi.segment.impl.SlotId;
import com.inmobi.segment.impl.UidPresent;
import com.inmobi.segment.impl.ZipCodePresent;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class IXPackageMatcher {

    public static final int PACKAGE_MAX_LIMIT = 30;

    public static List<String> findMatchingPackageIds(final SASRequestParameters sasParams,
                                                      final RepositoryHelper repositoryHelper, final Short selectedSlotId) {
        List<String> matchedPackageIds = new ArrayList<>();

        Segment requestSegment = createRequestSegment(sasParams, selectedSlotId);
        ResultSet<IXPackageEntity> resultSet =
                repositoryHelper.queryIXPackageRepository(sasParams.getOsId(), sasParams.getSiteId(), sasParams
                        .getCountryId().intValue(), selectedSlotId);

        int matchedPackagesCount = 0;
        for (IXPackageEntity packageEntity : resultSet) {
            if (requestSegment.isSubsetOf(packageEntity.getSegment())) {

                // TODO: 1) Honor scheduledTimeOfDay [Not in the scope of MVP]
                //Add to matchedPackageIds only if csId's match
                if (CollectionUtils.isNotEmpty(packageEntity.getDmpFilterSegmentExpression())
                        && !checkForCsidMatch(sasParams.getCsiTags(), packageEntity.getDmpFilterSegmentExpression())) {
                    continue;
                }
                matchedPackageIds.add(String.valueOf(packageEntity.getId()));
                // Break the loop if we reach the threshold.
                if (++matchedPackagesCount == PACKAGE_MAX_LIMIT) {
                    InspectorStats.incrementStatCount(InspectorStrings.IX_PACKAGE_THRESHOLD_EXCEEDED_COUNT);
                    break;
                }
            }
        }
        return matchedPackageIds;
    }

    private static boolean checkForCsidMatch(final Set<Integer> csiReqTags, final Set<Set<Integer>> dmpFilterExpression) {

        if (CollectionUtils.isEmpty(csiReqTags)) {
            return false;
        } else {
            for (Set<Integer> smallSet : dmpFilterExpression) {
                if (Collections.disjoint(smallSet, csiReqTags)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Segment createRequestSegment(final SASRequestParameters sasParams, final Short selectedSlotId) {

        Country reqCountry = new Country();
        DeviceOs reqDeviceOs = new DeviceOs();
        SiteId reqSiteId = new SiteId();
        SlotId reqSlotId = new SlotId();
        CarrierId reqCarrierId = new CarrierId();
        LatlongPresent reqLatlongPresent = new LatlongPresent();
        ZipCodePresent reqZipCodePresent = new ZipCodePresent();
        UidPresent reqUidPresent = new UidPresent();
        InventoryType reqInventoryType = new InventoryType();

        // Below are optional params and requires NULL check.
        NetworkType reqNetworkType = null;
        SiteCategory reqSiteCategory = null;

        reqCountry.init(Collections.singleton(sasParams.getCountryId().intValue()));
        reqDeviceOs.init(Collections.singleton(sasParams.getOsId()));
        reqSiteId.init(Collections.singleton(sasParams.getSiteId()));
        reqSlotId.init(Collections.singleton(selectedSlotId.intValue()));
        reqLatlongPresent.init(StringUtils.isNotEmpty(sasParams.getLatLong()));
        reqZipCodePresent.init(StringUtils.isNotEmpty(sasParams.getPostalCode()));
        reqUidPresent.init(isUdIdPresent(sasParams));
        reqCarrierId.init(Collections.singleton((long) sasParams.getCarrierId())); // TODO: fix long->int cast in ThriftRequestParser

        InventoryTypeEnum reqInventoryEnum =
                "APP".equalsIgnoreCase(sasParams.getSource()) ? InventoryTypeEnum.APP : InventoryTypeEnum.BROWSER;
        reqInventoryType.init(Collections.singleton(reqInventoryEnum));

        if (sasParams.getNetworkType() != null) {
            reqNetworkType = new NetworkType();
            reqNetworkType.init(Collections.singleton(sasParams.getNetworkType().getValue()));
        }

        if (sasParams.getSiteContentType() != null) {
            reqSiteCategory = new SiteCategory();
            reqSiteCategory.init(sasParams.getSiteContentType().name());
        }

        Segment.Builder requestSegmentBuilder = new Segment.Builder();
        requestSegmentBuilder
                .addSegmentParameter(reqCountry)
                .addSegmentParameter(reqDeviceOs)
                .addSegmentParameter(reqSiteId)
                .addSegmentParameter(reqSlotId)
                .addSegmentParameter(reqLatlongPresent)
                .addSegmentParameter(reqZipCodePresent)
                .addSegmentParameter(reqCarrierId)
                .addSegmentParameter(reqUidPresent)
                .addSegmentParameter(reqInventoryType);

        if (reqNetworkType != null) {
            requestSegmentBuilder.addSegmentParameter(reqNetworkType);
        }
        if (reqSiteCategory != null) {
            requestSegmentBuilder.addSegmentParameter(reqSiteCategory);
        }

        Segment requestSegment = requestSegmentBuilder.build();

        return requestSegment;
    }

    private static boolean isUdIdPresent(SASRequestParameters sasParams) {
        return (StringUtils.isNotEmpty(sasParams.getUidParams()) && !"{}".equals(sasParams.getUidParams()))
                || (null != sasParams.getTUidParams() && !sasParams.getTUidParams().isEmpty());
    }
}
