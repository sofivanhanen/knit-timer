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

    public static void saveToInternalStorage(Bitmap bitmap, String imagePath, Context context) {
        bitmap = resizeBitmap(bitmap);

        ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
        File directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE);
        File myPath = new File(directory, imagePath + ".jpg");
        FileOutputStream fileOutputStream = null;

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

    public static Bitmap loadImageFromStorage(String imagePath, Context context) {
        ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
        File directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE);
        Bitmap bitmap = null;
        try {
            File file = new File(directory, imagePath + ".jpg");
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap resizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int maxWidth = 1080;

        while (width > maxWidth) {
            width /= 2;
            height /= 2;
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }
}
