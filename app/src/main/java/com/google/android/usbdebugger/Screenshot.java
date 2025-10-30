package com.google.android.usbdebugger;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.PixelCopy;
import android.view.View;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Handler;

public class Screenshot {
    private static final String TAG = "Screenshot";

    // Для полного: MediaProjection (запрос в MainActivity)
    private static MediaProjection mediaProjection;
    private static ImageReader imageReader;
    private static VirtualDisplay virtualDisplay;

    @TargetApi(Build.VERSION_CODES.O)
    public static void initMediaProjection(Activity activity, MediaProjection mp) {
        mediaProjection = mp;
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        int density = metrics.densityDpi;

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        virtualDisplay = mediaProjection.createVirtualDisplay("Screenshot",
                width, height, density, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(), null, null);
        Log.d(TAG, "MediaProjection initialized");
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static Bitmap takeScreenshot(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null");
            return null;
        }

        // Get metrics from DisplayManager (works in Service)
        DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display display = displayManager.getDisplay(Display.DEFAULT_DISPLAY);
        if (display == null) {
            Log.e(TAG, "Default display not found");
            return null;
        }

        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        View rootView = ((Activity) context).getWindow().getDecorView();  // Если context — Activity; иначе fallback
        if (rootView == null) {
            Log.e(TAG, "Root view not found — use initMediaProjection for full screen");
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        PixelCopy.request(rootView, bitmap, result -> {
            if (result == PixelCopy.SUCCESS) {
                Log.d(TAG, "Screenshot taken successfully");
            } else {
                Log.e(TAG, "Screenshot failed with result: " + result);
            }
        }, new Handler(Looper.getMainLooper()));

        return bitmap;
    }

    public static void savePic(Bitmap b, String strFileName) {
        if (b == null) {
            Log.e(TAG, "Bitmap is null");
            return;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(strFileName);
            b.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
            Log.d(TAG, "Screenshot saved to " + strFileName);
        } catch (IOException e) {
            Log.e(TAG, "Save error: " + e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {}
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void cleanup() {
        if (virtualDisplay != null) virtualDisplay.release();
        if (imageReader != null) imageReader.close();
        if (mediaProjection != null) mediaProjection.stop();
        Log.d(TAG, "MediaProjection cleaned up");
    }
}