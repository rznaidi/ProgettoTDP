package com.example.progettotdp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class HomePage_activity extends AppCompatActivity {

    RecyclerView recyclerView;
    PostAdapter adapter;
    ArrayList<Post> postList = new ArrayList<>();
    String URL_GET_POSTS = "http://10.0.2.2/social-php-backend/api/get_posts.php";

    private String username; // Utente loggato

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page_activity);

        // Ricevi username dall'intent
        username = getIntent().getStringExtra("username");

        recyclerView = findViewById(R.id.recycler_view_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PostAdapter(this, postList, username); // Passa username all'adapter
        recyclerView.setAdapter(adapter);

        Button logoutButton = findViewById(R.id.LogoutButton);
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage_activity.this, Login_activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        Button createPostButton = findViewById(R.id.CreatePostButton);
        createPostButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage_activity.this, CreatePostActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        loadPosts();
    }

    void loadPosts() {
        new Thread(() -> {
            try {
                URL url = new URL(URL_GET_POSTS);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                InputStream inputStream = conn.getInputStream();
                Scanner scanner = new Scanner(inputStream);
                StringBuilder responseBuilder = new StringBuilder();
                while (scanner.hasNextLine()) {
                    responseBuilder.append(scanner.nextLine());
                }
                scanner.close();

                String response = responseBuilder.toString();
                Log.d("POST_RESPONSE", response);

                JSONArray jsonArray = new JSONArray(response);
                ArrayList<Post> newPosts = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    Post post = new Post(
                            obj.getInt("id"),                             // ← Assicurati che `id` sia incluso nella risposta JSON
                            obj.getString("username"),
                            obj.optString("description", ""),
                            obj.optString("location", ""),
                            obj.optString("image_url", "")
                    );
                    newPosts.add(post);
                }

                runOnUiThread(() -> adapter.updatePosts(newPosts));

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Errore nel caricamento post: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    // Riceve il risultato dalla modifica post
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            boolean updated = data.getBooleanExtra("updated", false);
            if (updated) {
                loadPosts(); // 🔁 Ricarica i post aggiornati
            }
        }
    }
}
