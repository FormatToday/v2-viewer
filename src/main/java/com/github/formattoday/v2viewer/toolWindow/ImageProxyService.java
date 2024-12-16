package com.github.formattoday.v2viewer.toolWindow;

import com.github.formattoday.v2viewer.settings.V2EXSettings;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public class ImageProxyService {
    private static final ConcurrentHashMap<String, String> imageCache = new ConcurrentHashMap<>();
    private final OkHttpClient.Builder clientBuilder;

    public ImageProxyService(OkHttpClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    public String getProxiedImageUrl(String originalUrl) {
        // 检查缓存
        String cachedImage = imageCache.get(originalUrl);
        if (cachedImage != null) {
            return cachedImage;
        }

        try {
            V2EXSettings settings = V2EXSettings.getInstance();
            OkHttpClient client = clientBuilder.proxy(V2EXNewsPanel.getProxy(settings)).build();

            Request request = new Request.Builder()
                .url(originalUrl)
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return originalUrl;
                }

                ResponseBody body = response.body();
                if (body == null) {
                    return originalUrl;
                }

                byte[] imageData = body.bytes();
                
                // 读取图片并调整大小
                BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
                if (originalImage == null) {
                    return originalUrl;
                }

                // 如果图片太大，进行缩放
                BufferedImage resizedImage = resizeImageIfNeeded(originalImage);
                
                // 转换为 Base64
                String base64Image = imageToBase64(resizedImage);
                String dataUrl = "data:image/png;base64," + base64Image;
                
                // 缓存结果
                imageCache.put(originalUrl, dataUrl);
                return dataUrl;
            }
        } catch (IOException e) {
            return originalUrl;
        }
    }

    private BufferedImage resizeImageIfNeeded(BufferedImage original) {
        int maxWidth = 800;  // 最大宽度
        int maxHeight = 600; // 最大高度

        if (original.getWidth() <= maxWidth && original.getHeight() <= maxHeight) {
            return original;
        }

        double scale = Math.min(
            (double) maxWidth / original.getWidth(),
            (double) maxHeight / original.getHeight()
        );

        int newWidth = (int) (original.getWidth() * scale);
        int newHeight = (int) (original.getHeight() * scale);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return resized;
    }

    private String imageToBase64(BufferedImage image) throws IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        ImageIO.write(image, "PNG", out);
        return Base64.getEncoder().encodeToString(out.toByteArray());
    }

    public static void clearCache() {
        imageCache.clear();
    }
} 