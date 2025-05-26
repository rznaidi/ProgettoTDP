package com.example.progettotdp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page_activity);

        recyclerView = findViewById(R.id.recycler_view_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PostAdapter(this, postList);
        recyclerView.setAdapter(adapter);

        Button logoutButton = findViewById(R.id.LogoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Vai alla schermata di login (LoginActivity)
                Intent intent = new Intent(HomePage_activity.this, Login_activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Cancella lo stack attività
                startActivity(intent);
                finish(); // Chiudi questa activity
            }
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
                String response = scanner.hasNext() ? scanner.nextLine() : "";
                scanner.close();

                Log.d("POST_RESPONSE", response);

                JSONArray jsonArray = new JSONArray(response);
                ArrayList<Post> newPosts = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    Post post = new Post(
                            obj.getString("username"),
                            obj.optString("description", ""),
                            obj.optString("location", ""),
                            obj.optString("image_url", "")
                    );
                    newPosts.add(post);
                }

                runOnUiThread(() -> adapter.updatePosts(newPosts));


            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Errore nel caricamento post: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
