package com.wefly.wealert.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.widget.Toast;

import com.appizona.yehiahd.fastsave.FastSave;
import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.wefly.wealert.activities.BootActivity;
import com.wefly.wealert.activities.onboardActivity;
import com.wefly.wealert.adapters.AlertListAdapter;
import com.wefly.wealert.dbstore.AlertData;
import com.wefly.wealert.dbstore.MyObjectBox;
import com.wefly.wealert.models.Piece;
import com.wefly.wealert.models.Recipient;
import com.wefly.wealert.services.APIClient;
import com.wefly.wealert.services.APIService;
import com.wefly.wealert.services.models.AlertResponse;
import com.wefly.wealert.services.models.EmployeId;
import com.wefly.wealert.tasks.RecipientGetTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;
import io.objectbox.android.BuildConfig;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by admin on 01/06/2018.
 */

public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();

    private static AppController mInstance;
    private static ArrayList<Activity> activitiesList = new ArrayList<>();
    private static ArrayList<AsyncTask<Void, Integer, Boolean>> tasksList = new ArrayList<>();
    private String token = "";
    private static List<Piece> pieceList = new ArrayList<>();
    private static String audioPath;
    public Map<String, Integer> alert_categories = new HashMap();
    private CopyOnWriteArrayList<Recipient> recipientsList = new CopyOnWriteArrayList<>();
    List<Recipient> recipients = new ArrayList<>();
    public Double latitude;
    public Double longitude;
    public static BoxStore boxStore;
    List<com.wefly.wealert.services.models.AlertData> alertDataList = new ArrayList<>();

    public List<Recipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<Recipient> recipients) {
        this.recipients = recipients;
    }

    public CopyOnWriteArrayList<Recipient> getRecipientsList() {
        return recipientsList;
    }

    public void setRecipientsList(CopyOnWriteArrayList<Recipient> recipientsList) {
        this.recipientsList = recipientsList;
    }

    public static List<Piece> getPieceList() {
        return pieceList;
    }

    public static void setPieceList(List<Piece> pieceList) {
        AppController.pieceList = pieceList;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        //Init ObjectBox
        boxStore = MyObjectBox.builder().androidContext(this).build();
        if (BuildConfig.DEBUG) {
            boolean started = new AndroidObjectBrowser(boxStore).start(getApplicationContext());
            Log.i("ObjectBrowser", "Started: " + started);
        }
        FastSave.init(getApplicationContext());
        getEmployeID();

        //GET RECIPIENTS LIST
        try {
            recipients = new RecipientGetTask().execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        // Support vector
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        //FIXE CAMERA Bug on Api 24+
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }


    public void restartApp() {
        Intent intent = new Intent(getApplicationContext(), onboardActivity.class);
        int mPendingIntentId = 320;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    public void reloadApp() {
        Intent intent = new Intent(getApplicationContext(), BootActivity.class);
        int mPendingIntentId = 320;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    public void quitApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    public static void addToDestroyList(final Activity activity) {
        try {
            activitiesList.add(activity);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    //Ajouter une tache dans la pile de tache
    public static void addTask(@NonNull final AsyncTask<Void, Integer, Boolean> task) {
        tasksList.add(task);
    }

    public static void clearDestroyList() {
        if (activitiesList != null && activitiesList.size() > 0) {
            for (Activity act : activitiesList) {
                if (!act.isFinishing())
                    act.finish();
            }
            activitiesList.clear();
        }
    }


    //Permet d'interrompre une tache
    public static void clearAsynTask() {
        if (tasksList != null && tasksList.size() > 0) {
            for (AsyncTask<Void, Integer, Boolean> mTask : tasksList) {
                if (!(null == mTask)) {
                    mTask.cancel(true);
                }
            }
            tasksList.clear();
        }
    }


    //Detruit le token
    public void clearToken(@NonNull final Context ctx) {
        try {
            Save.defaultSaveString(Constants.PREF_TOKEN, "", ctx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Verifie si le token jwt est valide
    public boolean isTokenValide() {
        try {
            token = Save.defaultLoadString(Constants.PREF_TOKEN, getApplicationContext());
            if (token != null && !token.equals("")) {
                if (token.equals(""))
                    return false;
                JWT jwt = new JWT(token);
                boolean isExpired = jwt.isExpired(0);
                return !isExpired;
            } else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    //Ajoute un token
    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        try {
            token = Save.defaultLoadString(Constants.PREF_TOKEN, getApplicationContext());
            Log.v("JWT TOKEN", token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }


    //Recupere le nom utilisateur des claims du jwt
    public String getUserId() {
        if (this.isTokenValide()) {
            JWT jwt = new JWT(getToken());
            Claim claim = jwt.getClaim("user_id");
            return claim.asString();
        } else {
            return "";
        }
    }

    public int getEmployeID() {
        APIService service = APIClient.getClient().create(APIService.class);
        Observable<EmployeId> observable = service.CurrentEmployeId("JWT " + getToken(), getUserId());
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<EmployeId>() {

                    @Override
                    public void onSubscribe(Disposable disposable) {

                    }

                    @Override
                    public void onNext(EmployeId emp) {
                        Log.e("JSON ID", "" + emp.getId());
                        FastSave.getInstance().saveInt("user_id", emp.getId());
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        return FastSave.getInstance().getInt("user_id");
    }


    //Verifie si le reseau est disponible
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public @NonNull
    CopyOnWriteArrayList<Recipient> recipiencesJSONArrToList(@NonNull JSONArray array) {
        CopyOnWriteArrayList<Recipient> list = new CopyOnWriteArrayList<>();
        Log.v("appController:", array.toString());
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Recipient reci = new Recipient();
                reci.setIdOnServer(obj.getInt("id"));
                reci.setTel(obj.getString("telephone"));
                reci.setRef(obj.getString("reference"));
                reci.setDateCreate(obj.getString("create_at"));
                reci.setDeleted(obj.getBoolean("delete"));
                //reci.setFonction(obj.getInt("fonction"));
                reci.setAdresse(obj.getInt("adresse"));
                reci.setRole(obj.getInt("role"));
                reci.setEntreprise(obj.getInt("entreprise"));
                reci.setSuperieur(obj.getInt("superieur"));
                reci.setFirstName(obj.getJSONObject("user")
                        .getString("first_name"));
                reci.setLastName(obj.getJSONObject("user")
                        .getString("last_name"));
                reci.setEmail(obj.getJSONObject("user")
                        .getString("email"));
                reci.setUserName(obj.getJSONObject("user")
                        .getString("username"));
                list.add(reci);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(Constants.APP_NAME, TAG + " CAN'T getRegions");
        }


        return list;

    }

    public @Nullable
    JSONArray recipientListToJSONArr(@NonNull CopyOnWriteArrayList<Recipient> rList) throws Exception {
        Log.v(Constants.APP_NAME, TAG + " recipientToArray RUN");
        JSONArray jArr = new JSONArray();

        for (Recipient dm : rList) {
            JSONObject object = new JSONObject();

            object.put("id", dm.getIdOnServer());

            object.put("telephone", dm.getTel());
            object.put("reference", dm.getRef());
            object.put("create_at", dm.getDateCreate());
            object.put("delete", dm.isDeleted());
            object.put("fonction", dm.getFonction());
            object.put("adresse", dm.getAdresse());
            object.put("role", dm.getRole());
            object.put("entreprise", dm.getEntreprise());
            object.put("superieur", dm.getSuperieur());
            object.put("first_name", dm.getFirstName());
            object.put("last_name", dm.getLastName());
            object.put("email", dm.getEmail());
            object.put("username", dm.getUserName());

            jArr.put(object);
        }
        return jArr;
    }

    public void setAlert_categories(Map<String, Integer> alert_categories) {
        this.alert_categories = alert_categories;
    }
}
