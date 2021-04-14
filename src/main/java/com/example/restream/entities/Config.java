package com.example.restream.entities;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
@Data
public class Config {

    private String ugVod;
    private String fragmentsLocation;
    private String host;
}
