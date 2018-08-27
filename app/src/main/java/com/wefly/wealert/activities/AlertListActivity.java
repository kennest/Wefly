package com.wefly.wealert.activities;

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

import com.gmail.samehadar.iosdialog.IOSDialog;
import com.wefly.wealert.R;
import com.wefly.wealert.adapters.AlertPagerAdapter;
import com.wefly.wealert.adapters.DrawerListAdapter;
import com.wefly.wealert.dbstore.AlertData;
import com.wefly.wealert.services.APIClient;
import com.wefly.wealert.services.models.AlertResponse;
import com.wefly.wealert.services.APIService;
import com.wefly.wealert.utils.AppController;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout;
import nl.psdcompany.duonavigationdrawer.views.DuoMenuView;
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle;

public class AlertListActivity extends AppCompatActivity {
    AppController appController = AppController.getInstance();
    List<AlertData> StorealertDataList = new ArrayList<>();
    List<com.wefly.wealert.services.models.AlertData> alertDataList = new ArrayList<>();
    FloatingActionButton new_alert;
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_list_activity);

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

    protected void AlertSentListRX() {
        Observer mObserver = new Observer<AlertResponse>() {
            Box<AlertData> Alertbox = AppController.boxStore.boxFor(AlertData.class);
            List<com.wefly.wealert.services.models.AlertData> alertDataList = new ArrayList<>();

            @Override
            public void onSubscribe(Disposable disposable) {
                Alertbox.removeAll();
            }

            @Override
            public void onNext(AlertResponse response) {
                for (com.wefly.wealert.services.models.AlertData x : response.getData()) {
                    alertDataList.add(x);
                    for (com.wefly.wealert.services.models.AlertData d : response.getData()) {
                        AlertData item = new AlertData();
                        item.setContenu(d.getContenu());
                        item.setTitre(d.getTitre());
                        item.setDate_de_creation(d.getDate_de_creation());
                        item.setLatitude(d.getLatitude());
                        item.setLongitude(d.getLongitude());
                        Alertbox.put(item);
                        Toast.makeText(getApplicationContext(), "ALert count" + Alertbox.getAll().size(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getApplicationContext(), "OBS error" + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("OBS error: ", e.getMessage());
            }

            @Override
            public void onComplete() {
                Toast.makeText(getApplicationContext(), "ALert count" + Alertbox.getAll().size(), Toast.LENGTH_LONG).show();
            }
        };

        APIService service = APIClient.getClient().create(APIService.class);
        Observable<AlertResponse> observable = service.AlertSentList("JWT " + AppController.getInstance().getToken());
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(mObserver);
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

}
