CREATE TYPE ix_package_type_06072015 AS
(
    id                          INTEGER,
    name                        CHARACTER VARYING(128),
    rp_data_segment_id          CHARACTER VARYING(16),
    pmp_class                   CHARACTER VARYING(32),
    country_ids                 INTEGER[],
    inventory_types             CHARACTER VARYING(16)[],
    os_ids                      SMALLINT[],
    os_version_targeting        TEXT,
    manuf_model_targeting       TEXT,
    carrier_ids                 BIGINT[],
    site_categories             CHARACTER VARYING(16)[],
    connection_types            CHARACTER VARYING(16)[],
    geo_source_types            CHARACTER VARYING(16)[],
    geo_fence_region            CHARACTER VARYING(255),
    app_store_categories        SMALLINT[],
    sdk_versions                CHARACTER VARYING(16)[],
    lat_long_only               BOOLEAN,
    zip_code_only               BOOLEAN,
    ifa_only                    BOOLEAN,
    site_ids                    CHARACTER VARYING(128)[],
    data_vendor_id              INTEGER,
    dmp_id                      INTEGER,
    dmp_filter_expression       TEXT,
    zip_codes                   CHARACTER VARYING(10)[],
    cs_ids                      INTEGER[],
    min_bid                     NUMERIC(10,6),
    scheduled_tods              SMALLINT[],
    placement_ad_types          CHARACTER VARYING(16)[],
    placement_slot_ids          SMALLINT[],
    is_active                   BOOLEAN,
    start_date                  TIMESTAMP WITHOUT TIME ZONE,
    end_date                    TIMESTAMP WITHOUT TIME ZONE,
    last_modified               TIMESTAMP WITHOUT TIME ZONE,
    data_vendor_cost            DOUBLE PRECISION,
    city_ids                    INTEGER[],
    modified_by                 CHARACTER VARYING(100),
    access_types                CHARACTER VARYING(32)[],
    deal_ids                    CHARACTER VARYING[],
    deal_floors                 DOUBLE PRECISION[],
    rp_agency_ids               INTEGER[],
    agency_rebate_percentages   DOUBLE PRECISION[]
);
ALTER TYPE ix_package_type_06072015 OWNER TO postgres;


