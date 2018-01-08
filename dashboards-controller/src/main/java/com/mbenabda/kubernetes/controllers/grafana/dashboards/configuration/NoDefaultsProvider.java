package com.mbenabda.kubernetes.controllers.grafana.dashboards.configuration;

import com.beust.jcommander.IDefaultProvider;

public class NoDefaultsProvider implements IDefaultProvider {
    @Override
    public String getDefaultValueFor(String optionName) {
        return null;
    }
}
