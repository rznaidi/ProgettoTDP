package com.example.progettotdp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class Register_activity extends AppCompatActivity {

    EditText emailEditText, usernameEditText, passwordEditText;
    Button registerButton;

    String URL_REGISTER = "http://10.0.2.2/social-php-backend/api/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        emailEditText = findViewById(R.id.Email);
        usernameEditText = findViewById(R.id.Username);
        passwordEditText = findViewById(R.id.Password);
        registerButton = findViewById(R.id.RegisterButton);

        registerButton.setOnClickListener(v -> doRegister());
    }

    void doRegister() {
        String email = emailEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Compila tutti i campi!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(URL_REGISTER);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("email", email);
                jsonRequest.put("username", username);
                jsonRequest.put("password", password);

                OutputStream os = conn.getOutputStream();
                os.write(jsonRequest.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                Scanner scanner = new Scanner(conn.getInputStream());
                String response = scanner.hasNext() ? scanner.nextLine() : "";
                scanner.close();

                JSONObject jsonResponse = new JSONObject(response);
                String result = jsonResponse.optString("result");

                runOnUiThread(() -> {
                    if ("OK".equalsIgnoreCase(result)) {
                        Toast.makeText(this, "Registrazione avvenuta con successo!", Toast.LENGTH_SHORT).show();
                        finish(); // Torna alla schermata di login
                    } else {
                        String message = jsonResponse.optString("message", "Errore");
                        Toast.makeText(this, "Registrazione fallita: " + message, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                Log.e("RegisterError", "Errore durante la registrazione", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Errore: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    // Metodo opzionale per tornare alla schermata di login da un TextView
    public void openLoginPage(View view) {
        Intent intent = new Intent(this, Login_activity.class);
        startActivity(intent);
        finish();
    }
}
