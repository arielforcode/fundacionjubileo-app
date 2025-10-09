package com.mrx.fundacionjubileo;

import android.content.Intent;
import android.os.Bundle;
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

public class LoginActivity extends AppCompatActivity {

    private MaterialSwitch switchPhoneLogin;
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;           // input donde el user escribe
    private TextInputLayout tilPassword;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;

    // Credenciales de prueba (hardcodeadas)
    private static final String DUMMY_PHONE = "61153463";
    private static final String DUMMY_EMAIL = "admin@demo.com";
    private static final String DUMMY_PASS  = "admin123";
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




        btnLogin.setOnClickListener(v -> {
            // Limpiar errores
            tilEmail.setError(null);
            tilPassword.setError(null);

            String input = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String pass  = etPassword.getText() != null ? etPassword.getText().toString() : "";

            // Validaciones básicas según modo
            if (switchPhoneLogin.isChecked()) {
                // TELEFONO: solo dígitos, exactamente 8, empieza con 6 o 7
                boolean onlyDigits = input.matches("^[0-9]{8}$");
                boolean startsOk   = input.startsWith("6") || input.startsWith("7");

                if (!(onlyDigits && startsOk)) {
                    tilEmail.setError("Número de celular inválido");
                    Toast.makeText(this, "Número de celular inválido", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                boolean hasAt = input.contains("@");
                boolean patternOk = Patterns.EMAIL_ADDRESS.matcher(input).matches();

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
            boolean authOk = false;
            if (switchPhoneLogin.isChecked()) {
                if (input.equals(DUMMY_PHONE) && pass.equals(DUMMY_PASS)) {
                    authOk = true;
                }
            } else {
                // comparar con email de prueba
                if (input.equalsIgnoreCase(DUMMY_EMAIL) && pass.equals(DUMMY_PASS)) {
                    authOk = true;
                }
            }

            if (authOk) {
                getSharedPreferences("session_prefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("isLoggedIn", true)
                        .apply();

                // Login exitoso (aquí invocarías tu siguiente pantalla / API)
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
            } else {
                // Credenciales incorrectas
                //tilPassword.setError("Usuario o contraseña incorrectos");
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
            }
        });
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
}