package com.inmobi.adserve.channels.adnetworks.ix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;
import com.googlecode.cqengine.resultset.ResultSet;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.GeoRegionFenceMapEntity;
import com.inmobi.adserve.channels.entity.IXPackageEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.config.GlobalConstant;
import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;
import com.inmobi.segment.Segment;
import com.inmobi.segment.impl.CarrierId;
import com.inmobi.segment.impl.City;
import com.inmobi.segment.impl.ConnectionType;
import com.inmobi.segment.impl.ConnectionTypeEnum;
import com.inmobi.segment.impl.Country;
import com.inmobi.segment.impl.DeviceOs;
import com.inmobi.segment.impl.GeoSourceType;
import com.inmobi.segment.impl.GeoSourceTypeEnum;
import com.inmobi.segment.impl.InventoryType;
import com.inmobi.segment.impl.InventoryTypeEnum;
import com.inmobi.segment.impl.LatlongPresent;
import com.inmobi.segment.impl.SiteCategory;
import com.inmobi.segment.impl.SiteId;
import com.inmobi.segment.impl.SlotId;
import com.inmobi.segment.impl.UidPresent;
import com.inmobi.segment.impl.ZipCodePresent;

public class IXPackageMatcher {
    private static final Logger LOG = LoggerFactory.getLogger(IXPackageMatcher.class);
    public static final int PACKAGE_MAX_LIMIT = 35;

