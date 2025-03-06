package com.app.mutliple.videos.compression.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.IOException;

/**
 * Created by admin on 12/8/2016.
 */

public class ImageCompression {
    public static float maxHeight = 1280.0f; //1280  1300
    public static float maxWidth = 720.0f;
    private static int orientation = -1, angleToRotate = -1;

    public static int calculateInSampleSize_new(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap compressImage(Context context, String imagePath) {

        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);

        maxHeight = getDisplayHeightPixels(context);
        maxWidth = getDisplayWidthPixels(context);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        float imgRatio = (float) actualWidth / (float) actualHeight;
        float maxRatio = maxWidth / maxHeight;

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }
        options.inSampleSize = calculateInSampleSize_new(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];
        try {
            bmp = BitmapFactory.decodeFile(imagePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }
        try {
            if (actualHeight == 0 || actualWidth == 0) {
                actualHeight = 300;
                actualWidth = 300;
            }
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888); //RGB_565  ARGB_8888
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f; //2.0  2.5
        float middleY = actualHeight / 2.0f; // 2.0
        Bitmap tempBitmap = null;
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        //canvas.drawBitmap(bmp, (float) (middleX - bmp.getWidth() / 2), (float) (middleY - bmp.getHeight() / 2), new Paint(Paint.FILTER_BITMAP_FLAG));
        canvas.drawBitmap(bmp, (float) (middleX - bmp.getWidth() / 2), (float) (middleY - bmp.getHeight() / 2), new Paint(Paint.FILTER_BITMAP_FLAG));
        //canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, null);
        if (bmp != null) {
            bmp.recycle();
        }


        // ----- modified on 14 dec 2017 for gallery img being rotated ----
        ExifInterface exif;
        Matrix matrix = new Matrix();
        int tmpOrientation = 0;
        try {
            exif = new ExifInterface(imagePath);
            if (exif != null) {
                tmpOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                switch (tmpOrientation) {
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.preRotate(270);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.preRotate(90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.preRotate(180);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            if (orientation == 0) {
                matrix.postRotate(angleToRotate);
            } else if (orientation == 1) {
                matrix.postRotate(180);
            } else if (orientation == 2) {
                matrix.postRotate(0);
            } else if (orientation == 3) {
                matrix.postRotate(270);
            }

            angleToRotate = -1;
            orientation = -1;


            tempBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(),
                    scaledBitmap.getHeight(), matrix, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempBitmap;
    }


    public static int getDisplayWidthPixels(Context context) {
        DisplayMetrics dm = new DisplayMetrics();

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getMetrics(dm);

        return dm.widthPixels;
    }

    public static int getDisplayHeightPixels(Context context) {


        DisplayMetrics dm = new DisplayMetrics();

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getMetrics(dm);

        return dm.heightPixels;
    }

}
