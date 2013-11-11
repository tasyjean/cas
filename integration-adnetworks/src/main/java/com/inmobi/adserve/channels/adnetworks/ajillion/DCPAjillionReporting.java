package com.inmobi.adserve.channels.adnetworks.ajillion;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.httpclient.HttpException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;
import com.inmobi.adserve.channels.util.DebugLogger;


public class DCPAjillionReporting extends BaseReportingImpl {
    private final Configuration config;
    private final String        password;
    private String              date;
    private final String        host;
    private final String        userName;
    private DebugLogger         logger;
    private String              endDate;
    private String              name;
    private ReportTime          startTime;

    public DCPAjillionReporting(final Configuration config, final String name) {
        this.config = config;
        password = config.getString(name + ".password");
        host = config.getString(name + ".host");
        userName = config.getString(name + ".user");
        this.name = name;
    }

    @Override
    public int ReportReconcilerWindow() {
        return 10;
    }

    @Override
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final String key,
            final ReportTime endTime) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException,
            ServerException, IOException, ParserConfigurationException, SAXException, JSONException {
        // NO Op
        return null;
    }

    @Override
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final ReportTime endTime)
            throws Exception {
        this.logger = logger;
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);

        logger.debug("inside fetch rows of ", name);
        try {
            this.startTime = startTime;
            date = startTime.getMullahMediaStringDate();
            endDate = endTime == null ? getEndDate() : date;

            if (ReportTime.compareStringDates(endDate, date) == -1) {
                logger.debug("date is greater than the current date reporting window for ", name);
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            logger.info("failed to obtain correct dates for fetching reports ", exception.getMessage());
            return null;
        }

        String tokenResponse = invokeUrl(host, getRequestParams("login", getRequestToken()));
        String token = new JSONObject(tokenResponse).getJSONObject("result").getString("token");
        JSONObject requestParamList = new JSONObject();
        requestParamList.put("token", token);
        String publisherListResponse = invokeUrl(host, getRequestParams("get_publishers_list", requestParamList));
        JSONArray pubLists = new JSONObject(publisherListResponse).getJSONArray("result");
        JSONArray pubIdArray = new JSONArray();
        for (int limit = 0; limit < pubLists.length(); limit++) {
            int pubId = pubLists.getJSONObject(limit).getInt("id");
            pubIdArray.put(pubId);
        }
        requestParamList.put("publisher_ids", pubIdArray);
        String placementListResponse = invokeUrl(host, getRequestParams("get_placements", requestParamList));
        JSONArray placementLists = new JSONObject(placementListResponse).getJSONArray("result");
        JSONArray placementIdArray = new JSONArray();
        for (int limit = 0; limit < placementLists.length(); limit++) {
            int placementId = placementLists.getJSONObject(limit).getInt("id");
            placementIdArray.put(placementId);
        }
        requestParamList.put("placement_ids", placementIdArray);
        requestParamList.put("start_date", date);
        requestParamList.put("end_date", date);
        JSONArray columns = new JSONArray();
        columns.put("external_publisher");
        JSONArray sums = new JSONArray();
        sums.put("hits");
        sums.put("impressions");
        sums.put("revenue");
        requestParamList.put("columns", columns);
        requestParamList.put("sums", sums);
        String revenueResponse = invokeUrl(host, getRequestParams("publisher_report", requestParamList));
        generateReportResponse(logger, reportResponse, new JSONObject(revenueResponse).getJSONArray("result"));
        return reportResponse;
    }

    @Override
    public String getAdvertiserId() {
        return (config.getString(name + ".advertiserId"));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public String getRequestUrl() {
        return host;
    }

    @Override
    public double getTimeZone() {
        return 1;
    }

    public String getEndDate() {
        try {
            logger.debug("calculating end date for ", name);
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getMullahMediaStringDate());
        }
        catch (Exception exception) {
            logger.info("failed to obtain end date inside  ", name, exception.getMessage());
            return "";
        }
    }

    private JSONObject getRequestParams(String methodName, JSONObject params) throws JSONException, ServerException {
        JSONObject requestParams = new JSONObject();
        requestParams.put("jsonrpc", "2.0");
        requestParams.put("method", methodName);
        requestParams.put("id", 1);
        requestParams.put("params", params);
        return requestParams;
    }

    private JSONObject getRequestToken() throws JSONException, ServerException {

        JSONObject userParams = new JSONObject();
        userParams.put("password", password);
        userParams.put("username", userName);
        return userParams;
    }

    public String invokeUrl(String host, JSONObject queryObject) throws HttpException, IOException {
        URL url = new URL(host);
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection hconn = (HttpURLConnection) url.openConnection();
        hconn.setRequestMethod("POST");
        hconn.setDoInput(true);
        hconn.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(hconn.getOutputStream());
        wr.writeBytes(queryObject.toString());
        wr.flush();
        wr.close();
        StringBuilder sBuffer = new StringBuilder();
        BufferedReader res = new BufferedReader(new InputStreamReader(hconn.getInputStream(), "UTF-8"));
        String inputLine;
        while ((inputLine = res.readLine()) != null) {
            sBuffer.append(inputLine);
        }
        res.close();
        return sBuffer.toString();
    }

    private void generateReportResponse(final DebugLogger logger, ReportResponse reportResponse, JSONArray responseArray)
            throws JSONException {

        for (int i = 0; i < responseArray.length(); i++) {
            JSONObject reportRow = responseArray.getJSONObject(i);
            ReportResponse.ReportRow row = new ReportResponse.ReportRow();
            if (!decodeBlindedSiteId(reportRow.getString("external_publisher"), row)) {
                logger.debug("Error decoded BlindedSite id in appier", reportRow.getString("external_publisher"));
                continue;
            }
            row.isSiteData = true;
            row.impressions = reportRow.getLong("impressions");
            row.revenue = reportRow.getDouble("revenue");
            row.request = reportRow.getLong("hits");
            row.reportTime = startTime;
            row.slotSize = getReportGranularity();
            reportResponse.addReportRow(row);
        }
    }
}