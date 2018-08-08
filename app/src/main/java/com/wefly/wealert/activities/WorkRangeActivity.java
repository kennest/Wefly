package com.wefly.wealert.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.widget.SnackBar;
import com.wefly.wealert.R;
import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorkRangeActivity extends AppCompatActivity {
    RangeSeekBar<Integer> range;
    Button save;
    TextView begin,end;
    Integer work_begin = 1;
    Integer work_end = 24;
    int preMin = -1;
    int preMax = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_range);

        SharedPreferences sp=getSharedPreferences("settings",0);

        range = findViewById(R.id.range);
        save = findViewById(R.id.saveRange);

        begin = findViewById(R.id.begin);
        end = findViewById(R.id.end);

        // Get noticed while dragging
        range.setNotifyWhileDragging(true);
        range.setRangeValues(0,24);

        String old_work_range=sp.getString("work_range","empty");
        Log.e(getLocalClassName(),"WR setting:"+old_work_range);

        if(!old_work_range.contentEquals("empty")){
            old_work_range=old_work_range.substring(1,old_work_range.length()-1);
            String[] stringArray=old_work_range.trim().split(",");
            //int[] intArray = new int[stringArray.length];
            //for (int i = 0; i < stringArray.length; i++) {
            //String numberAsString = stringArray[i];
            //intArray[i] = Integer.parseInt(numberAsString);
                range.setSelectedMinValue(Integer.parseInt(stringArray[0].trim()));
                range.setSelectedMaxValue(Integer.parseInt(stringArray[1].trim()));
            begin.setText(stringArray[0].trim()+"H");
            end.setText(stringArray[1].trim()+"H");
            //}
        }

        range.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                //Now you have the minValue and maxValue of your RangeSeekbar
                //Toast.makeText(getApplicationContext(), minValue + "-" + maxValue, Toast.LENGTH_LONG).show();
                int diff = maxValue - minValue;
                if (diff == 0 || diff < 1) {
                    bar.setEnabled(false);
                    if(minValue != preMin){
                        range.setSelectedMinValue(preMin);
                    }
                    else if(maxValue != preMax){
                        range.setSelectedMaxValue(preMax);
                    }
                    AlertDialog.Builder alert = new AlertDialog.Builder(WorkRangeActivity.this);
                    alert.setNegativeButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            range.setEnabled(true);
                        }
                    });
                    alert.setCancelable(false);
                    alert.setMessage(Html.fromHtml(getString(R.string.range_info))).show();

                } else {
                    preMin = minValue;
                    preMax = maxValue;
                }
                begin.setText(minValue+"H");
                end.setText(maxValue+"H");
                work_begin = minValue;
                work_end = maxValue;
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> work_range=new ArrayList<>();
                work_range.add(work_begin);
                work_range.add(work_end);
                Toast.makeText(getApplicationContext(),"Work range:"+work_range.toString(), Toast.LENGTH_LONG).show();
                sp.edit().putString("work_range",work_range.toString()).apply();
                Snackbar.make(view, R.string.range_stored,Snackbar.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                },1000);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(getLocalClassName(),"Destroyed");
    }
}
