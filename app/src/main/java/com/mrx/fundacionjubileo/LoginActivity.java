package com.mrx.fundacionjubileo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private MaterialSwitch switchPhoneLogin;
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;

    // Credenciales de prueba (hardcodeadas)
    private static final String TAG = "LoginActivity";
    private static final String BASE_URL = "https://fundacionjubileotest-debfhkc6ezg0c8h2.brazilsouth-01.azurewebsites.net"; // <-- cámbialo
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient http = new OkHttpClient();
    private final Gson gson = new GsonBuilder().create();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        switchPhoneLogin = findViewById(R.id.switchPhoneLogin);
        tilEmail         = findViewById(R.id.tilEmail);
        etEmail          = findViewById(R.id.etEmail);
        tilPassword      = findViewById(R.id.tilPassword);
        etPassword       = findViewById(R.id.etPassword);
        btnLogin         = findViewById(R.id.btnLogin);

        // --- VALIDAR ESTADO AL INICIAR ---
        updateHintBasedOnSwitch(switchPhoneLogin.isChecked());

        // --- ESCUCHAR CAMBIOS EN EL SWITCH ---
        switchPhoneLogin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateHintBasedOnSwitch(isChecked);
            tilEmail.setError(null);
            tilPassword.setError(null);
            etEmail.setText("");
            etPassword.setText("");
        });

        if (SessionStore.isTokenValid(this)) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }


        btnLogin.setOnClickListener(v -> {
            tilEmail.setError(null);
            tilPassword.setError(null);

            String input = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String pass  = etPassword.getText() != null ? etPassword.getText().toString() : "";

            // Validaciones
            if (switchPhoneLogin.isChecked()) {
                boolean onlyDigits = input.matches("^[0-9]{8}$");
                boolean startsOk   = input.startsWith("6") || input.startsWith("7");
                if (!(onlyDigits && startsOk)) {
                    tilEmail.setError("Número de celular inválido");
                    Toast.makeText(this, "Número de celular inválido", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                boolean hasAt = input.contains("@");
                boolean patternOk = android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches();
                if (!(hasAt && patternOk)) {
                    tilEmail.setError("Email incorrecto");
                    Toast.makeText(this, "Email incorrecto", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (pass.isEmpty()) {
                tilPassword.setError("Ingresa la contraseña");
                Toast.makeText(this, "Ingresa la contraseña", Toast.LENGTH_SHORT).show();
                return;
            }
            int tipo = switchPhoneLogin.isChecked() ? 2 : 1;
            doLoginApi(tipo, input, pass);
        });
    }

    private void doLoginApi(int tipo, String identificador, String password) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("tipo", tipo);
            body.addProperty("identificador", identificador);
            body.addProperty("password", password);

            Request req = new Request.Builder()
                    .url(BASE_URL + "/api/auth/login")
                    .post(RequestBody.create(gson.toJson(body), JSON))
                    .build();

            http.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "Error de red: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String err = response.body() != null ? response.body().string() : "error";
                        Log.e(TAG, "Login fallo: " + err);
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, "Credenciales inválidas", Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }

                    String json = response.body() != null ? response.body().string() : "{}";
                    LoginResponse lr = gson.fromJson(json, LoginResponse.class);

                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Sesión Correcta", Toast.LENGTH_LONG).show());
                    String userJson = gson.toJson(lr.user);
                    SessionStore.save(LoginActivity.this, lr.token, lr.expiresAtUtc, userJson);

                    Log.d(TAG, "Usuario logueado: " + userJson);

                    // 4) Ir al Home
                    runOnUiThread(() -> {
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    });
                }
            });

        } catch (Exception ex) {
            Toast.makeText(this, "Error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void updateHintBasedOnSwitch(boolean isPhoneMode) {
        tilEmail.setHint(isPhoneMode
                ? "Ingresa tu número de celular de Bolivia"
                : "Email");
        // Opcional: cambiar inputType para teclado numérico cuando es teléfono
        if (isPhoneMode) {
            etEmail.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        } else {
            etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }
    }
    public class BasicUserDto {
        public String nombre;
        public String apellido;
        public String username;
        public String email;
        public String celular;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getCelular() { return celular; }
        public void setCelular(String celular) { this.celular = celular; }
    }

    // LoginResponse.java
    public class LoginResponse {
        public String token;
        public String expiresAtUtc; // ISO-8601, ej: 2025-10-09T23:45:00Z
        public BasicUserDto user;
    }
}

