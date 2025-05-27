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

    RecyclerView recyclerView; // Vista per lista scorrevole di post
    PostAdapter adapter; // Adapter per collegare dati alla RecyclerView
    ArrayList<Post> postList = new ArrayList<>(); // Lista di post da mostrare
    String URL_GET_POSTS = "http://10.0.2.2/social-php-backend/api/get_posts.php"; // URL per API dei post

    
    private String username; // Username dell'utente loggato, passato da attività precedente

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page_activity);

        // Riceve l'username dall'intent che ha avviato questa activity
        username = getIntent().getStringExtra("username");

        // Trova la RecyclerView nel layout e imposta il layout manager (lista verticale)
        recyclerView = findViewById(R.id.recycler_view_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Crea l'adapter con i dati attuali, username dell'utente loggato e flag area personale
        adapter = new PostAdapter(this, postList, username, false);
        recyclerView.setAdapter(adapter);

        // Configura il bottone logout: ritorna alla schermata di login pulendo la back stack
        Button logoutButton = findViewById(R.id.LogoutButton);
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage_activity.this, Login_activity.class);
            // FLAG per iniziare nuova task e rimuovere tutte le altre activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Bottone per creare un nuovo post, passa username all'attività CreatePostActivity
        Button createPostButton = findViewById(R.id.CreatePostButton);
        createPostButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage_activity.this, CreatePostActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        // Bottone per aprire l'area personale, passando username
        Button personalAreaButton = findViewById(R.id.PersonalAreaButton);
        personalAreaButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AreaPersonaleActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        // Carica i post dal backend appena l'attività viene creata
        loadPosts();
    }

    // Metodo che esegue la richiesta HTTP in un thread separato per ottenere i post
    void loadPosts() {
        new Thread(() -> {
            try {
                URL url = new URL(URL_GET_POSTS);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // Legge la risposta dal server
                InputStream inputStream = conn.getInputStream();
                Scanner scanner = new Scanner(inputStream);
                StringBuilder responseBuilder = new StringBuilder();

                // Costruisce la stringa JSON dalla risposta
                while (scanner.hasNextLine()) {
                    responseBuilder.append(scanner.nextLine());
                }
                scanner.close();

                String response = responseBuilder.toString();

                // Parsing della risposta JSON
                JSONArray jsonArray = new JSONArray(response);
                ArrayList<Post> newPosts = new ArrayList<>();

                // Itera tutti i post e li converte in oggetti Post
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    Post post = new Post(
                            obj.getInt("id"),
                            obj.getString("username"),
                            obj.optString("description", ""),// Usa stringa vuota se assente
                            obj.optString("location", ""),
                            obj.optString("image_url", "")
                    );
                    newPosts.add(post);
                }

                // Aggiorna la UI nel thread principale con i nuovi dati
                runOnUiThread(() -> adapter.updatePosts(newPosts));

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Errore nel caricamento post: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    // Gestisce il risultato ritornato dall'attività EditPostActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Controlla che sia il risultato dell'editing del post
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            boolean updated = data.getBooleanExtra("updated", false);
            if (updated) {
                loadPosts(); //Ricarica i post aggiornati
                // Se il post è stato modificato, ricarica la lista dei post
            }
        }
    }
}
