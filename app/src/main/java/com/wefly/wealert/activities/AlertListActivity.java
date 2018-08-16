package com.wefly.wealert.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.wefly.wealert.R;
import com.wefly.wealert.adapters.AlertPagerAdapter;
import com.wefly.wealert.fragments.ALertSentFragment;
import com.wefly.wealert.fragments.AlertReceiveFragment;
import com.wefly.wealert.services.APIClient;
import com.wefly.wealert.services.models.AlertData;
import com.wefly.wealert.services.models.AlertResponse;
import com.wefly.wealert.services.APIService;
import com.wefly.wealert.utils.AppController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AlertListActivity extends AppCompatActivity {
    AppController appController = AppController.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_list_activity);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Alert Sent"));
        tabLayout.addTab(tabLayout.newTab().setText("Alert Receive"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.alertpager);

        ALertSentFragment sentFragment = new ALertSentFragment();
        AlertReceiveFragment receiveFragment = new AlertReceiveFragment();

        List<android.support.v4.app.Fragment> fragmentList = new ArrayList<>();

        fragmentList.add(sentFragment);
        fragmentList.add(receiveFragment);

        final AlertPagerAdapter adapter = new AlertPagerAdapter(getSupportFragmentManager(), fragmentList);
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
        testRX();
    }

    protected void testRX() {
        APIService service = APIClient.getClient().create(APIService.class);
        Observable<AlertResponse> observable = service.AlertSentList("JWT " + appController.getToken());
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .timeout(10, TimeUnit.SECONDS)
                .subscribe(new Observer<AlertResponse>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(AlertResponse s) {
                        for(AlertData d:s.getData()) {
                            Toast.makeText(getApplicationContext(), "Alert list titre" + d.getDate_de_creation(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getApplicationContext(), "OBS error" + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("OBS error",e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
