package com.inmobi.adserve.channels.server;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.util.DebugLogger;

public class RequestParser {

  public static JSONObject extractParams(Map<String, List<String>> params, DebugLogger logger) throws Exception {
    return extractParams(params, "args", logger);
  }

  // Extracting params.
  public static JSONObject extractParams(Map<String, List<String>> params, String jsonKey, DebugLogger logger)
      throws Exception, JSONException {
    JSONObject jObject = null;
    if(!params.isEmpty()) {
      for (Entry<String, List<String>> p : params.entrySet()) {
        String key = p.getKey();
        List<String> vals = p.getValue();
        for (String val : vals) {
          if(key.equalsIgnoreCase(jsonKey)) {
            jObject = new JSONObject(val);
          }
        }
      }
    }
    return jObject;
  }

  public static SASRequestParameters parseRequestParameters(JSONObject jObject, DebugLogger logger) {
    SASRequestParameters params = new SASRequestParameters();
    logger.debug("inside parameter parser");
    if(null == jObject) {
      logger.debug("Returning null as jObject is null.");
      return null;
    }
    params.setAllParametersJson(jObject.toString());
    params.setRemoteHostIp(stringify(jObject, "w-s-carrier", logger));
    params.setUserAgent(stringify(jObject, "rq-x-inmobi-phone-useragent", logger));
    if(null == params.getUserAgent()) {
      params.setUserAgent(stringify(jObject, "rq-h-user-agent", logger));
    }
    params.setLocSrc(stringify(jObject, "loc-src", logger));
    params.setLatLong(stringify(jObject, "latlong", logger));
    params.setSiteId(stringify(jObject, "rq-mk-siteid", logger));
    params.setSource(stringify(jObject, "source", logger));
    params.setCountry(parseArray(jObject, "carrier", 2));
    params.setCountryStr(parseArray(jObject, "carrier", 1));
    params.setArea(parseArray(jObject, "carrier", 4));
    params.setSlot(stringify(jObject, "slot-served", logger));
    params.setRqMkSlot(stringify(jObject, "rq-mk-ad-slot", logger));
    params.setSdkVersion(stringify(jObject, "sdk-version", logger));
    params.setSiteType(stringify(jObject, "site-type", logger));
    params.setAdcode(stringify(jObject, "adcode", logger));
    params.setPlatformOsId(jObject.optInt("os-id", -1));
    if(params.getSiteType() != null) {
      params.setSiteType(params.getSiteType().toUpperCase());
    }
    params.setCategories(getCategory(jObject, logger, "new-category"));
    params.setRqIframe(stringify(jObject, "rq-iframe", logger));
    params.setRFormat(stringify(jObject, "r-format", logger));
    params.setRqMkAdcount(stringify(jObject, "rq-mk-adcount", logger));
    params.setTid(stringify(jObject, "tid", logger));
    params.setTp(stringify(jObject, "tp", logger));
    
    params.setAllowBannerAds(jObject.optBoolean("site-allowBanner", true));
    params.setSiteFloor(jObject.optDouble("site-floor", 0.0));
    params.setSiteSegmentId(jObject.optInt("sel-seg-id", 0));
    logger.debug("Site segment id is", params.getSiteSegmentId());
    params.setIpFileVersion(jObject.optInt("rq-ip-file-ver", 1));
    logger.debug("country obtained is", params.getCountry());
    logger.debug("site floor is", params.getSiteFloor());
    logger.debug("osId is", params.getPlatformOsId());
    params.setUidParams(stringify(jObject, "u-id-params", logger));
    params = getUserIdParams(params, jObject, logger);
    params = getUserParams(params, jObject, logger);
    try {
      JSONArray siteInfo = jObject.getJSONArray("site");
      if(siteInfo != null && siteInfo.length() > 0) {
        params.setSiteIncId(siteInfo.getLong(0));
      }
    } catch (JSONException exception) {
      logger.debug("site object not found in request");
      params.setSiteIncId(0);
    }
    try {
      params.setHandset(jObject.getJSONArray("handset"));
    } catch (JSONException e) {
      logger.debug("Handset array not found");
    }
    try {
      params.setCarrier(jObject.getJSONArray("carrier"));
    } catch (JSONException e) {
      logger.debug("carrier array not found");
    }
    if(null == params.getUid() || params.getUid().isEmpty()) {
      params.setUid(stringify(jObject, "u-id", logger));
    }
    params.setOsId(jObject.optInt("os-id", -1));
    params.setRichMedia(jObject.optBoolean("rich-media", false));
    params.setRqAdType(stringify(jObject, "rq-adtype", logger));
    logger.debug("successfully parsed params");
    return params;
  }

