package com.inmobi.adserve.channels.query;

import com.inmobi.phoenix.data.RepositoryQuery;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class SiteEcpmQuery implements RepositoryQuery {

    private String  siteId;
    private Integer countryId;
    private Integer osId;

}
