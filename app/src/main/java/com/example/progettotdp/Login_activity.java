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

    // Riferimenti agli elementi della UI
    EditText usernameEditText, passwordEditText, emailEditText;
    Button loginButton;

    // URL del backend per la richiesta di login
    String URL_LOGIN = "http://10.0.2.2/social-php-backend/api/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Imposta il layout dell'attività
        setContentView(R.layout.login_activity);

        // Collegamento degli elementi della UI ai rispettivi ID nel layout
        usernameEditText = findViewById(R.id.Username);
        passwordEditText = findViewById(R.id.Password);
        emailEditText = findViewById(R.id.Email);
        loginButton = findViewById(R.id.LoginButton);

        // Imposta il listener per il click del bottone di login
        loginButton.setOnClickListener(v -> doLogin());
    }

    void doLogin() {
        // Recupera il contenuto dei campi di input e rimuove spazi inutili
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        // Verifica che tutti i campi siano compilati
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Tutti i campi devono essere compilati!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Creazione di un nuovo thread per evitare NetworkOnMainThreadException
        new Thread(() -> {
            try {
                // Crea la connessione HTTP
                URL url = new URL(URL_LOGIN);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                // Abilita l'invio di dati
                conn.setDoOutput(true);

                // Crea l'oggetto JSON con le credenziali dell'utente
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username);
                jsonObject.put("password", password);
                jsonObject.put("email", email);

                // Scrive il corpo della richiesta JSON
                OutputStream os = conn.getOutputStream();
                os.write(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                // Legge la risposta del server
                Scanner scanner = new Scanner(conn.getInputStream());
                String response = scanner.hasNext() ? scanner.nextLine() : "";
                scanner.close();

                // Parse della risposta JSON
                JSONObject jsonResponse = new JSONObject(response);
                String result = jsonResponse.optString("result");
                String message = jsonResponse.optString("message");

                // Aggiorna la UI sul thread principale
                runOnUiThread(() -> {
                    // Login riuscito, passa alla HomePage_activity
                    if ("OK".equals(result)) {
                        Intent intent = new Intent(this, HomePage_activity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                        // Chiude l'attività attuale
                        finish();
                    } else {
                        Toast.makeText(this, "Login fallito: " + message, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                // In caso di errore di rete o eccezione, mostra un messaggio d’errore
                runOnUiThread(() -> {
                    e.printStackTrace();
                    Toast.makeText(this, "Errore di rete: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    // Metodo per aprire l'attività di registrazione (chiamato da un pulsante nel layout)
    public void openRegistrationPage(View view) {
        // Naviga alla pagina di registrazione
        Intent intent = new Intent(this, Register_activity.class);
        startActivity(intent);
    }
}