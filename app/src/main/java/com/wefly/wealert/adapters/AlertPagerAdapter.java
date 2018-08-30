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

import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.gmail.samehadar.iosdialog.IOSDialog;
import com.wefly.wealert.R;
import com.wefly.wealert.activities.BootActivity;
import com.wefly.wealert.dbstore.AlertData;
import com.wefly.wealert.dbstore.Category;
import com.wefly.wealert.dbstore.Category_;
import com.wefly.wealert.dbstore.Piece;
import com.wefly.wealert.models.Recipient;
import com.wefly.wealert.observables.RecipientsListObservable;
import com.wefly.wealert.services.APIClient;
import com.wefly.wealert.services.APIService;
import com.wefly.wealert.services.models.AlertDataCategory;
import com.wefly.wealert.services.models.AlertDataPiece;
import com.wefly.wealert.services.models.AlertDataRecipient;
import com.wefly.wealert.services.models.AlertResponse;
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

public class AlertPagerAdapter extends PagerAdapter {
    private List<View> fragmentList;
    private Context context;
    ListView list;

    public AlertPagerAdapter(Context context, List<View> fragments) {
        this.context = context;
        fragmentList = fragments;
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
    public Object instantiateItem(final ViewGroup container, final int position) {
        View view = null;
        switch (position) {
            case 0:
                //Alert Sent fragment
                view = fragmentList.get(0);
                list = view.findViewById(R.id.sent_alert_list);
                ReactiveNetwork.checkInternetConnectivity()
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                if (aBoolean) {
                                    loadRecipientRx();
                                } else {
                                    LocalAlertSentListRX();
                                }
                            }
                        });

                break;
            case 1:
                view = fragmentList.get(1);
                break;
            default:
                view = fragmentList.get(0);
        }
        container.addView(view);
        return view;
    }


    private void LocalAlertSentListRX() {
        List<com.wefly.wealert.services.models.AlertData> localList = new ArrayList<>();
        localList.clear();
        Box<AlertData> alertSentBox = AppController.boxStore.boxFor(AlertData.class);
        Box<com.wefly.wealert.dbstore.Category> categoryBox = AppController.boxStore.boxFor(com.wefly.wealert.dbstore.Category.class);

        for (com.wefly.wealert.dbstore.AlertData r : alertSentBox.getAll()) {
            com.wefly.wealert.services.models.AlertData item = new com.wefly.wealert.services.models.AlertData();
            item.setId(r.getRaw_id());
            item.setContenu(r.getContenu());
            item.setTitre(r.getTitre());
            item.setDate_de_creation(r.getDate_de_creation());
            item.setLatitude(r.getLatitude());
            item.setLongitude(r.getLongitude());

            Category c=categoryBox.query().equal(Category_.raw_id,r.getCategory_id()).build().findFirst();
            Toast.makeText(context, "LOCAL CATEGORY:" +c.getLabel(), Toast.LENGTH_LONG).show();

            AlertDataCategory category=new AlertDataCategory();
            category.setNom(c.getLabel());
            category.setId(c.getRaw_id());

            item.category=category;
            localList.add(item);
        }
        list.setAdapter(new AlertListAdapter(context, localList));
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
                Toast.makeText(context, "Error called: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                //Toast.makeText(context, "Recipients total" + recipientBox.count(), Toast.LENGTH_SHORT).show();
                RemoteAlertSentListRX();
            }
        };

        APIService service = APIClient.getClient().create(APIService.class);
        Observable<RecipientResponse> observable = service.RecipientsList("JWT " + AppController.getInstance().getToken());
        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(mObserver);
    }

    private void RemoteAlertSentListRX() {
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
                Toast.makeText(context, "OBS error" + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("OBS error: ", e.getMessage());
            }

            @Override
            public void onComplete() {
                //Toast.makeText(context, "Data total" + remotelist.size(), Toast.LENGTH_LONG).show();
                process(remotelist);
                //Toast.makeText(context, "Pieces total" + piecebox.getAll().size(), Toast.LENGTH_LONG).show();
                list.setAdapter(new AlertListAdapter(context, remotelist));
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
                Toast.makeText(context, "Category ID" + c.getRaw_id(), Toast.LENGTH_LONG).show();
                if (c.getRaw_id()==(x.category.getId())) {
                    item.setCategory_id(x.category.getId());
                    Toast.makeText(context, "Category added" + c.getLabel(), Toast.LENGTH_LONG).show();
                }
            }

            //Toast.makeText(context, "json pieces count" + x.getAlertDataPieces().size(), Toast.LENGTH_LONG).show();

            for (AlertDataPiece p : x.getAlertDataPieces()) {
                Piece n = new Piece();
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
