package com.wefly.wealert.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wefly.wealert.utils.TextFileReader;
import com.wefly.wealert.R;

public class MenuActivity extends AppCompatActivity {
    ViewGroup view;
    TextView option_content;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getApplicationContext().getSharedPreferences("menu", 0);
        view = (ViewGroup) getLayoutInflater().inflate(R.layout.fragment_option_content, null);
        option_content = view.findViewById(R.id.content);
        option_content.setMovementMethod(new ScrollingMovementMethod());
        TextFileReader txtReader = new TextFileReader();
        String label = sp.getString("option_label", null);
        option_content.setText("");
        switch (label) {
            case "About":
                option_content.setText(Html.fromHtml(txtReader.readRawText(this, "about.html")));
                break;
            case "Policy":
                option_content.setText(Html.fromHtml(txtReader.readRawText(this, "policy_privacy.html")));
                break;
            case "Terms":
                option_content.setText(Html.fromHtml(txtReader.readRawText(this, "terms_and_conditions.html")));
                break;
            case "Quit":
                finishAffinity();
                System.exit(0);
                break;
            default:
                break;
        }
        setContentView(view);
    }
}
