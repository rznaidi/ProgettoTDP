package com.example.progettotdp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;

public class EditPostActivity extends AppCompatActivity {

    EditText editDescription, editLocation;
    ImageView imagePreview;
    Button updateButton;

    int postId;
    String imageUrl, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        // Associa viste
        editDescription = findViewById(R.id.editDescription);
        editLocation = findViewById(R.id.editLocation);
        imagePreview = findViewById(R.id.imagePreview);
        updateButton = findViewById(R.id.updateButton);

        // Ottiene i dati passati dall'intent
        postId = getIntent().getIntExtra("post_id", -1);
        String desc = getIntent().getStringExtra("description");
        String loc = getIntent().getStringExtra("location");
        imageUrl = getIntent().getStringExtra("image_url");
        username = getIntent().getStringExtra("username");

        // Pre-compila i campi con i dati esistenti del post
        editDescription.setText(desc != null ? desc : "");
        editLocation.setText(loc != null ? loc : "");

        // Carica l'immagine esistente del post se presente
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(imagePreview);
        }
        // Imposta l’azione del pulsante di aggiornamento
        updateButton.setOnClickListener(v -> updatePost());
    }

    //Metodo che invia la richiesta per aggiornare il post al backend PHP.
    private void updatePost() {
        String newDesc = editDescription.getText().toString().trim();
        String newLoc = editLocation.getText().toString().trim();

        // Controlla che il postId sia valido
        if (postId == -1) {
            Toast.makeText(this, "ID post non valido.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.2.2/social-php-backend/api/update_post.php";

        // Crea una richiesta POST con Volley
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Successo: notifica utente e torna indietro con esito positivo
                    Toast.makeText(this, "Post aggiornato!", Toast.LENGTH_SHORT).show();

                    // Torna alla HomePage con segnale di aggiornamento
                    Intent resultIntent = new Intent();
                    // Segnale alla chiamante (es. per refresh)
                    resultIntent.putExtra("updated", true);
                    setResult(RESULT_OK, resultIntent);
                    // Chiude l’activity
                    finish();
                },
                error -> Toast.makeText(this, "Errore aggiornamento: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            // Parametri della richiesta POST da inviare al server
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("post_id", String.valueOf(postId));
                params.put("description", newDesc);
                params.put("location", newLoc);
                params.put("username", username);
                return params;
            }

        };
        // Aggiunge la richiesta alla coda Volley
        Volley.newRequestQueue(this).add(request);
    }
}
