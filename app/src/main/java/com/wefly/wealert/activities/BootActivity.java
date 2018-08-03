package com.wefly.wealert.activities;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.rxgps.RxGps;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.jetradarmobile.rxlocationsettings.RxLocationSettings;
import com.wefly.wealert.adapters.DrawerListAdapter;
import com.wefly.wealert.adapters.RecipientAdapter;
import com.wefly.wealert.adapters.ViewPagerAdapter;
import com.wefly.wealert.tracking.NavigationService;
import com.wefly.wealert.events.AlertSentEvent;
import com.wefly.wealert.events.BeforeSendAlertEvent;
import com.wefly.wealert.events.BeforeUploadEvent;
import com.wefly.wealert.events.JsonExceptionEvent;
import com.wefly.wealert.events.OptionSelectedEvent;
import com.wefly.wealert.events.PieceRemoveEvent;
import com.wefly.wealert.events.RecipientEmptyEvent;
import com.wefly.wealert.events.UploadDoneEvent;
import com.wefly.wealert.models.Alert;
import com.wefly.wealert.models.Piece;
import com.wefly.wealert.models.Recipient;
import com.wefly.wealert.observables.AlertPostObservable;
import com.wefly.wealert.observables.CategoriesListObservable;
import com.wefly.wealert.observables.PieceUploadObservable;
import com.wefly.wealert.observables.RecipientsListObservable;
import com.wefly.wealert.services.RemoteService;
import com.wefly.wealert.tasks.PieceUploadTask;
import com.wefly.wealert.utils.AppController;
import com.wefly.wealert.R;
import com.wefly.wealert.utils.PathUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import io.objectbox.Box;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout;
import nl.psdcompany.duonavigationdrawer.views.DuoMenuView;
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.functions.Action1;
import uk.co.markormesher.android_fab.FloatingActionButton;

import static android.app.AlarmManager.INTERVAL_HALF_HOUR;

