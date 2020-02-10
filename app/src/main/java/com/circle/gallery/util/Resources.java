package com.circle.gallery.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;


import androidx.loader.content.CursorLoader;

import com.circle.gallery.models.MediaObject;

import java.util.ArrayList;

public class Resources {
    public static ArrayList<MediaObject> getPicturePaths(Context context){
        ArrayList<MediaObject> mediaObjects = new ArrayList<>();
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.TITLE
        };


        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "  + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

        Uri queryUri = MediaStore.Files.getContentUri("external");

        CursorLoader cursorLoader = new CursorLoader(
                context,
                queryUri,
                projection,
                selection,
                null,
                MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC" // Sort order.
        );

        Cursor cursor = cursorLoader.loadInBackground();


        int count = 0;
        try {
            if (cursor != null) {
                cursor.moveToFirst();
            }
            do{
                count ++;
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE));
                String dateModified = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED));
                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE));
                int mediaType = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE));
                String dataPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));


                MediaObject mediaObject = new MediaObject(title, dateModified, dataPath, mediaType,mimeType );
                mediaObjects.add(mediaObject);

                if(count%4 ==0) {
                    mediaObjects.add(new MediaObject("Placeholder", "", "https://drive.google.com/file/d/1Mh9SlPCXpSdt1SoBZN2hzlFpi2WRUJ95/view?usp=sharing", MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, "placeholder"));
                }
            }while(cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mediaObjects;
    }
}
