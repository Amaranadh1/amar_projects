package com.inndata.mapitt.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.inndata.mapitt.R;
import com.inndata.mapitt.network.ConnectionDetector;
import com.inndata.mapitt.session.Cache;
import com.inndata.mapitt.session.CatchValue;
import com.inndata.mapitt.session.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class LoginActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 200;
    private String JSON_URL = "https://packitt-packitt.b9ad.pro-us-east-1.openshiftapps.com/packitt_android/rest/search/geologin?";
    private String BASE_URL;
    private TextView login, register;
    private AutoCompleteTextView emailEdit;
    private EditText passwordEdit;
    private String email, password;
    ProgressDialog progressDoalog;
    Boolean isInternetPresent = false;
    ConnectionDetector connectionDetector;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEdit = findViewById(R.id.auto_complete_email);
        passwordEdit = findViewById(R.id.edit_password);
        login = findViewById(R.id.text_login);
        register = findViewById(R.id.text_register);
        progressDoalog = new ProgressDialog(LoginActivity.this);
        connectionDetector = new ConnectionDetector(LoginActivity.this);
        isInternetPresent = connectionDetector.isConnectingToInternet();
        session = new SessionManager(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        if (!checkPermission()) {
            //check the permissions
            requestPermission();
        } else {
            //Login();
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetPresent) {
                    loginProcedure();
                } else {
                    ShowNoInternetDialog();
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
    }

    private void loginProcedure() {
        email = emailEdit.getText().toString();
        password = passwordEdit.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailEdit.setError("Please enter email address");
            emailEdit.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            passwordEdit.setError("Please enter password");
            passwordEdit.requestFocus();
        } else {
            BASE_URL = JSON_URL+"email="+email+"&password="+password;
            String URL = BASE_URL.replaceAll(" ","");
            Log.e("LoginActivity: ", " BASE_URL: " + URL);
            showProgressDialog();
            loadLogin(email, URL);
        }
    }

    private void loadLogin(String email, String base_url) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, base_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject jsonObject1 = jsonObject.getJSONObject("results");
                            int resultCode = jsonObject1.getInt("resultcode");
                            String status = jsonObject1.getString("status");
                            Log.e("LoginActivity: ", " result_code " + resultCode);
                            Log.e("LoginActivity: ", " result_status " + status);
                            if (resultCode == 200) {
                                Cache.putData(CatchValue.EMAIL, LoginActivity.this, email, Cache.CACHE_LOCATION_DISK);
                                Log.e("email: ", " @@@ " + email);
                                progressDoalog.dismiss();
                                session.createLoginSession(String.valueOf(email), email);
                                Intent loginIntent =  new Intent(LoginActivity.this, MapsActivity.class);
                                startActivity(loginIntent);
                                finish();
                            } else if (resultCode == 401) {
                                progressDoalog.dismiss();
                                loginFailed("Login failed, please try again.");
                            } else {
                                progressDoalog.dismiss();
                                loginFailed("Login failed, please try again.");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDoalog.dismiss();
                        Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        //creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void loginFailed(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int result3 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result4 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED &&
                result3 == PackageManager.PERMISSION_GRANTED && result4 == PackageManager.PERMISSION_GRANTED;
    }

    //check the run
    private void requestPermission() {
        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{CAMERA, ACCESS_FINE_LOCATION, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
    }

    //show the alert dialog
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(LoginActivity.this)
                .setMessage(message)
                .setPositiveButton(getResources().getString(R.string.text_ok), okListener)
                .setNegativeButton(getResources().getString(R.string.text_cancel), null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean readAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean writeAccpeted = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && locationAccepted && readAccepted && writeAccpeted)
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
                                                    requestPermissions(new String[]{CAMERA, ACCESS_FINE_LOCATION, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, READ_PHONE_STATE},
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

    //Alert dialog for internet isn't available
    public void ShowNoInternetDialog() {
        showAlertDialog(LoginActivity.this, getResources().getString(R.string.text_no_internet_connection),
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

    private void showProgressDialog() {
        progressDoalog.setMessage("Loading....");
        progressDoalog.setCancelable(false);
        progressDoalog.show();
    }

}
