package com.example.progettotdp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Login_activity extends AppCompatActivity {

    EditText usernameEditText, passwordEditText;
    Button loginButton;

    String URL_LOGIN = "http://10.0.2.2/social-php-backend/api/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        usernameEditText = findViewById(R.id.Username);
        passwordEditText = findViewById(R.id.Password);
        loginButton = findViewById(R.id.LoginButton);

        loginButton.setOnClickListener(v -> doLogin());
    }

    void doLogin() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        new Thread(() -> {
            try {
                URL url = new URL(URL_LOGIN);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String json = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
                OutputStream os = conn.getOutputStream();
                os.write(json.getBytes());
                os.close();

                Scanner scanner = new Scanner(conn.getInputStream());
                String response = scanner.hasNext() ? scanner.nextLine() : "";
                scanner.close();

                runOnUiThread(() -> {
                    if (response.contains("OK")) {
                        Toast.makeText(this, "Login riuscito!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Login fallito", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->{
                    e.printStackTrace(); // stampa completa in Logcat
                    Toast.makeText(this, "Errore: " + e, Toast.LENGTH_LONG).show();
                    }
                );
            }
        }).start();
    }
}
