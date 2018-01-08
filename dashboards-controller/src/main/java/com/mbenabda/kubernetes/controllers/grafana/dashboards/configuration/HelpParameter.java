package com.mbenabda.kubernetes.controllers.grafana.dashboards.configuration;

import com.beust.jcommander.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
public class HelpParameter {
    @Parameter(names = {"--help", "-h"}, help = true, description = "Show these usage instructions")
    private boolean requested;
}
