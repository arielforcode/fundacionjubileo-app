package com.mrx.fundacionjubileo;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etNombres, etApPat, etApMat, etCel, etCI;
    private TextInputEditText etFecEmision, etFecVenc, etNacionalidad, etFecNac;
    private TextInputEditText etProfesion, etDomicilio;
    private MaterialAutoCompleteTextView spEmpresa, etDepto,etLugarExp;
    private MaterialButton btnEnviar;
    private MaterialRadioButton rbMod1 ;
    private MaterialRadioButton rbMod2 ;
    private MaterialRadioButton rbMod3 ;
    TextInputLayout tilCuentaBancaria ;
    TextInputLayout tilBancoDestino   ;
    TextInputEditText etCuentaBancaria ;
    TextInputEditText etBancoDestino ;

    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Enums de UI
    private final String[] empresas = {"Viva", "Tigo", "Entel"};
    private final String[] departamentos = {"La Paz", "Cochabamba", "Santa Cruz","Oruro","Potosi","Chiquisaca","Tarija","Beni","Pando","Otro"};// mapea a 1..3 si lo necesitas
    private final String[] expediciones = {"LP", "SCZ", "CBBA","OR","PT","CH","TJA","BE","PD","EX"};

    //configruaciond ela peticionde envio de datos
    private OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

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

        RadioGroup rgModalidad = findViewById(R.id.rgModalidad);
         rbMod1 = findViewById(R.id.rbMod1);
         rbMod2 = findViewById(R.id.rbMod2);
         rbMod3 = findViewById(R.id.rbMod3);

         tilCuentaBancaria = findViewById(R.id.tilCuentaBancaria);
         tilBancoDestino   = findViewById(R.id.tilBancoDestino);
         etCuentaBancaria = findViewById(R.id.etCuentaBancaria);
         etBancoDestino   = findViewById(R.id.etBancoDestino);

        bindViews();
        setupDropdowns();
        setupDatePickers();
        setupEnviar();

        ArrayAdapter<String> empAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, empresas);
        ArrayAdapter<String> dtpAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, departamentos);
        ArrayAdapter<String> lexpAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, expediciones);

        spEmpresa.setAdapter(empAdapter);
        spEmpresa.setOnClickListener(v -> spEmpresa.showDropDown());

        etDepto.setAdapter(dtpAdapter);
        etDepto.setOnClickListener(v -> etDepto.showDropDown());
        etLugarExp.setAdapter(lexpAdapter);
        etLugarExp.setOnClickListener(v -> etLugarExp.showDropDown());
        updateBankFieldsVisibility(rbMod1.isChecked(), tilCuentaBancaria, tilBancoDestino);
        rgModalidad.setOnCheckedChangeListener((group, checkedId) -> {
            boolean showBank = (checkedId == R.id.rbMod1);
            updateBankFieldsVisibility(showBank, tilCuentaBancaria, tilBancoDestino);
            if (!showBank) {
                etCuentaBancaria.setText(null);
                etBancoDestino.setText(null);
                tilCuentaBancaria.setError(null);
                tilBancoDestino.setError(null);
            }
        });

    }

    // Función helper
    private void updateBankFieldsVisibility(boolean show,
                                            TextInputLayout tilCuenta,
                                            TextInputLayout tilBanco) {
        tilCuenta.setVisibility(show ? View.VISIBLE : View.GONE);
        tilBanco.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    private void bindViews() {
        etNombres     = findViewById(R.id.etNombres);
        etApPat       = findViewById(R.id.etApPat);
        etApMat       = findViewById(R.id.etApMat);
        etCel         = findViewById(R.id.etCel);
        spEmpresa     = findViewById(R.id.spEmpresa);
        etCI          = findViewById(R.id.etCI);
        etLugarExp    = findViewById(R.id.etLugarExp);
        etFecEmision  = findViewById(R.id.etFecEmision);
        etFecVenc     = findViewById(R.id.etFecVenc);
        etNacionalidad= findViewById(R.id.etNacionalidad);
        etFecNac      = findViewById(R.id.etFecNac);
        etProfesion   = findViewById(R.id.etProfesion);
        etDomicilio   = findViewById(R.id.etDomicilio);
        etDepto       = findViewById(R.id.etDepto);
        btnEnviar     = findViewById(R.id.btnEnviar);
    }

    private void setupDropdowns() {
        // Empresa telefónica
        ((TextInputLayout) findViewById(R.id.tilCI)).post(() -> {
            spEmpresa.setSimpleItems(empresas);
        });
    }

    private void setupDatePickers() {
        etFecEmision.setOnClickListener(v -> showDatePickerInto(etFecEmision));
        etFecVenc.setOnClickListener(v -> showDatePickerInto(etFecVenc));
        etFecNac.setOnClickListener(v -> showDatePickerInto(etFecNac));
    }

    private void showDatePickerInto(TextInputEditText target) {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar sel = Calendar.getInstance();
                    sel.set(Calendar.YEAR, year);
                    sel.set(Calendar.MONTH, month);
                    sel.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    target.setText(df.format(sel.getTime()));
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        dp.show();
    }

    private void setupEnviar() {
        btnEnviar.setOnClickListener(v -> {
            // Validaciones mínimas (los que marcaste con *)
           if (isEmpty(etNombres)) {
                toast("Nombres es obligatorio");
                return;
            }
            if (isEmpty(etApPat)) {
                toast("Apellido Paterno es obligatorio");
                return;
            }
            if (isEmpty(etCI)) {
                toast("Cédula de Identidad es obligatoria");
                return;
            }
            String lugarexp=etLugarExp.getText().toString().trim();
            if (lugarexp.isEmpty()) {
                toast("Lugar de Expedición es obligatorio");
                return;
            }
            if (etLugarExp.getText().length() > 4) {
                toast("Lugar de Expedición máx. 4 caracteres");
                return;
            }
            if (isEmpty(etFecEmision)) {
                toast("Fecha de emisión CI es obligatoria");
                return;
            }
            if (isEmpty(etFecVenc)) {
                toast("Fecha de vencimiento CI es obligatoria");
                return;
            }
            if (isEmpty(etNacionalidad)) {
                toast("Nacionalidad es obligatoria");
                return;
            }
            if (isEmpty(etFecNac)) {
                toast("Fecha de nacimiento es obligatoria");
                return;
            }
            if (isEmpty(etDomicilio)) {
                toast("El domicilio es obligatorio");
                return;
            }
            String departamentoDomicilio = etDepto.getText().toString().trim();
            if (departamentoDomicilio.isEmpty()) {
                toast("El departamento del domicilio obligatorio");
                return;
            }


            int modalidad; // 1=Depósito, 2=Transferencia, 3=Efectivo
            if (rbMod1.isChecked())      modalidad = 1;
            else if (rbMod2.isChecked()) modalidad = 2;
            else if (rbMod3.isChecked()) modalidad = 3;
            else {
                Toast.makeText(this, "Selecciona una modalidad de pago", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validación condicional para Depósito (mostrar error si vacío)
            if (modalidad == 1) {
                boolean ok = true;
                if (Objects.requireNonNull(etCuentaBancaria.getText()).toString().trim().isEmpty()) {
                    tilCuentaBancaria.setError("Requerido para depósito");
                    ok = false;
                } else tilCuentaBancaria.setError(null);

                if (Objects.requireNonNull(etBancoDestino.getText()).toString().trim().isEmpty()) {
                    tilBancoDestino.setError("Requerido para depósito");
                    ok = false;
                } else tilBancoDestino.setError(null);

                if (!ok) return;
            }


            // Mapear empresa telefónica (opcional: 1..3)


            //todo metodo de registro de personas

            SharedPreferences prefs = getSharedPreferences("AutorizacionQr", MODE_PRIVATE);
            String fechaGuardada = prefs.getString("vigencia", null);

            if (fechaGuardada != null) {
                try {
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    LocalDateTime fechaLocal = LocalDateTime.parse(fechaGuardada, inputFormatter);
                    LocalDateTime ahora = LocalDateTime.now();

                    if (fechaLocal.isAfter(ahora)) {
                        //todo variables obligatorias para el registro

                        String nombre=etNombres.getText().toString().trim();
                        String apellidoPaterno =etApPat.getText().toString().trim();
                        String cedulaIdentidad=etCI.getText().toString().trim();
                        String lugarExpedicion= etLugarExp.getText().toString().trim();
                        String fechaEmision=etFecEmision.getText().toString().trim();
                        String fechaVencimiento=etFecVenc.getText().toString().trim();
                        String nacionalidad=etNacionalidad.getText().toString().trim();
                        String fechaNacimiento =etFecNac.getText().toString().trim();
                        String domicilio=etDomicilio.getText().toString().trim();
                        String depoartamentoDelDomicilio = etDepto.getText().toString().trim();

                        //todo variables opcionales
                        String apellidoMaterno =etApMat.getText().toString().trim();
                        String numeroCelular =etCel.getText().toString().trim();
                        String empTxt = spEmpresa.getText().toString().trim();
                        String empresaTelefonica;

                        if (empTxt.equalsIgnoreCase("Viva")) {
                            empresaTelefonica = "3";
                        } else if (empTxt.equalsIgnoreCase("Tigo")) {
                            empresaTelefonica = "2";
                        } else if (empTxt.equalsIgnoreCase("Entel")) {
                            empresaTelefonica = "1";
                        } else {
                            empresaTelefonica = "1";
                        }
                        String profesion =etProfesion.getText().toString().trim();

                        //todo variables condicionales
                        String modalidadValor;

                        if (rbMod1.isChecked()) {
                            modalidadValor = "1";
                        } else if (rbMod2.isChecked()) {
                            modalidadValor = "2";
                        } else if (rbMod3.isChecked()) {
                            modalidadValor = "3";
                        } else {
                            Toast.makeText(this, "Selecciona una modalidad de pago", Toast.LENGTH_SHORT).show();
                            return;
                        }


                        String cuentaBancaria = "";
                        String bancoDestino = "";

                        if (modalidadValor.equals("1")) {
                            boolean ok = true;

                            cuentaBancaria = etCuentaBancaria.getText() != null ? etCuentaBancaria.getText().toString().trim() : "";
                            bancoDestino = etBancoDestino.getText() != null ? etBancoDestino.getText().toString().trim() : "";

                            if (cuentaBancaria.isEmpty()) {
                                tilCuentaBancaria.setError("Requerido para depósito");
                                ok = false;
                            } else tilCuentaBancaria.setError(null);

                            if (bancoDestino.isEmpty()) {
                                tilBancoDestino.setError("Requerido para depósito");
                                ok = false;
                            } else tilBancoDestino.setError(null);

                            if (!ok) return;
                        }

                        int idProyecto =prefs.getInt("IdProyecto",-1);


                        //todo peticion a la api para registrar los datos
                        JSONObject json = new JSONObject();
                        try {
                            json.put("nombres", nombre);
                            json.put("apellidoPaterno", apellidoPaterno);
                            json.put("apellidoMaterno", apellidoMaterno);
                            json.put("cedulaIdentidad", cedulaIdentidad);
                            json.put("lugarExpedicion", lugarExpedicion);
                            json.put("fechaEmisionCi", fechaServer(fechaEmision));
                            json.put("fechaVencimientoCi", fechaServer(fechaVencimiento));
                            json.put("nacionalidad", nacionalidad);
                            json.put("fechaNacimiento", fechaServer(fechaNacimiento));
                            json.put("modalidadPago", Integer.parseInt(modalidadValor));
                            json.put("idProyecto", idProyecto);
                            json.put("numeroCelular", numeroCelular);
                            json.put("empresaTelefonica", Integer.parseInt(empresaTelefonica));
                            json.put("profesion", profesion);
                            json.put("domicilio", domicilio);
                            json.put("departamentoCi", depoartamentoDelDomicilio);
                            json.put("cuentaBancaria", cuentaBancaria);
                            json.put("bancoDestino", bancoDestino);
                            json.put("urlFotoFrontalCi", "");
                            json.put("urlFotoReversaCi", "");
                            json.put("urlFirmaCi", "");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return;
                        }

                        SharedPreferences prefs1 = getSharedPreferences("session_prefs", MODE_PRIVATE);
                        String token = prefs1.getString("jwt_token", null);
                        RequestBody body = RequestBody.create(json.toString(), JSON);
                        Request request = new Request.Builder()
                                .url("https://fundacionjubileotest-debfhkc6ezg0c8h2.brazilsouth-01.azurewebsites.net/api/RegistroPersona/crear") // Cambia "tu-endpoint"
                                .addHeader("Authorization", "Bearer " + token)
                                .post(body)
                                .build();
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                                runOnUiThread(() ->
                                        Toast.makeText(RegisterActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show()
                                );
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                final String respStr = response.body() != null ? response.body().string() : "";
                                runOnUiThread(() ->{
                                    Log.d("Respuesta Server","mensaje"+ respStr);
                                    Toast.makeText(RegisterActivity.this, "Usuario RegistradoCorrectamente", Toast.LENGTH_LONG).show();
                                   }
                                );
                            }
                        });

                    } else {
                        Toast.makeText(this, "Renueva Tu codigo de autorizacion ya venció", Toast.LENGTH_SHORT).show();
                    }

                } catch (DateTimeParseException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Formato de fecha inválido", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No hay fecha guardada", Toast.LENGTH_SHORT).show();
            }
            //todo final del metodo

        });
    }
    private boolean isEmpty(TextInputEditText et) {
        return et.getText() == null || et.getText().toString().trim().isEmpty();
    }

    private String val(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private String fechaServer(String fecha){
        String fechaFormateada = java.time.LocalDate.parse(fecha, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                .atStartOfDay(java.time.ZoneOffset.UTC)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));

        return fechaFormateada;
    }
}