package com.mrx.fundacionjubileo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;

public class PerfilActivity extends AppCompatActivity {

    private LoginActivity.BasicUserDto currentUser;

    private ShapeableImageView imgAvatar;
    private TextView tvNombre, tvUsername, tvEmail, tvCelular, tvUsuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // === Recuperar datos del usuario guardados en SharedPreferences ===
        imgAvatar  = findViewById(R.id.imgAvatar);
        tvNombre   = findViewById(R.id.tvNombre);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail    = findViewById(R.id.tvEmail);
        tvCelular  = findViewById(R.id.tvCelular);
        tvUsuario  = findViewById(R.id.tvUsuario);

        // Cargar usuario de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("session_prefs", MODE_PRIVATE);
        String userJson = prefs.getString("jwt_user_json", null);

        if (userJson != null) {
            LoginActivity.BasicUserDto user = new Gson().fromJson(userJson, LoginActivity.BasicUserDto.class);

            // Poblar UI
            String nombreCompleto = (safe(user.getNombre()) + " " + safe(user.getApellido())).trim();
            tvNombre.setText(nombreCompleto.isEmpty() ? "Usuario" : nombreCompleto);
            tvUsername.setText("@" + safe(user.getUsername()));
            tvEmail.setText(safe(user.getEmail()));
            tvCelular.setText(safe(user.getCelular()));
            tvUsuario.setText(safe(user.getUsername()));

            // Si tuvieras URL de foto, aquí podrías cargarla con Glide/Picasso.
            // Glide.with(this).load(url).placeholder(R.drawable.ic_avatar_default).into(imgAvatar);

            Log.d("PerfilActivity", "Usuario cargado: " + userJson);
        } else {
            // fallback si no hay sesión
            tvNombre.setText("Usuario");
            tvUsername.setText("@usuario");
            tvEmail.setText("-");
            tvCelular.setText("-");
            tvUsuario.setText("-");
        }
    }
    private String safe(String s) {
        return s == null ? "" : s;
    }
}