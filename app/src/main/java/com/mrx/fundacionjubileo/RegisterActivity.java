package com.mrx.fundacionjubileo;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialAutoCompleteTextView spnEmpresa = findViewById(R.id.spnEmpresa);
        ArrayAdapter<CharSequence> empresaAdapter = ArrayAdapter.createFromResource(
                this, R.array.empresas, android.R.layout.simple_list_item_1
        );
        spnEmpresa.setAdapter(empresaAdapter);
        spnEmpresa.setOnClickListener(v -> spnEmpresa.showDropDown());

        // LUGAR DE EXPEDICIÓN
        MaterialAutoCompleteTextView spnLugar = findViewById(R.id.spnLugarExpedicion);
        ArrayAdapter<CharSequence> lugarAdapter = ArrayAdapter.createFromResource(
                this, R.array.departamentos, android.R.layout.simple_list_item_1
        );
        spnLugar.setAdapter(lugarAdapter);
        spnLugar.setOnClickListener(v -> spnLugar.showDropDown());
    }
}