package com.wefly.wealert.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.glide.slider.library.Animations.DescriptionAnimation;
import com.glide.slider.library.SliderLayout;
import com.glide.slider.library.SliderTypes.BaseSliderView;
import com.glide.slider.library.SliderTypes.TextSliderView;
import com.glide.slider.library.Tricks.ViewPagerEx;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.Chip;
import com.wefly.wealert.R;
import com.wefly.wealert.dbstore.AlertData;
import com.wefly.wealert.dbstore.Piece;
import com.wefly.wealert.dbstore.Recipient;
import com.wefly.wealert.utils.AppController;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.objectbox.Box;

public class AlertSentDetailsActivity extends AppCompatActivity implements BaseSliderView.OnSliderClickListener,
        ViewPagerEx.OnPageChangeListener {
    private SliderLayout mDemoSlider;
    private ChipsInput chipsInput;
    List<Recipient> recipients=new ArrayList<>();
    List<Piece> pieces=new ArrayList<>();
    JcPlayerView jcplayerView;
    TextView title,date,content;
    Toolbar toolbar;
    FloatingActionButton map;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_alert_detail);
        mDemoSlider = findViewById(R.id.slider);
        chipsInput=findViewById(R.id.chips_input);
        jcplayerView = (JcPlayerView) findViewById(R.id.jcplayer);
        title=findViewById(R.id.detail_title);
        content=findViewById(R.id.detail_content);
        date=findViewById(R.id.detail_date);
        map=findViewById(R.id.map);

        toolbar = findViewById(R.id.toolbar);

        Intent intent = getIntent();
        long id = intent.getLongExtra("alert_id",0);

        Box<AlertData> dataBox= AppController.boxStore.boxFor(AlertData.class);

        AlertData a=dataBox.get(id);

        title.setText(a.getTitre());
        content.setText(a.getContenu());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String title_date="";
        try {
            Date dt = format.parse(a.getDate_de_creation());
            title_date=dt.toString();
            date.setText(dt.toString());
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        toolbar.setTitle(a.getTitre()+"-"+title_date);
        setSupportActionBar(toolbar);

        recipients.addAll(a.destinataires);

        pieces.addAll(a.pieces);

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowOnMap(a.getLatitude(),a.getLongitude());
            }
        });

        InitSlider(pieces);
        InitRecipientChips(recipients);
    }

    protected void InitSlider(List<Piece> list) {
        ArrayList<String> listUrl = new ArrayList<>();
        ArrayList<String> listName = new ArrayList<>();

        for(Piece p:list){
            listUrl.add(p.getUrl());
            listName.add(p.getUrl().substring(p.getUrl().lastIndexOf("/") + 1));
        }

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.centerCrop();

        for (int i = 0; i < listUrl.size(); i++) {
            TextSliderView sliderView = new TextSliderView(this);
            // if you want show image only / without description text use DefaultSliderView instead
            if (listUrl.get(i).substring(listUrl.get(i).lastIndexOf(".")).equals(".m4a")) {
                ArrayList<JcAudio> jcAudios = new ArrayList<>();
                jcAudios.add(JcAudio.createFromURL("url audio", listUrl.get(i)));
                jcplayerView.initPlaylist(jcAudios, null);
                jcplayerView.setVisibility(View.VISIBLE);
            } else {
                // initialize SliderLayout
                sliderView
                        .image(listUrl.get(i))
                        .description(listName.get(i))
                        .setRequestOption(requestOptions)
                        .setBackgroundColor(Color.WHITE)
                        .setProgressBarVisible(true)
                        .setOnSliderClickListener(this);

                //add your extra information
                sliderView.bundle(new Bundle());
                sliderView.getBundle().putString("extra", listName.get(i));
                mDemoSlider.addSlider(sliderView);
            }

            // set Slider Transition Animation
            // mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Default);
            mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);

            mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
            mDemoSlider.setCustomAnimation(new DescriptionAnimation());
            mDemoSlider.setDuration(4000);
            mDemoSlider.addOnPageChangeListener(this);
        }
    }

    protected void InitRecipientChips(List<Recipient> list){
        if(list.size()>0) {
            for (Recipient r : list) {
                URL url = null;
                Uri avatar=null;
                try {
                    url = new URL(r.getAvatar());
                    try {
                        avatar = Uri.parse( url.toURI().toString() );
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                chipsInput.addChip(avatar, r.getUsername(), String.valueOf(r.getRaw_id()));
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDemoSlider.stopAutoCycle();
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
        Toast.makeText(this, slider.getBundle().get("extra") + "", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    protected void ShowOnMap(Double lat,Double lon){
        Uri gmmIntentUri = Uri.parse("google.navigation:q="+lat+","+lon+"");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }
}
