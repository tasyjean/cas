package com.inmobi.adserve.contracts.ix.request;

import java.util.List;

import com.inmobi.adserve.contracts.ix.common.CommonExtension;
import com.inmobi.adserve.contracts.common.request.nativead.Data;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@lombok.Data
public final class User {
    private String id;
    private String buyeruid;
    private String keywords;
    private String customdata;
    private List<Data> data;
    private CommonExtension ext;
}
