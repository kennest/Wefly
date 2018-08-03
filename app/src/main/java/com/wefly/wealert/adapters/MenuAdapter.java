package com.wefly.wealert.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wefly.wealert.events.OptionSelectedEvent;
import com.wefly.wealert.utils.AppController;
import com.wefly.wealert.utils.TextFileReader;
import com.wefly.wealert.R;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import uk.co.markormesher.android_fab.SpeedDialMenuAdapter;
import uk.co.markormesher.android_fab.SpeedDialMenuItem;

public class MenuAdapter extends SpeedDialMenuAdapter {
    List<SpeedDialMenuItem> menuItems;
    Context context;
    AppController appController=AppController.getInstance();

    public MenuAdapter(List<SpeedDialMenuItem> menuItems, Context context) {
        this.menuItems = menuItems;
        this.context = context;
    }

    @Override
    public int getCount() {
        return menuItems.size();
    }

    @NotNull
    @Override
    public SpeedDialMenuItem getMenuItem(Context context, int i) {
        return menuItems.get(i);
    }

    @Override
    public boolean onMenuItemClick(int position) {
        Toast.makeText(context, menuItems.get(position).getLabel(), Toast.LENGTH_SHORT).show();
        SharedPreferences sp=appController.getApplicationContext().getSharedPreferences("menu",0);
        SharedPreferences.Editor editor =sp.edit();
        String label = menuItems.get(position).getLabel();
        editor.putString("option_label",label);
        editor.apply();
        editor.commit();
        EventBus.getDefault().post(new OptionSelectedEvent(label));
        return super.onMenuItemClick(position);
    }

    @Override
    public void onPrepareItemLabel(Context context, int position, TextView label) {
        super.onPrepareItemLabel(context, position, label);
    }

    @Override
    public void onPrepareItemCard(Context context, int position, View card) {
        super.onPrepareItemCard(context, position, card);
    }

    @Override
    public float fabRotationDegrees() {
        return super.fabRotationDegrees();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled();
    }

    @Override
    public int getBackgroundColour(int position) {
        return super.getBackgroundColour(position);
    }
}
