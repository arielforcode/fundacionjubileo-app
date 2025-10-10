package com.mrx.fundacionjubileo;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.time.Instant;
public class SessionStore {
    private static final String SP = "session_prefs";
    private static final String K_TOKEN = "jwt_token";
    private static final String K_EXP   = "jwt_exp_utc";
    private static final String K_USER  = "jwt_user_json"; // guardamos user como JSON
    private static final String TAG     = "SessionStore";

    public static void save(Context ctx, String token, String expiresAtUtc, String userJson) {
        ctx.getSharedPreferences(SP, Context.MODE_PRIVATE)
                .edit()
                .putString(K_TOKEN, token)
                .putString(K_EXP, expiresAtUtc)
                .putString(K_USER, userJson)
                .apply();
    }

    public static String getToken(Context ctx) {
        return ctx.getSharedPreferences(SP, Context.MODE_PRIVATE).getString(K_TOKEN, null);
    }

    public static String getExpiresAt(Context ctx) {
        return ctx.getSharedPreferences(SP, Context.MODE_PRIVATE).getString(K_EXP, null);
    }

    public static String getUserJson(Context ctx) {
        return ctx.getSharedPreferences(SP, Context.MODE_PRIVATE).getString(K_USER, null);
    }

    public static boolean isTokenValid(Context ctx) {
        String token = getToken(ctx);
        String exp   = getExpiresAt(ctx);
        if (token == null || exp == null) return false;
        try {
            // Requiere API 26+ (Android 8). Si apuntas a menos, avísame y te paso una versión con SimpleDateFormat.
            Instant expInstant = Instant.parse(exp);
            return Instant.now().isBefore(expInstant);
        } catch (Exception e) {
            Log.e(TAG, "Error parseando expiración", e);
            return false;
        }
    }

    public static void clear(Context ctx) {
        ctx.getSharedPreferences(SP, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
