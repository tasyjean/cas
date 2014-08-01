package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

@Getter
public final class NativeAdTemplateEntity implements IdentifiableEntity<String> {
  private static final long serialVersionUID = -648051414378424341L;
  private final String siteId;
  private final long nativeAdId;
  private final String mandatoryKey;
  private final String imageKey;
  private final Timestamp modifiedOn;

  private NativeAdTemplateEntity(Builder builder) {
    this.siteId = builder.siteId;
    this.nativeAdId = builder.nativeAdId;
    this.imageKey = builder.imageKey;
    this.mandatoryKey = builder.mandatoryKey;
    this.modifiedOn = builder.modifiedOn;
  }

  @Override
  public String getJSON() {
    return String.format("{\"siteId\":\"%s\",\"nativeAdId\":%s,\"mandatoryKey\":\"%s\",\"imageKey\":\"%s\"}", siteId,
        nativeAdId, mandatoryKey, imageKey);
  }

  @Override
  public String getId() {
    return this.siteId;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Setter
  public static class Builder {
    private String siteId;
    private long nativeAdId;
    private String mandatoryKey;
    private String imageKey;
    private Timestamp modifiedOn;

    public NativeAdTemplateEntity build() {
      return new NativeAdTemplateEntity(this);
    }
  }

}
