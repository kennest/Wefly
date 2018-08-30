package com.wefly.wealert.activities;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.gmail.samehadar.iosdialog.IOSDialog;
import com.wefly.wealert.R;
import com.wefly.wealert.adapters.AlertPagerAdapter;
import com.wefly.wealert.adapters.DrawerListAdapter;
import com.wefly.wealert.adapters.RecipientAdapter;
import com.wefly.wealert.dbstore.AlertData;
import com.wefly.wealert.dbstore.Category;
import com.wefly.wealert.models.Recipient;
import com.wefly.wealert.observables.RecipientsListObservable;
import com.wefly.wealert.services.APIClient;
import com.wefly.wealert.services.models.AlertDataCategory;
import com.wefly.wealert.services.models.AlertDataPiece;
import com.wefly.wealert.services.models.AlertDataRecipient;
import com.wefly.wealert.services.models.AlertResponse;
import com.wefly.wealert.services.APIService;
import com.wefly.wealert.services.models.CategoriesResponse;
import com.wefly.wealert.services.models.RecipientResponse;
import com.wefly.wealert.utils.AppController;

import java.util.ArrayList;
import java.util.List;

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

public class AlertListActivity extends AppCompatActivity {
    FloatingActionButton new_alert;
    Toolbar toolbar;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_list_activity);

        ReactiveNetwork.checkInternetConnectivity()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            SyncData();
                        }
                    }
                });

        new_alert = findViewById(R.id.new_alert);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Alert Sent"));
        tabLayout.addTab(tabLayout.newTab().setText("Alert Receive"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.alertpager);

        View vSent = getLayoutInflater().inflate(R.layout.fragment_sent_alert, null);
        View vReceive = getLayoutInflater().inflate(R.layout.fragment_sent_alert, null);

        List<View> fragmentList = new ArrayList<>();

        fragmentList.add(vSent);
        fragmentList.add(vReceive);

        final AlertPagerAdapter adapter = new AlertPagerAdapter(getApplicationContext(), fragmentList);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        new_alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendNewAlert();
            }
        });
        InitSideMenu();
    }

    protected void SendNewAlert() {
        Intent intent = new Intent(getApplicationContext(), BootActivity.class);
        startActivity(intent);
    }

    private void InitSideMenu() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("WeFly Home");
        DuoDrawerLayout drawerLayout = (DuoDrawerLayout) findViewById(R.id.drawerlayout);
        DuoDrawerToggle drawerToggle = new DuoDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        ArrayList<String> mMenuOptions = new ArrayList<>();
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
                        startActivity(new Intent(AlertListActivity.this, WorkRangeActivity.class).addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
                        break;
                    case 1:
                        sp.edit().putString("option_label", "Terms").apply();
                        startActivity(new Intent(AlertListActivity.this, MenuActivity.class));
                        break;
                    case 2:
                        sp.edit().putString("option_label", "Policy").apply();
                        startActivity(new Intent(AlertListActivity.this, MenuActivity.class));
                        break;
                    case 3:
                        sp.edit().putString("option_label", "About").apply();
                        startActivity(new Intent(AlertListActivity.this, MenuActivity.class));
                        break;
                    case 4:
                        clearAppData();
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0);
                        break;
                    case 5:
                        finishAffinity();
                }
            }
        });
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

    private void SyncData(){
        loadCategorieRx();
        loadRecipientRx();
        loadAlertSentListRX();
    }

    public void loadCategorieRx() {
        Observer mObserver = new Observer<CategoriesResponse>() {
            Box<Category> categoryBox = AppController.boxStore.boxFor(com.wefly.wealert.dbstore.Category.class);

            @Override
            public void onSubscribe(Disposable d) {
                //Toast.makeText(BootActivity.this, "onSubscribe called", Toast.LENGTH_SHORT).show();
                categoryBox.removeAll();
            }

            @Override
            public void onNext(CategoriesResponse r) {
                for(AlertDataCategory s:r.categories) {
                    Toast.makeText(AlertListActivity.this, "Category ID: " + s.getId(), Toast.LENGTH_SHORT).show();
                    Category c = new Category();
                    c.id = categoryBox.count() + 1;
                    c.setLabel(s.getNom());
                    c.setRaw_id(s.getId());
                    categoryBox.put(c);
                }
            }

            @Override
            public void onError(Throwable e) {
                // Toast.makeText(BootActivity.this, "onError called", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                Toast.makeText(getApplicationContext(), "INIT Categories total"+categoryBox.count(), Toast.LENGTH_SHORT).show();
            }
        };

        APIService service = APIClient.getClient().create(APIService.class);
        Observable<CategoriesResponse> observable = service.CategoriesList("JWT " + AppController.getInstance().getToken());
        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(mObserver);
    }

    private void loadRecipientRx() {
        Observer mObserver = new Observer<RecipientResponse>() {
            Box<com.wefly.wealert.dbstore.Recipient> recipientBox = AppController.boxStore.boxFor(com.wefly.wealert.dbstore.Recipient.class);

            @Override
            public void onSubscribe(Disposable d) {
                // Toast.makeText(BootActivity.this, "onSubscribe called", Toast.LENGTH_SHORT).show();
                recipientBox.removeAll();
            }

            @Override
            public void onNext(RecipientResponse response) {
                //Toast.makeText(BootActivity.this, "onNext called: " + r.getLastName(), Toast.LENGTH_SHORT).show();
                for(AlertDataRecipient r:response.recipients) {
                    com.wefly.wealert.dbstore.Recipient recipient = new com.wefly.wealert.dbstore.Recipient();
                    recipient.id = recipientBox.count() + 1;
                    recipient.setRaw_id(r.getId());
                    recipient.setAvatar(r.getPhoto());
                    recipient.setUsername(r.getUser().getUsername());
                    recipient.setFirstname(r.user.getFirstname());
                    recipient.setLastname(r.user.getLastname());
                    recipientBox.put(recipient);
                }
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getApplicationContext(), "Error called: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                Toast.makeText(getApplicationContext(), "INIT Recipients total" + recipientBox.count(), Toast.LENGTH_SHORT).show();
            }
        };

        APIService service = APIClient.getClient().create(APIService.class);
        Observable<RecipientResponse> observable = service.RecipientsList("JWT " + AppController.getInstance().getToken());
        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(mObserver);
    }

    private void loadAlertSentListRX() {
        Observer mObserver = new Observer<AlertResponse>() {
            Box<AlertData> Alertbox = AppController.boxStore.boxFor(AlertData.class);
            List<com.wefly.wealert.services.models.AlertData> remotelist = new ArrayList<>();

            @Override
            public void onSubscribe(Disposable disposable) {
                //Toast.makeText(context, "alerts sent download Init" + Alertbox.getAll().size(), Toast.LENGTH_LONG).show();
                Alertbox.removeAll();
            }

            @Override
            public void onNext(AlertResponse response) {
                remotelist.clear();
                remotelist.addAll(response.getData());
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getApplicationContext(), "OBS error" + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("OBS error: ", e.getMessage());
            }

            @Override
            public void onComplete() {
                //Toast.makeText(context, "Data total" + remotelist.size(), Toast.LENGTH_LONG).show();
                process(remotelist);
                Toast.makeText(getApplicationContext(), "INIT Alerts total" + Alertbox.count(), Toast.LENGTH_LONG).show();
            }
        };

        APIService service = APIClient.getClient().create(APIService.class);
        Observable<AlertResponse> observable = service.AlertSentList("JWT " + AppController.getInstance().getToken());
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribe(mObserver);
    }
    private void process(List<com.wefly.wealert.services.models.AlertData> list){
        Box<AlertData> Alertbox = AppController.boxStore.boxFor(AlertData.class);
        Box<com.wefly.wealert.dbstore.Recipient> recipientbox = AppController.boxStore.boxFor(com.wefly.wealert.dbstore.Recipient.class);
        Box<com.wefly.wealert.dbstore.Piece> piecebox = AppController.boxStore.boxFor(com.wefly.wealert.dbstore.Piece.class);
        Box<com.wefly.wealert.dbstore.Category> categoryBox = AppController.boxStore.boxFor(com.wefly.wealert.dbstore.Category.class);

        for (com.wefly.wealert.services.models.AlertData x : list) {
            //Toast.makeText(context, "Recipients Data count" +x.getDestinataires().size(), Toast.LENGTH_LONG).show();

            AlertData item = new AlertData();
            List<com.wefly.wealert.dbstore.Recipient> recipients=new ArrayList<>();

            item.id = Alertbox.count() + 1;
            item.setContenu(x.getContenu());
            item.setRaw_id(x.getId());
            item.setTitre(x.getTitre());
            item.setDate_de_creation(x.getDate_de_creation());
            item.setLatitude(x.getLatitude());
            item.setLongitude(x.getLongitude());

            for (AlertDataRecipient r : x.getDestinataires()) {
                for (com.wefly.wealert.dbstore.Recipient e : recipientbox.getAll()) {
                    //Toast.makeText(context, "Current recipient "+r.getUser().getUsername()+" *Comparing: "+r.getId()+"/"+e.getRaw_id(), Toast.LENGTH_LONG).show();
                    if (r.getId() == e.getRaw_id()) {
                        recipients.add(e);
                    }
                }
            }

            for (com.wefly.wealert.dbstore.Category c : categoryBox.getAll()) {
                Toast.makeText(getApplicationContext(), "Category ID:" + c.getRaw_id(), Toast.LENGTH_LONG).show();
                if (c.getRaw_id()==(x.category.getId())) {
                    item.setCategory_id(x.category.getId());
                    Toast.makeText(getApplicationContext(), "Category added" + c.getLabel(), Toast.LENGTH_LONG).show();
                }
            }

            //Toast.makeText(context, "json pieces count" + x.getAlertDataPieces().size(), Toast.LENGTH_LONG).show();

            for (AlertDataPiece p : x.getAlertDataPieces()) {
                com.wefly.wealert.dbstore.Piece n = new com.wefly.wealert.dbstore.Piece();
                n.setId(p.getId());
                n.setUrl(p.getPiece());
                Alertbox.attach(item);
                if (item.pieces.add(n)) {
                    //Toast.makeText(context, "json piece added" + p.getId(), Toast.LENGTH_LONG).show();
                }
            }
            //Toast.makeText(context, "recipients find count" + recipients.size(), Toast.LENGTH_LONG).show();
            List<Integer> ids=new ArrayList<>();
            for(com.wefly.wealert.dbstore.Recipient r:recipients){
                Alertbox.attach(item);
                ids.add(r.getRaw_id());
            }
            item.setRecipients(ids.toString());

            //Toast.makeText(context,"Current Ids:"+item.getRecipients(),Toast.LENGTH_LONG).show();

            Alertbox.put(item);
        }
    }

}
