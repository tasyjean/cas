package com.inmobi.adserve.channels.server.requesthandler;

import com.google.gson.Gson;
import com.inmobi.adserve.adpool.AdPoolRequest;
import com.inmobi.adserve.adpool.SupplyCapability;
import com.inmobi.adserve.adpool.UidParams;
import com.inmobi.adserve.adpool.UidType;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.util.DebugLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


public class ThriftRequestParser {

    // Extracting params.
    public static AdPoolRequest extractParams(Map<String, List<String>> params) throws JSONException,
            UnsupportedEncodingException {
        Gson gson = new Gson();
        if (!params.isEmpty()) {
            List<String> values = params.get("post");
            if (CollectionUtils.isNotEmpty(values)) {
                String stringVal = values.iterator().next();
                return gson.fromJson(URLDecoder.decode(stringVal, "UTF-8"), AdPoolRequest.class);
            }
        }
        return null;
    }

    public static void parseRequestParameters(AdPoolRequest tObject, SASRequestParameters params,
            CasInternalRequestParameters casInternalRequestParameters, DebugLogger logger, int dst) {
        logger.debug("Inside thrift request parser");
        params.setAllParametersJson(tObject.toString());
        params.setDst(dst);
        if (2 == dst) {
            params.setResponseOnlyFromDcp(true);
        }
        else {
            params.setResponseOnlyFromDcp(false);
        }
        // TODO params.setAccountSegment(tObject.accountSegment);
        params.setRemoteHostIp(tObject.remoteHostIp);
        params.setUserAgent(tObject.device.userAgent);

        params.setLocSrc(tObject.geo.locationSource.name());
        // TODO Confirm the string format
        params.setLatLong(tObject.geo.latLong.toString());
        // TODO check for site null
        params.setSiteId(tObject.site.siteId);
        params.setSource(tObject.site.contentRating.toString());
        params.setCountry(tObject.geo.countryCode);
        // TODO Clean the names country name and country id
        params.setCountryStr(tObject.geo.countryId + "");
        // TODO Iterate over the segments using all slots
        if (null != tObject.selectedSlots) {
            params.setSlot(tObject.selectedSlots.get(0) + "");
        } else {
            params.setSlot(tObject.requestSlotId + "");
        }
        params.setRqMkSlot(tObject.requestSlotId + "");
        params.setSdkVersion(tObject.sdkVersion);
        // TODO verify the names
        params.setSiteType(tObject.site.inventoryType.toString());
        // TODO add adcode in thrift
        // params.setAdcode(tObject.adCode);
        // TODO Change to int in thrift
        params.setOsId(new Long(tObject.device.osId).intValue());
        params.setCategories(convertIntToLong(tObject.site.siteTags));
        // TODO rframe not present params.setRqIframe(stringify(jObject, "rqIframe", logger));
        params.setRFormat(tObject.responseFormat);
        // TODO change to short in DCP too
        params.setRqMkAdcount(tObject.requestedAdCount + "");
        params.setTid(tObject.requestId);
        params.setAllowBannerAds(tObject.supplyCapability == SupplyCapability.BANNER);
        params.setSiteFloor(tObject.site.ecpmFloor);
        //TODO use segment id in cas as long
        params.setSiteSegmentId(new Long(tObject.segmentId).intValue());
        params.setModelId(new Long(tObject.device.modelId).intValue());
        // TODO Ip File Version not present params.setIpFileVersion(jObject.optInt("rqIpFileVer", 1));
        params.setSiteIncId(tObject.site.siteIncId);
        params.setAppUrl(tObject.site.siteUrl);
        params.setRqAdType(tObject.requestedAdType.name());
        params.setRichMedia(tObject.supplyCapability == SupplyCapability.RICH_MEDIA);
        // TODO Change age to integer in DCP
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int yob = tObject.user.yearOfBirth;
        int age = currentYear - yob;
        params.setAge(age + "");

        params.setGender(tObject.user.gender.name());
        // TODO add postal code in thrift params.setPostalCode(tObject.uidParams.user.);
        // TODO user Location??
        // params.setUserLocation(tObject.uidParams.user.);
        params.setGenderOrig(tObject.user.gender.name());
        params.setGender(tObject.user.gender.name());
        setUserIdParams(casInternalRequestParameters, tObject);
        params.setHandsetInternalId(tObject.getDevice().getHandsetInternalId());
        params.setCarrierId(new Long(tObject.getCarrier().carrierId).intValue());
        //TODO Need area/region
        // params.setArea(tObject.geo.);
        params.setCity(tObject.geo.cityId + "");
        logger.debug("Successfully parsed tObject, SAS params are : ", params.toString());
    }

    private static ArrayList<Long> convertIntToLong(List<Integer> intList) {
        ArrayList<Long> longList = new ArrayList<Long>();
        for (Integer obj : intList) {
            longList.add(Long.valueOf(obj));
        }
        return longList;
    }

    public static void setUserIdParams(CasInternalRequestParameters parameter, AdPoolRequest adPoolRequest) {
        if (null == adPoolRequest) {
            return;
        }
        UidParams uidParams = adPoolRequest.uidParams;
        Map<UidType, String> uidMap = uidParams.getUidValues();
        for (UidType uidType : uidMap.keySet()) {
            switch (uidType) {
                //TODO add case for adt value
                case UDID:
                    parameter.uid = uidMap.get(uidType);
                    if (StringUtils.isNotBlank(parameter.uid) && parameter.uid.length() != 32) {
                        parameter.uid = MD5(parameter.uid);
                    }
                case O1:
                    parameter.uidO1 = uidMap.get(uidType);
                    break;
                case UM5:
                    parameter.uidMd5 = uidMap.get(uidType);
                    break;
                case IDA:
                    parameter.uidIFA = uidMap.get(uidType);
                    break;
                case SO1:
                    parameter.uidSO1 = uidMap.get(uidType);
                    break;
                case IDV:
                    parameter.uidIFV = uidMap.get(uidType);
                    break;
                case IUDS1:
                    parameter.uidIDUS1 = uidMap.get(uidType);
                    break;
                default:
                    break;
            }
        }

    }

    public static String MD5(String md5) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        }
        catch (java.security.NoSuchAlgorithmException ignored) {
        }
        return null;
    }
}
