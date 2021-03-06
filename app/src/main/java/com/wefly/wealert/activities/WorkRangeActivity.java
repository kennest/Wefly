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
    TextView begin, end;
    Integer work_begin = 1;
    Integer work_end = 24;
    int preMin = -1;
    int preMax = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_range);


        range = findViewById(R.id.range);
        save = findViewById(R.id.saveRange);

        begin = findViewById(R.id.begin);
        end = findViewById(R.id.end);

        // Get noticed while dragging
        range.setNotifyWhileDragging(true);
        range.setRangeValues(0, 24);

        range.setSelectedMinValue(6);
        range.setSelectedMaxValue(18);

        begin.setText(6 + "H");
        end.setText(18+ "H");


        SharedPreferences sharedPreferences = getSharedPreferences("settings", 0);

        String old_work_range = sharedPreferences.getString("work_range", "");
        Log.e(getLocalClassName(), "WR setting:" + old_work_range);

        if (old_work_range != "") {
            old_work_range = old_work_range.substring(1, old_work_range.length() - 1);
            String[] stringArray = old_work_range.trim().split(",");

            range.setSelectedMinValue(Integer.parseInt(stringArray[0].trim()));
            range.setSelectedMaxValue(Integer.parseInt(stringArray[1].trim()));

            begin.setText(stringArray[0].trim() + "H");
            end.setText(stringArray[1].trim() + "H");

            work_begin = Integer.parseInt(stringArray[0].trim());
            work_end = Integer.parseInt(stringArray[1].trim());
        }

        range.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                //Now you have the minValue and maxValue of your RangeSeekbar
                //Toast.makeText(getApplicationContext(), minValue + "-" + maxValue, Toast.LENGTH_LONG).show();
                int diff = maxValue - minValue;
                if (diff == 0 || diff < 1) {
                    bar.setEnabled(false);
                    if (minValue != preMin) {
                        range.setSelectedMinValue(preMin);
                    } else if (maxValue != preMax) {
                        range.setSelectedMaxValue(preMax);
                    }
                    AlertDialog.Builder alert = new AlertDialog.Builder(WorkRangeActivity.this);
                    alert.setNegativeButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            bar.setEnabled(true);
                        }
                    });
                    alert.setCancelable(false);
                    alert.setMessage(Html.fromHtml(getString(R.string.range_info))).show();

                } else {
                    preMin = minValue;
                    preMax = maxValue;
                }
                begin.setText(minValue + "H");
                end.setText(maxValue + "H");
                work_begin = minValue;
                work_end = maxValue;
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> work_range = new ArrayList<>();
                work_begin=range.getSelectedMinValue();
                work_end=range.getSelectedMaxValue();
                work_range.add(work_begin);
                work_range.add(work_end);
                Toast.makeText(getApplicationContext(),"Work range:"+work_range.toString(), Toast.LENGTH_LONG).show();
                Snackbar.make(view, R.string.range_stored, Snackbar.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sharedPreferences.edit().putString("work_range", work_range.toString()).apply();
                        sharedPreferences.edit().putInt("work_begin",work_begin).apply();
                        sharedPreferences.edit().putInt("work_end",work_end).apply();

                        SharedPreferences sp2 = getSharedPreferences("boot_options", 0);
                        sp2.edit().putBoolean("setPeriodPassed", true).apply();

                        finish();
                    }
                }, 1000);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(getLocalClassName(), "Destroyed");
    }
}
