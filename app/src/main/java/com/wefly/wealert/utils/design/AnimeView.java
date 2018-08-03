package com.wefly.wealert.utils.design;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.View;

/**
 * Created by admin on 11/06/2018.
 */

public final class AnimeView {

    private View v;
    private OnAnimationEndCallBack listener;

    public AnimeView(@NonNull final View v, @NonNull OnAnimationEndCallBack listener){
        this.v = v;
        this.listener = listener;
    }

    public void startAnimation(){
        if (v != null){
            ViewCompat.animate(v)
                    .setDuration(200)
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setInterpolator(new CycleInterpolator())
                    .setListener(new ViewPropertyAnimatorListener() {
                        @Override
                        public void onAnimationStart(final View view) {

                        }

                        @Override
                        public void onAnimationEnd(final View view) {
                            notifyOnAnimationEndCallBack(v);
                        }

                        @Override
                        public void onAnimationCancel(final View view) {

                        }
                    })
                    .withLayer()
                    .start();
        }
    }

    public interface OnAnimationEndCallBack {
        void onEnd(@NonNull final View view);
    }


    private class CycleInterpolator implements android.view.animation.Interpolator {

        private final float mCycles = 0.5f;

        @Override
        public float getInterpolation(final float input) {
            return (float) Math.sin(2.0f * mCycles * Math.PI * input);
        }
    }

    private void notifyOnAnimationEndCallBack(@NonNull final View v) {
        if (listener != null ){
            listener.onEnd(v);
        }


    }
}
