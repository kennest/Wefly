package com.wefly.wealert.presenters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.wefly.wealert.models.Alert;
import com.wefly.wealert.models.Common;
import com.wefly.wealert.models.Recipient;
import com.wefly.wealert.utils.Constants;
import com.wefly.wealert.utils.Save;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by admin on 27/03/2018.
 */

public class DataBasePresenter extends SQLiteOpenHelper {


    private static DataBasePresenter instance;
    private final CopyOnWriteArrayList<Alert> alertList = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Common> commonList = new CopyOnWriteArrayList<>();
    private final String TAG = getClass().getSimpleName();

    public DataBasePresenter(final Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);

    }

    public static void init(@NonNull Context ctx) {
        if (null == instance) {
            instance = new DataBasePresenter(ctx);
        }
    }

    public static @NonNull
    DataBasePresenter getInstance() {
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try {
            //create table ALERT
            String CREATE_TABLE_ALERT = "CREATE TABLE " + Constants.TABLE_ALERT + "("
                    + Constants.TABLE_ALERT_KEY_ID + " INTEGER PRIMARY KEY, "
                    + Constants.TABLE_ALERT_OBJECT_NAME + " TEXT, "
                    + Constants.TABLE_ALERT_CONTENT_NAME + " TEXT, "
                    + Constants.TABLE_ALERT_CATEGORY_NAME + " TEXT, "
                    + Constants.TABLE_ALERT_SENDER_NAME + " TEXT, "
                    + Constants.TABLE_ALERT_RECIPIENTS_ID_NAME + " TEXT, "
                    + Constants.TABLE_ALERT_CREATED_DATE_NAME + " TEXT);";

            //create table Sms
            String CREATE_TABLE_COMMON = "CREATE TABLE " + Constants.TABLE_COMMON + "("
                    + Constants.TABLE_COMMON_KEY_ID + " INTEGER PRIMARY KEY, "
                    + Constants.TABLE_COMMON_HAS_NEXT_NAME + " INT, "
                    + Constants.TABLE_COMMON_HAS_PREVIOUS_NAME + " INT, "
                    + Constants.TABLE_COMMON_NEXT_PAGE_NAME + " TEXT, "
                    + Constants.TABLE_COMMON_PREVIOUS_PAGE_NAME + " TEXT, "
                    + Constants.TABLE_COMMON_COUNT_NAME + " INT);";

            db.execSQL(CREATE_TABLE_ALERT);
            db.execSQL(CREATE_TABLE_COMMON);
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(Constants.APP_NAME, TAG + "onCreate fail");
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_ALERT);
            db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_COMMON);

            //create a new one
            onCreate(db);
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(Constants.APP_NAME, TAG + "onUpgrade fail");
        }

    }


    public int getAlertTotalItems() {
        int totalItems = 0;
        try {
            String query = "SELECT * FROM " + Constants.TABLE_ALERT;
            SQLiteDatabase dba = this.getReadableDatabase();
            Cursor cursor = dba.rawQuery(query, null);

            totalItems = cursor.getCount();

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(Constants.APP_NAME, "TAG getParcelleTotalItems error");
        }

        return totalItems;

    }


    //delete email item
    public boolean deleteAlert(int alertId) {
        try {
            SQLiteDatabase dba = this.getWritableDatabase();
            dba.delete(Constants.TABLE_ALERT, Constants.TABLE_ALERT_KEY_ID + " = ?",
                    new String[]{String.valueOf(alertId)});

            dba.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(Constants.APP_NAME, TAG + "deleteAlert  Id " + alertId + "fail");
            return false;
        }

    }

    //delete Sms item
    public boolean deleteSms(int smsId) {
        try {
            SQLiteDatabase dba = this.getWritableDatabase();
            dba.delete(Constants.TABLE_SMS, Constants.TABLE_SMS_KEY_ID + " = ?",
                    new String[]{String.valueOf(smsId)});

            dba.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(Constants.APP_NAME, TAG + "deleteSms  Id " + smsId + "fail");
            return false;
        }

    }

    //delete Sms item
    public boolean deleteCommon(int commonId) {
        try {
            SQLiteDatabase dba = this.getWritableDatabase();
            dba.delete(Constants.TABLE_COMMON, Constants.TABLE_COMMON_KEY_ID + " = ?",
                    new String[]{String.valueOf(commonId)});

            dba.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(Constants.APP_NAME, TAG + "deletecommon  Id " + commonId + "fail");
            return false;
        }

    }


    //add content to db - add Alert
    public boolean addAlert(@NonNull Alert alert) {
        try {
            SQLiteDatabase dba = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(Constants.TABLE_ALERT_OBJECT_NAME, alert.getObject());
            values.put(Constants.TABLE_ALERT_CONTENT_NAME, alert.getContent());
            values.put(Constants.TABLE_ALERT_CATEGORY_NAME, alert.getCategory());
            values.put(Constants.TABLE_ALERT_CREATED_DATE_NAME, alert.getDateCreated());
            values.put(Constants.TABLE_ALERT_RECIPIENTS_ID_NAME, alert.getRecipientsIds());

            dba.insert(Constants.TABLE_ALERT, null, values);


            dba.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(Constants.APP_NAME, TAG + "addAlert Error");
        }

        return false;
    }


    //add content to db - add common
    public boolean addCommon(@NonNull Common com) {
        try {
            SQLiteDatabase dba = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(Constants.TABLE_COMMON_HAS_NEXT_NAME, com.getHasNextAsInt());
            values.put(Constants.TABLE_COMMON_HAS_PREVIOUS_NAME, com.getHasPreviousAsInt());
            values.put(Constants.TABLE_COMMON_NEXT_PAGE_NAME, com.getNextPage());
            values.put(Constants.TABLE_COMMON_PREVIOUS_PAGE_NAME, com.getPrevPage());
            values.put(Constants.TABLE_COMMON_COUNT_NAME, com.getCount());

            dba.insert(Constants.TABLE_COMMON, null, values);


            dba.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(Constants.APP_NAME, TAG + "addCommon Error");
        }

        return false;
    }


    //Get all Alert
    public CopyOnWriteArrayList<Alert> getAlerts() {

        alertList.clear();
        try {


            SQLiteDatabase dba = this.getReadableDatabase();
            Cursor cursor = dba.query(Constants.TABLE_ALERT,
                    new String[]{
                            Constants.TABLE_ALERT_KEY_ID,
                            Constants.TABLE_ALERT_OBJECT_NAME,
                            Constants.TABLE_ALERT_CONTENT_NAME,
                            Constants.TABLE_ALERT_CATEGORY_NAME,
                            Constants.TABLE_ALERT_SENDER_NAME,
                            Constants.TABLE_ALERT_RECIPIENTS_ID_NAME,
                            Constants.TABLE_ALERT_CREATED_DATE_NAME}, null, null, null, null, null);

            //loop through...
            if (cursor.moveToFirst()) {
                do {

                    Alert alert = new Alert();

                    alert.setAlertId(cursor.getInt(cursor.getColumnIndex(Constants.TABLE_ALERT_KEY_ID)));
                    alert.setObject(cursor.getString(cursor.getColumnIndex(Constants.TABLE_ALERT_OBJECT_NAME)));
                    alert.setContent(cursor.getString(cursor.getColumnIndex(Constants.TABLE_ALERT_CONTENT_NAME)));
                    alert.setCategory(cursor.getString(cursor.getColumnIndex(Constants.TABLE_ALERT_CATEGORY_NAME)));
                    alert.setRecipientsString(cursor.getString(cursor.getColumnIndex(Constants.TABLE_ALERT_RECIPIENTS_ID_NAME)));
                    alert.setDateCreated(cursor.getString(cursor.getColumnIndex(Constants.TABLE_ALERT_CREATED_DATE_NAME)));


                    alertList.add(alert);

                } while (cursor.moveToNext());


            }

            cursor.close();
            dba.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(Constants.APP_NAME, TAG + "getAlerts Error");
        }

        return alertList;

    }


    // Get common
    public CopyOnWriteArrayList<Common> getCommon() {

        commonList.clear();
        try {

            SQLiteDatabase dba = this.getReadableDatabase();
            Cursor cursor = dba.query(Constants.TABLE_COMMON,
                    new String[]{
                            Constants.TABLE_COMMON_KEY_ID,
                            Constants.TABLE_COMMON_HAS_NEXT_NAME,
                            Constants.TABLE_COMMON_HAS_PREVIOUS_NAME,
                            Constants.TABLE_COMMON_NEXT_PAGE_NAME,
                            Constants.TABLE_COMMON_PREVIOUS_PAGE_NAME,
                            Constants.TABLE_COMMON_COUNT_NAME}, null, null, null, null, null);

            //loop through...
            if (cursor.moveToFirst()) {

            }

            cursor.close();
            dba.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(Constants.APP_NAME, TAG + "getPoints Error");
        }

        return commonList;

    }

    public void saveRecipientList(@NonNull Context ctx, @NonNull JSONArray array) {
        try {
            Save.defaultSaveString(Constants.PREF_RECIPIENTS, array.toString(), ctx);
            Log.v(Constants.APP_NAME, TAG + " saveRecipientsList SUCCES STORE IN SHARED " + array.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(Constants.APP_NAME, TAG + "saveRecipientsList Error");
        }
    }

    public void clearRecipients(@NonNull Context ctx) {
        try {
            Save.defaultSaveString(Constants.PREF_RECIPIENTS, "", ctx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public @NonNull
    CopyOnWriteArrayList<Recipient> getRecipients(@NonNull Context ctx) {
        CopyOnWriteArrayList<Recipient> list = new CopyOnWriteArrayList<>();

        try {
            String res = Save.defaultLoadString(Constants.PREF_RECIPIENTS, ctx);
            Log.v(Constants.APP_NAME, TAG + " SharedPref res JSONArray" + res);
            JSONArray array = new JSONArray(res);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Recipient reci = new Recipient();
                reci.setIdOnServer(obj.getInt("id"));
                reci.setTel(obj.getString("telephone"));
                reci.setRef(obj.getString("reference"));
                reci.setDateCreate(obj.getString("create_at"));
                reci.setDeleted(obj.getBoolean("delete"));
                reci.setFonction(obj.getInt("fonction"));
                reci.setAdresse(obj.getInt("adresse"));
                reci.setRole(obj.getInt("role"));
                reci.setEntreprise(obj.getInt("entreprise"));
                reci.setSuperieur(obj.getInt("superieur"));
                reci.setFirstName(obj.getString("first_name"));
                reci.setLastName(obj.getString("last_name"));
                reci.setEmail(obj.getString("email"));
                reci.setUserName(obj.getString("username"));

                list.add(reci);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(Constants.APP_NAME, TAG + " CAN'T Recipients");
        }

        return list;
    }


    public void onInit() {
        alertList.clear();
        // reload everything in all Array
    }

    public void onReload() {
        onInit();
    }
}
