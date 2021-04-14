package com.example.restream.utils;

import com.example.restream.entities.Config;
import io.lindstrom.m3u8.model.MasterPlaylist;
import io.lindstrom.m3u8.model.MediaPlaylist;
import io.lindstrom.m3u8.model.Variant;
import io.lindstrom.m3u8.parser.MasterPlaylistParser;
import io.lindstrom.m3u8.parser.MediaPlaylistParser;
import io.lindstrom.m3u8.parser.ParsingMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FragmentUtils {

    private Config config;

    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final MasterPlaylistParser masterPlaylistParser = new MasterPlaylistParser(ParsingMode.LENIENT);
    private final MediaPlaylistParser mediaPlaylistParser = new MediaPlaylistParser();

    public void loadData() throws IOException {

        Request request = new Request.Builder()
                .url(config.getUgVod())
                .build();
        Response response = okHttpClient.newCall(request).execute();
        MasterPlaylist masterPlaylist = masterPlaylistParser.readPlaylist(response.body().string().replaceAll("\r", ""));
        masterPlaylist.variants().forEach(this::downloadVariantPlayList);
    }

    @SneakyThrows
    public void downloadVariantPlayList(Variant variants) {
        Request request = new Request.Builder().url(variants.uri()).build();
        MediaPlaylist mediaPlaylist = mediaPlaylistParser.readPlaylist(okHttpClient.newCall(request).execute().body().string().replaceAll("\r", ""));

        for (int i = 0; i < 10; i++) {
            Response response = okHttpClient.newCall(new Request.Builder().url(mediaPlaylist.mediaSegments().get(i).uri()).build()).execute();
            File file = new File(config.getFragmentsLocation() + getProfile(variants) + "/");
            file.mkdir();
            FileOutputStream fileOutputStream = new FileOutputStream(new File(config.getFragmentsLocation() + getProfile(variants) + "/" + i + ".ts"));
            fileOutputStream.write(response.body().bytes());
            fileOutputStream.close();
        }
        String dataString = String.format("BANDWIDTH=%s,FRAME-RATE=%s,WIDTH=%s,HEIGHT=%s,", variants.bandwidth(), variants.frameRate().get(), variants.resolution().get().width(), variants.resolution().get().height());
        Files.write(Path.of(config.getFragmentsLocation() + getProfile(variants) + "/data"), dataString.getBytes());
    }

    private String getProfile(Variant variant) {

        return StringUtils.substringBetween(variant.uri(), "stream=", "&");
    }

    private String getName(Variant variant) {

        return StringUtils.substringBetween(variant.uri(), "stream=", "&");
    }
}
