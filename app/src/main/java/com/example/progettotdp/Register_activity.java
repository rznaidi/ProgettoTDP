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

    // URL dell'API di registrazione (PHP backend locale per emulatore Android)
    String URL_REGISTER = "http://10.0.2.2/social-php-backend/api/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        emailEditText = findViewById(R.id.Email);
        usernameEditText = findViewById(R.id.Username);
        passwordEditText = findViewById(R.id.Password);
        registerButton = findViewById(R.id.RegisterButton);

        // Imposta listener per il bottone di registrazione
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
        // Creazione di un nuovo thread per eseguire la chiamata di rete
        new Thread(() -> {
            try {
                // Inizializzazione della connessione HTTP
                URL url = new URL(URL_REGISTER);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                // Abilita output per invio dati
                conn.setDoOutput(true);

                // Creazione dell’oggetto JSON da inviare al server
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("email", email);
                jsonRequest.put("username", username);
                jsonRequest.put("password", password);

                // Scrive l’oggetto JSON nel corpo della richiesta
                OutputStream os = conn.getOutputStream();
                os.write(jsonRequest.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                // Legge la risposta del server
                Scanner scanner = new Scanner(conn.getInputStream());
                String response = scanner.hasNext() ? scanner.nextLine() : "";
                scanner.close();

                // Parsing della risposta JSON
                JSONObject jsonResponse = new JSONObject(response);
                String result = jsonResponse.optString("result");

                // Esegue operazioni sull’interfaccia utente nel thread principale
                runOnUiThread(() -> {
                    if ("OK".equalsIgnoreCase(result)) {
                        Toast.makeText(this, "Registrazione avvenuta con successo!", Toast.LENGTH_SHORT).show();
                        // Chiude l’activity e torna alla schermata precedente (login)
                        finish();
                    } else {
                        String message = jsonResponse.optString("message", "Errore");
                        Toast.makeText(this, "Registrazione fallita: " + message, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                // In caso di errore (es. connessione assente)
                Log.e("RegisterError", "Errore durante la registrazione", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Errore: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    // Metodo opzionale per tornare alla schermata di login da un bottone o un link
    public void openLoginPage(View view) {
        Intent intent = new Intent(this, Login_activity.class);
        startActivity(intent);
        // Chiude la schermata di registrazione
        finish();
    }
}
