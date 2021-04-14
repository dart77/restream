package com.example.restream.controllers;

import com.example.restream.entities.Config;
import com.example.restream.utils.PlaylistUtils;
import io.lindstrom.m3u8.parser.MasterPlaylistParser;
import io.lindstrom.m3u8.parser.MediaPlaylistParser;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@RestController
public class PlayListController {

    @Autowired
    public PlaylistUtils playlistUtils;
    @Autowired
    public Config config;

    @SneakyThrows
    @GetMapping("/master")
    public ResponseEntity<String> masterPlayList() {

        MasterPlaylistParser parser = new MasterPlaylistParser();
        return ResponseEntity.ok()
                .header("Content-type", "application/vnd.apple.mpegurl")
                .body(parser.writePlaylistAsString(playlistUtils.createMasterPlayList()));
    }

    @SneakyThrows
    @GetMapping("/media")
    public ResponseEntity<String> mediaPlayList(@RequestParam(value = "profile") String profile) {

        MediaPlaylistParser parser = new MediaPlaylistParser();
        return ResponseEntity.ok()
                .header("Content-type", "application/vnd.apple.mpegurl")
                .body(parser.writePlaylistAsString(playlistUtils.createMediaPlayList(profile)));
    }

    @SneakyThrows
    @GetMapping(value = "/fragment")
    public byte[] getFragment(@RequestParam(value = "index") Integer index,
                              @RequestParam(value = "profile") String profile) {

        InputStream outputStream = new FileInputStream(new File(config.getFragmentsLocation() + profile + "/" + index + ".ts"));
        return IOUtils.toByteArray(outputStream);
    }
}
