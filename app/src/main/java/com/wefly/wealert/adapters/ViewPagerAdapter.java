package com.wefly.wealert.adapters;

import android.content.Context;

import android.content.SharedPreferences;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.wefly.wealert.models.Alert;
import com.wefly.wealert.models.Piece;
import com.wefly.wealert.tasks.CategoryGetTask;
import com.wefly.wealert.utils.AppController;
import com.wefly.wealert.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ViewPagerAdapter extends PagerAdapter {
    private Context context;
    List<View> fragments;
    Spinner category;
    Alert alert = new Alert();
    static Map<String, Integer> response_category = new HashMap<>();
    AppController appController = AppController.getInstance();
    static LinearLayout pieceLayout;
    private HashSet<Piece> pieces = new HashSet<>();

    public ViewPagerAdapter(Context context, List<View> fragments) {
        this.context = context;
        this.fragments = fragments;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(final View container, final int position, final Object object) {
        ((ViewPager) container).removeView((View) object);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        final View view;
        try {
            switch (position) {
                case 0:
                    //FORM fragment
                    view = fragments.get(0);
//                    category = view.findViewById(R.id.categorySpinner);
//                    //We launch category task and pass the categorie Map to the dialog
//                    try {
//                        CategoryGetTask task = new CategoryGetTask(appController);
//                        response_category = task.execute().get();
//                    } catch (InterruptedException | ExecutionException e) {
//                        e.printStackTrace();
//                    }
//                    initCategorySpinner();
                    break;
                case 1:
                    //RECIPIENT fragement
                    view = fragments.get(1);
                    //We prepared a String set to store the id of the recipients
                    SharedPreferences sp = appController.getSharedPreferences("recipients", 0);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putStringSet("recipient_id", new HashSet<String>());
                    break;
                default:
                    view = fragments.get(0);
                    // do something
                    break;
            }
            container.addView(view);
            return view;

        } catch (Exception e) {
            e.printStackTrace();
            // Details
            View v = fragments.get(0);
            return v;
        }

    }

    /**********vFORM**********/
    //INIT SPINNER DATA
    protected void initCategorySpinner() {
        //Load Category list in spinner
        List<String> category_list = new ArrayList<>();
        if (response_category != null) {
            for (Map.Entry entry : response_category.entrySet()) {
                category_list.add((String) entry.getKey().toString().toUpperCase());
            }
        }
        Log.e("categories size", String.valueOf(category_list.size()));

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, category_list);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
         category.setAdapter(spinnerAdapter);
    }

    /*************************/
}