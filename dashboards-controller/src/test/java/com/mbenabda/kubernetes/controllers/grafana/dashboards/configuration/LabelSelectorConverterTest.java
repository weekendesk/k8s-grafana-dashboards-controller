package com.mbenabda.kubernetes.controllers.grafana.dashboards.configuration;

import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LabelSelectorConverterTest {

    @Test
    public void should_parse_selector() {
        assertEquals(
                new LabelSelectorBuilder()
                        .addToMatchLabels("label1", "value1")
                        .addToMatchLabels("label2", "value2")
                        .build(),
                new LabelSelectorConverter().convert("label1=value1,label2=value2")
        );
    }

}