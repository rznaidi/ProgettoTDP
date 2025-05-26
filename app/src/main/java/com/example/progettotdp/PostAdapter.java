package com.example.progettotdp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Post> posts;

    public PostAdapter(Context context, ArrayList<Post> posts) {
        this.context = context;
        this.posts = posts;
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

        // LOCATION HANDLING
        if (post.getLocation() != null && !post.getLocation().isEmpty()) {
            holder.location.setText("📍 " + post.getLocation());
            holder.location.setVisibility(View.VISIBLE);

            holder.location.setOnClickListener(v -> {
                // Encode the location to be URL-safe
                String encodedLocation = Uri.encode(post.getLocation().trim());

                // Create Google Maps URL for browser
                String mapsUrl = "https://www.google.com/maps/search/?api=1&query=" + encodedLocation;

                // Open in browser (not in Maps app)
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl));
                context.startActivity(browserIntent);
            });
        } else {
            holder.location.setVisibility(View.GONE);
            holder.location.setOnClickListener(null);
        }

        // IMAGE HANDLING
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.image.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(Uri.parse(post.getImageUrl()))
                    .centerCrop()
                    .into(holder.image);
        } else {
            holder.image.setVisibility(View.GONE);
        }
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.postUsername);
            text = itemView.findViewById(R.id.postText);
            location = itemView.findViewById(R.id.postLocation);
            image = itemView.findViewById(R.id.postImage);
        }
    }
}