package com.inmobi.adserve.channels.server.module;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import com.inmobi.adserve.channels.server.config.AdapterConfig;
import com.inmobi.adserve.channels.server.config.BaseAdapterConfig;


/**
 * @author abhishek.parwal
 * 
 */
public class AdapterConfigModule extends AbstractModule {

    private final Configuration adapterConfiguration;
    private final String        dcName;

    public AdapterConfigModule(final Configuration adapterConfiguration, final String dcName) {
        this.adapterConfiguration = adapterConfiguration;
        this.dcName = dcName;
    }

    @Override
    protected void configure() {

        @SuppressWarnings("unchecked")
        Iterator<String> keyIterator = adapterConfiguration.getKeys();

        Set<String> adapterNames = Sets.newHashSet();

        Set<AdapterConfig> adapterConfigs = Sets.newHashSet();

        while (keyIterator.hasNext()) {
            String key = keyIterator.next();

            String adapterName = key.substring(0, key.indexOf("."));
            adapterNames.add(adapterName);
        }

        for (String adapterName : adapterNames) {
            Configuration adapterConfig = adapterConfiguration.subset(adapterName);

            BaseAdapterConfig baseAdapterConfig = new BaseAdapterConfig(adapterConfig, adapterName, dcName);

            adapterConfigs.add(baseAdapterConfig);

        }

        MapBinder<String, AdapterConfig> advertiserIdConfigMapBinder = MapBinder.newMapBinder(binder(), String.class,
            AdapterConfig.class);
        MapBinder<String, String> advertiserIdToNameMapBinder = MapBinder.newMapBinder(binder(), String.class,
            String.class, Names.named("advertiserIdToNameMap"));

        for (AdapterConfig adapterConfig : adapterConfigs) {
            advertiserIdConfigMapBinder.addBinding(adapterConfig.getAdvertiserId()).toInstance(adapterConfig);
            advertiserIdToNameMapBinder.addBinding(adapterConfig.getAdvertiserId()).toInstance(
                adapterConfig.getAdapterName());
        }

    }
}