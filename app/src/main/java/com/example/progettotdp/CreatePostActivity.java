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

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTextDescription;
    private EditText editTextLocation;
    private ImageView imageViewPreview;
    private Button buttonSelectImage, buttonSubmitPost;
    private Bitmap bitmap;

    private String username; // Passato da Login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        editTextDescription = findViewById(R.id.editTextDescription);
        editTextLocation = findViewById(R.id.editTextLocation);
        imageViewPreview = findViewById(R.id.imageViewPreview);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonSubmitPost = findViewById(R.id.buttonSubmitPost);

        username = getIntent().getStringExtra("username");

        buttonSelectImage.setOnClickListener(v -> openImageChooser());

        buttonSubmitPost.setOnClickListener(v -> {
            uploadPost();  // immagine ora è opzionale
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleziona Immagine"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageViewPreview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadPost() {
        String description = editTextDescription.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String imageBase64 = (bitmap != null) ? encodeImageToBase64(bitmap) : "";

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Errore: username non trovato", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.2.2/social-php-backend/api/create_post.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(CreatePostActivity.this, "Post creato!", Toast.LENGTH_SHORT).show();
                    // Torna alla HomePage e aggiorna
                    Intent intent = new Intent(CreatePostActivity.this, HomePage_activity.class);
                    intent.putExtra("username", username);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                },
                error -> Toast.makeText(CreatePostActivity.this, "Errore: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("description", description);
                params.put("posizione", location);
                params.put("image_base64", imageBase64);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }
}
