package com.wefly.wealert.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageProcessing {

    /**
     * Conversion ByteArray to Drawable
     */
    public static Drawable byteArrayToDrawable(Context context, byte[] bytes) {
        return bitmapToDrawable(context, byteArrayToBitmap(bytes));
    }

    /**
     * Conversion Drawable to ByteArray
     */
    public static byte[] drawableToByteArray(Drawable drawable) {
        return bitmapToByteArray(drawableToBitmap(drawable));
    }

    /**
     * Conversion Bitmap to ByteArray (Pour du format PNG, idem pour JPG, etc.)
     */
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Conversion ByteArray to Bitmap
     */
    public static Bitmap byteArrayToBitmap(byte[] bytes) {
        InputStream is = new ByteArrayInputStream(bytes);
        return BitmapFactory.decodeStream(is);
    }

    /**
     * Conversion Drawable to Bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Conversion Bitmap to Drawable
     */
    public static Drawable bitmapToDrawable(Context context,Bitmap bitmap) {
        return new BitmapDrawable(context.getResources(), bitmap);
    }

}
