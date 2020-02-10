package com.circle.gallery.main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.circle.gallery.R;
import com.circle.gallery.models.MediaObject;
import com.circle.gallery.util.Constants;
import com.circle.gallery.util.Resources;
import com.circle.gallery.util.VerticalSpacingItemDecorator;


import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private VideoRecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.recycler_view);
    }

    private void initRecyclerView(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        VerticalSpacingItemDecorator itemDecorator = new VerticalSpacingItemDecorator(10);
        mRecyclerView.addItemDecoration(itemDecorator);

        ArrayList<MediaObject> mediaObjects = Resources.getPicturePaths(this);
        mRecyclerView.setMediaObjects(mediaObjects);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mediaObjects, initGlide());
        adapter.setRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(adapter);

    }

    private RequestManager initGlide(){
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.white_background)
                .error(R.drawable.white_background);

        return Glide.with(this)
                .setDefaultRequestOptions(options);
    }


    @Override
    protected void onDestroy() {
        if(mRecyclerView!=null)
            mRecyclerView.releasePlayer();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if(mRecyclerView!=null)
            mRecyclerView.setPlayControl(VideoRecyclerView.PlayState.OFF);
        super.onPause();
    }

    @Override
    protected void onRestart() {
        if(mRecyclerView!=null)
            mRecyclerView.setPlayControl(VideoRecyclerView.PlayState.ON);
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE) || !EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, permission, Constants.PERMISSION_REQUEST_CODE);
        }else {
            initRecyclerView();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        initRecyclerView();

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage("Please grant storage permissions from app settings")
                    .setPositiveButton("Ok", (dialog1, which) ->{
                        dialog1.dismiss();

                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        setResult(RESULT_OK);
                        startActivityForResult(intent, Constants.SETTING_REQUEST_CODE);
                    })
                    .setTitle("Storage Permission Required")
                    .setNegativeButton("Cancel", (dialog1, which) -> dialog1.dismiss())
                    .setCancelable(false)
                    .create();
            dialog.show();

        } else {

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage(" Please grant storage permissions from app settings")
                    .setPositiveButton("Ok", (dialog1, which) -> {
                        dialog1.dismiss();
                        String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(MainActivity.this, permission, Constants.PERMISSION_REQUEST_CODE);

                    })
                    .setTitle("Storage Permission Required")
                    .setNegativeButton("Cancel", (dialog1, which) -> dialog1.dismiss())
                    .setCancelable(false)
                    .create();
            dialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.SETTING_REQUEST_CODE || requestCode == Constants.PERMISSION_REQUEST_CODE) {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                initRecyclerView();
            }
        }
    }
}

















