package com.mrx.fundacionjubileo;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class HomeActivity extends AppCompatActivity {

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


        MaterialButton btnLogin = findViewById(R.id.btnRegistrar);
        MaterialButton btncodigo = findViewById(R.id.btnCodigo);

        btncodigo.setOnClickListener(view -> {
            startActivity(new Intent(HomeActivity.this, QrScanActivity.class));
        });

        // Demo: al tocar login, navega a HomeActivity
        btnLogin.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, RegisterActivity.class);
            startActivity(i);
        });
    }
}