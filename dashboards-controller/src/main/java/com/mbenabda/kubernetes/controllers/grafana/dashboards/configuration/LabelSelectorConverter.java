package com.mbenabda.kubernetes.controllers.grafana.dashboards.configuration;

import com.beust.jcommander.IStringConverter;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;

import java.util.Arrays;

public class LabelSelectorConverter implements IStringConverter<LabelSelector> {
    @Override
    public LabelSelector convert(String selector) {
        return selector == null
                ? null
                : Arrays.stream(selector.split(","))
                .map(matcher -> matcher.split("="))
                .reduce(
                        new LabelSelectorBuilder(),
                        (builder, pair) -> builder.addToMatchLabels(pair[0], pair[1]),
                        (left, right) -> left.addToMatchLabels(right.getMatchLabels())
                ).build();
    }
}
