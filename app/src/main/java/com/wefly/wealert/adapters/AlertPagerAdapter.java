package com.wefly.wealert.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.gmail.samehadar.iosdialog.IOSDialog;
import com.wefly.wealert.R;
import com.wefly.wealert.activities.BootActivity;
import com.wefly.wealert.dbstore.AlertData;
import com.wefly.wealert.services.APIClient;
import com.wefly.wealert.services.APIService;
import com.wefly.wealert.services.models.AlertResponse;
import com.wefly.wealert.utils.AppController;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AlertPagerAdapter extends PagerAdapter {
private List<View> fragmentList;
    private Context context;
    ListView list;
    public AlertPagerAdapter(Context context, List<View> fragments) {
        this.context = context;
        fragmentList=fragments;
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(final ViewGroup container,final int position) {
        View view = null;
        switch (position) {
            case 0:
                //Alert Sent fragment
                view = fragmentList.get(0);
                list=view.findViewById(R.id.sent_alert_list);
                AlertSentListRX();
                break;
            case 1:
                view=fragmentList.get(1);
                break;
                default:
                    view=fragmentList.get(0);
        }
        container.addView(view);
        return view;
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
                        Toast.makeText(context, "ALert count" + Alertbox.getAll().size(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(context, "OBS error" + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("OBS error: ", e.getMessage());
            }

            @Override
            public void onComplete() {
                Toast.makeText(context, "ALert count" + Alertbox.getAll().size(), Toast.LENGTH_LONG).show();
                list.setAdapter(new AlertListAdapter(context,alertDataList));
            }
        };

        APIService service = APIClient.getClient().create(APIService.class);
        Observable<AlertResponse> observable = service.AlertSentList("JWT " + AppController.getInstance().getToken());
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(mObserver);
    }

}
