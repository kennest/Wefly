package com.wefly.wealert.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Obrina.KIMI on 12/7/2017.
 */

public class Save {
    /**
     * Save string to preferences
     *
     * @param string
     * @param key
     * @param mContext
     * @return
     */
    public static boolean saveString(String string, String key, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SAVE_PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, string);
        return editor.commit();
    }

    public static boolean saveBoolean(Boolean b, String key, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SAVE_PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, b);
        return editor.commit();
    }

    @NonNull
    public static Boolean loadBoolean(String key, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SAVE_PREFERENCE_NAME, 0);
        return prefs.getBoolean(key, false);
    }

    /**
     * Load string from preferences
     *
     * @param key
     * @param mContext
     * @return
     */
    public static String loadString(String key, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SAVE_PREFERENCE_NAME, 0);
        return prefs.getString(key, null);
    }

    public static boolean saveJsonObject(JSONObject jObject, String key, Context mContext){
        String objToString = jObject.toString();
        return saveString(objToString, key, mContext);
    }

    @Nullable
    public static JSONObject loadJsonObject(String key, Context mContext){
        JSONObject jObject;
        try {
            jObject = new JSONObject(loadString(key, mContext));
            return jObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean saveJsonArray(JSONArray jArray, String key, Context mContext){
        String arrayToString = jArray.toString();
        return defaultSaveString(arrayToString, key, mContext);
    }

    @Nullable
    public static JSONArray loadJsonArray(String key, Context mContext){
        JSONArray jArray;
        try {
            jArray = new JSONArray(defaultLoadString(key, mContext));
            return jArray;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Save int to preferences
     *
     * @param value
     * @param key
     * @param mContext
     * @return
     */
    public static boolean saveInt(int value, String key, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SAVE_PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    /**
     * Load int from preferences
     *
     * @param key
     * @param mContext
     * @return
     */
    public static int loadInt(String key, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SAVE_PREFERENCE_NAME, 0);
        return prefs.getInt(key, -1);
    }

    /**
     * Save string array to preferences
     *
     * @param array
     * @param arrayName
     * @param mContext
     * @return
     */
    public static boolean saveArray(String[] array, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SAVE_PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName + "_size", array.length);
        for (int i = 0; i < array.length; i++)
            editor.putString(arrayName + "_" + i, array[i]);
        return editor.commit();
    }


    /**
     * Add string to a string element in preferences
     *
     * @param item
     * @param arrayName
     * @param mContext
     * @return
     */
    public static boolean addToArray(String item, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SAVE_PREFERENCE_NAME, 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName + "_size", size + 1);
        editor.putString(arrayName + "_" + size, item);
        return editor.commit();
    }


    /**
     * Load string array from preferences
     *
     * @param arrayName
     * @param mContext
     * @return
     */
    public static String[] loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SAVE_PREFERENCE_NAME, 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        String array[] = new String[size];
        for (int i = 0; i < size; i++)
            array[i] = prefs.getString(arrayName + "_" + i, null);

        return array;
    }


    /**
     * Save integer array to preferences
     *
     * @param array
     * @param arrayName
     * @param mContext
     * @return
     */
    public static boolean saveArray(int[] array, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SAVE_PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName + "_size", array.length);
        for (int i = 0; i < array.length; i++)
            editor.putInt(arrayName + "_" + i, array[i]);
        return editor.commit();
    }

    /**
     * Add integer to integer array in preferences
     *
     * @param item
     * @param arrayName
     * @param mContext
     * @return
     */
    public static boolean addToArray(int item, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SAVE_PREFERENCE_NAME, 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName + "_size", size + 1);
        editor.putInt(arrayName + "_" + size, item);
        return editor.commit();
    }


    /**
     * Load integer array from preferences
     *
     * @param arrayName
     * @param mContext
     * @return
     */
    public static int[] loadIntArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SAVE_PREFERENCE_NAME, 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        int array[] = new int[size];
        for (int i = 0; i < size; i++)
            array[i] = prefs.getInt(arrayName + "_" + i, 0);

        //for (int i = 0; i < array.length; i++)
        //   System.out.println("load index :" + i + " content:" + array[i]);
        return array;
    }

    /**
     * Remove item from integer array in preferences
     *
     * @param index
     * @param arrayName
     * @param mContext
     * @return
     */
    public static boolean removeFromIntArray(int index, String arrayName, Context mContext) {
        int[] array = loadIntArray(arrayName, mContext);
//        for (int i = 0; i < array.length; i++)
//            System.out.println("removeFromIntArray index :" + i + " content:" + array[i]);
        int[] arrayRem = removeElement(array, index);
//        for (int i = 0; i < arrayRem.length; i++)
//            System.out.println("after removeFromIntArray index :" + i + " content:" + arrayRem[i]);
        removeArray(arrayName, mContext);
        // System.out.println("save :" + saveArray(arrayRem, arrayName, mContext));

        int[] array2 = loadIntArray(arrayName, mContext);
//        for (int i = 0; i < array2.length; i++)
//            System.out.println("reload index :" + i + " content:" + array2[i]);
        return true;
    }


    /**
     * Destroy array in preferences
     *
     * @param arrayName
     * @param mContext
     * @return
     */
    public static boolean removeArray(String arrayName, Context mContext) {
        SharedPreferences preferences = mContext.getSharedPreferences(Constants.SAVE_PREFERENCE_NAME, 0);
        return preferences.edit().remove(arrayName + "_size").commit();
    }


    /**
     * Remove element at index from array
     *
     * @param index
     * @param arrayName
     * @param mContext
     * @return
     */
    public static boolean removeFromArray(int index, String arrayName, Context mContext) {
        String[] array = loadArray(arrayName, mContext);
        String[] arrayRem = removeElement(array, index);
        removeArray(arrayName, mContext);
        return saveArray(arrayRem, arrayName, mContext);
    }


    /**
     * Remove element from array
     *
     * @param input   - input array
     * @param element - index of element
     * @return - output array
     */
    private static int[] removeElement(int[] input, int element) {
        List<Integer> list = new ArrayList<Integer>();
        for (int index = 0; index < input.length; index++) {
            list.add(input[index]);
        }
//        for (int i = 0; i < list.size(); i++)
//            System.out.println("removeElement index :" + i + " " + list.get(i));
        list.remove(element);
        int[] output = new int[input.length - 1];
        for (int i = 0; i < list.size(); i++)
            output[i] = list.get(i);

        return output;
    }

    /**
     * Remove element from array
     *
     * @param input   -        input array
     * @param element - index of element
     * @return- output array
     */
    private static String[] removeElement(String[] input, int element) {
        List<String> list = new ArrayList<String>();
        for (int index = 0; index < input.length; index++) {
            list.add(input[index]);
        }
        list.remove(element);
        String[] output = new String[input.length - 1];
        for (int i = 0; i < list.size(); i++)
            output[i] = list.get(i);

        return output;
    }

    public static boolean defaultSaveString(@NonNull String key, @NonNull String value, @NonNull final Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public static @Nullable String defaultLoadString(@NonNull String key, @NonNull final Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, null);
    }

    public static boolean defaultSaveInt(@NonNull String key, @NonNull int value, @NonNull final Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public static int defaultLoadInt(@NonNull String key, @NonNull final Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(key, 0);
    }

    public static boolean defaultSaveBoolean(@NonNull String key, @NonNull boolean value, @NonNull final Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public static boolean defaultLoadBoolean(@NonNull String key, @NonNull final Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(key, false);
    }
}
