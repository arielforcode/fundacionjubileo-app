package com.mrx.fundacionjubileo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class HomeActivity extends AppCompatActivity {
    private static final String PREF_SESSION = "session_prefs";
    private static final String PREF_QR = "AutorizacionQr";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MaterialButton btnLogin = findViewById(R.id.btnRegistrar);
        MaterialButton btncodigo = findViewById(R.id.btnCodigo);
        MaterialButton btnlista= findViewById(R.id.btnEstatus);

        btnlista.setOnClickListener(view -> {
            SharedPreferences prefs = this.getSharedPreferences("AutorizacionQr", Context.MODE_PRIVATE);
            String fechaStr = prefs.getString("vigencia", null);
            int pid = prefs.getInt("IdProyecto", -1);
            Toast.makeText(this, "vigencia " + fechaStr + ","+pid, Toast.LENGTH_SHORT).show();
        });

        btncodigo.setOnClickListener(view -> {
            startActivity(new Intent(HomeActivity.this, QrScanActivity.class));
        });

        // Demo: al tocar login, navega a HomeActivity
        btnLogin.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, RegisterActivity.class);
            startActivity(i);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_clear_qr) {
            showConfirmDialog(
                    "¿Estás seguro de limpiar el código QR?",
                    "Esto eliminará la información del QR guardada en el dispositivo.",
                    () -> clearQr()
            );
            return true;
        } else if (id == R.id.action_logout) {
            showConfirmDialog(
                    "¿Deseas cerrar sesión?",
                    "Tu sesión actual se cerrará y deberás iniciar nuevamente.",
                    () -> logout()
            );
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearQr() {
        getSharedPreferences(PREF_QR, MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
        Toast.makeText(this, "Código QR limpiado correctamente", Toast.LENGTH_SHORT).show();
    }

    /** Cierra sesión, borra preferencias y redirige al login */
    private void logout() {
        getSharedPreferences(PREF_SESSION, MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    /** Muestra un diálogo de confirmación reutilizable */
    private void showConfirmDialog(String title, String message, Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Sí", (dialog, which) -> onConfirm.run())
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!SessionStore.isTokenValid(this)) {
            SessionStore.clear(this);
            Toast.makeText(this, "Sesión expirada. Inicia sesión de nuevo.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}