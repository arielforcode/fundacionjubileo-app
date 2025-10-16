package com.mrx.fundacionjubileo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VerificarStatusActivity extends AppCompatActivity {
    private TextInputEditText etCedula;
    private MaterialButton btnVerificar, btnSi, btnNo;
    private CircularProgressIndicator progressBar;
    private View layoutAcciones, layoutAprovado, layoutRechazado;

    private final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    // TODO: reemplaza con tu URL base
    private static final String BASE_URL = "https://fundacionjubileotest-debfhkc6ezg0c8h2.brazilsouth-01.azurewebsites.net/api/RegistroPersona/verificar";

    String cedulaIdentidad,idUser,idProyectoFinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verificar_status);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etCedula = findViewById(R.id.etCedula);
        btnVerificar = findViewById(R.id.btnVerificar);
        btnSi = findViewById(R.id.btnSi);
        btnNo = findViewById(R.id.btnNo);
        progressBar = findViewById(R.id.progressBar);
        layoutAcciones = findViewById(R.id.layoutAcciones);
        layoutRechazado= findViewById(R.id.layoutRechazado);
        layoutAprovado = findViewById(R.id.layoutAprovado);

        btnVerificar.setOnClickListener(v -> verificarEstado());

        btnSi.setOnClickListener(v ->
                Toast.makeText(this, "Has presionado Sí", Toast.LENGTH_SHORT).show()
        );


    }

    private void verificarEstado() {
        // Verificar vigencia de autorización (igual que en tu otro código)
        SharedPreferences prefs = getSharedPreferences("AutorizacionQr", MODE_PRIVATE);
        String fechaGuardada = prefs.getString("vigencia", null);

        if (fechaGuardada == null) {
            Toast.makeText(this, "Sesión no válida. Inicie sesión nuevamente.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime fechaLocal = LocalDateTime.parse(fechaGuardada, inputFormatter);
            LocalDateTime ahora = LocalDateTime.now();

            if (fechaLocal.isBefore(ahora)) {
                Toast.makeText(this, "La autorización ha caducado.", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al validar vigencia.", Toast.LENGTH_SHORT).show();
            return;
        }

        String ci = etCedula.getText() != null ? etCedula.getText().toString().trim() : "";

        if (ci.isEmpty()) {
            Toast.makeText(this, "Ingresa un número de CI", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        layoutAcciones.setVisibility(View.GONE);

        SharedPreferences prefsSession = getSharedPreferences("session_prefs", MODE_PRIVATE);
        String token = prefsSession.getString("jwt_token", null);
        int idProyecto = prefs.getInt("IdProyecto", -1);

        idProyectoFinal=String.valueOf(idProyecto);
        if (token == null) {
            Toast.makeText(this, "Token no encontrado.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Construir el cuerpo JSON
        JSONObject json = new JSONObject();
        try {
            json.put("idProyecto", idProyecto);
            json.put("cedulaIdentidad", ci);
        } catch (JSONException e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);

        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(VerificarStatusActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respStr = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);

                    try {
                        JSONObject json = new JSONObject(respStr);
                        boolean success = json.optBoolean("success", false);
                        boolean existe = json.optBoolean("existe", false);

                        if (!success) {
                            String message = json.optString("message", "Error desconocido");
                            Toast.makeText(VerificarStatusActivity.this, message, Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (!existe) {
                            Toast.makeText(VerificarStatusActivity.this, "No se encontró registro para esta cédula", Toast.LENGTH_SHORT).show();
                            layoutAcciones.setVisibility(View.VISIBLE);
                            return;
                        }

                        JSONObject registro = json.optJSONObject("registro");
                        String estado = registro != null ? registro.optString("estado", "Sin estado") : "Sin estado";
                        idUser =registro != null ? registro.optString("id", "-1") : "-1";
                        cedulaIdentidad =registro != null ? registro.optString("cedulaIdentidad", "Sin estado") : "Sin estado";
                        // Ocultamos los botones por defecto
                        layoutAcciones.setVisibility(View.GONE);
                        layoutAprovado.setVisibility(View.GONE);
                        layoutRechazado.setVisibility(View.GONE);
                        btnSi.setVisibility(View.GONE);
                        btnNo.setVisibility(View.GONE);

                        switch (estado) {
                            case "Aprobado":
                                layoutAprovado.setVisibility(View.VISIBLE);
                                break;

                            case "Rechazado":
                                layoutRechazado.setVisibility(View.VISIBLE);
                                break;

                            case "Corregir datos":
                                Toast.makeText(VerificarStatusActivity.this, "⚠️ Debes corregir tus datos", Toast.LENGTH_SHORT).show();
                                layoutAcciones.setVisibility(View.VISIBLE);
                                btnSi.setVisibility(View.VISIBLE);
                                btnSi.setEnabled(true); // ahora sí

                                break;

                            case "Corregir fotos":
                                Toast.makeText(VerificarStatusActivity.this, "📷 Debes corregir tus fotos", Toast.LENGTH_SHORT).show();
                                layoutAcciones.setVisibility(View.VISIBLE);
                                btnNo.setVisibility(View.VISIBLE);
                                btnNo.setOnClickListener(v ->
                                        {
                                            Intent intent = new Intent(VerificarStatusActivity.this, UploadDocuments.class);
                                            intent.putExtra("idProyecto", Integer.parseInt(idProyectoFinal));
                                            intent.putExtra("idUser",Integer.parseInt(idUser));
                                            intent.putExtra("dni",Integer.parseInt(cedulaIdentidad));
                                            startActivity(intent);
                                            Toast.makeText(VerificarStatusActivity.this, "Usuario Registrado Correctamente", Toast.LENGTH_LONG).show();
                                            finish();
                                        }
                                );
                                break;

                            default:
                                Toast.makeText(VerificarStatusActivity.this, "Estado desconocido: " + estado, Toast.LENGTH_SHORT).show();
                                break;
                        }


                    } catch (Exception e) {
                        Toast.makeText(VerificarStatusActivity.this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}