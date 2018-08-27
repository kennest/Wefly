package com.wefly.wealert.activities;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appizona.yehiahd.fastsave.FastSave;
import com.github.florent37.rxgps.RxGps;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.gmail.samehadar.iosdialog.IOSDialog;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.jetradarmobile.rxlocationsettings.RxLocationSettings;
import com.wefly.wealert.adapters.DrawerListAdapter;
import com.wefly.wealert.adapters.PieceAdapter;
import com.wefly.wealert.adapters.RecipientAdapter;
import com.wefly.wealert.adapters.ViewPagerAdapter;
import com.wefly.wealert.dbstore.Category;
import com.wefly.wealert.services.OfflineService;
import com.wefly.wealert.tracking.NavigationService;
import com.wefly.wealert.events.BeforeSendAlertEvent;
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
import com.wefly.wealert.utils.AppController;
import com.wefly.wealert.R;
import com.wefly.wealert.utils.PathUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.reactivestreams.Publisher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import io.objectbox.Box;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout;
import nl.psdcompany.duonavigationdrawer.views.DuoMenuView;
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle;
import rx.functions.Action1;

import static android.support.v4.graphics.TypefaceCompatUtil.getTempFile;

public class BootActivity extends AppCompatActivity {
    AppController appController = AppController.getInstance();
    private static List<Piece> pieces = new ArrayList<>();
    View vForm, vRecipient;
    List<View> fragments = new ArrayList<>();
    //private LinearLayout pieceLayout;
    private GridView pieceLayout;
    static ListView recipientList;
    private static Alert alert = new Alert();
    private android.support.design.widget.FloatingActionButton recordBtn;
    private Button btnSend, nextBtn;
    private ImageButton addPicBtn;
    private EditText edObject, edContent;
    static ViewPager viewPager;
    CircularProgressIndicator piece_loader;
    static LinearLayout piece_loader_content;
    Toolbar toolbar;
    Spinner category;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onStart() {
        super.onStart();
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
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(broadcastReceiver);
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
                sp.edit().remove(key).apply();
        }

        ensureLocationSettings();

        vForm = LayoutInflater.from(getBaseContext()).inflate(R.layout.fragment_send, null);
        vRecipient = LayoutInflater.from(getBaseContext()).inflate(R.layout.fragment_recipient, null);

        fragments.add(vForm);
        fragments.add(vRecipient);

        pieceLayout = vForm.findViewById(R.id.pieceToSend);


        recordBtn = vForm.findViewById(R.id.recordBtn);
        nextBtn = vForm.findViewById(R.id.nextBtn);
        btnSend = vRecipient.findViewById(R.id.btnSend);
        addPicBtn = vForm.findViewById(R.id.addPic);
        edObject = vForm.findViewById(R.id.objectEdText);
        edContent = vForm.findViewById(R.id.contentEdText);
        category = vForm.findViewById(R.id.categorySpinner);

        Log.e(getLocalClassName(), "Title txt:" + edObject.getText().toString());

        Box<com.wefly.wealert.dbstore.Recipient> recipientBox = AppController.boxStore.boxFor(com.wefly.wealert.dbstore.Recipient.class);
        if (recipientBox.count() == 0) {
            loadRecipientRx();
        } else {
            List<Recipient> recList = new ArrayList<>();
            for (com.wefly.wealert.dbstore.Recipient r : recipientBox.getAll()) {
                Recipient item = new Recipient();
                item.setAvatarUrl(r.getAvatar());
                item.setUserName(r.getUsername());
                item.setRecipientId(r.getRaw_id());
                recList.add(item);
            }
            recipientList = vRecipient.findViewById(R.id.recipientList);
            recipientList.setAdapter(new RecipientAdapter(getApplicationContext(), recList));
        }

