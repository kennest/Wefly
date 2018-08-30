package com.wefly.wealert.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.wefly.wealert.adapters.RecipientAdapter;
import com.wefly.wealert.dbstore.AlertData;
import com.wefly.wealert.dbstore.Piece;
import com.wefly.wealert.dbstore.Recipient;
import com.wefly.wealert.dbstore.Recipient_;
import com.wefly.wealert.observables.RecipientsListObservable;
import com.wefly.wealert.utils.AppController;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import io.objectbox.Box;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AlertSentDetailsActivity extends AppCompatActivity implements BaseSliderView.OnSliderClickListener,
        ViewPagerEx.OnPageChangeListener {
    private SliderLayout mDemoSlider;
    List<Recipient> recipients = new ArrayList<>();
    List<Piece> pieces = new ArrayList<>();
    JcPlayerView jcplayerView;
    TextView title, date, content;
    Toolbar toolbar;
    FloatingActionButton map;
    LinearLayout hScrollView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_alert_detail);

        mDemoSlider = findViewById(R.id.slider);
        jcplayerView = (JcPlayerView) findViewById(R.id.jcplayer);
        title = findViewById(R.id.detail_title);
        content = findViewById(R.id.detail_content);
        date = findViewById(R.id.detail_date);
        map = findViewById(R.id.map);
        hScrollView = findViewById(R.id.recipientScroll);

        toolbar = findViewById(R.id.toolbar);

        Intent intent = getIntent();
        long id = intent.getLongExtra("alert_id", 0);

        Box<AlertData> dataBox = AppController.boxStore.boxFor(AlertData.class);

        AlertData a = dataBox.get(id);
        extractRecipient(a.getRecipients());
        //Toast.makeText(getApplicationContext(),"recipient ID"+a.getRecipients(),Toast.LENGTH_LONG).show();

        title.setText(a.getTitre());
        content.setText(a.getContenu());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String title_date = "";
        try {
            Date dt = format.parse(a.getDate_de_creation());
            title_date = dt.toString();
            date.setText(dt.toString());
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        toolbar.setTitle(a.getTitre() + "-" + title_date);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        pieces.addAll(a.pieces);

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowOnMap(a.getLatitude(), a.getLongitude());
            }
        });

        InitSlider(pieces);
        InitRecipients(recipients);
    }

    protected void InitSlider(List<Piece> list) {
        ArrayList<String> listUrl = new ArrayList<>();
        ArrayList<String> listName = new ArrayList<>();

        for (Piece p : list) {
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
            mDemoSlider.setPresetTransformer(SliderLayout.Transformer.ZoomOutSlide);

            mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
            mDemoSlider.setCustomAnimation(new DescriptionAnimation());
            mDemoSlider.setDuration(4000);
            mDemoSlider.addOnPageChangeListener(this);
        }
    }

    protected void InitRecipients(List<Recipient> recipients) {
        for (Recipient r : recipients) {
            ConstraintLayout parent= new ConstraintLayout(getApplicationContext());
            parent= (ConstraintLayout) getLayoutInflater().inflate(R.layout.recipient_item,null);
            CircleImageView avatar=parent.findViewById(R.id.avatar);
            TextView username=parent.findViewById(R.id.username);
            Glide
                    .with(getApplicationContext())
                    .load(r.getAvatar())
                    .into(avatar);
            username.setText(String.format("%s %s", r.getFirstname(), r.getLastname()));
            parent.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            hScrollView.addView(parent);
        }
    }

    private void extractRecipient(String s){
        Box<Recipient> recipientBox=AppController.boxStore.boxFor(Recipient.class);
        String replace = s.replace("[","");
        System.out.println(replace);
        String replace1 = replace.replace("]","");
        System.out.println(replace1);
        List<String> IDlists = new ArrayList<String>(Arrays.asList(replace1.split(",")));
        for(String n:IDlists){
            int i=Integer.parseInt(n.trim());
            Recipient x=recipientBox.query().equal(Recipient_.raw_id,i).build().findFirst();
            if(x!=null)
                recipients.add(x);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDemoSlider.stopAutoCycle();
        jcplayerView.createNotification();
    }


    @Override
    public void onSliderClick(BaseSliderView slider) {
        // Toast.makeText(this, slider.getBundle().get("extra") + "", Toast.LENGTH_SHORT).show();
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

    protected void ShowOnMap(Double lat, Double lon) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon + "");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        jcplayerView.kill();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
