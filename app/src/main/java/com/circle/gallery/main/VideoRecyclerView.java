package com.circle.gallery.main;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.circle.gallery.R;
import com.circle.gallery.models.MediaObject;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

public class VideoRecyclerView extends RecyclerView {

    private ImageView thumbnail;
    private ProgressBar progressBar;
    private View viewHolderParent;
    private FrameLayout frameLayout;
    private PlayerView videoSurfaceView;
    public SimpleExoPlayer videoPlayer;

    RequestManager requestManager;
    ImageView volumeControl, playControl, forward, backward;

    private ArrayList<MediaObject> mediaObjects = new ArrayList<>();
    private int videoSurfaceDefaultHeight = 0;
    private int screenDefaultHeight = 0;
    private Context context;
    private int playPosition = -1;
    private boolean isVideoViewAdded;

    private Boolean endOfList =false;
    private int targetPosition;

    public enum VolumeState {ON, OFF};
    public enum PlayState {ON, OFF};
    private VolumeState volumeState;
    private PlayState playState;

    private Handler handler;
    private int itemAtLastDifference = 0;
    private SeekBar seekBar;

    public VideoRecyclerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VideoRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context){
        this.context = context.getApplicationContext();
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final Point point = new Point();
        display.getSize(point);
        videoSurfaceDefaultHeight = point.x;
        screenDefaultHeight = point.y;

        videoSurfaceView = new PlayerView(this.context);
        videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        videoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        videoSurfaceView.setUseController(false);
        videoSurfaceView.setPlayer(videoPlayer);

        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if(thumbnail != null){ // show the old thumbnail
                        thumbnail.setVisibility(VISIBLE);
                    }

                    if(!recyclerView.canScrollVertically(1)){
                        endOfList = true;
                    }else {
                        endOfList =false;
                    }

                    setTargetPosition(endOfList);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                if (viewHolderParent != null && viewHolderParent.equals(view)) {
                    resetVideoView();
                }
            }
        });

        videoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) { }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) { }

            @Override
            public void onLoadingChanged(boolean isLoading) { }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        if (progressBar != null) {
                            progressBar.setVisibility(VISIBLE);
                        }
                        break;

                    case Player.STATE_ENDED:
                        if (progressBar != null) {
                            progressBar.setVisibility(GONE);
                        }
                        videoPlayer.seekTo(0);
                        videoPlayer.setPlayWhenReady(false);
                        videoPlayer.getPlaybackState();

                        break;

                    case Player.STATE_IDLE:
                        if (progressBar != null) {
                            progressBar.setVisibility(GONE);
                        }
                        break;

                    case Player.STATE_READY:
                        if (progressBar != null) {
                            progressBar.setVisibility(GONE);
                        }
                        if(!isVideoViewAdded){
                            addVideoView();
                        }
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) { }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) { }

            @Override
            public void onPlayerError(ExoPlaybackException error) { }

            @Override
            public void onPositionDiscontinuity(int reason) { }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) { }

            @Override
            public void onSeekProcessed() { }
        });
    }

    public void setTargetPosition(boolean isEndOfList) {
        if(!isEndOfList){
            int startPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
            int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();

            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1;
            }

            if (startPosition < 0 || endPosition < 0) {
                return;
            }

            if (startPosition != endPosition) {

                targetPosition = endPosition;
            }
            else {
                targetPosition = startPosition;
            }

            setPlayControl(PlayState.OFF);
                if(thumbnail!=null) {
                    thumbnail.setVisibility(VISIBLE);
                }
                if(seekBar!=null){
                    seekBar.setVisibility(GONE);
                }

            playVideo();

        }
        else{
            int startPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
            int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();

            if (startPosition<=targetPosition){
                return;
            }

            if (startPosition < 0 || endPosition < 0) {
                return;
            }

            if (startPosition != endPosition) {
                targetPosition = startPosition+1;
            }
            else {
                targetPosition = startPosition;
            }

            if (endPosition - targetPosition > 1) {
                itemAtLastDifference = endPosition - targetPosition;
            }

            if(mediaObjects.get(targetPosition).getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                setPlayControl(PlayState.OFF);
                    if(thumbnail!=null) {
                        thumbnail.setVisibility(VISIBLE);
                }
                if(seekBar!=null){
                    seekBar.setVisibility(GONE);
                }
                playVideo();

            }else {
                setPlayControl(PlayState.OFF);
                if(thumbnail!=null) {
                    thumbnail.setVisibility(VISIBLE);
                }
                if(seekBar!=null){
                    seekBar.setVisibility(GONE);
                }
                while (itemAtLastDifference>0){
                    itemAtLastDifference = itemAtLastDifference - 1;
                    targetPosition = targetPosition + 1;
                    if(mediaObjects.get(targetPosition).getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                        playVideo();
                        break;
                    }
                }

                playVideo();
            }
        }
    }


    private void playVideo(){
            if (targetPosition == playPosition) {
                return;
            }

            playPosition = targetPosition;
            if (videoSurfaceView == null) {
                return;
            }

            videoSurfaceView.setVisibility(INVISIBLE);
            removeVideoView(videoSurfaceView);

            int currentPosition = targetPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();

            View child = getChildAt(currentPosition);
            if (child == null) {
                return;
            }

            final VideoViewHolder holder = (VideoViewHolder) child.getTag();
            if (holder == null) {
                playPosition = -1;
                return;
            }

        if(mediaObjects.get(targetPosition).getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            thumbnail = holder.thumbnail;
            progressBar = holder.progressBar;

            volumeControl = holder.volumeControl;
            playControl = holder.playControl;
            forward = holder.forward;
            backward = holder.backward;

            viewHolderParent = holder.itemView;
            frameLayout = holder.itemView.findViewById(R.id.media_container);

            requestManager = holder.requestManager;
            videoSurfaceView.setPlayer(videoPlayer);
            seekBar = holder.seekBar;

            handler = new Handler();

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "RecyclerView VideoPlayer"));
            String mediaUrl = mediaObjects.get(targetPosition).getFilePath();
            if (mediaUrl != null) {
                MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(mediaUrl));
                videoPlayer.prepare(videoSource);
                videoPlayer.setPlayWhenReady(true);
                setPlayControl(PlayState.ON);

                seekBar.bringToFront();
                seekBar.setAlpha(1.0f);
                seekBar.setVisibility(VISIBLE);
                seekBar.setProgress(0);
                seekBar.setMax((int)videoPlayer.getDuration()/1000);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        seekBar.bringToFront();
                        seekBar.setMax((int)videoPlayer.getDuration()/1000);
                        seekBar.setProgress((int)videoPlayer.getCurrentPosition()/1000);
                        handler.postDelayed(this,1000);
                    }
                }, 1000);

            }

            viewHolderParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animatePlayControl();
                    animateVolumeControl();
                }
            });
        }
    }

    private void removeVideoView(PlayerView videoView) {

        ViewGroup parent = (ViewGroup) videoView.getParent();
        if (parent == null) {
            return;
        }

        int index = parent.indexOfChild(videoView);
        if (index >= 0) {
            parent.removeViewAt(index);
            isVideoViewAdded = false;

            viewHolderParent.setOnClickListener(null);
        }
    }

    private void addVideoView(){
        frameLayout.addView(videoSurfaceView);
        isVideoViewAdded = true;
        videoSurfaceView.requestFocus();
        videoSurfaceView.setVisibility(VISIBLE);
        videoSurfaceView.setAlpha(1);
        thumbnail.setVisibility(GONE);
    }

    private void resetVideoView(){
        if(isVideoViewAdded){
                removeVideoView(videoSurfaceView);
                playPosition = -1;
                videoSurfaceView.setVisibility(INVISIBLE);
                thumbnail.setVisibility(VISIBLE);
                seekBar.setVisibility(GONE);
        }
    }

    public void releasePlayer() {
        if (videoPlayer != null) {
            videoPlayer.release();
            videoPlayer = null;
        }

        viewHolderParent = null;
    }

    private void pausePlayer(){
        if (videoPlayer != null) {
            videoPlayer.setPlayWhenReady(false);
            videoPlayer.getPlaybackState();
        }
    }

    private void startPlayer(){
        if(videoPlayer !=null) {
            videoPlayer.setPlayWhenReady(true);
            videoPlayer.getPlaybackState();
        }
    }

    public void setMediaObjects(ArrayList<MediaObject> mediaObjects){
        this.mediaObjects = mediaObjects;
    }

    public void toggleVolume(int position) {
        if (videoPlayer != null) {
            if(targetPosition==position){
                if (volumeState == VideoRecyclerView.VolumeState.OFF) {
                    setVolumeControl(VolumeState.ON);
                } else if (volumeState == VideoRecyclerView.VolumeState.ON) {
                    setVolumeControl(VideoRecyclerView.VolumeState.OFF);
                }
            }
        }
    }

    public void fastForward(int position) {
        if (videoPlayer != null) {
            if(targetPosition == position){
                if(playState == PlayState.ON){
                    if(videoPlayer.getDuration()> videoPlayer.getContentPosition() + 10000) {
                        videoPlayer.seekTo(videoPlayer.getCurrentWindowIndex(), videoPlayer.getCurrentPosition() + 10000);
                        animatePlayControl();
                    }
                }
            }
        }
    }

    public void fastBackward(int position) {
        if (videoPlayer != null) {
            if(targetPosition == position){
                    if(playState == PlayState.ON){
                        if(videoPlayer.getContentPosition() - 10000 > 0) {
                            videoPlayer.seekTo(videoPlayer.getCurrentWindowIndex(), videoPlayer.getCurrentPosition() - 10000);
                            animatePlayControl();
                        }
                    }
            }
        }
    }


    public void setVolumeControl(VolumeState state){
        volumeState = state;
        if(state == VolumeState.OFF){
            videoPlayer.setVolume(0f);
            animateVolumeControl();
        }
        else if(state == VolumeState.ON){
            videoPlayer.setVolume(1f);
            animateVolumeControl();
        }
    }

    private void animateVolumeControl(){
        if(volumeControl != null){
            volumeControl.bringToFront();

            if(volumeState == VolumeState.OFF){
                requestManager.load(R.drawable.ic_volume_off_grey_24dp).into(volumeControl);
                volumeControl.animate().cancel();
                volumeControl.setAlpha(1f);
            }
            else if(volumeState == VolumeState.ON){
                requestManager.load(R.drawable.ic_volume_up_grey_24dp).into(volumeControl);
                volumeControl.animate().cancel();
                volumeControl.setAlpha(1f);
                volumeControl.animate().alpha(0f).setDuration(600).setStartDelay(1000);
            }
        }
    }

    public void togglePlay(int position) {

        if(targetPosition!=position){
            setPlayControl(PlayState.OFF);
            targetPosition =position;
                if(thumbnail!=null) {
                    thumbnail.setVisibility(VISIBLE);
                }
                if(seekBar!=null){
                    seekBar.setVisibility(GONE);
                }
            playVideo();
        }else {
            if (videoPlayer != null) {
                if (playState == PlayState.OFF) {
                    setPlayControl(PlayState.ON);
                } else if (playState == PlayState.ON) {
                    setPlayControl(PlayState.OFF);
                }
            }
        }
    }

    public void setPlayControl(PlayState state){
        playState = state;
        if(state == PlayState.OFF){
            pausePlayer();
            animatePlayControl();
        }
        else if(state == PlayState.ON){
            startPlayer();
            animatePlayControl();
        }
    }

    private void animatePlayControl(){
        if(playControl != null){
            playControl.bringToFront();

            forward.bringToFront();
            backward.bringToFront();
            if(playState == PlayState.OFF){
                requestManager.load(R.drawable.exo_controls_play).into(playControl);
                playControl.animate().cancel();
                playControl.setAlpha(1f);
            }
            else if(playState == PlayState.ON){
                requestManager.load(R.drawable.exo_controls_pause).into(playControl);
                playControl.animate().cancel();
                playControl.setAlpha(1f);
                playControl.animate().alpha(0f).setDuration(600).setStartDelay(1000);

                forward.animate().cancel();
                forward.setAlpha(1f);
                forward.animate().alpha(0f).setDuration(600).setStartDelay(1000);

                backward.animate().cancel();
                backward.setAlpha(1f);
                backward.animate().alpha(0f).setDuration(600).setStartDelay(1000);
            }
        }
    }
}



























