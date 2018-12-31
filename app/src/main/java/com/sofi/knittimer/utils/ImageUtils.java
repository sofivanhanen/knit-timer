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

    private static final String TAG = ImageUtils.class.toString();

    public static File createImageFile(Context context, String path) throws IOException {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        storageDir.mkdirs();
        File image = new File(storageDir, path);
        return image;
    }

    public static void saveToExternalStorage(Bitmap bitmap, String imagePath, Context context) {
        if (bitmap == null) {
            return;
        }
        bitmap = resizeBitmapForStorage(bitmap);
        FileOutputStream fileOutputStream = null;
        try {
            File myPath = createImageFile(context, imagePath);
            fileOutputStream = new FileOutputStream(myPath);
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 10, fileOutputStream)) throw new Exception("Compression failed");
        } catch (Exception e) {
            Log.e(TAG, "Error saving bitmap to storage");
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException while saving bitmap to storage");
            }
        }
    }

    public static Bitmap loadImageFromStorage(String imagePath, Context context) {
        Bitmap bitmap = null;
        try {
            File file = createImageFile(context, imagePath);
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
        } catch (Exception e) {
            Log.i(TAG, "Error while loading bitmap from storage");
        }
        return bitmap;
    }

    public static Bitmap resizeBitmapForStorage(Bitmap bitmap) {
        return resizeBitmap(bitmap, 1920);
    }

    public static Bitmap resizeBitmapForCache(Bitmap bitmap) {
        return resizeBitmap(bitmap, 192);
    }

    private static Bitmap resizeBitmap(Bitmap bitmap, int maxWidth) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        while (width > maxWidth) {
            width /= 2;
            height /= 2;
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }
}
