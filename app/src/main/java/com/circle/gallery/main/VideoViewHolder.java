package com.circle.gallery.main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.circle.gallery.R;
import com.circle.gallery.models.MediaObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


class VideoViewHolder extends RecyclerView.ViewHolder {
    private FrameLayout media_container, image_container,placeholder_container;
    private TextView title, imageTitle;

    ImageView volumeControl,thumbnail, image, placeholder, playControl,backward, forward;
    ProgressBar progressBar;
    RequestManager requestManager;

    private View parent;
    private Bitmap bitmap;
    SeekBar seekBar;

    VideoViewHolder(@NonNull View itemView) {
        super(itemView);

        parent = itemView;
        media_container = itemView.findViewById(R.id.media_container);
        image_container = itemView.findViewById(R.id.image_container);
        placeholder_container = itemView.findViewById(R.id.placeholder_container);

        thumbnail = itemView.findViewById(R.id.thumbnail);
        title = itemView.findViewById(R.id.title);
        imageTitle = itemView.findViewById(R.id.image_title);

        progressBar = itemView.findViewById(R.id.progressBar);
        image = itemView.findViewById(R.id.image);
        placeholder = itemView.findViewById(R.id.placeholder);

        volumeControl = itemView.findViewById(R.id.volume_control);
        playControl = itemView.findViewById(R.id.play_control);

        forward = itemView.findViewById(R.id.play_forward);
        backward = itemView.findViewById(R.id.play_backward);

        seekBar = itemView.findViewById(R.id.seekbar);
    }

    void onBind(MediaObject mediaObject, RequestManager requestManager, final VideoRecyclerView videoPlayerRecyclerView, final int position) {
        this.requestManager = requestManager;
        parent.setTag(this);

        if(mediaObject.getMediaType() != MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE){
            media_container.setVisibility(View.VISIBLE);
            image_container.setVisibility(View.GONE);
            placeholder_container.setVisibility(View.GONE);

            title.setText(mediaObject.getTitle());

            requestManager.load(mediaObject.getFilePath()).optionalCenterCrop().thumbnail(0.1f).into(thumbnail);

            videoPlayerRecyclerView.setVolumeControl(VideoRecyclerView.VolumeState.ON);

            playControl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    videoPlayerRecyclerView.togglePlay(position);
                }
            });

            volumeControl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    videoPlayerRecyclerView.toggleVolume(position);
                }
            });

            forward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    videoPlayerRecyclerView.fastForward(position);
                }
            });

            backward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    videoPlayerRecyclerView.fastBackward(position);
                }
            });
        }else if(mediaObject.getMimeType().equals("placeholder")) {
            media_container.setVisibility(View.GONE);
            placeholder_container.setVisibility(View.VISIBLE);
            image_container.setVisibility(View.GONE);

            //                requestManager.load(R.drawable.placeholder)
//                        .thumbnail(0.1f).into(image);

                            Observable.create(new ObservableOnSubscribe() {
                                @Override
                                public void subscribe(ObservableEmitter emitter) throws Exception {
                                            try {
                                                emitter.onNext(downloadImage("https://as1.ftcdn.net/jpg/03/09/61/40/500_F_309614005_gMwoxfcwjmVY1u2ndZeu3RWMlFEkPe2v.jpg"));
                                            }catch (Exception e){
                                                emitter.onError(e);
                                            }
                                        }
                                    })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Observer<Bitmap>() {
                                        @Override
                                        public void onSubscribe(Disposable d) { }

                                        @Override
                                        public void onNext(Bitmap response) {
                                            bitmap = response;
                                            placeholder.setScaleType(ImageView.ScaleType.FIT_XY);
                                            placeholder.setImageBitmap(bitmap);
                                        }

                                        @Override
                                        public void onError(Throwable e) { }

                                        @Override
                                        public void onComplete() { }
                                    });


        }else if(mediaObject.getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE){
            media_container.setVisibility(View.GONE);
            image_container.setVisibility(View.VISIBLE);
            placeholder_container.setVisibility(View.GONE);

            imageTitle.setText(mediaObject.getTitle());
            requestManager.load(mediaObject.getFilePath()).optionalCenterCrop().thumbnail(0.1f).into(image);
        }
    }

    private Bitmap downloadImage(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = null;
            input = connection.getInputStream();

            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}














