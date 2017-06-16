package com.sofi.knittimer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class ImageUtils {

    private ImageUtils() {

    }

    public static File createImageFile(Context context, String path) throws IOException {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        storageDir.mkdirs();
        Log.i("!!! storageDir", storageDir.getAbsolutePath());
        File image = new File(storageDir, path);
        Log.i("!!! imageDir", image.getAbsolutePath());
        return image;
    }

    public static void saveToExternalStorage(Bitmap bitmap, String imagePath, Context context) {
        if (bitmap == null) {
            Log.w("!!!saveToExternalStorag", "Bitmap was null!");
        } else {
            Log.i("!!! save", "Bitmap not null");
        }
        bitmap = resizeBitmap(bitmap);
        FileOutputStream fileOutputStream = null;
        try {
            File myPath = createImageFile(context, imagePath);
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
        Bitmap bitmap = null;
        try {
            File file = createImageFile(context, imagePath);
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
        } catch (Exception e) {
            Log.w("!!!loadImageFromStorage", "Exception!! " + e.getMessage());
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap resizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int maxWidth = 1920;

        while (width > maxWidth) {
            width /= 2;
            height /= 2;
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }
}
