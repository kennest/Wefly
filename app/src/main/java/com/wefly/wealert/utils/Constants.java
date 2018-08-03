package com.wefly.wealert.utils;

/**
 * Created by admin on 01/06/2018.
 */

public class Constants {

    // APP NAME
    public static final String APP_NAME = "WeFly";
    public static final String PATH = "WeFly";

    // BASE
    //public static final String BASE_URL = "http://192.168.1.66:8000/";
    public static final String BASE_URL = "http://217.182.133.143:8000/";

    // GET
    public static final String LOGIN_URL = BASE_URL + "login/";

    //GET
    public static final String RECIPIENTS_URL = BASE_URL + "communications/api/liste-employes/";

    public static final String ALERT_RECEIVE_URL = BASE_URL + "communications/api/alerte-receive-status/";


    public static final String SEND_FILE_URL = BASE_URL + "communications/api/files/";

    public static final String SEND_ALERT_URL = BASE_URL + "communications/api/alertes/";

    public static final String ALERT_CATEGORY_URL = BASE_URL + "communications/api/categorie-alerte/";

    //Util
    public static final double DOUBLE_NULL = 0.0d;


    public static final String PREF_TOKEN = PATH + ".token";
    public static final String PREF_USER_NAME = PATH + ".user.name";
    public static final String PREF_USER_PASSWORD = PATH + ".user.password";

    //Preference
    public static final String SAVE_PREFERENCE_NAME = "NonameSave";
    public static final String PREF_RECIPIENTS = PATH + ".precipients";

    //Volley
    public static final int VOLLEY_TIME_OUT = 300000; //5 min

    //JWT TOKEN
    public static final String TOKEN_HEADER_NAME = "JWT ";

    //NetWork
    public static final String SERVER_ERROR = "error";
    public static final String RESPONSE_EMPTY_INPUT = "non_field_errors";
    public static final String RESPONSE_EMPTY = "{}";
    public static final String RESPONSE_ERROR_HTML = "<html>";

    // Save state
    public static final String STATE_USER_NAME = PATH + ".state.user.name";
    public static final String STATE_USER_PASSWORD = PATH + ".state.user.password";
    public static final String STATE_ALERT = PATH + ".state.alert";

    public static final int REQUEST_GROUP_PERMISSION = 425;
    public static final int REQUEST_APP_PERMISSION = 425;

    //DB
    public static final String DATABASE_NAME = "wecollectdb";
    public static final int DATABASE_VERSION = 1;



    //DB Table Email
    public static final String TABLE_EMAIL = "email_tbl";

    //DB Table ALERT
    public static final String TABLE_ALERT = "alert_tbl";
    public static final String TABLE_ALERT_KEY_ID = "_id";
    public static final String TABLE_ALERT_OBJECT_NAME = "_obj";
    public static final String TABLE_ALERT_CONTENT_NAME = "_content";
    public static final String TABLE_ALERT_CATEGORY_NAME = "_category";
    public static final String TABLE_ALERT_CREATED_DATE_NAME = "_date_al";
    public static final String TABLE_ALERT_RECIPIENTS_ID_NAME = "_recip";
    public static final String TABLE_ALERT_SENDER_NAME = "_sender";

    //DB Table Sms
    public static final String TABLE_SMS = "sms_tbl";
    public static final String TABLE_SMS_KEY_ID = "_id";

    //DB Table common
    public static final String TABLE_COMMON = "common_tbl";
    public static final String TABLE_COMMON_KEY_ID = "_id";
    public static final String TABLE_COMMON_HAS_NEXT_NAME = "_has_next";
    public static final String TABLE_COMMON_HAS_PREVIOUS_NAME = "_has_prev";
    public static final String TABLE_COMMON_NEXT_PAGE_NAME = "_next_pg";
    public static final String TABLE_COMMON_PREVIOUS_PAGE_NAME = "_prev_pg";
    public static final String TABLE_COMMON_COUNT_NAME = "_count";

}
