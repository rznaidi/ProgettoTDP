package com.example.progettotdp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Login_activity extends AppCompatActivity {

    EditText usernameEditText, passwordEditText, emailEditText;
    Button loginButton;

    String URL_LOGIN = "http://10.0.2.2/social-php-backend/api/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        usernameEditText = findViewById(R.id.Username);
        passwordEditText = findViewById(R.id.Password);
        emailEditText = findViewById(R.id.Email);
        loginButton = findViewById(R.id.LoginButton);

        loginButton.setOnClickListener(v -> doLogin());
    }

    void doLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Tutti i campi devono essere compilati!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(URL_LOGIN);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username);
                jsonObject.put("password", password);
                jsonObject.put("email", email);

                OutputStream os = conn.getOutputStream();
                os.write(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                Scanner scanner = new Scanner(conn.getInputStream());
                String response = scanner.hasNext() ? scanner.nextLine() : "";
                scanner.close();

                JSONObject jsonResponse = new JSONObject(response);
                String result = jsonResponse.optString("result");
                String message = jsonResponse.optString("message");

                runOnUiThread(() -> {
                    if ("OK".equals(result)) {
                        Toast.makeText(this, "Login riuscito!", Toast.LENGTH_SHORT).show();
                        // Navigazione futura o salvataggio token/sessione
                    } else {
                        Toast.makeText(this, "Login fallito: " + message, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    e.printStackTrace();
                    Toast.makeText(this, "Errore di rete: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    public void openRegistrationPage(View view) {
        Intent intent = new Intent(this, Register_activity.class);
        startActivity(intent);
    }
}
