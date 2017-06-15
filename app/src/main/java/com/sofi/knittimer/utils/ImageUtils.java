package com.sofi.knittimer.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public final class ImageUtils {

    private ImageUtils() {

    }

    public static void saveToInternalStorage(Bitmap bitmap, int projectId, Context context) {

        Log.i("saveToInternalStorage", "here we are");

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int maxWidth = 2048;

        Log.i("saveToInternalStorage", width + " " + height);

        while (width >= maxWidth) {
            width /= 2;
            height /= 2;
        }

        Log.i("saveToInternalStorage", width + " " + height);

        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
        File directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE);
        File myPath = new File(directory, projectId + ".jpg");
        Log.i("saveToInternalStorage", myPath.getAbsolutePath());
        FileOutputStream fileOutputStream = null;

        Log.i("saveToInternalStorage", "before if");
        try {
            fileOutputStream = new FileOutputStream(myPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap loadImageFromStorage(int projectId, Context context) {
        ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
        File directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE);
        Bitmap bitmap = null;
        try {
            File file = new File(directory, projectId + ".jpg");
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
