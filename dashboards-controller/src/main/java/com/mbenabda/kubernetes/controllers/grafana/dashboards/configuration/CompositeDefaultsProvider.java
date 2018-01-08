package com.mbenabda.kubernetes.controllers.grafana.dashboards.configuration;

import com.beust.jcommander.IDefaultProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class CompositeDefaultsProvider implements IDefaultProvider {
    private final IDefaultProvider[] defaultProviders;

    public CompositeDefaultsProvider(IDefaultProvider... defaultProviders) {
        this.defaultProviders = requireNonNull(defaultProviders);
    }

    public CompositeDefaultsProvider(List<IDefaultProvider> defaultProviders) {
        this(requireNonNull(defaultProviders).toArray(new IDefaultProvider[defaultProviders.size()]));
    }

    public CompositeDefaultsProvider(Map<String, Supplier<String>> defaultProviders) {
        this(
                requireNonNull(defaultProviders)
                        .entrySet()
                        .stream()
                        .map(
                                entry -> (IDefaultProvider) optionName -> entry.getKey().equals(optionName)
                                        ? entry.getValue().get()
                                        : null
                        )
                        .collect(toList())
        );
    }

    @Override
    public String getDefaultValueFor(String optionName) {
        return Arrays.stream(defaultProviders)
                .map(p -> p.getDefaultValueFor(optionName))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
