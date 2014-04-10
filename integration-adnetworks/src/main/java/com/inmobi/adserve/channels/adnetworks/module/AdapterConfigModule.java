package com.inmobi.adserve.channels.adnetworks.module;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.config.AdapterConfig;
import com.inmobi.adserve.channels.api.config.AdapterConfigFactory;
import com.inmobi.adserve.channels.util.annotations.AdvertiserIdNameMap;


/**
 * @author abhishek.parwal
 * 
 */
public class AdapterConfigModule extends AbstractModule {

    private final Configuration allAdapterConfiguration;
    private final String        dcName;

    public AdapterConfigModule(final Configuration allAdapterConfiguration, final String dcName) {
        this.allAdapterConfiguration = allAdapterConfiguration;
        this.dcName = dcName;
    }

    @Override
    protected void configure() {

        requestStaticInjection(BaseAdNetworkImpl.class);
        requestStaticInjection(RtbAdNetwork.class);

        install(new FactoryModuleBuilder().build(AdapterConfigFactory.class));
        bind(String.class).annotatedWith(Names.named("dcName")).toProvider(Providers.of(dcName));
    }

    @Provides
    @Singleton
    Map<String, AdapterConfig> provideAdvertiserIdConfigMap(final AdapterConfigFactory adapterConfigFactory) {

        @SuppressWarnings("unchecked")
        Iterator<String> keyIterator = allAdapterConfiguration.getKeys();

        Set<String> adapterNames = Sets.newHashSet();

        Set<AdapterConfig> adapterConfigs = Sets.newHashSet();

        while (keyIterator.hasNext()) {
            String key = keyIterator.next();

            String adapterName = key.substring(0, key.indexOf("."));
            adapterNames.add(adapterName);
        }

        for (String adapterName : adapterNames) {
            Configuration adapterConfiguration = allAdapterConfiguration.subset(adapterName);
            AdapterConfig adapterConfig = adapterConfigFactory.create(adapterConfiguration, adapterName);
            adapterConfigs.add(adapterConfig);
        }

        Map<String, AdapterConfig> advertiserIdConfigMap = Maps.newHashMap();

        for (AdapterConfig adapterConfig : adapterConfigs) {
            advertiserIdConfigMap.put(adapterConfig.getAdvertiserId(), adapterConfig);
        }
        return advertiserIdConfigMap;
    }

    @Provides
    @Singleton
    @AdvertiserIdNameMap
    Map<String, String> provideAdvertiserIdToNameMap(final AdapterConfigFactory adapterConfigFactory) {

        @SuppressWarnings("unchecked")
        Iterator<String> keyIterator = allAdapterConfiguration.getKeys();

        Set<String> adapterNames = Sets.newHashSet();

        Set<AdapterConfig> adapterConfigs = Sets.newHashSet();

        while (keyIterator.hasNext()) {
            String key = keyIterator.next();

            String adapterName = key.substring(0, key.indexOf("."));
            adapterNames.add(adapterName);
        }

        for (String adapterName : adapterNames) {
            Configuration adapterConfiguration = allAdapterConfiguration.subset(adapterName);
            AdapterConfig adapterConfig = adapterConfigFactory.create(adapterConfiguration, adapterName);
            adapterConfigs.add(adapterConfig);
        }

        Map<String, String> advertiserIdToNameMap = Maps.newHashMap();

        for (AdapterConfig adapterConfig : adapterConfigs) {
            advertiserIdToNameMap.put(adapterConfig.getAdvertiserId(), adapterConfig.getAdapterName());
        }

        return advertiserIdToNameMap;
    }

}