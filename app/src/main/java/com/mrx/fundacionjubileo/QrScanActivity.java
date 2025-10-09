package com.mrx.fundacionjubileo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class QrScanActivity extends AppCompatActivity {

    //todo Marcar el token con el mismo token de encriptado en el qr del sistema web
    private static final String QR_SECRET = "cambia-esta-clave-larga-y-aleatoria";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.google.zxing.integration.android.IntentIntegrator integrator =
                new com.google.zxing.integration.android.IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(
                com.google.zxing.integration.android.IntentIntegrator.QR_CODE);
        integrator.setPrompt("Apunta al código QR");
        integrator.setBeepEnabled(true);
        integrator.setCameraId(0);
        integrator.setBarcodeImageEnabled(false);
        integrator.setOrientationLocked(true);
        integrator.setCaptureActivity(CaptureActivityPortrait.class);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        var result = com.google.zxing.integration.android.IntentIntegrator
                .parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Lectura cancelada", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            String token = result.getContents(); // "<b64url payload>.<b64url firma>"
            try {
                Payload p = verificarYDecodificarToken(token, QR_SECRET);
                if (p == null) {
                    Toast.makeText(this, "Firma inválida", Toast.LENGTH_LONG).show();
                } else {
                    String msg = "ID proyecto: " + p.pid + "\nVigencia UTC: " + p.vigInstant;
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
            } catch (Exception ex) {
                Toast.makeText(this, "Error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // ==== MODELO DE RETORNO ====
    static class Payload {
        public int pid;
        public java.time.Instant vigInstant;
    }

    // ==== VERIFICACIÓN Y DECODE ====
    private Payload verificarYDecodificarToken(String token, String secret) throws Exception {
        int dot = token.lastIndexOf('.');
        if (dot <= 0 || dot == token.length() - 1) return null;

        String payloadB64 = token.substring(0, dot);
        String sigB64 = token.substring(dot + 1);

        byte[] payloadBytes = base64UrlDecode(payloadB64);
        byte[] firmaRecibida = base64UrlDecode(sigB64);

        byte[] firmaCalculada = hmacSha256(payloadBytes, secret);
        if (!java.security.MessageDigest.isEqual(firmaRecibida, firmaCalculada)) {
            return null; // firma inválida
        }

        // payload es JSON como: {"pid":1,"vig":"20251010T183000Z"}
        String payloadJson = new String(payloadBytes, java.nio.charset.StandardCharsets.UTF_8);
        org.json.JSONObject obj = new org.json.JSONObject(payloadJson);
        int pid = obj.getInt("pid");
        String vig = obj.getString("vig");

        // Parsear "yyyyMMddTHHmmssZ" a Instant UTC
        java.time.format.DateTimeFormatter fmt =
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                        .withZone(java.time.ZoneOffset.UTC);
        java.time.Instant vigInstant = java.time.Instant.from(fmt.parse(vig));

        Payload p = new Payload();
        p.pid = pid;
        p.vigInstant = vigInstant;
        return p;
    }

    // ==== HELPERS CRIPTO/BASE64URL ====
    private static byte[] hmacSha256(byte[] data, String secret) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.spec.SecretKeySpec key = new javax.crypto.spec.SecretKeySpec(
                secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(key);
        return mac.doFinal(data);
    }

    private static byte[] base64UrlDecode(String s) {
        return android.util.Base64.decode(
                s, android.util.Base64.URL_SAFE | android.util.Base64.NO_WRAP | android.util.Base64.NO_PADDING);
    }

}