package com.example.progettotdp;

import static android.content.Intent.getIntent;

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

        editDescription = findViewById(R.id.editDescription);
        editLocation = findViewById(R.id.editLocation);
        imagePreview = findViewById(R.id.imagePreview);
        updateButton = findViewById(R.id.updateButton);

        // Ricevi dati
        postId = getIntent().getIntExtra("post_id", -1);
        String desc = getIntent().getStringExtra("description");
        String loc = getIntent().getStringExtra("location");
        imageUrl = getIntent().getStringExtra("image_url");
        username = getIntent().getStringExtra("username");

        // Popola
        editDescription.setText(desc);
        editLocation.setText(loc);
        Glide.with(this).load(imageUrl).into(imagePreview);

        updateButton.setOnClickListener(v -> updatePost());
    }

    private void updatePost() {
        String newDesc = editDescription.getText().toString().trim();
        String newLoc = editLocation.getText().toString().trim();

        String url = "http://10.0.2.2/social-php-backend/api/update_post.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Post aggiornato!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, HomePage_activity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();
                },
                error -> Toast.makeText(this, "Errore aggiornamento: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("post_id", String.valueOf(postId));
                params.put("description", newDesc);
                params.put("location", newLoc);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
