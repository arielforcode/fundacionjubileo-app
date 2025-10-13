package com.mrx.fundacionjubileo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadDocuments extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 101;

    private ImageView imgFrontal, imgReversa, imgFirma;
    private Button btnFrontal, btnReversa, btnFirma;
    private MaterialButton btnEnviar;

    private File fileFrontal, fileReversa, fileFirma;
    private String currentTag = "";

    private static final String BASE_URL = "https://fundacionjubileotest-debfhkc6ezg0c8h2.brazilsouth-01.azurewebsites.net/api/upload/imagenes";
    private OkHttpClient http = new OkHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_documents);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imgFrontal = findViewById(R.id.imgFrontal);
        imgReversa = findViewById(R.id.imgReversa);
        imgFirma = findViewById(R.id.imgFirma);
        btnFrontal = findViewById(R.id.btnFrontal);
        btnReversa = findViewById(R.id.btnReversa);
        btnFirma = findViewById(R.id.btnFirma);
        btnEnviar = findViewById(R.id.btnEnviar);
        Intent intent = getIntent();

        // Recuperar los valores enviados
        int idProyecto = intent.getIntExtra("idProyecto", -1);
        int idUser = intent.getIntExtra("idUser", -1);
        int dni = intent.getIntExtra("dni", -1);

        // Verificamos si llegaron correctamente
        if (idProyecto != -1 && idUser != -1 && dni !=-1) {
            Toast.makeText(this, "Proyecto: " + idProyecto + " - Usuario: " + idUser, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No se recibieron los IDs correctamente", Toast.LENGTH_SHORT).show();
        }

        btnFrontal.setOnClickListener(v -> openCamera("frontal"));
        btnReversa.setOnClickListener(v -> openCamera("reversa"));
        btnFirma.setOnClickListener(v -> openCamera("firma"));
        btnEnviar.setOnClickListener(v -> uploadImages(idProyecto,idUser,dni));
    }
    private void openCamera(String tag) {
        currentTag = tag;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile(tag);
            Uri photoUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    private File createImageFile(String tag) {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String fileName = tag + "_" + System.currentTimeMillis() + ".jpg";
        File file = new File(dir, fileName);
        switch (tag) {
            case "frontal": fileFrontal = file; break;
            case "reversa": fileReversa = file; break;
            case "firma": fileFirma = file; break;
        }
        return file;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(getImageFile(currentTag).getAbsolutePath());
            switch (currentTag) {
                case "frontal": imgFrontal.setImageBitmap(bitmap); break;
                case "reversa": imgReversa.setImageBitmap(bitmap); break;
                case "firma": imgFirma.setImageBitmap(bitmap); break;
            }
        }
    }

    private File getImageFile(String tag) {
        switch (tag) {
            case "frontal": return fileFrontal;
            case "reversa": return fileReversa;
            case "firma": return fileFirma;
        }
        return null;
    }

    private void uploadImages(int idProyecto, int idUser,int dni) {
        if (fileFrontal == null || fileReversa == null || fileFirma == null) {
            Toast.makeText(this, "Debe tomar las 3 fotos", Toast.LENGTH_SHORT).show();
            return;
        }

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("IdRegistro", String.valueOf(idUser)); // el ID del registro real
        builder.addFormDataPart("IdProyecto", String.valueOf(idProyecto));
        builder.addFormDataPart("CedulaIdentidad", String.valueOf(dni));

        builder.addFormDataPart("FotoFrontal", fileFrontal.getName(),
                RequestBody.create(fileFrontal, MediaType.parse("image/jpeg")));
        builder.addFormDataPart("FotoReversa", fileReversa.getName(),
                RequestBody.create(fileReversa, MediaType.parse("image/jpeg")));
        builder.addFormDataPart("Firma", fileFirma.getName(),
                RequestBody.create(fileFirma, MediaType.parse("image/jpeg")));

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(BASE_URL)
                .header("Authorization", "Bearer " + SessionStore.getToken(this))
                .post(requestBody)
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Log.d("ariel",e.toString());
                    Toast.makeText(UploadDocuments.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                runOnUiThread(() -> {
                    Log.d("ariel",res.toString());
                    Toast.makeText(UploadDocuments.this,
                            response.isSuccessful() ? "Enviado correctamente" : "Error: " + res,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}