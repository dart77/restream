package com.example.restream;

import com.example.restream.entities.Config;
import com.example.restream.utils.FragmentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
@EnableConfigurationProperties(Config.class)
public class RestreamApplication {

    @Autowired
    private Config config;

    public static void main(String[] args) {

        SpringApplication.run(RestreamApplication.class, args);
    }

    @PostConstruct
    public void postConstruct() throws IOException {

        if (Files.list(Paths.get(config.getFragmentsLocation())).count() == 0) {
            FragmentUtils fragmentUtils = new FragmentUtils(config);
            fragmentUtils.loadData();
        }
    }
}
