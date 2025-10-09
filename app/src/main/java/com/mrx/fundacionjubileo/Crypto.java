package com.mrx.fundacionjubileo;

import android.util.Base64;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class Crypto {
    private Crypto() {}

    public static String hmacSha256Base64Url(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(raw, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
    }

    public static String base64UrlDecodeToString(String base64Url) {
        byte[] raw = Base64.decode(base64Url, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
        return new String(raw, StandardCharsets.UTF_8);
    }

    public static String base64UrlEncode(String plain) {
        byte[] raw = plain.getBytes(StandardCharsets.UTF_8);
        return Base64.encodeToString(raw, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
    }
}
