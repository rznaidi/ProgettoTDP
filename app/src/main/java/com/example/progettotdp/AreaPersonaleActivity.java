package com.example.progettotdp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Request;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AreaPersonaleActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private ArrayList<Post> posts = new ArrayList<>();
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_personale);

        // Ottiene lo username dell'utente passato dalla login o schermata precedente
        username = getIntent().getStringExtra("username");

        // Inizializza la RecyclerView per mostrare i post
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inizializza l'adapter per la lista post.
        adapter = new PostAdapter(this, posts, username, true);
        recyclerView.setAdapter(adapter);

        // Carica i post dell'utente dal backend
        loadUserPosts();
    }

    //Recupera i post dell'utente dal backend tramite richiesta POST.
    private void loadUserPosts() {
        String url = "http://10.0.2.2/social-php-backend/api/get_user_posts.php";

        // Richiesta Volley al backend
        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            // Pulisce lista per evitare duplicati
                            posts.clear();

                            JSONArray arr = json.getJSONArray("posts");

                            // Itera su ogni post ricevuto e lo aggiunge all'elenco
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject o = arr.getJSONObject(i);
                                posts.add(new Post(
                                        o.getInt("id"),
                                        username,
                                        o.optString("contenuto", ""),
                                        o.optString("posizione", ""),
                                        o.optString("immagine_url", "")
                                ));

                            }
                            // Notifica aggiornamento UI
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Errore parsing JSON", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Errore di rete: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                // Parametri POST da inviare: solo lo username
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                return params;
            }
        };
        // Invia la richiesta alla coda Volley
        Volley.newRequestQueue(this).add(req);
    }

    //Metodo chiamato quando si torna da un'altra activity (es. modifica post).
    //Se il post è stato aggiornato o eliminato, ricarica i dati.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Codice 1001: ritorno da EditPostActivity o simili
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // Ricarica i post aggiornati
            loadUserPosts(); // ricarica lista
        }
    }
}
