package com.wefly.wealert.utils;

import com.wefly.wealert.R;

public class BuilderManager {

    private static int[] imageResources = new int[]{
            R.drawable.ic_login,
            R.drawable.ic_login,
            R.drawable.ic_login,
            R.drawable.ic_login,
            R.drawable.ic_login,
            R.drawable.ic_login,
            R.drawable.ic_login,
            R.drawable.ic_login,
            R.drawable.ic_login,
            R.drawable.ic_login,
            R.drawable.ic_login,
            R.drawable.ic_login,
            R.drawable.ic_login,
            R.drawable.ic_login,
            R.drawable.ic_login,
            R.drawable.ic_login
    };

    private static int imageResourceIndex = 0;

    public static int getImageResource() {
        if (imageResourceIndex >= imageResources.length) imageResourceIndex = 0;
        return imageResources[imageResourceIndex++];
    }




    private static BuilderManager ourInstance = new BuilderManager();

    public static BuilderManager getInstance() {
        return ourInstance;
    }

    private BuilderManager() {
    }
}
