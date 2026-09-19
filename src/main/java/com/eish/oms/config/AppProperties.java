package com.eish.oms.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Application settings bound from the {@code app.*} keys in application.yml.
 *
 * @param taxRate flat tax rate applied to the post-discount subtotal, e.g. 0.10 for 10%
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(@DefaultValue("0.10") BigDecimal taxRate) {
}