  public static String stringify(JSONObject jObject, String field, DebugLogger logger) {
    String fieldValue = "";
    try {
      Object fieldValueObject = jObject.get(field);
      if (null != fieldValueObject) {
        fieldValue = fieldValueObject.toString();
      }
    } catch (JSONException e) {
      return null;
    }
    logger.debug("Retrived from json", field, " = ", fieldValue);
    return fieldValue;
  }

  public static String parseArray(JSONObject jObject, String param, int index) {
    if(null == jObject) {
      return null;
    }
    try {
      JSONArray jArray = jObject.getJSONArray(param);
      if(null == jArray) {
        return null;
      } else {
        return (jArray.getString(index));
      }
    } catch (JSONException e) {
      return null;
    }
  }

  public static List<Long> getCategory(JSONObject jObject, DebugLogger logger, String oldORnew) {
    try {
      JSONArray categories = jObject.getJSONArray(oldORnew);
      Long[] category = new Long[categories.length()];
      for (int index = 0; index < categories.length(); index++) {
        category[index] = categories.getLong(index);
      }
      return Arrays.asList(category);
    } catch (JSONException e) {
      logger.error("error while reading category array");
      return null;
    }
  }

  // Get user specific params
  public static SASRequestParameters getUserParams(SASRequestParameters parameter, JSONObject jObject,
      DebugLogger logger) {
    logger.debug("inside parsing user params");
    String utf8 = "UTF-8";
    try {
      JSONObject userMap = (JSONObject) jObject.get("uparams");
      parameter.setAge(stringify(userMap, "u-age", logger));
      parameter.setGender(stringify(userMap, "u-gender", logger));
      if(StringUtils.isEmpty(parameter.getUid())) {
        parameter.setUid(stringify(userMap, "u-id", logger));
      }
      parameter.setPostalCode(stringify(userMap, "u-postalcode", logger));
      if(!StringUtils.isEmpty(parameter.getPostalCode())) {
        parameter.setPostalCode(parameter.getPostalCode().replaceAll(" ", ""));
      }
      parameter.setUserLocation(stringify(userMap, "u-location", logger));
      parameter.setGenderOrig(stringify(userMap, "u-gender-orig", logger));
      try {
        if (null != parameter.getAge()) {
          parameter.setAge(URLEncoder.encode(parameter.getAge(), utf8));
        }
        if (null != parameter.getGender()) {
          parameter.setGender(URLEncoder.encode(parameter.getGender(), utf8));
        }
        if (null != parameter.getPostalCode()) {
          parameter.setPostalCode(URLEncoder.encode(parameter.getPostalCode(), utf8));
        }
      } catch (UnsupportedEncodingException e) {
        logger.error("Error in encoding u params", e.getMessage());
      }
    } catch (JSONException exception) {
      logger.error("json exception in parsing u params", exception);
    }
    return parameter;
  }

  // Get user id params
  public static SASRequestParameters getUserIdParams(SASRequestParameters parameter, JSONObject jObject,
      DebugLogger logger) {
    logger.debug("inside parsing userid params");
    if (null == jObject) {
      return parameter;
    }
    try {
      JSONObject userIdMap = (JSONObject) jObject.get("u-id-params");
      if (null == userIdMap) {
        return parameter;
      }
      String o1Uid = stringify(userIdMap, "SO1", logger);
      parameter.setUid(stringify(userIdMap, "u-id", logger));
      parameter.setUidO1((o1Uid != null) ? o1Uid : stringify(userIdMap, "O1", logger));
      parameter.setUidMd5(stringify(userIdMap, "UM5", logger));
      String uidIFA = "iphone".equalsIgnoreCase(parameter.getSource()) ? stringify(userIdMap, "IDA", logger) : null;
      parameter.setUidIFA(uidIFA);
    } catch (JSONException exception) {
      setNullValueForUid(parameter, logger);
    }
    return parameter;
  }

  private static void setNullValueForUid(SASRequestParameters parameter, DebugLogger logger) {
    parameter.setUidO1(null);
    parameter.setUidMd5(null);
    parameter.setUidIFA(null);
    logger.error("uidparams missing in the request");
  }
}
