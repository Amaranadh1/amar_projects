package com.deepwares.checkpointdwi.activities;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.deepwares.checkpointdwi.R;
import com.deepwares.checkpointdwi.facetracker.EnrollActivity;
import com.deepwares.checkpointdwi.facetracker.models.Encoding;
import com.deepwares.checkpointdwi.facetracker.models.Enroll;
import com.deepwares.checkpointdwi.facetracker.models.Match;
import com.deepwares.checkpointdwi.network.ConnectionDetector;
import com.deepwares.checkpointdwi.service.WebServiceCallBack;
import com.deepwares.checkpointdwi.service.WebserviceHelper;
import com.deepwares.checkpointdwi.session.Cache;
import com.deepwares.checkpointdwi.session.CatchValue;
import com.deepwares.checkpointdwi.session.SessionManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.loopj.android.http.RequestParams;
import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class LoginUserActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>, WebServiceCallBack {

    private static final String TAG = LoginUserActivity.class.getName();
    private AutoCompleteTextView mLoginEmailView;
    private EditText mLoginPasswordView;
    private Button mLoginButton;
    BluetoothAdapter BA;
    private static final int PERMISSION_REQUEST_CODE = 200;
    //Check Internet
    Boolean isInternetPresent = false;
    ConnectionDetector connectionDetector;
    static int MatchResult = 1003;
    static int EnrollResult = 1004;
    static int EnrollWizardResult = 1005;
    private LinearLayout loginLayout, languagesLayout, passwordLayout, emailLayout, loginLinearLayout;
    private String appLang;
    private Locale locale;
    private Spinner spinnerLanguages;
    ArrayAdapter<String> languagesAdapter;
    ArrayList<String> languagesList;
    Date crashCurrentDate;
    String crashDate, crashTime;
    android.app.AlertDialog alertDialog;
    SessionManager session;
    private String emb0 = "", emb1 = "";
    Bundle bundle;
    private ProgressDialog progressDialog;
    private String activeUser = "", text = "User is not in active mode!";
    private Button nextButton;
    private TextView requirePassword;
    private String email, password, requireValue = "";
    private EditText et_One, et_two, et_three, et_four, et_five, et_six;
    private boolean cancel = false, status = false;
    View focusView = null;
    Boolean legal = false,enroll= false;
    private String emptyNext = "Please enter the Email!", wrongNext = "Invalid Email Address!",
            emptyPass = "Please enter the Password!", wrongPass = "Invalid Password!",
            message = "Invalid Email and Password!";
    boolean userPicStatus;
    boolean resetPassStatus = false;
    private FirebaseAnalytics mFirebaseAnalytics;
    Bundle params;


    //create a textWatcher member
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // check Fields For Empty Values
            checkFieldsForEmptyValues();
        }
    };

    void checkFieldsForEmptyValues() {
        nextButton = (Button) findViewById(R.id.next_button);
        email = mLoginEmailView.getText().toString();
        if (email.matches("[a-zA-Z0-9._-]+@[a-z]+.[a-z]+")) {
            nextButton.setEnabled(true);
        } else {
            nextButton.setEnabled(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        initViews();


        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // set listeners
        mLoginEmailView.addTextChangedListener(mTextWatcher);
        // run once to disable if empty
        checkFieldsForEmptyValues();
        legal = (Boolean) Cache.getData(CatchValue.USER_LEGAL_STATUS, LoginUserActivity.this);
        Log.e("legal_value", "" + legal);

        mLoginEmailView.setOnEditorActionListener(new AutoCompleteTextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //do here your stuff f
                    if (isInternetPresent) {
                        emailLayout.setVisibility(View.GONE);
                        passwordLayout.setVisibility(View.VISIBLE);
                        mLoginButton.setVisibility(View.VISIBLE);
                        et_One.requestFocus();
                        status = true;
                    } else {
                        ShowNoInternetDialog();
                    }
                    return true;
                }
                return false;
            }

        });

        loginLinearLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        if (et_One.getText().length() == 1) {
            et_One.clearFocus();
            et_two.requestFocus();
            et_two.setEnabled(true);
        }

        textwatcher();
        //put the bundle values
        bundle = new Bundle();
        //check the internet is available or not
        //get the language data from session
        appLang = (String) Cache.getData(CatchValue.APP_LANGUAGE, LoginUserActivity.this);
        //check bluetooth is available or not
        BA = BluetoothAdapter.getDefaultAdapter();
        if (!checkPermission()) {
            //check the permissions
            requestPermission();
        } else {
            //Login();
        }
        //Bluetooth permissions
        checkReadBluetoothPermission();
        // Set up the login form.
        Boolean reLogin = false;
        Bundle bundle= getIntent().getExtras();

        try {
            if (bundle != null) {
                reLogin = bundle.getBoolean("relogin", false);
            } else {}
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        if (requireValue.equalsIgnoreCase("require_activity")) {
            passwordLayout.setVisibility(View.VISIBLE);
            mLoginButton.setVisibility(View.VISIBLE);
            et_One.requestFocus();
            status = true;
        }

        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetPresent) {
                    emailLayout.setVisibility(View.GONE);
                    passwordLayout.setVisibility(View.VISIBLE);
                    mLoginButton.setVisibility(View.VISIBLE);
                    et_One.requestFocus();
                    status = true;
                } else {
                    ShowNoInternetDialog();
                }
            }
        });

        requirePassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetPresent) {
                    if (resetPassStatus) {
                        final int interval = 180000; // 180 Seconds
                        Handler handler = new Handler();
                        Runnable runnable = new Runnable(){
                            public void run() {
                                resetPassStatus = false;
                            }
                        };
                        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
                        handler.postDelayed(runnable, interval);
                        timerAlertDialog();
                    } else {
                        showProgressDialog();
                        RequestParams requestParams = new RequestParams();
                        requestParams.put("email", email);
                        new WebserviceHelper(getApplicationContext()).postData(getString(R.string.base_url) + "resetPassword/", requestParams, "resetPassword", LoginUserActivity.this);
                    }
                } else {
                    ShowNoInternetDialog();
                }
            }
        });

        if (isInternetPresent) {
            if (reLogin) {
                languagesLayout.setVisibility(View.GONE);
                loginLayout.setVisibility(View.VISIBLE);
                //logout functionality
                ParseUser.logOut();
                //set the alert dialog
                final AlertDialog.Builder builder = new AlertDialog.Builder(LoginUserActivity.this);
                builder.setTitle(getResources().getString(R.string.text_security_alert));
                builder.setMessage(getResources().getString(R.string.text_your_face_didnt_match));
                builder.setPositiveButton(getResources().getString(R.string.text_login), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            } else {
                if (ParseUser.getCurrentUser() != null) {
                    if (TextUtils.isEmpty(appLang)) {
                        languagesLayout.setVisibility(View.VISIBLE);
                        loginLayout.setVisibility(View.GONE);
                    } else {
                        //pass the data to MainActivity
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    if (TextUtils.isEmpty(appLang)) {
                        languagesLayout.setVisibility(View.VISIBLE);
                        loginLayout.setVisibility(View.GONE);
                    } else {
                        languagesLayout.setVisibility(View.GONE);
                        loginLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        } else {
        }

        //choose the languages
        locale = getResources().getConfiguration().locale;
        spinnerLanguages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                } else if (position == 1) {
                    try {
                        //send the english language data to session
                        Cache.putData(CatchValue.APP_LANGUAGE, LoginUserActivity.this, "en", Cache.CACHE_LOCATION_DISK);
                        setLocaleLanguage("en");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (position == 2) {
                    try {
                        //send the french language data to session
                        Cache.putData(CatchValue.APP_LANGUAGE, LoginUserActivity.this, "fr", Cache.CACHE_LOCATION_DISK);
                        setLocaleLanguage("fr");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (position == 3) {
                    try {
                        //send the spanish language data to session
                        Cache.putData(CatchValue.APP_LANGUAGE, LoginUserActivity.this, "es", Cache.CACHE_LOCATION_DISK);
                        setLocaleLanguage("es");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //get the list from string file.
        languagesList = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.languages_names)));
        //set the layout for language selection
        languagesAdapter = new ArrayAdapter<String>(LoginUserActivity.this, R.layout.spinner_text_view, languagesList) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return false;
                } else {
                    return true;
                }
            }
        };

        //set the dropdown style
        languagesAdapter.setDropDownViewResource(R.layout.spinner_text_view);
        spinnerLanguages.setAdapter(languagesAdapter);
        mLoginPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    //attemptLogin();
                    return true;
                }
                return false;
            }
        });

        //login functionalitiy
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check Internet
                if (isInternetPresent) {
                    showProgressDialog();
                    attemptLogin();
                    // hide keyboard
                    view = LoginUserActivity.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                } else {
                    ShowNoInternetDialog();
                }
            }
        });
        //store the data into session
        Cache.putData(CatchValue.TIMER, LoginUserActivity.this, false, Cache.CACHE_LOCATION_DISK);
        Cache.putData(CatchValue.TOGGLE, LoginUserActivity.this, false, Cache.CACHE_LOCATION_DISK);
        Cache.putData(CatchValue.NOTIFICATION, LoginUserActivity.this, false, Cache.CACHE_LOCATION_DISK);
        Cache.putData(CatchValue.LOCKOUT, LoginUserActivity.this, false, Cache.CACHE_LOCATION_DISK);
    }

    private void initViews() {
        et_One = (EditText) findViewById(R.id.editText_One);
        et_two = (EditText) findViewById(R.id.editText_two);
        et_three = (EditText) findViewById(R.id.editText_three);
        et_four = (EditText) findViewById(R.id.editText_four);
        et_five = (EditText) findViewById(R.id.editText_five);
        et_six = (EditText) findViewById(R.id.editText_six);
        mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mLoginPasswordView = (EditText) findViewById(R.id.password);
        loginLinearLayout = (LinearLayout) findViewById(R.id.login_linear_layout);
        emailLayout = (LinearLayout) findViewById(R.id.email_layout);
        requirePassword = (TextView) findViewById(R.id.text_require_password);
        passwordLayout = (LinearLayout) findViewById(R.id.password_layout);
        loginLayout = (LinearLayout) findViewById(R.id.email_login_form);
        languagesLayout = (LinearLayout) findViewById(R.id.languages_layout);
        spinnerLanguages = (Spinner) findViewById(R.id.language_spinner_login);
        alertDialog = new android.app.AlertDialog.Builder(LoginUserActivity.this).create();
        session = new SessionManager(this);
        progressDialog = new ProgressDialog(LoginUserActivity.this);
        connectionDetector = new ConnectionDetector(this);
        isInternetPresent = connectionDetector.isConnectingToInternet();
        et_One.setTransformationMethod(new AsteriskPasswordTransformationMethod());
        et_two.setTransformationMethod(new AsteriskPasswordTransformationMethod());
        et_three.setTransformationMethod(new AsteriskPasswordTransformationMethod());
        et_four.setTransformationMethod(new AsteriskPasswordTransformationMethod());
        et_five.setTransformationMethod(new AsteriskPasswordTransformationMethod());
        et_six.setTransformationMethod(new AsteriskPasswordTransformationMethod());
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(LoginUserActivity.this);
        params = new Bundle();
    }

    //check the facial recognization
    public void executeTruefaceFlow(ParseUser user) {
        if (user != null) {
            Cache.putData(CatchValue.USER_ACTIVE_FR, LoginUserActivity.this, false, Cache.CACHE_LOCATION_DISK);
            if (TextUtils.isEmpty(user.getString("status"))) {
                dismissProgressDialog();
                showUserModeAlertDialog(text);
                mLoginEmailView.setText("");
                mLoginPasswordView.setText("");
            } else {
                activeUser = user.getString("status");
                if (activeUser.equalsIgnoreCase("active")) {
                    session.createLoginSession(String.valueOf(user), user.getEmail());
                    boolean face_value = user.getBoolean("hasFRenabled");
                    Log.e("face_value: ", " ### " + face_value);
                    if (face_value == false) {
                        dismissProgressDialog();
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        if (user.has("enforce_enroll")) {
                            enroll = user.getBoolean("enforce_enroll");
                        }
                        Log.e("Enforc_false", String.valueOf(enroll));
                        if (enroll) {
                            Intent mainIntent = new Intent(LoginUserActivity.this, MainActivity.class);
                            startActivity(mainIntent);
                            finish();
                        } else {
                            Intent i = new Intent(LoginUserActivity.this, EnrollActivity.class);
                            i.putExtra("Intent", "Login");
                            startActivity(i);
                            finish();
                        }
                    }
                } else {
                    dismissProgressDialog();
                    showUserModeAlertDialog(text);
                    mLoginEmailView.setText("");
                }
            }
            Log.e("active_user", "### " + activeUser);
        }
    }

    private void showUserModeAlertDialog(String text) {
        showUserAlert(LoginUserActivity.this, "Alert Message", text, false);
    }

    private void showUserAlert(Context context, String title, String message, Boolean status) {
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon((status) ? R.mipmap.ic_action_checked : R.mipmap.ic_action_warning);
        alertDialog.setButton(getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                showProgressDialog();
                logout();
                session.logoutUser();
            }
        });
        alertDialog.show();
        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
        textView.setTextSize(16);
    }

    private void logout() {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                dismissProgressDialog();
            }
        });
    }

    //show the progress dialog
    public void showProgressDialog() {
        progressDialog.setMessage(getResources().getString(R.string.text_loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    //disable the progress dialog
    public void dismissProgressDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isInternetPresent) {
            ParseUser user;
            if (requestCode == MatchResult) {
                Match match = data.getParcelableExtra("result");
                if (match == null) {
                    try {
                        //send the face match data to server
                        user = ParseUser.getCurrentUser();
                        ParseObject BACEntry = new ParseObject("BACEntry");
                        BACEntry.put("timestamp", System.currentTimeMillis());
                        BACEntry.put("user", user);
                        BACEntry.put("faceMatchFailedAtLogin", true);
                        //data will be save in background for server
                        BACEntry.saveInBackground();

                        Crashlytics.setString("domain", "LoginUserActivity");
                        Crashlytics.setInt("code", 200);
                        Crashlytics.setString("UserName", ParseUser.getCurrentUser().getEmail());
                        Crashlytics.setString("Time", crashTime);
                        Crashlytics.setString("ModuleName", "FaceMatch");
                        Crashlytics.setString("deviceType", "Android");

                    } catch (Exception e) {
                        Log.e(TAG, "Time Stamp" + e.getMessage());
                        Crashlytics.logException(e);
                        Crashlytics.setString("domain", "LoginUserActivity");
                        Crashlytics.setString("UserName", ParseUser.getCurrentUser().getEmail());
                        Crashlytics.setString("Time", crashTime);
                        Crashlytics.setString("ModuleName", "FaceMatch");
                        Crashlytics.setString("ErrorDescription", e.getMessage());
                        Crashlytics.setString("deviceType", "Android");
                    }
                }
                //pass the data into main activity
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            if (requestCode == EnrollWizardResult) {
                if (data != null) {
                    user = ParseUser.getCurrentUser();
                    Enroll enroll = data.getParcelableExtra("result");
                    List<Encoding> encodings = enroll.getEncodings();
                    // false because enroll has happened, no longer needed
                    user.put("enforce_enroll", false);
                    user.put("tfEmb0", encodings.get(0).getEmb0());
                    user.put("tfEmb1", encodings.get(1).getEmb1());
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Crashlytics.setString("domain", "LoginUserActivity");
                                Crashlytics.setInt("code", 200);
                                Crashlytics.setString("UserName", ParseUser.getCurrentUser().getEmail());
                                Crashlytics.setString("Time", crashTime);
                                Crashlytics.setString("ModuleName", "FREnroll");
                                Crashlytics.setString("deviceType", "Android");

                                //pass the data into main activity
                                Intent intent = new Intent(LoginUserActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                                Crashlytics.setString("domain", "LoginUserActivity");
                                Crashlytics.setInt("code", e.getCode());
                                Crashlytics.setString("UserName", ParseUser.getCurrentUser().getEmail());
                                Crashlytics.setString("Time", crashTime);
                                Crashlytics.setString("ModuleName", "FREnroll");
                                Crashlytics.setString("ErrorDescription", e.getMessage());
                                Crashlytics.logException(new Exception("ErrorDescription"));
                                Crashlytics.setString("deviceType", "Android");
                            }
                        }
                    });
                }
            }

            if (requestCode == EnrollResult) {
                if (data != null) {
                    Enroll enroll = data.getParcelableExtra("result");
                    List<Encoding> encodings = enroll.getEncodings();
                    if (enroll == null) {
                        // false because enroll has happened, no longer needed
                        user = ParseUser.getCurrentUser();
                        user.put("enforce_enroll", false);
                        user.put("tfEmb0", encodings.get(0).getEmb0());
                        user.put("tfEmb1", encodings.get(1).getEmb1());
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Crashlytics.setString("domain", "LoginUserActivity");
                                    Crashlytics.setInt("code", 200);
                                    Crashlytics.setString("UserName", ParseUser.getCurrentUser().getEmail());
                                    Crashlytics.setString("Time", crashTime);
                                    Crashlytics.setString("ModuleName", "FREnrollResult");
                                    Crashlytics.setString("deviceType", "Android");
                                } else {
                                    e.printStackTrace();
                                    Crashlytics.logException(e);
                                    Crashlytics.setString("domain", "LoginUserActivity");
                                    Crashlytics.setInt("code", e.getCode());
                                    Crashlytics.setString("UserName", ParseUser.getCurrentUser().getEmail());
                                    Crashlytics.setString("Time", crashTime);
                                    Crashlytics.setString("ModuleName", "FREnrollResult");
                                    Crashlytics.setString("ErrorDescription", e.getMessage());
                                    Crashlytics.setString("deviceType", "Android");
                                }
                            }
                        });
                    }
                    Intent intent = new Intent(LoginUserActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        } else {
        }
    }

    private void timerAlertDialog() {
        showTimerAlert(LoginUserActivity.this, getResources().getString(R.string.your_passcode_sent), false);
    }

    public void showTimerAlert(Context context, String message, Boolean status) {
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(context).create();
        alertDialog.setMessage(message);
        alertDialog.setButton(getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
        textView.setTextSize(16);
    }

    //used to show no internet connection dialog
    public void ShowNoInternetDialog() {
        showAlertDialog(LoginUserActivity.this, getResources().getString(R.string.text_no_internet_connection),
                getResources().getString(R.string.text_please_check_your_network), false);
    }

    //show alert
    public void showAlertDialog(Context context, String title, String message, Boolean status) {
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon((status) ? R.mipmap.ic_action_checked : R.mipmap.ic_action_warning);
        alertDialog.setButton(getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
        textView.setTextSize(16);
    }

    //login procedure
    private void attemptLogin() {
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mLoginEmailView.setError(getString(R.string.error_field_required));
            focusView = mLoginEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mLoginEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mLoginEmailView;
            cancel = true;
        }

        password = et_One.getText().toString() + et_two.getText().toString() +
                et_three.getText().toString() + et_four.getText().toString() + et_five.getText().toString() +
                et_six.getText().toString();
        Log.e("@@@ ### ", "password_data: " + password);
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            dismissProgressDialog();
            showLoginFailedAlertDialog(emptyPass);
            focusView = et_One;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            dismissProgressDialog();
            showLoginFailedAlertDialog(wrongPass);
            focusView = et_One;
            cancel = true;
        } else {
            //check the internet permissions
            if (isInternetPresent) {
                ParseUser.logInInBackground(email, password, new LogInCallback() {
                    public void done(ParseUser user, ParseException e) {
                        if (e == null) {
                            dismissProgressDialog();
                            if (user != null) {
                                //Face Match
                                try {
                                    user = ParseUser.getCurrentUser();
                                    params.putString("User", user.getEmail());
                                    params.putString("Domain", "LoginUserActivity");
                                    params.putString("ModuleName", "Check the Login page");
                                    params.putString("MobileModel", Build.MODEL);
                                    params.putString("MobileBrand", Build.BRAND);
                                    params.putString("MobileOS", Build.VERSION.RELEASE);
                                    params.putString("DeviceType", "Android");
                                    mFirebaseAnalytics.logEvent("LoginUserActivity", params);
                                    String userPic = user.getString("userProfilePicture");
                                    session.createLoginSession(String.valueOf(user), user.getEmail());
                                    if (legal == null || legal == false) {
                                        Cache.putData(CatchValue.USER_LEGAL_STATUS, LoginUserActivity.this, false, Cache.CACHE_LOCATION_DISK);
                                        Intent legal_intent = new Intent(LoginUserActivity.this, LegalDisclaimerActivity.class);
                                        startActivity(legal_intent);
                                        finish();
                                    } else if (TextUtils.isEmpty(userPic)) {
                                        userPicStatus = false;
                                        Cache.putData(CatchValue.USER_PIC_STATUS, LoginUserActivity.this, userPicStatus, Cache.CACHE_LOCATION_DISK);
                                        Log.e("user_profile: ", " Picture_Activity1 @@@@");
                                        Intent picture_intent = new Intent(LoginUserActivity.this, PictureActivity.class);
                                        startActivity(picture_intent);
                                        finish();
                                    } else if (userPic.equalsIgnoreCase("")) {
                                        userPicStatus = false;
                                        Cache.putData(CatchValue.USER_PIC_STATUS, LoginUserActivity.this, userPicStatus, Cache.CACHE_LOCATION_DISK);
                                        Log.e("user_profile: ", " Picture_Activity ####");
                                        Intent picture_intent = new Intent(LoginUserActivity.this, PictureActivity.class);
                                        startActivity(picture_intent);
                                        finish();
                                    } else {
                                        Log.e("user_profile: ", " Mian_Activity ####");
                                        executeTruefaceFlow(user);
                                    }
                                } catch (Exception ex) {
                                    Log.e("checkbac_active", "Get the active history" + ex.getMessage());
                                    params.putString("Domain", "LoginUserActivity");
                                    params.putInt("Code", 500);
                                    params.putString("ModuleName", "Open the Login Page");
                                    params.putString("MobileModel", Build.MODEL);
                                    params.putString("MobileBrand", Build.BRAND);
                                    params.putString("MobileOS", Build.VERSION.RELEASE);
                                    params.putString("DeviceType", "Android");
                                    params.putString("ErrorDescription", e.getMessage());
                                    mFirebaseAnalytics.logEvent("LoginUserActivity", params);
                                }
                            } else {
                                showLoginFailedAlertDialog(message);
                            }
                        } else {
                            showLoginFailedAlertDialog(message);
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                ShowNoInternetDialog();
            }
        }
    }

    //check the email validation
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    //check the password validation
    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,
                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE},
                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }
        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginUserActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);
        mLoginEmailView.setAdapter(adapter);
    }

    @Override
    public int getLayoutResource() {
        return 0;
    }

    @Override
    public void onJSONResponse(String jsonResponse, String type) {
        switch (type) {
            case "resetPassword":
                Log.e("resetPassword", jsonResponse.toString());
                try {
                    JSONObject recentlyjoined_jsonObject = new JSONObject(jsonResponse);
                    String message = recentlyjoined_jsonObject.getString("message");
                    String status = recentlyjoined_jsonObject.getString("status");
                    Log.e("resetPassword", message);
                    Log.e("resetPassword", status);
                    dismissProgressDialog();
                    showLoginAlertDialog(message);
                    resetPassStatus = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onFailure() {
        Toast.makeText(LoginUserActivity.this, getResources().getString(R.string.text_something_went_wrong), Toast.LENGTH_SHORT).show();
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };
        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    public void showLoginAlertDialog(String message) {
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setMessage(message);
        alertDialog.setButton(getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //reqEmail.setText("");
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
        textView.setTextSize(18);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean contactAccpeted = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean readAccepted = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean writeAccpeted = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    boolean callAccepted = grantResults[5] == PackageManager.PERMISSION_GRANTED;
                    boolean recordAccpted = grantResults[6] == PackageManager.PERMISSION_GRANTED;
                    boolean smsAccepted = grantResults[7] == PackageManager.PERMISSION_GRANTED;

                    if (contactAccpeted && cameraAccepted && locationAccepted && readAccepted && writeAccpeted && callAccepted && recordAccpted && smsAccepted)
                        Log.e("Status", "Permison Granted");
                    else {
                        Log.e("Status", "Permison  Not Granted");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel("You need to allow access to all the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{READ_CONTACTS, CAMERA, ACCESS_FINE_LOCATION, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, READ_PHONE_STATE, SEND_SMS, RECORD_AUDIO},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    //check bluetooth permissions
    private boolean checkReadBluetoothPermission() {
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, PackageManager.PERMISSION_GRANTED);
            return true;
        } else {
            return false;
        }
    }

    //check the run-timer permissions
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CONTACTS);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int result3 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int result4 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result5 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);
        int result6 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result7 = ContextCompat.checkSelfPermission(getApplicationContext(), SEND_SMS);
        //int result8 = ContextCompat.checkSelfPermission(getApplicationContext(), BLUETOOTH);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED &&
                result3 == PackageManager.PERMISSION_GRANTED && result4 == PackageManager.PERMISSION_GRANTED && result5 == PackageManager.PERMISSION_GRANTED &&
                result6 == PackageManager.PERMISSION_GRANTED && result7 == PackageManager.PERMISSION_GRANTED/*&& result8== PackageManager.PERMISSION_GRANTED*/;
    }

    //check the run
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS, CAMERA, ACCESS_FINE_LOCATION, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, READ_PHONE_STATE, RECORD_AUDIO, SEND_SMS}, PERMISSION_REQUEST_CODE);
    }

    //show the alert dialog
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(LoginUserActivity.this)
                .setMessage(message)
                .setPositiveButton(getResources().getString(R.string.text_ok), okListener)
                .setNegativeButton(getResources().getString(R.string.text_cancel), null)
                .create()
                .show();
    }

    @Override
    public void onBackPressed() {
        if (status) {
            et_One.setText("");
            et_two.setText("");
            et_three.setText("");
            et_four.setText("");
            et_five.setText("");
            et_six.setText("");
            passwordLayout.setVisibility(View.GONE);
            mLoginButton.setVisibility(View.GONE);
            emailLayout.setVisibility(View.VISIBLE);
            mLoginEmailView.setText("");
            status = false;
        } else {
            LoginUserActivity.this.finish();
        }
    }

    //choose the languages
    private void setLocaleLanguage(String language) {
        locale = new Locale(language);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
        languagesLayout.setVisibility(View.GONE);
        loginLayout.setVisibility(View.VISIBLE);
    }

    //show the alert dialog when test failed
    private void showLoginFailedAlertDialog(String message) {
        showLoginAlertDialog(getApplicationContext(), message, false);
    }

    //show the alert dialog
    public void showLoginAlertDialog(Context context, String message, Boolean status) {
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setMessage(message);
        alertDialog.setButton(getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dismissProgressDialog();
                mLoginEmailView.setText("");
                et_One.setText("");
                et_two.setText("");
                et_three.setText("");
                et_four.setText("");
                et_five.setText("");
                et_six.setText("");
                et_One.requestFocus();
                alertDialog.dismiss();
            }
        });
        if (!isFinishing()) {
            alertDialog.show();
            TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
            textView.setTextSize(18);
        } else {
            alertDialog.dismiss();
        }
    }

    private void textwatcher() {
        //first edittext
        et_One.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (et_One.getText().length() == 1) {
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (et_One.getText().length() == 1) {
                    et_One.clearFocus();
                    et_two.requestFocus();
                    et_One.setCursorVisible(true);
                }
                if (et_One.getText().toString().equalsIgnoreCase("")) {
                    et_One.requestFocus();
                }
            }
        });

        //second edittext
        et_two.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (et_two.getText().length() == 1) {
                    et_two.clearFocus();
                    et_three.requestFocus();
                    et_three.setCursorVisible(true);
                }
                if (et_two.getText().toString().equalsIgnoreCase("")) {
                    et_two.clearFocus();
                    et_One.requestFocus();
                    et_One.setCursorVisible(true);
                }
            }
        });

        //third edittext
        et_three.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (et_three.getText().length() == 1) {
                    et_three.clearFocus();
                    et_four.requestFocus();
                    et_four.setCursorVisible(true);
                }
                if (et_three.getText().toString().equalsIgnoreCase("")) {
                    et_three.clearFocus();
                    et_two.requestFocus();
                    et_two.setCursorVisible(true);
                }
            }
        });

        //four edittext
        et_four.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (et_four.getText().length() == 1) {
                    et_four.clearFocus();
                    et_five.requestFocus();
                    et_five.setCursorVisible(true);
                }
                if (et_four.getText().toString().equalsIgnoreCase("")) {
                    et_four.clearFocus();
                    et_three.requestFocus();
                    et_three.setCursorVisible(true);
                }
            }
        });

        //five edittext
        et_five.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (et_five.getText().length() == 1) {
                    et_five.clearFocus();
                    et_six.requestFocus();
                    et_six.setCursorVisible(true);
                }
                if (et_five.getText().toString().equalsIgnoreCase("")) {
                    et_five.clearFocus();
                    et_four.requestFocus();
                    et_four.setCursorVisible(true);
                }
            }
        });

        //six edittext
        et_six.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (et_six.getText().length() == 0) {
                    et_six.clearFocus();
                    et_five.requestFocus();
                    et_five.setCursorVisible(true);
                }
                if (et_six.getText().length() == 1) {
                }
                String pass = et_One.getText().toString() + et_two.getText().toString() +
                        et_three.getText().toString() + et_four.getText().toString() + et_five.getText().toString() +
                        et_six.getText().toString();
                if (pass.length() == 6) {
                    mLoginButton.setEnabled(true);
                } else {
                    mLoginButton.setEnabled(false);
                }
            }
        });
    }

    public class AsteriskPasswordTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
        }

        private class PasswordCharSequence implements CharSequence {
            private CharSequence mSource;

            public PasswordCharSequence(CharSequence source) {
                mSource = source; // Store char sequence
            }

            public char charAt(int index) {
                return '*'; // This is the important part
            }

            public int length() {
                return mSource.length(); // Return default
            }

            public CharSequence subSequence(int start, int end) {
                return mSource.subSequence(start, end); // Return default
            }
        }
    }

}

