package com.example.progettotdp;

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

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Post> posts;
    private String loggedInUsername; // Nuovo campo

    public PostAdapter(Context context, ArrayList<Post> posts, String loggedInUsername) {
        this.context = context;
        this.posts = posts;
        this.loggedInUsername = loggedInUsername;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.username.setText(post.getUsername());
        holder.text.setText(post.getDescription());

        // LOCATION
        if (post.getLocation() != null && !post.getLocation().isEmpty()) {
            holder.location.setText("📍 " + post.getLocation());
            holder.location.setVisibility(View.VISIBLE);
            holder.location.setOnClickListener(v -> {
                String mapsUrl = "https://www.google.com/maps/search/?api=1&query=" + Uri.encode(post.getLocation().trim());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl));
                context.startActivity(browserIntent);
            });
        } else {
            holder.location.setVisibility(View.GONE);
        }

        // IMAGE
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.image.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(Uri.parse(post.getImageUrl()))
                    .centerCrop()
                    .into(holder.image);
        } else {
            holder.image.setVisibility(View.GONE);
        }

        // DELETE BUTTON visibile solo al proprietario
        if (post.getUsername().equals(loggedInUsername)) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> confirmDelete(post.getId(), position));
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }

        //EDIT BUTTON visibile solo al proprietario
        if (post.getUsername().equals(loggedInUsername)) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.editButton.setVisibility(View.VISIBLE);

            holder.deleteButton.setOnClickListener(v -> confirmDelete(post.getId(), position));

            holder.editButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, EditPostActivity.class);
                intent.putExtra("post_id", post.getId());
                intent.putExtra("description", post.getDescription());
                intent.putExtra("location", post.getLocation());
                intent.putExtra("image_url", post.getImageUrl());
                intent.putExtra("username", loggedInUsername); // utile per tornare
                context.startActivity(intent);
            });
        } else {
            holder.deleteButton.setVisibility(View.GONE);
            holder.editButton.setVisibility(View.GONE);
        }

    }

    private void confirmDelete(int postId, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Conferma eliminazione")
                .setMessage("Sei sicuro di voler eliminare questo post?")
                .setPositiveButton("Elimina", (dialog, which) -> deletePost(postId, position))
                .setNegativeButton("Annulla", null)
                .show();
    }

    private void deletePost(int postId, int position) {
        String url = "http://10.0.2.2/social-php-backend/api/delete_post.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    posts.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Post eliminato", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(context, "Errore eliminazione: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("post_id", String.valueOf(postId));
                return params;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void updatePosts(ArrayList<Post> newPosts) {
        posts.clear();
        posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, text, location;
        ImageView image;
        Button deleteButton, editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.postUsername);
            text = itemView.findViewById(R.id.postText);
            location = itemView.findViewById(R.id.postLocation);
            image = itemView.findViewById(R.id.postImage);
            deleteButton = itemView.findViewById(R.id.deletePostButton);
            editButton = itemView.findViewById(R.id.editPostButton);
        }
    }
}
