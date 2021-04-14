package com.example.restream.utils;

import com.example.restream.entities.Config;
import io.lindstrom.m3u8.model.MasterPlaylist;
import io.lindstrom.m3u8.model.MediaPlaylist;
import io.lindstrom.m3u8.model.MediaSegment;
import io.lindstrom.m3u8.model.Resolution;
import io.lindstrom.m3u8.model.Variant;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlaylistUtils {

    @Autowired
    private Config config;

    public MediaPlaylist createMediaPlayList(String profile) throws IOException {
        List<String> files = Files.list(Paths.get(config.getFragmentsLocation() + profile + "/"))
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> name.contains(".ts"))
                .sorted()
                .collect(Collectors.toList());

        return MediaPlaylist.builder()
                .version(3)
                .targetDuration(18)
                .mediaSequence(1)
                .ongoing(false)
                .addAllMediaSegments(
                        files.stream()
                                .map(file -> MediaSegment.builder()
                                        .duration(6)
                                        .uri(config.getHost() + "/fragment?index=" + file.split("\\.")[0] + "&profile=" + profile)
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }

    public MasterPlaylist createMasterPlayList() throws IOException {
        List<String> files = Files.list(Paths.get(config.getFragmentsLocation()))
                .map(Path::getFileName).map(Path::toString).sorted().collect(Collectors.toList());

        return MasterPlaylist.builder()
                .version(3)
                .independentSegments(true)
                .addAllVariants(
                        files.stream().map(folder ->
                                                   Variant.builder()
                                                           .bandwidth(getBandwith(folder))
                                                           .frameRate(getFramerate(folder))
                                                           .resolution(getResolition(folder))
                                                           .uri(config.getHost() + "/media?profile=" + folder)
                                                           .build())
                                .collect(Collectors.toList()))
                .build();

    }

    @SneakyThrows
    private Resolution getResolition(String profile) {
        String data = getDataString(profile);
        return Resolution.of(Integer.parseInt(StringUtils.substringBetween(data, ",WIDTH=", ",HEIGHT")), Integer.parseInt(StringUtils.substringBetween(data, "HEIGHT=", ",")));
    }

    @SneakyThrows
    private Long getBandwith(String profile) {
        String data = getDataString(profile);
        return Long.parseLong(StringUtils.substringBetween(data, "BANDWIDTH=", ","));
    }

    @SneakyThrows
    private Double getFramerate(String profile) {
        String data = getDataString(profile);
        return Double.parseDouble(StringUtils.substringBetween(data, "FRAME-RATE=", ","));
    }

    @SneakyThrows
    private String getDataString(String profile) {
        return Files.readString(Path.of(config.getFragmentsLocation() + profile + "/data"));
    }
}
