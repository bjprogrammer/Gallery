package com.circle.gallery.main;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.circle.gallery.R;
import com.circle.gallery.models.MediaObject;

import java.util.ArrayList;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<MediaObject> mediaObjects;
    private RequestManager requestManager;

    private VideoRecyclerView videoPlayerRecyclerView;

    RecyclerViewAdapter(ArrayList<MediaObject> mediaObjects, RequestManager requestManager) {
        this.mediaObjects = mediaObjects;
        this.requestManager = requestManager;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new VideoViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_video_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ((VideoViewHolder)viewHolder).onBind(mediaObjects.get(i), requestManager, videoPlayerRecyclerView,i);
    }
    
    @Override
    public int getItemCount() {
        return mediaObjects.size();
    }

    void setRecyclerView(VideoRecyclerView videoPlayerRecyclerView){
        this.videoPlayerRecyclerView = videoPlayerRecyclerView;
    }

}














