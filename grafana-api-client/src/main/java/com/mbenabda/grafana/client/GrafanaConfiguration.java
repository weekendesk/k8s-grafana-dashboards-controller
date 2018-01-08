package com.mbenabda.grafana.client;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(fluent = true)
public class GrafanaConfiguration {
    private String host;
    private GrafanaAuthConfiguration auth;
}
