package com.wefly.wealert.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.wefly.wealert.models.Piece;
import com.wefly.wealert.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by paulodichone on 2/28/15.
 */
public class Utils {
    private final static String TAG = "Utils";

    public static String formatNumber( double value) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        String formatted = formatter.format(value);

        return formatted;
    }

    public static String formatDouble( double value) {
        DecimalFormat precision = new DecimalFormat("#,###.000");
        String dFormatted = precision.format(value);

        return dFormatted.replace(".", ",");
    }

    public static String formatDoubleToString( double value) {
        DecimalFormat precision = new DecimalFormat("0.0000");
        String dFormatted = precision.format(value);

        return dFormatted;
    }




    public static String convertDate(String dateInMilliseconds) {
        String dFormat = "dd/MM/yyyy hh:mm:ss";
        return DateFormat.format(dFormat, Long.parseLong(dateInMilliseconds)).toString();
    }

    public static String convertDate(@NonNull Calendar calendar) {
        String dFormat = "dd/MM/yyyy hh:mm:ss";
        return DateFormat.format(dFormat, calendar).toString();
    }

    public static String convertDate(long dateInMilliseconds) {
        String dFormat = "dd/MM/yyyy hh:mm:ss";
        return DateFormat.format(dFormat, dateInMilliseconds).toString();
    }


    public static @NonNull String getCurrentDate() {
        String dFormat = "dd/MM/yyyy hh:mm:ss";
        long timeInMillis = System.currentTimeMillis();
        return DateFormat.format(dFormat, timeInMillis).toString();
    }


    public static @NonNull Calendar getDateFromString(@NonNull String date){
        Calendar c = Calendar.getInstance();

//        String Date = "Tue Apr 25 18:06:45 GMT+05:30 2017";
//        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
//        try {
//            Date mDate = sdf.parse(Date);
//            long timeInMilliseconds = mDate.getTime();
//
//
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
        //public static final String DATE_TIME_FORMAT = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            try {
                Date mDate = sdf.parse(date);
                c.setTimeInMillis(mDate.getTime()) ;

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return c;
    }


    public static @NonNull Date getDateFromStringCopy(@NonNull String date){
        Date mDate = null;

//        String Date = "Tue Apr 25 18:06:45 GMT+05:30 2017";
//        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
//        try {
//            Date mDate = sdf.parse(Date);
//            long timeInMilliseconds = mDate.getTime();
//
//
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
        //public static final String DATE_TIME_FORMAT = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            try {
                 mDate = sdf.parse(date);

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return mDate;
    }

    public static @NonNull Bitmap resizeMapIcons(@DrawableRes int id, int width, int height, @NonNull  Context ctx){
        Bitmap imageBitmap = BitmapFactory.decodeResource(ctx.getResources(), id);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    public static @NonNull Bitmap resizeMapBitmap(@NonNull Bitmap bmp, int width, int height, @NonNull  Context ctx){
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bmp, width, height, false);
        return resizedBitmap;
    }

    public static @NonNull Bitmap drawableToBitmap( @NonNull  Context ctx, @DrawableRes int id){
        Bitmap imageBitmap = BitmapFactory.decodeResource(ctx.getResources(), id);
        return imageBitmap;
    }

    public void animActivityClose(@NonNull Activity act){
        act.overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }
    public void animActivityOpen(@NonNull Activity act){
        act.overridePendingTransition(R.anim.open_next, R.anim.close_next);
    }


    public static Bitmap getBitmapFromView(@NonNull View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    public static Bitmap getViewBitmap(@NonNull View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            Log.v(Constants.APP_NAME, "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }

    public static void showToast( @NonNull final  Context ctx, @StringRes int restId , @NonNull View view) {
        try {
            Snackbar snackbar = Snackbar
                    .make(view, restId, Snackbar.LENGTH_SHORT);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimary));
            snackbar.show();
        }catch (Exception e){
            e.printStackTrace();
            Log.v(Constants.APP_NAME,TAG + " CAN'T show Toast");
        }

    }

    public static void waitThreeSec(){
        try {
            Thread.currentThread().wait(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    // Activity Animation
    public void onFinishWithAnimation(@NonNull Activity act) {
        act.finish();
        act.overridePendingTransition(R.anim.right_to_left_1, R.anim.right_to_left_2);
    }

    public void onOpenWithAnimation(@NonNull Activity act) {
        act.overridePendingTransition(R.anim.left_to_right_2, R.anim.left_to_right_1);
    }

    // MARK: - SCALE BITMAP TO MAX SIZE -------------------------------------------------
    public static Bitmap scaleBitmapToMaxSize(int maxSize, Bitmap bm) {
        int outWidth;
        int outHeight;
        int inWidth = bm.getWidth();
        int inHeight = bm.getHeight();
        if(inWidth > inHeight){
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, outWidth, outHeight, false);
        return resizedBitmap;
    }



    public static Bitmap getResizedBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth / (float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        float pivotX = 0;
        float pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

        Canvas canvas = new Canvas(resizedBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return resizedBitmap;
    }

    public static boolean isConnectedToInternet(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL("http://google.com");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(500);          // 5 centi
                urlc.connect();
                if (urlc.getResponseCode() == 200 || urlc.getResponseCode() == 301) {        // 200 = "OK" && 301 "Google: you again what you want"
                    Log.wtf("Connection", "Success !");
                    return true;
                } else {
                    return false;
                }
            } catch (MalformedURLException e1) {
                return false;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean isAppRunning(final Context context, final String packageName) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null)
        {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }


    public Bitmap getCircularBitmap(final Bitmap bitmap) {
        Bitmap output;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        float r = 0;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static Bitmap drawableToBitmap (final Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static void smartCombine(List<Piece> first, List<Piece> second) {
        for(Piece num : second) {      // iterate through the second list
            if(first.contains(num)) {   // if first list doesn't contain current element
                first.remove(num);          // add it to the first list
            }
        }
    }

}
