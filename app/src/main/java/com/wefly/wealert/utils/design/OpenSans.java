package com.wefly.wealert.utils.design;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.wefly.wealert.R;

public class OpenSans {
    private static OpenSans instance;
    private static Typeface typeface;
    public static OpenSans getInstance(Context context) {
        synchronized (OpenSans.class) {
            if (instance == null) {
                instance = new OpenSans();
                typeface = Typeface.createFromAsset(context.getResources().getAssets(), "fonts/Montserrat-Bold.ttf");
            }
            return instance;
        }
    }
    public Typeface getTypeFace() {
        return typeface;
    }
}