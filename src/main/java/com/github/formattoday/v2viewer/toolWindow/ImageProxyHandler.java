package com.github.formattoday.v2viewer.toolWindow;

import com.github.formattoday.v2viewer.settings.V2EXSettings;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class ImageProxyHandler {
    private final OkHttpClient.Builder clientBuilder;
    private static final Pattern IMAGE_URL_PATTERN = 
        Pattern.compile("https?://[^\\s<>]+?\\.(?:jpg|jpeg|png|gif|webp)", Pattern.CASE_INSENSITIVE);

    public ImageProxyHandler(OkHttpClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    public String processImageUrl(String url) {
        if (!IMAGE_URL_PATTERN.matcher(url).matches()) {
            return url;
        }

        try {
            V2EXSettings settings = V2EXSettings.getInstance();
            OkHttpClient client = clientBuilder
                .proxy(V2EXNewsPanel.getProxy(settings))
                .build();

            Request request = new Request.Builder()
                .url(url)
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return url;
                }

                // 直接返回原始URL，但确保已经通过代理预加载了图片
                return url;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return url;
        }
    }

    public byte[] fetchImage(String url) {
        try {
            V2EXSettings settings = V2EXSettings.getInstance();
            OkHttpClient client = clientBuilder.proxy(V2EXNewsPanel.getProxy(settings)).build();

            Request request = new Request.Builder()
                .url(url)
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return null;
                }
                return response.body().bytes();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
} 