public class BootActivity extends AppCompatActivity {
    AppController appController = AppController.getInstance();
    List<Recipient> recipients = new ArrayList<>();
    static Map<String, Integer> response_category = new HashMap<>();
    private HashSet<Piece> pieces = new HashSet<>();
    private static Bundle bundle = new Bundle();
    FloatingActionButton fab;
    View vForm, vRecipient;
    List<View> fragments = new ArrayList<>();
    private LinearLayout pieceLayout;
    static ListView recipientList;
    private static Alert alert = new Alert();
    private android.support.design.widget.FloatingActionButton recordBtn;
    private Button btnSend, nextBtn;
    private EditText edObject, edContent;
    static ViewPager viewPager;
    CircularProgressIndicator alert_loader;
    CircularProgressIndicator piece_loader;
    static LinearLayout alert_loader_content;
    static LinearLayout piece_loader_content;
    static TextView alert_loader_text;
    static TextView piece_loader_text;
    Toolbar toolbar;
    Spinner category;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(broadcastReceiver, new IntentFilter(NavigationService.str_receiver));
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(NavigationService.str_receiver));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       startTracking();

        //Launch Camera
        dispatchTakePictureIntent();

        SharedPreferences sp = getSharedPreferences("recipients", 0);
        for (String key : sp.getAll().keySet()) {
            if (key.matches("recipients_id"))
                sp.edit().remove(key);
        }
        sp.edit().commit();

        ensureLocationSettings();

        vForm = LayoutInflater.from(getBaseContext()).inflate(R.layout.fragment_send, null);
        vRecipient = LayoutInflater.from(getBaseContext()).inflate(R.layout.fragment_recipient, null);

        fragments.add(vForm);
        fragments.add(vRecipient);

        pieceLayout = vForm.findViewById(R.id.pieceToSend);
        recordBtn = vForm.findViewById(R.id.recordBtn);
        nextBtn = vForm.findViewById(R.id.nextBtn);
        btnSend = vRecipient.findViewById(R.id.btnSend);
        edObject = vForm.findViewById(R.id.objectEdText);
        edContent = vForm.findViewById(R.id.contentEdText);
        alert_loader = findViewById(R.id.alert_loader);
        piece_loader = findViewById(R.id.piece_loader);
        category = vForm.findViewById(R.id.categorySpinner);
        alert_loader_content = (LinearLayout) findViewById(R.id.alert_loader_content);
        piece_loader_content = (LinearLayout) findViewById(R.id.piece_loader_content);
        alert_loader_text = findViewById(R.id.alertloadertext);
        piece_loader_text = findViewById(R.id.pieceloadertext);

        loadCategorieRx();
        loadRecipientRx();

        recordBtn.setOnClickListener(v -> {
            LaunchRecord();
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInput();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ensureLocationSettings();
                SendRx();
            }
        });

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getApplicationContext(), fragments);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Toast.makeText(getApplicationContext(), "current position" + position, Toast.LENGTH_SHORT).show();
                if (position == 1) {
                    toolbar.setTitle(getString(R.string.choose_recipients));
                    saveInput();
                } else {
                    toolbar.setTitle("Add additionnal informations");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        InitSideMenu();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(getLocalClassName(), "Req code:" + requestCode + "Res Code:" + resultCode);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), imageBitmap, "Title", null);
            String filepath = PathUtil.getPath(getApplicationContext(), Uri.parse(path));

            Log.v("Picture path", filepath);

            galleryAddPic(filepath);
            Piece p = new Piece();
            p.setIndex(System.currentTimeMillis());
            p.setUrl(filepath.trim());
            p.setContentUrl(Uri.fromFile(new File(filepath.trim())));
            pieces.add(p);
            pieceLayout.removeAllViews();
            FillPieceLayout();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == 0) {
            restart();
        } else if (resultCode == 200 && requestCode == 352 && data != null) {
            Piece audio = new Piece();
            String url = data.getExtras().getString("audioPath");
            audio.setIndex(System.currentTimeMillis());
            Log.v("OnResult Audio index", String.valueOf(audio.getIndex()));
            audio.setUrl(url);
            audio.setContentUrl(Uri.fromFile(new File(audio.getUrl().trim())));

            pieces.add(audio);
            Log.v("piece size added", String.valueOf(pieces.size()));

            ImageView audioimage = new ImageView(getApplicationContext());
            audioimage.setImageResource(R.drawable.microphone);
            audioimage.setTag(audio.getIndex());
            audioimage.setLayoutParams(new LinearLayout.LayoutParams(150, 150));
            audioimage.setScaleType(ImageView.ScaleType.FIT_XY);
            recordBtn.setClickable(false);
            recordBtn.setEnabled(false);

            audioimage.setOnClickListener(v -> {
                Toast.makeText(getApplicationContext(), "Piece removed!", Toast.LENGTH_SHORT).show();
                long index = Long.parseLong(v.getTag().toString());
                Log.v("audio clicked index", String.valueOf(index));
                Set<Piece> tmp = new HashSet<>();
                tmp.addAll(pieces);
                for (Piece p : tmp) {
                    Log.v("clicked index", String.valueOf(index));
                    Log.v("stored index", String.valueOf(p.getIndex()));
                    if (p.getIndex() == index) {
                        pieces.remove(p);
                    }
                    recordBtn.setClickable(true);
                    recordBtn.setEnabled(true);
                }
                Log.v("piece size removed", String.valueOf(pieces.size()));
                pieceLayout.removeView(v);
                EventBus.getDefault().post(new PieceRemoveEvent(pieces.size()));
            });
            pieceLayout.addView(audioimage);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Log.e(getLocalClassName(), "back pressed");
        pieceLayout.removeAllViews();
        dispatchTakePictureIntent();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(getLocalClassName(), "destroyed");
    }

    private void InitSideMenu() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Add additionnal informations");
        DuoDrawerLayout drawerLayout = (DuoDrawerLayout) findViewById(R.id.drawerlayout);
        DuoDrawerToggle drawerToggle = new DuoDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        ArrayList<String> mMenuOptions = new ArrayList<>();
        mMenuOptions.add("Camera");
        mMenuOptions.add("Sent Alerts");
        mMenuOptions.add("Terms and conditions");
        mMenuOptions.add("Policy privacy");
        mMenuOptions.add("About");
        mMenuOptions.add("Quit");

        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
        DuoMenuView duoMenuView = (DuoMenuView) findViewById(R.id.sidemenu);
        DrawerListAdapter menuAdapter = new DrawerListAdapter(mMenuOptions);
        duoMenuView.setAdapter(menuAdapter);

        duoMenuView.setOnMenuClickListener(new DuoMenuView.OnMenuClickListener() {
            @Override
            public void onFooterClicked() {

            }

            @Override
            public void onHeaderClicked() {

            }

            @Override
            public void onOptionClicked(int position, Object objectClicked) {
                SharedPreferences sp = getApplicationContext().getSharedPreferences("menu", 0);
                switch (position) {
                    case 0:
                        restart();
                        break;
                    case 1:
                        Toast.makeText(getApplicationContext(), String.valueOf(position), Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        sp.edit().putString("option_label", "Terms").apply();
                        startActivity(new Intent(BootActivity.this, MenuActivity.class));
                        break;
                    case 3:
                        sp.edit().putString("option_label", "Policy").apply();
                        startActivity(new Intent(BootActivity.this, MenuActivity.class));
                        break;
                    case 4:
                        sp.edit().putString("option_label", "About").apply();
                        startActivity(new Intent(BootActivity.this, MenuActivity.class));
                        break;
                    case 5:
                        finishAffinity();
                }
            }
        });
    }

    /****************SendFragment*********************/
    private void FillPieceLayout() {
        //Fill Image View
        Log.e(getLocalClassName(), "FillPieceLayout piece size 1: " + String.valueOf(pieces.size()));
        for (Piece item : pieces) {
            ImageView image = new ImageView(getApplicationContext());
            Log.e(getLocalClassName(), "image retrieved path: " + item.getUrl().toString());
            Log.v("Image path", item.getUrl());
            if (item.getExtension(item.getUrl()).matches(".m4a")) {
                image.setImageResource(R.drawable.microphone);
            } else {
                image.setImageURI(item.getContentUrl());
            }
            image.setTag(item.getIndex());
            image.setLayoutParams(new LinearLayout.LayoutParams(150, 150));
            image.setPadding(0, 0, 5, 0);
            image.setScaleType(ImageView.ScaleType.FIT_XY);
            image.setOnClickListener(view -> {
                removeImage(view);
                EventBus.getDefault().post(new PieceRemoveEvent(pieces.size()));
            });
            pieceLayout.addView(image);
        }
    }

    private void removeImage(View view) {
        Toast.makeText(appController.getApplicationContext(), "Piece removed!", Toast.LENGTH_SHORT).show();
        long index = Long.parseLong(view.getTag().toString());
        Log.v("image clicked index", String.valueOf(index));
        Set<Piece> tmp = new HashSet<>();
        tmp.addAll(pieces);
        for (Piece p : tmp) {
            Log.v("clicked index", String.valueOf(index));
            Log.v("stored index", String.valueOf(p.getIndex()));
            if (p.getIndex() == index) {
                pieces.remove(p);
            }
        }
        Log.v("piece size 2", String.valueOf(pieces.size()));
        pieceLayout.removeView(view);
    }

    private void LaunchRecord() {
        Log.v("Audio recordbtn", "clicked");
        Intent recorder = new Intent(this, RecorderActivity.class);
        startActivityForResult(recorder, 352);
    }

    /****************SendFragment*********************/

    private void saveInput() throws NullPointerException {
        if (alert != null) {
            alert.setObject(edObject.getText().toString().trim());
            alert.setContent(edContent.getText().toString().trim());
            alert.setCategory(category.getSelectedItem().toString());
            appController.setPieceList(pieces);
        }
        if (viewPager.getCurrentItem() == 0) {
            viewPager.setCurrentItem(1);
        }
    }

    private void resetAllField() {
        edContent.setText("");
        edObject.setText("");
        pieceLayout.removeAllViews();
        pieces.clear();
    }

    private void StoreAlert() {
        //Stored Alert with ObjectBox
        Box<com.wefly.wealert.dbstore.Alert> box = appController.boxStore.boxFor(com.wefly.wealert.dbstore.Alert.class);
        com.wefly.wealert.dbstore.Alert a = new com.wefly.wealert.dbstore.Alert();
        com.wefly.wealert.dbstore.Piece piece = new com.wefly.wealert.dbstore.Piece();
        a.setTitle(alert.getObject());
        a.setContent(alert.getContent());
        for (Piece p : pieces) {
            piece.setUrl(p.getUrl());
            a.pieces.add(piece);
        }
        long id = box.put(a);
        System.out.println("Box alert:" + id);
    }

    private boolean hasRecipientsID() {
        SharedPreferences sp = getSharedPreferences("recipients", 0);
        Set recipient_ids = sp.getStringSet("recipients_id", new HashSet<String>());
        Log.e(getLocalClassName(), "recipient id size" + recipient_ids.size());
        if (recipient_ids.size() <= 0) {
            return false;
        } else {
            return true;
        }
    }

    /***************GREEN ROBOT EVENT*********************/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBeforeSendAlert(BeforeSendAlertEvent event) {
        StoreAlert();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBeforeUpload(BeforeUploadEvent event) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecipientEmptyEvent(RecipientEmptyEvent event) {
        viewPager.setCurrentItem(1);
        Snackbar.make(vRecipient, event.message, Snackbar.LENGTH_LONG).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAlertSentEvent(AlertSentEvent event) {
        alert_loader_content.setVisibility(View.GONE);

        SharedPreferences sp = getApplicationContext().getSharedPreferences("recipients", 0);
        for (String key : sp.getAll().keySet()) {
            if (key.matches("recipients_id"))
                sp.edit().remove(key);
        }
        sp.edit().commit();

        alert_loader.setCurrentProgress(0);
        if (appController.getPieceList().size() > 0) {
            Log.v("Alert Post Execute", "RUN");
            PieceUploadTask pieceUploadTask = new PieceUploadTask(pieces, alert);
            piece_loader.setMaxProgress(pieces.size());
            pieceUploadTask.setProgressBar(piece_loader);
            piece_loader_text.setText("Uploading pieces...");
            piece_loader_content.setVisibility(View.VISIBLE);
            pieceUploadTask.execute();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUploadDoneEvent(UploadDoneEvent event) {
        Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show();
        resetAllField();
        piece_loader_content.setVisibility(View.GONE);
        piece_loader.setCurrentProgress(0);
        restart();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOptionSelected(OptionSelectedEvent event) {
        Intent option = new Intent(this, MenuActivity.class);
        option.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(option);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPieceRemoved(PieceRemoveEvent event) {
        if (event.size == 0) {
            restart();
        } else {
            return;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onJsonExceptionEvent(JsonExceptionEvent event) {
        Toast.makeText(BootActivity.this, event.message, Toast.LENGTH_LONG).show();
        return;
    }

    /***************GREEN ROBOT EVENT*********************/

    private void restart() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    //Check if Lacation is enabled and launch teask
    private void ensureLocationSettings()
    {
        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY))
                .build();
        RxLocationSettings.with(BootActivity.this).ensure(locationSettingsRequest)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean enabled) {
                        //Toast.makeText(BootActivity.this, enabled ? "Enabled" : "Failed", Toast.LENGTH_LONG).show();
                        if (enabled) {
                            startLocationService();
                            startTracking();
                        }
                    }
                });
    }

    @SuppressLint("CheckResult")
    protected void startLocationService() {
        new RxGps(this).locationLowPower()
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(location -> {
                    appController.latitude = location.getLatitude();
                    appController.longitude = location.getLongitude();
                    Log.v(getLocalClassName(), "LONG:" + location.getLongitude() + "/" + "LAT:" + location.getLatitude());
                    //you've got the location
                }, throwable -> {
                    if (throwable instanceof RxGps.PermissionException) {
                        //the user does not allow the permission
                    } else if (throwable instanceof RxGps.PlayServicesNotAvailableException) {
                        //the user do not have play services
                    }
                });
    }

    protected void testRX() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://217.182.133.143:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        RemoteService service = retrofit.create(RemoteService.class);

        Observable<String> observable = service.CategoriesList("JWT " + appController.getToken());

        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .timeout(10, TimeUnit.SECONDS)
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        Toast.makeText(getApplicationContext(), "OBS result" + s, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getApplicationContext(), "OBS error" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void loadCategorieRx() {
        Observer mObserver = new Observer<String>() {
            List<String> categorielist = new ArrayList<>();

            @Override
            public void onSubscribe(Disposable d) {
                //Toast.makeText(BootActivity.this, "onSubscribe called", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(String s) {
                //Toast.makeText(BootActivity.this, "onNext called: " + s, Toast.LENGTH_SHORT).show();
                categorielist.add(s);
            }

            @Override
            public void onError(Throwable e) {
               // Toast.makeText(BootActivity.this, "onError called", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                //Toast.makeText(BootActivity.this, "onComplete called", Toast.LENGTH_SHORT).show();
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, categorielist);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                category.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                category.setAdapter(spinnerAdapter);
            }
        };

        Observable<String> observable = new CategoriesListObservable().getList();

        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(mObserver);
    }

    public void loadRecipientRx() {
        Observer mObserver = new Observer<Recipient>() {
            List<Recipient> recipients = new ArrayList<>();

            @Override
            public void onSubscribe(Disposable d) {
               // Toast.makeText(BootActivity.this, "onSubscribe called", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(Recipient r) {
                //Toast.makeText(BootActivity.this, "onNext called: " + r.getLastName(), Toast.LENGTH_SHORT).show();
                recipients.add(r);
            }

            @Override
            public void onError(Throwable e) {
                //Toast.makeText(BootActivity.this, "onError called", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                //Toast.makeText(BootActivity.this, "onComplete called", Toast.LENGTH_SHORT).show();
                recipientList = vRecipient.findViewById(R.id.recipientList);
                recipientList.setAdapter(new RecipientAdapter(getApplicationContext(), recipients));
            }
        };
        Observable<Recipient> observable = new RecipientsListObservable().getList();

        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(mObserver);
    }

    public void SendRx() {
        if (hasRecipientsID()) {
            Observer mObserver = new Observer<Boolean>() {
                @Override
                public void onSubscribe(Disposable d) {
                    Toast.makeText(BootActivity.this, "Sending Alert...", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNext(Boolean r) {

                }

                @Override
                public void onError(Throwable e) {
                    Toast.makeText(BootActivity.this, "Alert send Error", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onComplete() {
                    Toast.makeText(BootActivity.this, "Alert Sended!", Toast.LENGTH_SHORT).show();
                    uploadRx();
                }
            };
            Observable<Boolean> observable = new AlertPostObservable().send(alert);

            observable.observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(mObserver);
        } else {
            EventBus.getDefault().post(new RecipientEmptyEvent(getString(R.string.empty_recipient)));
        }
    }

    private void uploadRx(){
        Observer mObserver = new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
                Toast.makeText(BootActivity.this, "Uploading piece...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(Boolean r) {

            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(BootActivity.this, "Piece send Error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                Toast.makeText(BootActivity.this, "Piece Sended!", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        restart();
                    }
                },1000);
            }
        };
        Observable<Boolean> observable = new PieceUploadObservable().upload(pieces,alert);

        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(mObserver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Double latitude = Double.valueOf(intent.getStringExtra("latutide"));
            Double longitude = Double.valueOf(intent.getStringExtra("longitude"));
            Log.e("Latitude2", latitude+"");
            Log.e("Longitude2", longitude+"");
        }
    };

    private void startTracking(){
        //AlarmManager manager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        //Calendar calendar = Calendar.getInstance();
        //calendar.set(Calendar.SECOND,1);
        Intent intent = new Intent(getApplicationContext(),NavigationService.class);
       // manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,calendar.getTimeInMillis(),manager.INTERVAL_HALF_HOUR,intent);
        stopService(intent);
        startService(intent);
    }
}