        Box<com.wefly.wealert.dbstore.Category> categoryBox = AppController.boxStore.boxFor(com.wefly.wealert.dbstore.Category.class);
        if (categoryBox.count() == 0) {
            loadCategorieRx();
        } else {
            List<String> categorielist = new ArrayList<>();
            for (Category c : categoryBox.getAll()) {
                categorielist.add(c.getLabel());
            }
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, categorielist);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            category.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            category.setAdapter(spinnerAdapter);
        }

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
            @SuppressLint("CheckResult")
            @Override
            public void onClick(View v) {
                ensureLocationSettings();
                ReactiveNetwork.checkInternetConnectivity()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                if (aBoolean) {
                                    SendRx();
                                } else {
                                    showStoreAlertDialog();
                                }
                            }
                        });
            }
        });

        addPicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        edContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkAlertIsNull();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        edObject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkAlertIsNull();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getApplicationContext(), fragments);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 1) {
                    checkAlertIsNull();
                }
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

    private void checkAlertIsNull() {
        if (alert == null) {
            viewPager.setCurrentItem(0);
        }
        if (edObject.getText().toString().trim().length() == 0) {
            viewPager.setCurrentItem(0);
            Snackbar.make(vForm, R.string.empty_object, Snackbar.LENGTH_LONG).show();
            edObject.setBackgroundColor(Color.RED);
        } else {
            edObject.setBackgroundColor(Color.WHITE);
        }

        if (edContent.getText().toString().trim().length() == 0) {
            viewPager.setCurrentItem(0);
            Snackbar.make(vForm, R.string.empty_content, Snackbar.LENGTH_LONG).show();
            edContent.setBackgroundColor(Color.RED);
        } else {
            edContent.setBackgroundColor(Color.WHITE);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

//        Intent chooser = getPickImageIntent(getApplicationContext());
//        if (chooser.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(chooser, PICK_IMAGE_REQUEST);
//        }
    }

    @SuppressLint("RestrictedApi")
    public static Intent getPickImageIntent(Context context) {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(context)));
        intentList = addIntentsToList(context, intentList, pickIntent);
        intentList = addIntentsToList(context, intentList, takePhotoIntent);

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                    context.getString(R.string.pick_image_intent_text));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        return chooserIntent;
    }

    private static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
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
            pieces = getStoredPieces();
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), imageBitmap, "Title", null);
            String filepath = PathUtil.getPath(getApplicationContext(), Uri.parse(path));

            Log.e("Picture path", filepath);

            galleryAddPic(filepath);

            Piece p = new Piece();
            p.setIndex(UUID.randomUUID().toString());
            p.setUrl(filepath);

            pieces.add(p);

            storePieces();

            //pieceLayout.removeAllViews();
            FillPieceLayout();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == 0) {
            restart();
        } else if (resultCode == 200 && requestCode == 352 && data != null) {
            pieces = getStoredPieces();
            Piece audio = new Piece();
            String url = data.getExtras().getString("audioPath");
            audio.setIndex(UUID.randomUUID().toString());

            Log.e("OnResult Audio index", String.valueOf(audio.getIndex()));

            audio.setUrl(url);

            pieces.add(audio);

            storePieces();

            ImageView audioimage = new ImageView(getApplicationContext());
            audioimage.setImageResource(R.drawable.microphone);
            audioimage.setTag(audio.getIndex());
            audioimage.setLayoutParams(new LinearLayout.LayoutParams(150, 150));
            audioimage.setScaleType(ImageView.ScaleType.FIT_XY);
            recordBtn.setClickable(false);
            recordBtn.setEnabled(false);

            audioimage.setOnClickListener(v -> {
                Toast.makeText(getApplicationContext(), "AlertDataPiece removed!", Toast.LENGTH_SHORT).show();
                String index = v.getTag().toString();
                Log.e("audio clicked index", String.valueOf(index));
                List<Piece> tmp = new ArrayList<>();
                tmp.addAll(pieces);
                for (Piece p : tmp) {
                    Log.v("clicked index", String.valueOf(index));
                    Log.v("stored index", String.valueOf(p.getIndex()));
                    if (p.getIndex().equals(index)) {
                        pieces.remove(p);
                        storePieces();
                    }
                    recordBtn.setClickable(true);
                    recordBtn.setEnabled(true);
                }
                Log.v("piece size removed", String.valueOf(pieces.size()));
                pieceLayout.removeView(v);
                EventBus.getDefault().post(new PieceRemoveEvent(pieces.size()));
            });
            FillPieceLayout();
        }

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Log.e(getLocalClassName(), "back pressed");
        storePieces();
        pieceLayout.invalidateViews();
        //dispatchTakePictureIntent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //deleteStoredPieces();
        Log.e(getLocalClassName(), "destroyed");
    }

    protected void storePieces() {
        FastSave.getInstance().saveObjectsList("alertDataPieces", pieces);
    }

    protected List<Piece> getStoredPieces() {
        List<Piece> list = new ArrayList<>();
        if (FastSave.getInstance().isKeyExists("alertDataPieces")) {
            list = FastSave.getInstance().getObjectsList("alertDataPieces", Piece.class);
        }
        return list;
    }

    protected void deleteStoredPieces() {
        FastSave.getInstance().deleteValue("alertDataPieces");
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
        mMenuOptions.add("Adjust Work Period");
        mMenuOptions.add("Terms and conditions");
        mMenuOptions.add("Policy privacy");
        mMenuOptions.add("About");
        mMenuOptions.add("Logout");
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
                        Toast.makeText(getApplicationContext(), String.valueOf(position) + "Not Implemented Yet!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(BootActivity.this, AlertListActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(BootActivity.this, WorkRangeActivity.class).addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
                        break;
                    case 3:
                        sp.edit().putString("option_label", "Terms").apply();
                        startActivity(new Intent(BootActivity.this, MenuActivity.class));
                        break;
                    case 4:
                        sp.edit().putString("option_label", "Policy").apply();
                        startActivity(new Intent(BootActivity.this, MenuActivity.class));
                        break;
                    case 5:
                        sp.edit().putString("option_label", "About").apply();
                        startActivity(new Intent(BootActivity.this, MenuActivity.class));
                        break;
                    case 6:
                        clearAppData();
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0);
                        break;
                    case 7:
                        finishAffinity();
                }
            }
        });
    }

    /****************SendFragment*********************/
    private void FillPieceLayout() {
        //Fill Image View
        PieceAdapter pieceAdapter = new PieceAdapter(getApplicationContext(), getStoredPieces());
        //pieceAdapter.notifyDataSetChanged();
        pieceLayout.setAdapter(pieceAdapter);
    }

    private void showStoreAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set title
        alertDialogBuilder.setTitle(R.string.store_alert_title);

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.store_alert_msg)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        StoreAlert();
                        deleteStoredPieces();
                        restart();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void LaunchRecord() {
        Log.v("Audio recordbtn", "clicked");
        Intent recorder = new Intent(this, RecorderActivity.class);
        startActivityForResult(recorder, 352);
    }

    /****************SendFragment*********************/

    private void saveInput() throws NullPointerException {

        if (alert != null && edObject.getText().toString().trim().length() != 0 && edContent.getText().toString().trim().length() != 0) {
            alert.setObject(edObject.getText().toString().trim());
            alert.setContent(edContent.getText().toString().trim());
            alert.setCategory(category.getSelectedItem().toString());
            Log.e("Selected Category", alert.getCategory());
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

        SharedPreferences sp = getSharedPreferences("recipients", 0);
        Set recipient_ids = sp.getStringSet("recipients_id", new HashSet<String>());

        List<String> list = new ArrayList<String>(recipient_ids);

        a.setTitle(edObject.getText().toString());
        a.setContent(edContent.getText().toString());
        a.setCategory(category.getSelectedItem().toString());

        for (String s : list) {
            com.wefly.wealert.dbstore.OtherRecipient r = new com.wefly.wealert.dbstore.OtherRecipient();
            r.setRaw_id(Integer.parseInt(s));
            a.otherRecipients.add(r);
        }

        for (Piece p : pieces) {
            piece.setUrl(p.getUrl());
            a.pieces.add(piece);
        }

        long id = box.put(a);
        if (id != 0) {
            Snackbar.make(vRecipient, "Alert successfully saved!", Snackbar.LENGTH_LONG).show();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecipientEmptyEvent(RecipientEmptyEvent event) {
        viewPager.setCurrentItem(1);
        Snackbar.make(vRecipient, event.message, Snackbar.LENGTH_LONG).show();
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
    private void ensureLocationSettings() {
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

                    FastSave.getInstance().saveString("lat", String.valueOf(location.getLatitude()));
                    FastSave.getInstance().saveString("long", String.valueOf(location.getLongitude()));

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


    public void loadCategorieRx() {
        Observer mObserver = new Observer<String>() {
            List<String> categorielist = new ArrayList<>();
            Box<com.wefly.wealert.dbstore.Category> categoryBox = appController.boxStore.boxFor(com.wefly.wealert.dbstore.Category.class);

            @Override
            public void onSubscribe(Disposable d) {
                //Toast.makeText(BootActivity.this, "onSubscribe called", Toast.LENGTH_SHORT).show();
                categoryBox.removeAll();
            }

            @Override
            public void onNext(String s) {
                //Toast.makeText(BootActivity.this, "onNext called: " + s, Toast.LENGTH_SHORT).show();
                categorielist.add(s);
                Category c = new Category();
                c.setLabel(s);
                categoryBox.put(c);
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
            Box<com.wefly.wealert.dbstore.Recipient> recipientBox = appController.boxStore.boxFor(com.wefly.wealert.dbstore.Recipient.class);

            @Override
            public void onSubscribe(Disposable d) {
                // Toast.makeText(BootActivity.this, "onSubscribe called", Toast.LENGTH_SHORT).show();
                recipientBox.removeAll();
            }

            @Override
            public void onNext(Recipient r) {
                //Toast.makeText(BootActivity.this, "onNext called: " + r.getLastName(), Toast.LENGTH_SHORT).show();
                recipients.add(r);

                com.wefly.wealert.dbstore.Recipient recipient = new com.wefly.wealert.dbstore.Recipient();
                recipient.setRaw_id(r.getRecipientId());
                recipient.setAvatar(r.getAvatarUrl());
                recipient.setUsername(r.getUserName());
                recipientBox.put(recipient);
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
        IOSDialog dialog = LoaderProgress("Alert", "Sending...");
        if (hasRecipientsID()) {
            Observer mObserver = new Observer<Boolean>() {
                @Override
                public void onSubscribe(Disposable d) {
                    //Toast.makeText(BootActivity.this, "Sending Alert...", Toast.LENGTH_SHORT).show();
                    dialog.show();
                }

                @Override
                public void onNext(Boolean r) {

                }

                @Override
                public void onError(Throwable e) {
                    Toast.makeText(BootActivity.this, "Alert send Error", Toast.LENGTH_SHORT).show();
                    showStoreAlertDialog();
                }

                @Override
                public void onComplete() {
                    showNotification("Alert Sended!");
                    dialog.dismiss();

                    SharedPreferences sp = getSharedPreferences("recipients", 0);
                    sp.edit().remove("recipients_id").apply();

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

    private void uploadRx() {
        IOSDialog dialog = LoaderProgress("AlertDataPiece", "Uploading...");
        Observer mObserver = new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
                //Toast.makeText(BootActivity.this, "Uploading piece...", Toast.LENGTH_SHORT).show();
                dialog.show();
            }

            @Override
            public void onNext(Boolean r) {

            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(BootActivity.this, "AlertDataPiece send Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                //Toast.makeText(BootActivity.this, "AlertDataPiece Sended!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                showNotification("AlertDataPiece Sended!");
                deleteStoredPieces();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1000);
            }
        };
        Observable<Boolean> observable = new PieceUploadObservable().upload(pieces);

        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(mObserver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Double latitude = Double.valueOf(intent.getStringExtra("latutide"));
            Double longitude = Double.valueOf(intent.getStringExtra("longitude"));
            Log.e("Latitude2", latitude + "");
            Log.e("Longitude2", longitude + "");
        }
    };

    private void startTracking() {
        //AlarmManager manager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        //Calendar calendar = Calendar.getInstance();
        //calendar.set(Calendar.SECOND,1);
        Intent intent = new Intent(getApplicationContext(), NavigationService.class);
        Intent offline = new Intent(getApplicationContext(), OfflineService.class);
        // manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,calendar.getTimeInMillis(),manager.INTERVAL_HALF_HOUR,intent);
        stopService(intent);
        stopService(offline);

        startService(intent);
        startService(offline);
    }

    private void clearAppData() {
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData(); // note: it has a return value!
            } else {
                String packageName = getApplicationContext().getPackageName();
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("pm clear " + packageName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showNotification(String msg) {
        //Get an instance of NotificationManager//
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_logo)
                        .setContentTitle("Wefly")
                        .setContentText(msg);

        // Gets an instance of the NotificationManager service//

        NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

//        NotificationManager.notify().
        mNotificationManager.notify(001, mBuilder.build());
    }

    public IOSDialog LoaderProgress(String title, String content) {
        final IOSDialog dialog = new IOSDialog.Builder(BootActivity.this)
                .setTitle(title)
                .setMessageContent(content)
                .setSpinnerColorRes(R.color.colorPrimary)
                .setCancelable(false)
                .setTitleColorRes(R.color.white)
                .setMessageContentGravity(Gravity.END)
                .build();
        return dialog;
    }
}