    public static List<Integer> findMatchingPackageIds(final SASRequestParameters sasParams,
        final RepositoryHelper repositoryHelper, final Short selectedSlotId, final ChannelSegmentEntity adGroupEntity) {
        LOG.debug("Inside IX Package Matcher");
        List<Integer> matchedPackageIds = new ArrayList<>();

        // TODO: Do package matching on the intersection of ump selected slots and those present in the adgroup
        Segment requestSegment = createRequestSegment(sasParams, selectedSlotId);
        ResultSet<IXPackageEntity> resultSet = repositoryHelper
            .queryIXPackageRepository(sasParams.getOsId(), sasParams.getSiteId(), sasParams.getCountryId()
                .intValue(), selectedSlotId);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Number of packages selected after OS({}), Site({}), Country({}) and Slot({}) filtration: {}",
                sasParams.getOsId(), sasParams.getSiteId(), sasParams.getCountryId().intValue(), selectedSlotId,
                resultSet.size());
        }

        int matchedPackagesCount = 0;
        int droppedInPackageDMPFilter = 0;
        int droppedInPackageManufModelFilter = 0;
        int droppedInPackageOsVersionFilter = 0;
        int droppedInPackageGeoRegionFilter = 0;
        int droppedInPackageSegmentSubsetFilter = 0;
        int droppedInPackageAdTypeTargetingFilter = 0;

        // TODO: Refactor into proper filters
        for (IXPackageEntity packageEntity : resultSet) {
            if (requestSegment.isSubsetOf(packageEntity.getSegment())) {

                final boolean failsAdTypeTargetingFilter = !checkForAdTypeTargeting(
                    adGroupEntity.getSecondaryAdFormatConstraints(), packageEntity.getSecondaryAdFormatConstraints());

                if (failsAdTypeTargetingFilter) {
                    LOG.debug("Package {} dropped in Ad Type Targeting Filter", packageEntity.getId());
                    droppedInPackageAdTypeTargetingFilter += 1;
                    continue;
                }

                // Add to matchedPackageIds only if csId's match
                if (CollectionUtils.isNotEmpty(packageEntity.getDmpFilterSegmentExpression())
                        && !checkForCsidMatch(sasParams.getCsiTags(), packageEntity.getDmpFilterSegmentExpression())) {
                    LOG.debug("Package {} dropped in DMP Filter", packageEntity.getId());
                    droppedInPackageDMPFilter += 1;
                    continue;
                }

                // Manuf Model Targeting
                if (!checkForManufModelTargeting(packageEntity.getManufModelTargeting(), sasParams.getManufacturerId(),
                        sasParams.getModelId())) {
                    LOG.debug("Package {} dropped in Manufacturer Model Filter", packageEntity.getId());
                    droppedInPackageManufModelFilter += 1;
                    continue;
                }

                // OS Version Targeting
                if (!checkForOsVersionTargeting(packageEntity.getOsVersionTargeting(), sasParams.getOsId(),
                        sasParams.getOsMajorVersion())) {
                    LOG.debug("Package {} dropped in OS Version Filter", packageEntity.getId());
                    droppedInPackageOsVersionFilter += 1;
                    continue;
                }

                // Add to matchedPackageIds only if at least one fence from the set of request fence ids
                // is present in the set of fence ids defined in the package.
                if (StringUtils.isNotEmpty(packageEntity.getGeoFenceRegion())) {
                    String geoRegionNameCountryCombo = packageEntity.getGeoFenceRegion() + "_"
                            + sasParams.getCountryId();
                    GeoRegionFenceMapEntity geoRegionFenceMapEntity = repositoryHelper.
                            queryGeoRegionFenceMapRepositoryByRegionNameCountryCombo(geoRegionNameCountryCombo);

                    if (null == geoRegionFenceMapEntity ||
                            (null != geoRegionFenceMapEntity.getFenceIdsList() &&
                                    (null == sasParams.getGeoFenceIds() ||
                                            !CollectionUtils.containsAny(sasParams.getGeoFenceIds(),
                                                    geoRegionFenceMapEntity.getFenceIdsList())))) {
                        LOG.debug("Package {} dropped in Geo Fence Filter", packageEntity.getId());
                        droppedInPackageGeoRegionFilter += 1;
                        continue;
                    }
                }

                matchedPackageIds.add(packageEntity.getId());
                // Break the loop if we reach the threshold.
                if (++matchedPackagesCount == PACKAGE_MAX_LIMIT) {
                    InspectorStats.incrementStatCount(InspectorStrings.PACKAGE_FILTER_STATS,
                            InspectorStrings.IX_PACKAGE_THRESHOLD_EXCEEDED_COUNT);
                    break;
                }
            } else {
                LOG.debug("Package {} dropped in Segment Subset Filter", packageEntity.getId());
                droppedInPackageSegmentSubsetFilter += 1;
            }
        }

        InspectorStats.incrementStatCount(InspectorStrings.PACKAGE_FILTER_STATS,
                InspectorStrings.DROPPED_IN_PACKAGE_AD_TYPE_TARGETING_FILTER, droppedInPackageAdTypeTargetingFilter);
        InspectorStats.incrementStatCount(InspectorStrings.PACKAGE_FILTER_STATS,
                InspectorStrings.DROPPED_IN_PACKAGE_DMP_FILTER, droppedInPackageDMPFilter);
        InspectorStats.incrementStatCount(InspectorStrings.PACKAGE_FILTER_STATS,
                InspectorStrings.DROPPED_IN_PACKAGE_MANUF_MODEL_FILTER, droppedInPackageManufModelFilter);
        InspectorStats.incrementStatCount(InspectorStrings.PACKAGE_FILTER_STATS,
                InspectorStrings.DROPPED_IN_PACKAGE_OS_VERSION_FILTER, droppedInPackageOsVersionFilter);
        InspectorStats.incrementStatCount(InspectorStrings.PACKAGE_FILTER_STATS,
                InspectorStrings.DROPPED_IN_PACKAGE_GEO_REGION_FILTER, droppedInPackageGeoRegionFilter);
        InspectorStats.incrementStatCount(InspectorStrings.PACKAGE_FILTER_STATS,
                InspectorStrings.DROPPED_IN_PACKAGE_SEGMENT_SUBSET_FILTER, droppedInPackageSegmentSubsetFilter);

        LOG.debug("Packages selected: ", matchedPackageIds.toArray().toString());
        return matchedPackageIds;
    }

    private static boolean checkForCsidMatch(final Set<Integer> csiReqTags,
        final Set<Set<Integer>> dmpFilterExpression) {
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

    protected static boolean checkForAdTypeTargeting(
        final SecondaryAdFormatConstraints adgroupSecondaryAdFormatConstraints,
        final Set<SecondaryAdFormatConstraints> packageSecondaryAdFormatConstraintsSet) {

        return packageSecondaryAdFormatConstraintsSet.contains(SecondaryAdFormatConstraints.ALL)
            || packageSecondaryAdFormatConstraintsSet.contains(adgroupSecondaryAdFormatConstraints);
    }

    /**
     * Checks whether Os Version Targeting is satisfied.
     *
     * If OS Version Targeting map is null or empty
     *   then no targeting takes place and true is returned.
     *
     * Otherwise, check whether the os (major version) lies within the range.
     *
     * Assumption: osId will always have an entry in the osVersionTargeting map if the map is not empty
     *
     * @param osVersionTargeting
     * @param osId
     * @param osMajorVersionStr
     * @return
     */
    protected static boolean checkForOsVersionTargeting(final Map<Integer, Range<Double>> osVersionTargeting,
                                                        final int osId, final String osMajorVersionStr) {
        if (null == osVersionTargeting || osVersionTargeting.isEmpty()) {
            return true;
        }

        Double osMajorVersion;
        Boolean checkPassed = false;

        try {
            osMajorVersion = Double.parseDouble(osMajorVersionStr);
            checkPassed = osVersionTargeting.get(osId).contains(osMajorVersion);
        } catch (NumberFormatException nfe) {
            // Sanity: If the osMajorVersion is not a double, check whether any os version targeting was applied
            if (osVersionTargeting.get(osId).encloses(Range.all())) {
                checkPassed = true;
            } else {
                checkPassed = false;
                LOG.info("osMajorVersion is not a double");
            }
        } catch (NullPointerException npe) {
            checkPassed = false;
            LOG.info("osId not present in the osVersionTargeting Map");
        }

        return checkPassed;
    }

    /**
     * Checks whether Manufacturer Model Targeting is satisfied.
     *
     * If Manufacturer Model Targeting map is null or empty; or if the corresponding Model Id Set is empty for the
     * manufacturerId,
     *   then no targeting takes place and true is returned.
     *
     * If the Manufacturer Id is not found in the Manufacturer Model Targeting map, then false is returned.
     * Otherwise,
     * If inclusion is true, then true is returned only if the modelId is present in the Set of Model Ids.
     * Else if inclusion is false, then true is returned only if the modelId is not present in the Set of Model Ids.
     *
     * @param manufModelTargeting
     * @param manufacturerId
     * @param modelId
     * @return
     */
    protected static boolean checkForManufModelTargeting(final Map<Long, Pair<Boolean, Set<Long>>> manufModelTargeting,
                                                         final long manufacturerId, final long modelId) {
        if (null == manufModelTargeting || manufModelTargeting.isEmpty()) {
            return true;
        }

        Pair<Boolean, Set<Long>> inclusionModelIdSetPair = manufModelTargeting.get(manufacturerId);

        if (null == inclusionModelIdSetPair) {
            return false;
        }

        boolean incl = inclusionModelIdSetPair.getLeft();
        Set<Long> modelIdsSet = inclusionModelIdSetPair.getRight();

        boolean checkPassed = false;
        if (modelIdsSet.isEmpty()) {
            checkPassed = true;
        } else if (incl) {
            checkPassed = modelIdsSet.contains(modelId);
        } else {
            checkPassed = !modelIdsSet.contains(modelId);
        }

        return checkPassed;
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
        ConnectionType reqConnectionType = null;
        SiteCategory reqSiteCategory = null;
        City reqCity = null;
        GeoSourceType reqGeoSource = null;

        reqCountry.init(Collections.singleton(sasParams.getCountryId().intValue()));
        reqDeviceOs.init(Collections.singleton(sasParams.getOsId()));
        reqSiteId.init(Collections.singleton(sasParams.getSiteId()));
        reqSlotId.init(Collections.singleton(selectedSlotId.intValue()));
        reqLatlongPresent.init(StringUtils.isNotEmpty(sasParams.getLatLong()));
        reqZipCodePresent.init(StringUtils.isNotEmpty(sasParams.getPostalCode()));
        reqUidPresent.init(isUdIdPresent(sasParams));
        reqCarrierId.init(Collections.singleton((long) sasParams.getCarrierId())); // TODO: fix long->int cast in ThriftRequestParser

        InventoryTypeEnum reqInventoryEnum =
                GlobalConstant.APP.equalsIgnoreCase(sasParams.getSource()) ? InventoryTypeEnum.APP : InventoryTypeEnum.BROWSER;
        reqInventoryType.init(Collections.singleton(reqInventoryEnum));

        if (null != sasParams.getConnectionType()) {
            reqConnectionType = new ConnectionType();
            ConnectionTypeEnum connectionTypeEnum = ConnectionTypeEnum.valueOf(sasParams.getConnectionType().name());
            reqConnectionType.init(Collections.singleton(connectionTypeEnum));
        }

        if (sasParams.getSiteContentType() != null) {
            reqSiteCategory = new SiteCategory();
            reqSiteCategory.init(sasParams.getSiteContentType().name());
        }

        if (sasParams.getCity() != null) {
            reqCity = new City();
            reqCity.init(Collections.singleton(sasParams.getCity()));
        }

        if (sasParams.getLocationSource() != null) {
            reqGeoSource = new GeoSourceType();
            GeoSourceTypeEnum geoSourceTypeEnum = GeoSourceTypeEnum.valueOf(sasParams.getLocationSource().name());
            reqGeoSource.init(Collections.singleton(geoSourceTypeEnum));
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

        if (reqConnectionType != null) {
            requestSegmentBuilder.addSegmentParameter(reqConnectionType);
        }
        if (reqSiteCategory != null) {
            requestSegmentBuilder.addSegmentParameter(reqSiteCategory);
        }
        if (reqCity != null) {
            requestSegmentBuilder.addSegmentParameter(reqCity);
        }
        if (reqGeoSource != null) {
            requestSegmentBuilder.addSegmentParameter(reqGeoSource);
        }

        Segment requestSegment = requestSegmentBuilder.build();

        return requestSegment;
    }

    private static boolean isUdIdPresent(SASRequestParameters sasParams) {
        return (StringUtils.isNotEmpty(sasParams.getUidParams()) && !"{}".equals(sasParams.getUidParams()))
                || (null != sasParams.getTUidParams() && !sasParams.getTUidParams().isEmpty());
    }
}
