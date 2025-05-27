package com.example.progettotdp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {

    // Codice di richiesta per la selezione immagine
    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTextDescription;
    private EditText editTextLocation;
    private ImageView imageViewPreview;
    private Button buttonSelectImage, buttonSubmitPost;
    // Bitmap per immagine selezionata
    private Bitmap bitmap;

    // Username dell’utente, passato dall'activity precedente
    private String username; // Passato da Login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Associa le view dal layout
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextLocation = findViewById(R.id.editTextLocation);
        imageViewPreview = findViewById(R.id.imageViewPreview);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonSubmitPost = findViewById(R.id.buttonSubmitPost);

        // Riceve lo username passato dall'intent (es. da Login o HomePage)
        username = getIntent().getStringExtra("username");

        // Listener per selezionare immagine dalla galleria
        buttonSelectImage.setOnClickListener(v -> openImageChooser());

        // Listener per inviare il post
        buttonSubmitPost.setOnClickListener(v -> {
            uploadPost();  // L'immagine è opzionale
        });
    }

    // Apre il selettore immagini (galleria)
    private void openImageChooser() {
        Intent intent = new Intent();
        / Solo immagini
        intent.setType("image/*");
        // Azione di pick
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleziona Immagine"), PICK_IMAGE_REQUEST);
    }

    // Riceve il risultato della selezione immagine
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Se l'immagine è stata selezionata correttamente
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            // Ottiene URI immagine
            Uri imageUri = data.getData();
            try {
                // Converte URI in bitmap
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                // Mostra anteprima
                imageViewPreview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Invio dei dati del post al backend
    private void uploadPost() {

        // Legge i dati da UI
        String description = editTextDescription.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();

        // Se c'è un'immagine, la converte in base64, altrimenti stringa vuota
        String imageBase64 = (bitmap != null) ? encodeImageToBase64(bitmap) : "";

        // Verifica che lo username sia valido
        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Errore: username non trovato", Toast.LENGTH_SHORT).show();
            return;
        }

        // URL della API per creare un nuovo post
        String url = "http://10.0.2.2/social-php-backend/api/create_post.php";

        // Crea una richiesta POST usando Volley
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Se il post ha avuto successo
                    Toast.makeText(CreatePostActivity.this, "Post creato!", Toast.LENGTH_SHORT).show();

                    // Torna alla HomePage e ricarica i post
                    Intent intent = new Intent(CreatePostActivity.this, HomePage_activity.class);
                    intent.putExtra("username", username);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                },
                error -> Toast.makeText(CreatePostActivity.this, "Errore: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            // Parametri inviati via POST alla API
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("description", description);
                params.put("posizione", location);
                params.put("image_base64", imageBase64); // Immagine codificata in base64
                return params;
            }
        };
        // Aggiunge la richiesta alla coda di Volley
        Volley.newRequestQueue(this).add(stringRequest);
    }

    // Converte un'immagine Bitmap in stringa base64
    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Compressione JPEG
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        // Codifica in base64
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }
}
