package com.example.progettotdp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Adapter per gestire e mostrare la lista di post in una RecyclerView
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context context; // Context utile per creare view, avviare attività e accedere a risorse
    private ArrayList<Post> posts; // Lista di oggetti Post da visualizzare
    private String loggedInUsername; // Username dell’utente attualmente loggato, per abilitare azioni solo sui propri post
    private boolean isPersonalArea; // Flag che indica se siamo in un’area personale (non usato nel codice attuale ma utile per logica futura)

    // Metodo chiamato quando serve creare una nuova ViewHolder (cioè una nuova riga)
    // Usa il layout XML e crea il ViewHolder associato
    public PostAdapter(Context context, ArrayList<Post> posts, String loggedInUsername, boolean isPersonalArea) {
        this.context = context;
        this.posts = posts;
        this.loggedInUsername = loggedInUsername;
        this.isPersonalArea = isPersonalArea;
    }

    // Metodo chiamato quando serve creare una nuova ViewHolder (cioè una nuova riga)
    // Usa il layout XML e crea il ViewHolder associato
    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Crea il layout personalizzato per ogni singolo post nella lista
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    // Metodo chiamato per "riempire" la ViewHolder con i dati del post corrispondente
    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);

        // Setta username e descrizione nel layout
        holder.username.setText(post.getUsername());
        holder.text.setText(post.getDescription());

        // GESTIONE POSIZIONE
        // Se la posizione esiste e non è vuota, la mostra e abilita click per aprire Google Maps
        if (post.getLocation() != null && !post.getLocation().isEmpty()) {
            holder.location.setText("📍 " + post.getLocation());
            holder.location.setVisibility(View.VISIBLE);
            holder.location.setOnClickListener(v -> {
                // Crea URL per aprire Google Maps con la ricerca della posizione
                String mapsUrl = "https://www.google.com/maps/search/?api=1&query=" + Uri.encode(post.getLocation().trim());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl));
                context.startActivity(browserIntent);
            });
        } else {
            // Nasconde la TextView della posizione se non presente
            holder.location.setVisibility(View.GONE);
            holder.location.setOnClickListener(null);
        }

        // GESTIONE IMMAGINE
        // Se il post ha un’immagine, la carica con Glide e la mostra; altrimenti la nasconde
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.image.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(Uri.parse(post.getImageUrl()))
                    .centerCrop()  // Ritaglia l'immagine per riempire l'area in modo proporzionale
                    .into(holder.image);
        } else {
            holder.image.setVisibility(View.GONE);
        }

        // Controlla se l'utente loggato è il proprietario del post per mostrare i pulsanti di modifica e cancellazione
        boolean isOwner = post.getUsername().equals(loggedInUsername);
        holder.deleteButton.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        holder.editButton.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        if (isOwner) {
            // Se proprietario, abilita il click sul pulsante elimina con conferma
            holder.deleteButton.setOnClickListener(v -> confirmDelete(post.getId(), position));

            // Pulsante modifica apre la EditPostActivity passando i dati attuali del post
            holder.editButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, EditPostActivity.class);
                intent.putExtra("post_id", post.getId());
                intent.putExtra("description", post.getDescription());
                intent.putExtra("location", post.getLocation());
                intent.putExtra("image_url", post.getImageUrl());
                intent.putExtra("username", loggedInUsername);

                // Usa startActivityForResult per ricevere aggiornamenti sulla modifica
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, 1001);
                }
            });
        }
    }

    // Dialogo di conferma per evitare eliminazioni accidentali
    private void confirmDelete(int postId, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Conferma eliminazione")
                .setMessage("Sei sicuro di voler eliminare questo post?")
                .setPositiveButton("Elimina", (dialog, which) -> deletePost(postId, position))
                .setNegativeButton("Annulla", null)
                .show();
    }

    // Effettua chiamata POST all’API per eliminare il post e aggiorna la RecyclerView rimuovendo l’elemento
    private void deletePost(int postId, int position) {
        String url = "http://10.0.2.2/social-php-backend/api/delete_post.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Rimuove il post dalla lista locale e aggiorna la RecyclerView
                    posts.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Post eliminato", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(context, "Errore eliminazione: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                // Parametro richiesto dall’API per identificare il post da cancellare
                Map<String, String> params = new HashMap<>();
                params.put("post_id", String.valueOf(postId));
                return params;
            }
        };

        // Aggiunge la richiesta alla coda di Volley per eseguirla in background
        Volley.newRequestQueue(context).add(request);
    }

    // Restituisce il numero totale di post nella lista (utile per RecyclerView)
    @Override
    public int getItemCount() {
        return posts.size();
    }

    // Metodo per aggiornare la lista dei post con una nuova lista (es. dopo modifica o refresh)
    public void updatePosts(ArrayList<Post> newPosts) {
        posts.clear();
        posts.addAll(newPosts);
        notifyDataSetChanged(); // Notifica la RecyclerView per ridisegnare la lista
    }

    // Classe statica ViewHolder che memorizza i riferimenti ai componenti della singola riga/post
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, text, location; // Campi testuali
        ImageView image;                   // Immagine associata al post
        Button deleteButton, editButton;  // Pulsanti per cancellare o modificare il post

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Trova i componenti nel layout item_post.xml una volta sola per ogni ViewHolder
            username = itemView.findViewById(R.id.postUsername);
            text = itemView.findViewById(R.id.postText);
            location = itemView.findViewById(R.id.postLocation);
            image = itemView.findViewById(R.id.postImage);
            deleteButton = itemView.findViewById(R.id.deletePostButton);
            editButton = itemView.findViewById(R.id.editPostButton);
        }
    }
}
