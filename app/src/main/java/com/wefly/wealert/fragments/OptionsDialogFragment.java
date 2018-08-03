package com.wefly.wealert.fragments;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wefly.wealert.R;
import com.wefly.wealert.activities.onboardActivity;
import com.wefly.wealert.utils.AppController;
import com.wefly.wealert.utils.TextFileReader;

public class OptionsDialogFragment extends DialogFragment {
    ViewGroup view;
    TextView option_content;
    AppController appController = AppController.getInstance();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = (ViewGroup) inflater.inflate(R.layout.fragment_option_content, null);
        option_content = view.findViewById(R.id.content);
        option_content.setMovementMethod(new ScrollingMovementMethod());
        TextFileReader txtReader = new TextFileReader();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String tag = getArguments().getString("tag");
        switch (tag) {
            case "terms":
                option_content.setText(Html.fromHtml(txtReader.readRawText(getContext(), "terms_and_conditions.html")));
                SharedPreferences sp = getContext().getSharedPreferences("boot_options", 0);
                SharedPreferences.Editor editor = sp.edit();
                boolean agree = sp.getBoolean("agree", false);
                if (agree == false)
                    builder
                            .setPositiveButton(R.string.options_agree, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    editor.putBoolean("agree", true);
                                    editor.apply();
                                }
                            })
                            .setNegativeButton(R.string.options_disagree, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AlertDialog.Builder closer = new AlertDialog.Builder(getActivity());
                                    editor.putBoolean("agree", false);
                                    editor.apply();
                                    dismiss();
                                    closer.setTitle(R.string.disagree_closer_title);
                                    closer.setMessage(R.string.disagree_dialog);
                                    closer.setNegativeButton(R.string.disagree_close_btn, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            appController.restartApp();
                                        }
                                    });
                                    closer.show();
                                }
                            });
                break;
            case "about":
                option_content.setText(Html.fromHtml(txtReader.readRawText(getContext(), "about.html")));
                break;
            case "policy":
                option_content.setText(Html.fromHtml(txtReader.readRawText(getContext(), "policy_privacy.html")));
                break;
            case "quit":
                //appController.quitApp();
                getActivity().finishAffinity();
                System.exit(0);
                break;
            default:
                option_content.setText(Html.fromHtml(txtReader.readRawText(getContext(), "about.html")));
                break;
        }

        builder.setView(view);
        AlertDialog optionsDialog = builder.create();
        return optionsDialog;
    }


}
