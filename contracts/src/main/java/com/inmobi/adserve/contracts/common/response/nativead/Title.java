package com.inmobi.adserve.contracts.common.response.nativead;

import com.inmobi.adserve.contracts.ix.common.CommonExtension;
import com.inmobi.template.gson.GsonContract;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author ritwik.kumar
 */
@Getter
@Setter
@NoArgsConstructor
@GsonContract
public class Title {
    // Required by contract
    private String text;
    private CommonExtension ext;
}
