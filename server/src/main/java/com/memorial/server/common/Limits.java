package com.memorial.server.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("memorial.limits")
public record Limits(int writesPerMinute, int photoBytes, int faceBytes) {}
