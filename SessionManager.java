package com.deepwares.checkpointdwi.session;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.deepwares.checkpointdwi.activities.LoginUserActivity;

import java.util.HashMap;

/**
 * Created by hemanthhemu on 02/05/18.
 */

public class SessionManager {
    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "name";
    // Id address (make variable public to access from outside)
    public static final String KEY_ID = "matri_id";
    // Image address (make variable public to access from outside)
    public static final String KEY_IMAGE = "profile_image";
    public static final String KEY_PROFILEID = "profile_id";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_INDEXID = "index_id";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_PHOTO1 = "photo1";
    public static final String KEY_RELIGION = "religion";
    public static final String KEY_CASTE = "caste";
    public static final String KEY_STATUS = "status";
    public static final String KEY_FR = "fr";
    public static final String KEY_ACTIVE = "active";
    public static final String KEY_LEGAL = "legal";


    // Sharedpref file name
    private static final String PREF_NAME = "SpottingPref";
    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";
    // Shared Preferences
    SharedPreferences pref;
    // Editor for Shared preferences
    SharedPreferences.Editor editor;
    // Context
    Context _context;
    // Shared pref mode
    int PRIVATE_MODE = 0;
    HashMap<String, String> user;

    // Constructor
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
//    public void createLoginSession(String name, String eid,String profile_id,String email, String profile_image) {
//        // Storing login value as TRUE
//        editor.putBoolean(IS_LOGIN, true);
//
//        // Storing name in pref
//        editor.putString(KEY_NAME, name);
//
//        // Storing email in pref
//        editor.putString(KEY_ID, eid);
//        // Storing email in pref
//        editor.putString(KEY_PROFILEID, profile_id); // Storing email in pref
//        editor.putString(KEY_EMAIL, email);
//        // Storing email in pref
//        editor.putString(KEY_IMAGE, profile_image);
//
//        // commit changes
//        editor.commit();
//    }
    public void createLoginSession(String eid, String email) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        // editor.putString(KEY_NAME, name);

        // Storing email in pref
        editor.putString(KEY_ID, eid);
        editor.putString(KEY_EMAIL, email);

        // Storing email in pref
        //  editor.putString(KEY_IMAGE, profile_image);
        //  editor.putString(KEY_PROFILEID, profile_id);


        // commit changes
        editor.commit();
    }


    public void saveFR_Active(String pincode, String location) {
        // Storing login value as TRUE


        // Storing name in pref
        // editor.putString(KEY_NAME, name);

        // Storing email in pref

        editor.putString(KEY_ACTIVE, pincode);
        editor.putString(KEY_FR, location);

        // Storing email in pref
        //  editor.putString(KEY_IMAGE, profile_image);
        //  editor.putString(KEY_PROFILEID, profile_id);


        // commit changes
        editor.commit();
    }

    public void check_Legal(String legal) {
        // Storing login value as TRUE


        // Storing name in pref
        // editor.putString(KEY_NAME, name);

        // Storing email in pref

        editor.putString(KEY_LEGAL, legal);


        // Storing email in pref
        //  editor.putString(KEY_IMAGE, profile_image);
        //  editor.putString(KEY_PROFILEID, profile_id);


        // commit changes
        editor.commit();
    }



    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     */
    public void checkLogin() {
        // Check login status
        if (!this.isLoggedIn()) {
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginUserActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }


    /**
     * Get stored session data
     */
    public HashMap<String, String> getUserDetails() {
        user = new HashMap<String, String>();
        //user name
        // user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        //user email id
        user.put(KEY_ID, pref.getString(KEY_ID, null));
        //user image
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        //user image
        user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, null));
        //user image
        user.put(KEY_INDEXID, pref.getString(KEY_INDEXID, null));

        user.put(KEY_GENDER, pref.getString(KEY_GENDER, null));

        user.put(KEY_PHOTO1, pref.getString(KEY_PHOTO1, null));
        user.put(KEY_RELIGION, pref.getString(KEY_RELIGION, null));
        user.put(KEY_CASTE, pref.getString(KEY_CASTE, null));
        user.put(KEY_STATUS, pref.getString(KEY_STATUS, null));
        user.put(KEY_FR, pref.getString(KEY_FR, null));
        user.put(KEY_ACTIVE, pref.getString(KEY_ACTIVE, null));
        user.put(KEY_LEGAL, pref.getString(KEY_LEGAL, null));

        //return user
        return user;
    }

    /**
     * Clear session details
     *
     * @return
     */
    public Fragment logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
       /* Intent i = new Intent(_context, LoginUserActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);*/
        return null;
    }

    /**
     * Quick check for login
     * *
     */
    // Get Login State
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }
}
