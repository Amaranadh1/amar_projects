package com.deepwares.checkpointdwi.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.StrictMode;
import android.os.TransactionTooLargeException;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.deepwares.checkpointdwi.BuildConfig;
import com.deepwares.checkpointdwi.R;
import com.deepwares.checkpointdwi.adapters.DrawerMenuAdapter;
import com.deepwares.checkpointdwi.adapters.TabAdapter;
import com.deepwares.checkpointdwi.entities.BACRecord;
import com.deepwares.checkpointdwi.entities.HistoryProvider;
import com.deepwares.checkpointdwi.facetracker.EnrollActivity;
import com.deepwares.checkpointdwi.fragments.HelpFragment;
import com.deepwares.checkpointdwi.fragments.HomeFragment;
import com.deepwares.checkpointdwi.localdatabase.CheckBACDB;
import com.deepwares.checkpointdwi.network.ConnectionDetector;
import com.deepwares.checkpointdwi.report.SideMenu;
import com.deepwares.checkpointdwi.service.BluetoothHelper;
import com.deepwares.checkpointdwi.service.BluetoothLeService;
import com.deepwares.checkpointdwi.service.CheckBACApp;
import com.deepwares.checkpointdwi.service.LocationService;
import com.deepwares.checkpointdwi.service.Logger;
import com.deepwares.checkpointdwi.service.ReportService;
import com.deepwares.checkpointdwi.session.Cache;
import com.deepwares.checkpointdwi.session.CatchValue;
import com.deepwares.checkpointdwi.session.SessionManager;
import com.deepwares.checkpointdwi.slidemenu.AboutActivity;
import com.deepwares.checkpointdwi.slidemenu.ChecbacActiveActivity;
import com.deepwares.checkpointdwi.slidemenu.HistoryActivity;
import com.deepwares.checkpointdwi.slidemenu.LegalActivity;
import com.deepwares.checkpointdwi.slidemenu.RequirementsActivity;
import com.deepwares.checkpointdwi.slidemenu.SettingsActivity;
import com.deepwares.checkpointdwi.slidemenu.SupportActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.twilio.client.impl.analytics.EventType;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import BACtrackAPI.API.BACtrackAPI;
import BACtrackAPI.API.BACtrackAPICallbacks;
import BACtrackAPI.Constants.BACTrackDeviceType;
import BACtrackAPI.Exceptions.BluetoothLENotSupportedException;
import BACtrackAPI.Exceptions.BluetoothNotEnabledException;
import io.fabric.sdk.android.Fabric;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity implements HomeFragment.BlueToothActivation, LocationListener, HelpFragment.LocationHelper, HomeFragment.PanicHandler, BACtrackAPICallbacks {

    private BACtrackAPI baCtrackAPI;
    private Set<BACtrackAPICallbacks> listeners = new HashSet<>();
    private Handler mHandler = new Handler(Looper.getMainLooper());
    ViewPager mViewPager;
    TabAdapter mAdapter;
    private static final UUID MY_UUID = UUID.randomUUID();
    private static final String NAME = "checkpointdwi";
    private boolean isBluetoothSupported, isBluetoothEnabled;
    List<String> devices = new ArrayList<String>();
    private static final int REQUEST_ENABLE_BT = 100;
    private static final int HOME_FRAGMENT_IDX = 0;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String[] tabTitles = new String[]{"Breathlayzer", "History", "Settings"};
    private static final int[] icons = new int[]{R.drawable.ic_menu_home, R.drawable.ic_menu_recent_history, R.drawable.ic_settings};
    public static final String DEVICE = "DEVICE";
    public static final String BAC_VALUE = "BAC_VALUE";
    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String TEST_COUNT = "TEST_COUNT";
    public static final String BATTERY_STATUS = "BATTERY_STATUS";
    public static final String PHOTO = "PHOTO";
    public static final String ISSC_PROPRIETARY_SERVICE_UUID = "49535343-fe7d-4ae5-8fa9-9fafd205e455";
    String package_name = "com.deepwares.checkpointdwi";
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    BluetoothDevice btDevice, marsBtdevice;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private int mConnectionState = STATE_DISCONNECTED;
    private static int BREATHTEST_REQUEST_CODE = 10;
    private static int PANIC_REQUEST_CODE = 11;
    private static int BAC_REQUEST_CODE = 123;
    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public static final String UUIDSTR_ISSC_TRANS_TX = "49535343-1e4d-4bd9-ba61-23c647249616";
    public static final String ABT_HANDSHAKE = "$abt_HANDS_SHAKE";
    protected LocationManager locationManager;
    Location currentLocation;
    Dialog dialog;
    AlertDialog alertDialog;
    private ImageView sideMenuBar;
    private DrawerLayout mDrawerLayout;
    private DrawerMenuAdapter mMenuAdapter;
    private ListView mDrawerList;
    LinearLayout sideLayout;
    ArrayList<SideMenu> menuItems;
    private RecyclerView recyclerView;
    List_BAC_Adapter bacRecordAdapter;
    private List<BACRecord> notificationList = new ArrayList<>();
    private LinearLayout connectLayout, ll_bacBar, connectedLayout, backLayout;
    private TextView textDeviceName, tv_bacValue, textStartTest;
    List<BACRecord> recordsList;
    BACRecord bacRecord;
    private TextView viewHistory;
    private ImageView backImage, batteryImage;
    private TextView backText;
    private RelativeLayout statusLayout;
    private TextView deviceStatusText, deviceNameText, startTestText;
    //Check Internet
    Boolean isInternetPresent = false;
    ConnectionDetector connectionDetector;
    CheckBACDB CHECKBACDB;
    String bacDate, bacTime, dummy;
    double bacResult;
    Date date;
    ProgressDialog progressDialog;
    public LinearLayout progressBarLayout, ll_result, result_layout, ll_home;
    private Date currentTime, startedAt_time;
    Button button_ok;
    ImageView back_image_view;
    //Notification
    private LinearLayout notificationLayout;
    private TextView notificationStartTest;
    private TextView notificationTimer, text_backtestHistory, result_text;
    CountDownTimer timer;
    private static final int PERMISSION_REQUEST_CODE = 200;
    ParseUser user;
    private boolean mAlreadyStartedService = false;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    //Pass the values for local db
    private String resultDate, resultTime;
    Double latitudeFile, longitudeFile;
    private String resultStatus, randomPassword;
    String videoFile, testCountValue, batteryStatus, deviceName;
    Double bacValue;
    List<BACRecord> bacResultRecordsList;
    private SwipeRefreshLayout swipeRefreshLayout;
    BACRecord notification;
    List<String> resultList = null;
    private boolean activeValue = false, faceValue = false, activeValue_backend;
    String failDate, failTime, failStatus;
    double failBAC, failLati, failLongi;
    String failVideo, crashTime, crashDate;
    File file;
    String failResultDate, failResultTime, failResultVideo;
    double failResultBAC, failResultLati, failResultLongi;
    Date crashCurrentDate;
    private BluetoothHelper bluetoothHelper;
    private BluetoothDevice deviceToConnect;
    private BluetoothLeService mBluetoothLeService;
    ArrayList<Byte> byteArrayList, byteArrayListtoSend;
    boolean receiverTest = false, serviceTest = false;
    private List<BluetoothDevice> foundDevices;
    private ImageView profilePicImageView;
    int versionCode;
    private String versionName, androidOS, brand, model, failDeviceName, failResultDeviceName;
    private String failDateTime, time24, replaceTime, picStatus;
    private static Handler handler = new Handler(Looper.getMainLooper());
    //BluetoothGatt gattcoonectio;
    BluetoothGattCharacteristic readCharacteristic;
    SessionManager sessionManager;
    BluetoothGattService bluetoothGattService;
    private Animation startAnimation;
    static Boolean faceMatchFailedAtBAC = false;
    Bitmap bacImageBitmap;
    private int imageWidth, imageHeight, passwordSize = 6;
    private boolean faceMatchBAC;
    private boolean failFaceMatch, failResultFaceMatch;
    private boolean LocationStatus = false, StorageStatus = false, CameaStatus = false, MicrophoneStatus = false, NotificationStatus = false;
    int result, result1, result2, result3, result4, result5, result6, result7;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    Boolean active_fr = false;
    private Boolean notiStatus, legal, pic;
    static String value="", faceRatio = "";
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private static final long SCAN_PERIOD = 10000;
    private Boolean frStatus = false;
    private FirebaseAnalytics mFirebaseAnalytics;
    Bundle params;

    //scanning the bluetooth devices
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("Device_name", "Device detected : DEVICE NAME - " + device.getName() + " | DEVICE MAC - " + device.getAddress());
                            //devices.add(device.getName() + "\n" + device.getAddress());
                            if (device.getName() != null && device.getName().equalsIgnoreCase("ALCOREAL")) {
                                btDevice = device;
                                Log.e(TAG, "**************************BLUETHOOTH DEVICE ********************");
                                Log.d(TAG, "Device found : DEVICE NAME - " + device.getName() + " | DEVICE MAC - " + device.getAddress());
                                Log.e(TAG, "Device found : DEVICE NAME - " + device.getName() + " | DEVICE MAC - " + device.getAddress());
                                launchBreathlyzer(device);
                            } else if (device.getName() != null && device.getName().contains("HM")) {
                                marsBtdevice = device;
                                initConnection(device);
                                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            } else if (device.getName() != null && device.getName().equalsIgnoreCase("SmartBreathalyzer")) {
                                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            }
                        }
                    });
                }
            };

    //launching the bluetooth devices
    private void launchBreathlyzer(BluetoothDevice device) {
        Log.e("###", "launchBreathalyzer()");
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        //connect the Gatt device
        connectGatt(device);
    }

    public void resetBluetoothGattServer() {
        if (mBluetoothGatt != null) mBluetoothGatt.close();
    }

    boolean activityStarted = false;
    BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                //broadcastUpdate(intentAction);
                Log.e("###", "Connected to GATT server" + gatt.discoverServices());
                Log.d(TAG, "Connected to GATT server.");
                Log.d(TAG, "Attempting to start service discovery:" + gatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.e(TAG, "Disconnected from GATT server.");
                Log.e("###", "Disconnected_from_GATT_server");

                mBluetoothAdapter.enable();
                mBluetoothAdapter.startLeScan(mLeScanCallback);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deviceStatusText.setText(getResources().getString(R.string.text_connect_breathalyzer));
                        deviceStatusText.setBackground(getResources().getDrawable(R.drawable.connect_status));
                        deviceNameText.setText(getResources().getString(R.string.frame_1));
                        deviceNameText.setTextSize(16);
                        statusLayout.setVisibility(View.VISIBLE);
                        startTestText.setVisibility(View.GONE);

                        mBluetoothAdapter.enable();
                        mBluetoothAdapter.startLeScan(mLeScanCallback);
                        Log.e("Started_again", "YEs");
                    }
                });
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e("###", "GATT_SUCCESS" + status);
                Log.e("### ", "GATT_SUCCESS" + gatt);
                Log.w(TAG, "onServicesDiscovered received: BluetoothGatt.GATT_SUCCESS" + status);
                for (BluetoothGattService service : gatt.getServices()) {
                    if ((service == null) || (service.getUuid() == null)) {
                        continue;
                    }
                    Log.e("### ", "service.getUuid().toString()" + service.getUuid().toString());
                    Log.d(TAG, "service.getUuid().toString() " + service.getUuid().toString());
                    if (service.getUuid().toString().equals(ISSC_PROPRIETARY_SERVICE_UUID)) {
                        readCharacteristic = service.getCharacteristic(UUID.fromString(UUIDSTR_ISSC_TRANS_TX));
                        gatt.setCharacteristicNotification(readCharacteristic, true);
                        BluetoothGattDescriptor descriptor = readCharacteristic.getDescriptor(
                                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                        Log.d(TAG, "Retrieving ISSC_PROPRIETARY_SERVICE_UUID service characteristics");
                    }
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                Log.e("### ", "onServicesDiscovered received: " + status);
            }
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            final String value = new String(characteristic.getValue()).trim();
            Log.d(TAG, "onCharacteristicChanged " + value);
            if (characteristic.getUuid().toString().equals(UUIDSTR_ISSC_TRANS_TX)) {
                synchronized (this) {
                    if (!activityStarted && ABT_HANDSHAKE.equals(value) || ABT_HANDSHAKE.startsWith(value) || value.startsWith(ABT_HANDSHAKE)) {
                        activityStarted = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                deviceStatusText.setText(getResources().getString(R.string.text_status_connected));
                                deviceStatusText.setBackground(getResources().getDrawable(R.drawable.connected_status));
                                deviceNameText.setText("iAlco");
                                statusLayout.setVisibility(View.GONE);
                                notiStatus = (Boolean) Cache.getData(CatchValue.NOTIFICATION, MainActivity.this);
                                startTestText.setVisibility(View.VISIBLE);
                            }
                        });

                        //Take the test BACtrack device connection
                        startTestText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                notificationLayout.setVisibility(View.GONE);
                                if (notiStatus.equals(true)) {
                                    alertDialog.dismiss();
                                }

                                //pass the breathtestactivity class
                                final Intent intent = new Intent(MainActivity.this, BreathTestActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                intent.putExtra(DEVICE, btDevice);
                                startActivityForResult(intent, BREATHTEST_REQUEST_CODE);
                            }
                        });
                    }
                }
            }
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            final String value = new String(characteristic.getValue()).trim();
            Log.d(TAG, "Characteristic read " + value);
        }
    };

    //connect the gatt device
    private void connectGatt(BluetoothDevice device) {
        Log.e("iAlco", "connectGatt()");
        mBluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
    }

    public static MainActivity INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fabric.with(this, new Crashlytics());

        //read the bluetooth permissions
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!checkPermission()) {
            //check the runtime permissions
            requestPermission();
        } else {
            //Login();
        }
        //check for bluetooth permissions
        checkReadBluetoothPermission();
        INSTANCE = this;
        // locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }

        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            alertMessageNoGPS();
        } else {}

        bluetoothHelper = BluetoothHelper.getSharedInstance(MainActivity.this);
        //bluetooth adapter
        if (!mBluetoothAdapter.isEnabled()) {
            //enabled the bluetooth
            activateBluetooth();
        } else {
            if (mBluetoothGatt != null) mBluetoothGatt.close();
            mBluetoothAdapter.enable();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mBluetoothAdapter.startDiscovery();
            //scan the BACtrack breathalyzer
            try {
                baCtrackAPI = new BACtrackAPI(MainActivity.this, baCtrackAPICallbacks);
                baCtrackAPI.startScan();
            } catch (BluetoothLENotSupportedException e) {
                Log.e(TAG, "BluetoothLENotSupportedException", e);
                e.printStackTrace();
            } catch (BluetoothNotEnabledException e) {
                Log.e(TAG, "BluetoothNotEnabledException", e);
            }
            MainActivity.this.addListener(this);
        }

        initViews();
        bacResultRecordsList = CHECKBACDB.getAllBACResultList();

        batteryOptimization();

        //onclick functionality for ok button
        button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
                ll_result.setVisibility(View.GONE);
                ll_home.setVisibility(View.VISIBLE);
            }
        });

        try {
            crashCurrentDate = new Date();
            crashDate = new SimpleDateFormat("MMM dd, yyyy").format(crashCurrentDate);
            crashTime = new SimpleDateFormat("HH:mm").format(crashCurrentDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isInternetPresent) {
            try {
                params.putString("User", ParseUser.getCurrentUser().getEmail());
                params.putString("Domain", "MainActivity");
                params.putString("ModuleName", "Open the MainActivity");
                params.putString("MobileModel", Build.MODEL);
                params.putString("MobileBrand", Build.BRAND);
                params.putString("MobileOS", Build.VERSION.RELEASE);
                params.putString("DeviceType", "Android");
                mFirebaseAnalytics.logEvent("MainActivity", params);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
                calendar.setTime(crashCurrentDate);
                Date time = calendar.getTime();
                SimpleDateFormat outputFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZ");
                String dateAsString = outputFmt.format(time);

                //send the data into sashido
                ParseObject notificationParse = new ParseObject("DetailedActivity");
                notificationParse.put("date", dateAsString);
                notificationParse.put("email", ParseUser.getCurrentUser().getEmail());
                notificationParse.put("activity", "User opened CheckBAC App at" + dateAsString);
                notificationParse.saveInBackground();
            } catch (Exception e) {
                e.printStackTrace();
                params.putString("Domain", "MainActivity");
                params.putInt("Code", 500);
                params.putString("ModuleName", "Open the MainActivity");
                params.putString("MobileModel", Build.MODEL);
                params.putString("MobileBrand", Build.BRAND);
                params.putString("MobileOS", Build.VERSION.RELEASE);
                params.putString("DeviceType", "Android");
                params.putString("ErrorDescription", e.getMessage());
                mFirebaseAnalytics.logEvent("MainActivity", params);
            }
        } else {}

        progressBarLayout.setVisibility(View.VISIBLE);
        getLocalRecords();
        //set the refresh functionality
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // cancle the Visual indication of a refresh
                        swipeRefreshLayout.setRefreshing(false);
                        if (isInternetPresent) {
                            //get the data from server/sashido
                            getTestHistoryFormBACentry();
                        } else {
                            //get the data from local database
                            getLocalRecords();
                        }
                    }
                }, 2000);
            }
        });

        showProgressDialog();
        menuItems = new ArrayList<SideMenu>();
        if (isInternetPresent) {
            try {
                ParseQuery<ParseObject> notificationListQuery = ParseQuery.getQuery("_User");
                notificationListQuery.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
                notificationListQuery.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> scoreList, ParseException e) {
                        if (e == null) {
                            if (scoreList.size() > 0) {
                                for (ParseObject comment : scoreList) {
                                    picStatus = comment.getString("userProfilePicture");
                                    if (TextUtils.isEmpty(picStatus) || picStatus.equalsIgnoreCase("")) {
                                        checkPicStatus();
                                    }
                                    activeValue = comment.getBoolean("hasActiveEnabled");
                                    faceValue = comment.getBoolean("hasFRenabled");
                                    String userStatus = comment.getString("status");
                                    Log.e("user_status", userStatus);
                                    if (!userStatus.equalsIgnoreCase("active")) {
                                        showProgressDialog();
                                        showUserModeAlertDialog();
                                    }
                                    menuItems(faceValue);
                                    Log.e("Face_inside", String.valueOf(faceValue));
                                    if (activeValue) {
                                        //Start notification with timer
                                        getNotificationTimer();
                                        appUpdationProcess();
                                    }
                                    checkActiveFRValues(activeValue, faceValue);
                                    checkFREnrollMent(faceValue);
                                    sessionManager.saveFR_Active(String.valueOf(activeValue), String.valueOf(faceValue));
                                }
                            }
                        } else {
                            Log.d("score", "Error: " + e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                Log.e("checkbac_active", "Get the active history" + e.getMessage());
            }
        } else {
            //getting valus from sessionmanager
            HashMap<String, String> user = sessionManager.getUserDetails();
            activeValue = Boolean.parseBoolean(user.get(SessionManager.KEY_ACTIVE));
            faceValue = Boolean.parseBoolean(user.get(SessionManager.KEY_FR));
            menuItems(faceValue);
        }

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        //checks for app update from google play
        web_update();
        if (web_update()) {
            Alert("Update Available", "Do you want to update?");
        }

        versionCode = BuildConfig.VERSION_CODE;
        versionName = BuildConfig.VERSION_NAME;
        androidOS = Build.VERSION.RELEASE;
        brand = Build.BRAND; // for getting BrandName
        model = Build.MODEL;
        Log.e("Device_info", "versionCode==" + "" + versionCode + "\n" +
                "versionName==" + "" + versionName + "\n" + "androidOS==" + "" + androidOS + "\n" +
                "brand==" + "" + brand + "\n" + "model==" + "" + model);
        if (isInternetPresent) {
            try {
                ParseQuery<ParseObject> bacEntryQuery = ParseQuery.getQuery("UserDevicesInfo");
                bacEntryQuery.whereEqualTo("userId", ParseUser.getCurrentUser());
                bacEntryQuery.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> scoreList, ParseException e) {
                        Log.e("Response", "Success");
                        if (e == null) {
                            Log.e("Response", "Score List ----" + scoreList.size());
                            if (scoreList.size() > 0) {
                                for (ParseObject comment : scoreList) {
                                    String objectId;
                                    objectId = comment.getObjectId();
                                    Log.e("Dat_format", "" + objectId);
                                    if (!TextUtils.isEmpty("deviceType") || !TextUtils.isEmpty("deviceModel") || !TextUtils.isEmpty("currentBuild") || !TextUtils.isEmpty("currentBuildVersion") ||
                                            !TextUtils.isEmpty("deviceVersion")) {
                                        if (sessionManager.isLoggedIn()) {
                                            UpdateRecord(objectId);
                                        }
                                    } else {
                                        if (!comment.getString("deviceType").equalsIgnoreCase("Android") ||
                                                !comment.getString("deviceModel").equalsIgnoreCase(brand + " " + model) ||
                                                !comment.getString("currentBuild").equalsIgnoreCase(versionName) ||
                                                !comment.getString("currentBuildVersion").equalsIgnoreCase(String.valueOf(versionCode)) ||
                                                !comment.getString("deviceVersion").equalsIgnoreCase(androidOS)) {
                                            if (sessionManager.isLoggedIn()) {
                                                UpdateRecord(objectId);
                                            }
                                        }
                                    }
                                }
                                Log.e("Scorelist_exits", "Exits`");
                            } else if (scoreList.size() == 0) {
                                Log.e("Scorelist_exits", "Not Exits ----");
                                sendFirstTime();
                            }
                        } else {
                            e.printStackTrace();
                            Log.e("Response", "Fail");
                            Log.e("score", "Error: " + e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "user device info" + e.getMessage());
            }
        } else {}
    }

    private void initViews() {
        sideMenuBar = (ImageView) findViewById(R.id.home_page_side_menu_icon);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        sideLayout = (LinearLayout) findViewById(R.id.side_menu_layout);
        backLayout = (LinearLayout) findViewById(R.id.layout_back);
        mDrawerList = (ListView) findViewById(R.id.side_menu_list);
        recyclerView = (RecyclerView) findViewById(R.id.list_view);
        viewHistory = (TextView) findViewById(R.id.text_view_history);
        backImage = (ImageView) findViewById(R.id.back_pic);
        backText = (TextView) findViewById(R.id.text_view_back);
        statusLayout = (RelativeLayout) findViewById(R.id.status_layout);
        deviceStatusText = (TextView) findViewById(R.id.textView_device_status);
        deviceNameText = (TextView) findViewById(R.id.textView_device_name);
        startTestText = (TextView) findViewById(R.id.text_start_test);
        connectLayout = (LinearLayout) findViewById(R.id.connect_layout);
        ll_bacBar = (LinearLayout) findViewById(R.id.ll_bacBar);
        connectedLayout = (LinearLayout) findViewById(R.id.connected_layout);
        textDeviceName = (TextView) findViewById(R.id.text_device_name);
        textStartTest = (TextView) findViewById(R.id.text_start_test);
        tv_bacValue = (TextView) findViewById(R.id.tv_bacValue);
        progressBarLayout = (LinearLayout) findViewById(R.id.progressBar_layout);
        notificationLayout = (LinearLayout) findViewById(R.id.notification_layout);
        notificationStartTest = (TextView) findViewById(R.id.notification_start_test);
        notificationTimer = (TextView) findViewById(R.id.notification_timer);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        ll_home = (LinearLayout) findViewById(R.id.ll_home);
        ll_result = (LinearLayout) findViewById(R.id.ll_result);
        result_layout = (LinearLayout) findViewById(R.id.result_layout);
        result_text = (TextView) findViewById(R.id.result_text);
        batteryImage = (ImageView) findViewById(R.id.image_battery);
        button_ok = (Button) findViewById(R.id.button_ok);
        text_backtestHistory = (TextView) findViewById(R.id.text_backtestHistory);
        startAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_animation);
        alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        connectionDetector = new ConnectionDetector(MainActivity.this);
        isInternetPresent = connectionDetector.isConnectingToInternet();
        sessionManager = new SessionManager(this);
        CHECKBACDB = new CheckBACDB(MainActivity.this);
        notificationList = HistoryProvider.getInstance(getApplicationContext()).getHistory();
        progressDialog = new ProgressDialog(MainActivity.this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(MainActivity.this);
        params = new Bundle();

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Open the MainActivity");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "CheckBAC");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "MainActivity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
        mFirebaseAnalytics.setMinimumSessionDuration(20000);
        mFirebaseAnalytics.setSessionTimeoutDuration(500);

    }

    private void batteryOptimization() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = MainActivity.this.registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
    }
    private void checkPicStatus() {
        dismissProgressDialog();
        Intent picture_intent = new Intent(MainActivity.this, PictureActivity.class);
        startActivity(picture_intent);
        finish();
    }

    private void checkActiveFRValues(Boolean active, Boolean face) {
        Boolean activeValue_sasido, faceVale_sashido;
        activeValue_sasido = active;
        faceVale_sashido = face;
        HashMap<String, String> user = sessionManager.getUserDetails();
        Boolean activeValue_session = Boolean.parseBoolean(user.get(SessionManager.KEY_ACTIVE));
        Boolean faceValue_session = Boolean.parseBoolean(user.get(SessionManager.KEY_FR));
        if (activeValue_sasido != activeValue_session) {
            Cache.putData(CatchValue.USER_ACTIVE_FR, MainActivity.this, true, Cache.CACHE_LOCATION_DISK);
        } else if (faceVale_sashido != faceValue_session) {
            Cache.putData(CatchValue.USER_ACTIVE_FR, MainActivity.this, true, Cache.CACHE_LOCATION_DISK);
        } else {
            Cache.putData(CatchValue.USER_ACTIVE_FR, MainActivity.this, false, Cache.CACHE_LOCATION_DISK);
        }
    }

    private void refresh() {
        swipeRefreshLayout.setRefreshing(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // cancle the Visual indication of a refresh
                swipeRefreshLayout.setRefreshing(false);
                if (isInternetPresent) {
                    //get the data from server/sashido
                    getTestHistoryFormBACentry();
                } else {
                    //get the data from local database
                    getLocalRecords();
                }
            }
        }, 11000);
//        }, 2000);
    }


    // Enroll when user didnt
    private void checkFREnrollMent(Boolean faceValue) {
        if (isInternetPresent) {
            try {
                Boolean face_intent = faceValue;
                Log.e("faceValue_test", String.valueOf(faceValue));
                if (face_intent) {
                    user = ParseUser.getCurrentUser();
                    Boolean enroll =false;
                    if (user.has("enforce_enroll")) {
                        enroll = user.getBoolean("enforce_enroll");
                    }
                    if (!enroll) {
                        Intent intent = new Intent(MainActivity.this, EnrollActivity.class);
                        intent.putExtra("Intent", "Main_complsory");
                        startActivity(intent);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "checke_fr_enrollment" + e.getMessage());
            }
        } else {}
    }

    /*
     Timer will start when CheckBac Active enable.
    */
    private void getNotificationTimer() {
        currentTime = new Date();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ActiveNotify");
        query.whereEqualTo("user_id", ParseUser.getCurrentUser());
        query.orderByDescending("createdAt");
        query.setLimit(1);
        query.whereEqualTo("notification", false);
        try {
            Crashlytics.setString("domain", "MainActivity");
            Crashlytics.setInt("code", 200);
            Crashlytics.setString("Time", crashTime);
            Crashlytics.setString("ModuleName", "CheckBACActiveNotifyDisplay");
            Crashlytics.setString("deviceType", "Android");

            List<ParseObject> gameScore = query.find();
            if (gameScore.size() > 0) {
                for (ParseObject comment : gameScore) {
                    startedAt_time = comment.getCreatedAt();
                }
                long diff_elapsed = currentTime.getTime() - startedAt_time.getTime();
                long seconds = diff_elapsed / 1000;
                long minutes = seconds / 60;

                //get the current date and time.
                Calendar cal = Calendar.getInstance();
                cal.setTime(startedAt_time);
                cal.add(Calendar.MINUTE, 5);
                startedAt_time = cal.getTime();
                if (minutes < 5) {
                    diff_elapsed = startedAt_time.getTime() - currentTime.getTime();
                    Log.e("withinTime", String.valueOf(minutes));
                    notificationLayout.setVisibility(View.VISIBLE);
                    notificationStartTest.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.text_you_are_in_motion), Toast.LENGTH_SHORT).show();
                    notificationTimer.setVisibility(View.VISIBLE);
                    notificationTimer.setBackground(getResources().getDrawable(R.drawable.red_background));

                    //Set count down timer
                    timer = new CountDownTimer(diff_elapsed, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            Cache.putData(CatchValue.TIMER, MainActivity.this, true, Cache.CACHE_LOCATION_DISK);
                            notificationTimer.setText("" + String.format("%02d : %02d",
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                        }

                        @Override
                        public void onFinish() {
                            notificationTimer.setVisibility(View.GONE);
                            //show alert dialog when test failed
                            showTestFailedAlertDialog();
                        }
                    }.start();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            Crashlytics.setString("domain", "MainActivity");
            Crashlytics.setInt("code", e.getCode());
            Crashlytics.setString("Time", crashTime);
            Crashlytics.setString("ErrorDescription", e.getMessage());
            Crashlytics.setString("ModuleName", "CheckBACActiveNotifyDisplay");
            Crashlytics.setString("deviceType", "Android");
        }
    }

    private void UpdateRecord(String objectId) {
        try {
            Log.e("getFirstRecord", "getFirstRecord");
            ParseQuery<ParseObject> query = ParseQuery.getQuery("UserDevicesInfo");
            // Retrieve the object by id
            query.getInBackground(objectId, new GetCallback<ParseObject>() {
                public void done(ParseObject gameScore, ParseException e) {
                    if (e == null) {
                        gameScore.put("userId", ParseUser.getCurrentUser());
                        gameScore.put("deviceType", "Android");
                        gameScore.put("deviceModel", brand + " " + model);
                        gameScore.put("currentBuild", versionName);
                        gameScore.put("deviceVersion", androidOS);
                        gameScore.put("currentBuildVersion", String.valueOf(versionCode));
                        gameScore.saveInBackground();

                        Crashlytics.setString("domain", "MainActivity");
                        Crashlytics.setInt("code", 200);
                        Crashlytics.setString("UserName", ParseUser.getCurrentUser().getEmail());
                        Crashlytics.setString("Time", crashTime);
                        Crashlytics.setString("ModuleName", "UpdateRecord");
                        Crashlytics.setString("deviceType", "Android");

                    } else {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Get user device information" + e.getMessage());
            e.printStackTrace();
            Crashlytics.logException(e);
            Crashlytics.setString("domain", "MainActivity");
            Crashlytics.setString("UserName", ParseUser.getCurrentUser().getEmail());
            Crashlytics.setString("Time", crashTime);
            Crashlytics.setString("ModuleName", "UpdateRecord");
            Crashlytics.setString("ErrorDescription", e.getMessage());
            Crashlytics.setString("deviceType", "Android");
        }
    }

    private void sendFirstTime() {
        try {
            Log.e("Update", "Update");
            ParseObject BACEntry = new ParseObject("UserDevicesInfo");
            BACEntry.put("userId", ParseUser.getCurrentUser());
            BACEntry.put("deviceType", "Android");
            BACEntry.put("deviceModel", brand + " " + model);
            BACEntry.put("currentBuild", versionName);
            BACEntry.put("deviceVersion", androidOS);
            BACEntry.put("currentBuildVersion", String.valueOf(versionCode));
            //data will be save in background for server
            BACEntry.saveInBackground();

            Crashlytics.setString("domain", "MainActivity");
            Crashlytics.setInt("code", 200);
            Crashlytics.setString("UserName", ParseUser.getCurrentUser().getEmail());
            Crashlytics.setString("Time", crashTime);
            Crashlytics.setString("ModuleName", "SendFirstTime");
            Crashlytics.setString("deviceType", "Android");

        } catch (Exception e) {
            Log.e(TAG, "Send user devices information" + e.getMessage());
            e.printStackTrace();
            Crashlytics.logException(e);
            Crashlytics.setString("domain", "MainActivity");
            Crashlytics.setString("Time", crashTime);
            Crashlytics.setString("ModuleName", "SendFirstTime");
            Crashlytics.setString("ErrorDescription", e.getMessage());
            Crashlytics.setString("deviceType", "Android");
        }
    }


    //Get the data from local database
    @SuppressLint("LongLogTag")
    private void getLocalRecords() {
        notificationList.clear();
        ArrayList<String> recordsList = new ArrayList<String>();
        //Display the data from Local DB when internet isn't available
        bacResultRecordsList.clear();
        bacResultRecordsList = CHECKBACDB.getAllBACResultList();
        Log.e("bacResultRecordsList_size", "" + bacResultRecordsList.size());
        if (bacResultRecordsList.size() > 0) {
            for (int i = 0; i < bacResultRecordsList.size(); i++) {
                notificationList.add(new BACRecord(bacResultRecordsList.get(i).getDate(), bacResultRecordsList.get(i).getTime(),
                        bacResultRecordsList.get(i).getBac(), bacResultRecordsList.get(i).getLatitudeValue(),
                        bacResultRecordsList.get(i).getLongitudeValue(), bacResultRecordsList.get(i).getVideoFile(),
                        bacResultRecordsList.get(i).getStatus(), bacResultRecordsList.get(i).isFaceMatchFailBAC(),
                        bacResultRecordsList.get(i).getDeviceName()));
            }
            progressBarLayout.setVisibility(View.GONE);
            bacRecordAdapter = new List_BAC_Adapter(MainActivity.this, notificationList);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.this);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(bacRecordAdapter);
            recyclerView.setHasFixedSize(true);
            bacRecordAdapter.notifyDataSetChanged();

            if (isInternetPresent) {
                sendFailedRecords();   // Check if status is fail
            } else {}
        } else {
            if (isInternetPresent) {
                getTestHistoryFormBACentry();
            } else {}
        }
    }

    //send the data to server
    private void sendFailedRecords() {
        if (bacResultRecordsList.size() > 0) {
            resultList = new ArrayList<String>();
            for (int i = 0; i < bacResultRecordsList.size(); i++) {
                failDate = bacResultRecordsList.get(i).getDate();
                failTime = bacResultRecordsList.get(i).getTime();
                failBAC = bacResultRecordsList.get(i).getBac();
                failLati = bacResultRecordsList.get(i).getLatitudeValue();
                failLongi = bacResultRecordsList.get(i).getLongitudeValue();
                failVideo = bacResultRecordsList.get(i).getVideoFile();
                failStatus = bacResultRecordsList.get(i).getStatus();
                failFaceMatch = bacResultRecordsList.get(i).isFaceMatchFailBAC();
                failDeviceName = bacResultRecordsList.get(i).getDeviceName();
            }
            if (failStatus.equals("FAIL")) {
                Log.e("FAILStatus", "Yes" + failStatus);
                failResultDate = failDate;
                failResultTime = failTime;
                failResultBAC = failBAC;
                failResultLati = failLati;
                failResultLongi = failLongi;
                failResultVideo = failVideo;
                failResultFaceMatch = failFaceMatch;
                failResultDeviceName = failDeviceName;
                Log.e("@@@", "###" + failResultTime);
                SimpleDateFormat inFormat = new SimpleDateFormat("hh:mm aa");
                SimpleDateFormat outFormat = new SimpleDateFormat("HH:mm");
                try {
                    time24 = outFormat.format(inFormat.parse(failResultTime));
                    Log.e("@@@", "###" + time24);
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
                failDateTime = failResultDate + " " + time24;
                Log.e("LocalResultts", failResultDate + "---" + failResultTime + "---" + failResultBAC + "----" +
                        failResultLati + "----" + failResultLongi + "----" + failResultVideo + "----" + failResultFaceMatch);
                if (failDateTime != null && !failDateTime.isEmpty()) {
                    Date testFailDateTime = null;
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        testFailDateTime = dateFormat.parse(failDateTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.e("result = ", "==========>" + testFailDateTime);
                    if (testFailDateTime != null) {
                        Log.e("testFailDateTime", "yes");
                        Log.e("video_file: ", " ### " + failResultVideo);
                        //Local DB data send to the server
                        sendFailResults(testFailDateTime, failResultBAC, failResultLati, failResultLongi, failResultVideo, failResultFaceMatch, failResultDeviceName);
                    }
                }
            } else {
                Log.e("FAILStatus", "NO");
            }
        }
    }

    private void getTestHistoryFormBACentry() {
        try {
            ParseQuery<ParseObject> bacEntryQuery = ParseQuery.getQuery("BACEntry");
            bacEntryQuery.whereEqualTo("user", ParseUser.getCurrentUser());
            bacEntryQuery.orderByDescending("createdAt");
            bacEntryQuery.whereExists("testTakenTime");
            bacEntryQuery.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> scoreList, ParseException e) {
                    Log.e("Response", "Success");
                    progressBarLayout.setVisibility(View.GONE);
                    if (e == null) {
                        Crashlytics.setString("domain", "MainActivity");
                        Crashlytics.setInt("code", 200);
                        Crashlytics.setString("Time", crashTime);
                        Crashlytics.setString("ModuleName", "GettingBACEntryResults");
                        Crashlytics.setString("deviceType", "Android");
                        Log.e("Response", "Score List ----" + scoreList.size());
                        if (scoreList.size() > 0) {
                            CHECKBACDB.removeBACResultList();
                            for (ParseObject comment : scoreList) {
                                String createdDate;
                                createdDate = comment.getString("testTakenTime");
                                Log.e("Dat_format", "" + createdDate);
                                String[] splited = createdDate.split("\\s+");
                                Log.e("Dat_format", "" + createdDate);
                                bacDate = splited[0];
                                bacTime = splited[1];
                                dummy = splited[2];
                                try {
                                    final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    final Date dateObj = sdf.parse(bacTime);
                                    bacTime = new SimpleDateFormat("KK:mm a").format(dateObj);
                                } catch (java.text.ParseException e1) {
                                    e1.printStackTrace();
                                }
                                Log.e("BAC_TIME_DATE", bacDate + "----" + bacTime);
                                Log.e("Bacresult", "" + comment.getDouble("entry"));
                                bacResult = comment.getDouble("entry");
                                faceMatchBAC = comment.getBoolean("faceMatchFailedAtBAC");
                                resultStatus = "PASS";
                                CHECKBACDB.addChecBACResultList(new BACRecord(bacDate, bacTime, bacResult, 0.0, 0.0,
                                        null, resultStatus, faceMatchBAC, ""));
                            }
                            bacResultRecordsList.clear();
                            notificationList.clear();
                            //Data will be displaying from the local DB
                            bacResultRecordsList = CHECKBACDB.getAllBACResultList();
                            for (int i = 0; i < bacResultRecordsList.size(); i++) {
                                notificationList.add(new BACRecord(bacResultRecordsList.get(i).getDate(), bacResultRecordsList.get(i).getTime(),
                                        bacResultRecordsList.get(i).getBac(), bacResultRecordsList.get(i).getStatus()));
                            }
                            bacRecordAdapter = new List_BAC_Adapter(MainActivity.this, notificationList);
                            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.this);
                            recyclerView.setLayoutManager(mLayoutManager);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());
                            recyclerView.setAdapter(bacRecordAdapter);
                            recyclerView.setHasFixedSize(true);
                            bacRecordAdapter.notifyDataSetChanged();
                        } else {
                            text_backtestHistory.setText("Take your first BAC test");
                        }
                    } else {
                        progressBarLayout.setVisibility(View.GONE);
                        Log.e("Response", "Fail");
                        Log.e("score", "Error: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
            Crashlytics.setString("domain", "MainActivity");
            Crashlytics.setString("Time", crashTime);
            Crashlytics.setString("ErrorDescription", e.getMessage());
            Crashlytics.setString("ModuleName", "GettingBACEntryResults");
            Crashlytics.setString("deviceType", "Android");
        }
    }

    //Local DB data send to the server
    private void sendFailResults(Date testFailDateTime, double failResultBAC, double failResultLati,
                                 double failResultLongi, String failResultVideo, boolean failResultFaceMatch, String failResultDeviceName) {

        Log.e("video_file: ", " ### " + failResultVideo);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(testFailDateTime);
        Date time = calendar.getTime();
        SimpleDateFormat outputFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZ");
        outputFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateAsString = outputFmt.format(time);
        Log.e("dateAsString", dateAsString);

        checkFaceMatch(failResultBAC, failResultLati, failResultLongi, failResultVideo, failResultDeviceName, true, dateAsString);

    }

    @Override
    protected void onStart() {
        Log.e("OnResume_startLescan", "onStart");

            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mBluetoothAdapter.startDiscovery();

        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.e("OnResume_startLescan","onResume");
        if (!mBluetoothAdapter.isEnabled()) {
            //enabled the bluetooth
            activateBluetooth();
        } else {
//            MainActivity.this.mBluetoothAdapter.enable();
            MainActivity.this.mBluetoothAdapter.startLeScan(mLeScanCallback);
            MainActivity.this.mBluetoothAdapter.startDiscovery();
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                MainActivity.this.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            } catch (Exception e) {
                Log.e(TAG, "Update the Gatt receiver" + e.getMessage());
            }
        } else {
            try {
                MainActivity.this.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            } catch (Exception e) {
                Log.e(TAG, "Update the Gatt receiver" + e.getMessage());
            }
        }
        if (mBluetoothLeService != null && deviceToConnect != null && !mBluetoothLeService.isConnected()) {
            final boolean result = mBluetoothLeService.connect(deviceToConnect.getAddress());
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (!mBluetoothAdapter.isEnabled()) {
            //enabled the bluetooth
            activateBluetooth();
        } else {
            MainActivity.this.mBluetoothAdapter.enable();
            MainActivity.this.mBluetoothAdapter.startLeScan(mLeScanCallback);
            MainActivity.this.mBluetoothAdapter.startDiscovery();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (progressDialog != null) {
            dismissProgressDialog();
        }
        MainActivity.this.removeListener(this);
        MainActivity.this.disconnect();
        try {
            MainActivity.this.unregisterReceiver(mGattUpdateReceiver);
            if (mBluetoothGatt != null) {
                MainActivity.this.mBluetoothGatt.disconnect();
            }
        } catch (Exception e) {
            if (e.getCause() instanceof TransactionTooLargeException ||
                    e.getCause() instanceof DeadObjectException) {
            } else {
                throw new RuntimeException(e);
            }
        }
        try {
            deleteCache(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        mBluetoothAdapter.disable();
        super.onDestroy();
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    //connect with bluetooth device
    private void initConnection(BluetoothDevice device) {
        deviceToConnect = device;
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null && deviceToConnect != null && !mBluetoothLeService.isConnected()) {
            final boolean result = mBluetoothLeService.connect(deviceToConnect.getAddress());
        }
        bindBleService();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        mBluetoothAdapter.cancelDiscovery();
    }

    //connect with bluetooth service
    private void bindBleService() {
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        MainActivity.this.bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    //set service connection for marsbt device
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @SuppressLint("NewApi")
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceTest = true;
            mBluetoothLeService = ((BluetoothLeService.BluetoothLeBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Logger.log("Unable to initialize Bluetooth");
                Toast.makeText(MainActivity.this, "Unable to initialize Bluetooth", Toast.LENGTH_SHORT).show();
            }

            // Automatically connects to the device upon successful start-up initialization.
            if (deviceToConnect != null) {
                // showProgressDialog("Connecting", "Please wait...");
                mBluetoothLeService.connect(deviceToConnect.getAddress());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private void connectVidya() {
        byte[] data = {
                0x68,
                (byte) 0x42,
                (byte) 0x30,
                (byte) 0x30,
                (byte) 0x30,
                (byte) 0x09,
                (byte) 0x09,
                0x68,
                0x04,
                0x03,
                0x00,
                0x04,
                (byte) 0xFF,
                (byte) 0x01,
                (byte) 0x00,
                0x16
        };
        sendData(MainActivity.this, data);
    }

    //connect for marsbt device
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_BYTE_DATA_AVAILABLE);
        return intentFilter;
    }


    //connect the broadcast receiver for marsbt device
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @SuppressLint("LongLogTag")
        @Override
        public void onReceive(Context context, Intent intent) {
            receiverTest = true;
            final String action = intent.getAction();
            //Register the Broadcast Receiver with android oreo version
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                MainActivity.this.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            } else {
                registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            }
            if (mBluetoothLeService != null && deviceToConnect != null && !mBluetoothLeService.isConnected()) {
                final boolean result = mBluetoothLeService.connect(deviceToConnect.getAddress());
                Logger.log("Connect request result=" + result);
            }
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                connectVidya();
                //  updateConnectionState("GATT_CONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("Connection state", "Disconnected");
                        deviceStatusText.setText(getResources().getString(R.string.text_connect_breathalyzer));
                        deviceStatusText.setBackground(getResources().getDrawable(R.drawable.connect_status));
                        deviceNameText.setText(getResources().getString(R.string.frame_1));
                        deviceNameText.setTextSize(16);
                        statusLayout.setVisibility(View.VISIBLE);
                        startTestText.setVisibility(View.GONE);
                        mBluetoothAdapter.startLeScan(mLeScanCallback);
                        mBluetoothAdapter.startDiscovery();
                    }
                });

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.e("Services Discovered", "Service discovered");
                // displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //  String error = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                final StringBuilder stringBuilder = new StringBuilder(data.length);

                if (data != null && data.length > 0) {
                    for (byte byteChar : data)
                        stringBuilder.append(String.format("%02X ", byteChar));
                }

                Log.e("returnData_", "-------" + stringBuilder.toString());
                if (String.valueOf(stringBuilder).contains("81")) {
                    final Intent intent_send = new Intent(MainActivity.this, BreathTestActivity.class);
                    intent_send.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent_send.putExtra(DEVICE, deviceToConnect);
                    Bundle args = new Bundle();
                    args.putSerializable("ARRAYLIST", (Serializable) byteArrayListtoSend);
                    intent_send.putExtra("byteArrayListtoSend", args);
                    startActivityForResult(intent_send, BREATHTEST_REQUEST_CODE);
                    if (mGattUpdateReceiver != null) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            MainActivity.this.unregisterReceiver(mGattUpdateReceiver);
                        } else {
                            unregisterReceiver(mGattUpdateReceiver);
                        }
                    } else if (mServiceConnection != null) {
                        unbindService(mServiceConnection);
                    }
                } else if (String.valueOf(stringBuilder).contains("84")) {
                    byteArrayListtoSend = new ArrayList<>();
                    for (int c = 0; c < 8; c++) {
                        byte i = data[c];
                        byteArrayListtoSend.add(i);
                    }
                    if (deviceToConnect != null && deviceToConnect.getName().contains("HM")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                deviceNameText.setText("ALCOVISOR");
                                deviceStatusText.setText(getResources().getString(R.string.text_status_connected));
                                deviceStatusText.setBackground(getResources().getDrawable(R.drawable.connected_status));
                                statusLayout.setVisibility(View.GONE);
                                startTestText.setVisibility(View.VISIBLE);
                            }
                        });
                        startTestText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                StartMarsBt(deviceToConnect, byteArrayListtoSend);
                            }
                        });
                    }
                } else {
                    byteArrayList = new ArrayList<>();
                    for (int c = 0; c < 8; c++) {
                        byte i = data[c];
                        byteArrayList.add(i);
                    }
                    byteArrayList.add((byte) 04);
                    byteArrayList.add((byte) 03);
                    byteArrayList.add((byte) 00);
                    byteArrayList.add((byte) 04);
                    byteArrayList.add((byte) 0xFF);
                    byteArrayList.add((byte) 01);
                    Byte[] arrayBytes = byteArrayList.toArray(new Byte[byteArrayList.size()]);
                    byte checksum = 0;

                    for (int i = 0; i < arrayBytes.length; i++) {
                        checksum += arrayBytes[i];
                    }

                    int v2 = checksum & 0xFF;
                    byteArrayList.add((byte) v2);
                    byteArrayList.add((byte) 22);
                    byte[] bytes_song_byte = new byte[byteArrayList.size()];
                    for (int i = 0; i < byteArrayList.size(); i++) {
                        bytes_song_byte[i] = byteArrayList.get(i);
                    }
                    sendData(MainActivity.this, bytes_song_byte);
                }
            } else if (BluetoothLeService.ACTION_BYTE_DATA_AVAILABLE.equals(action)) {
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    //send data to bluetooth service
    private static void sendData(Activity activity, byte[] data) {
        Intent writeDataIntent = new Intent(activity, BluetoothLeService.class);
        writeDataIntent.putExtra("WRITE_DATA", data);
        Log.e("data_sendToBluetooth", data.toString());
        activity.startService(writeDataIntent);
    }

    //stop the bluetooth scann
    public void stopScan() {
        if (bluetoothHelper != null)
            bluetoothHelper.getBleScanner().stopScan();
    }

    @SuppressLint("LongLogTag")
    private void StartMarsBt(BluetoothDevice device, ArrayList<Byte> object) {
        object.add((byte) 01);
        object.add((byte) 02);
        object.add((byte) 00);
        object.add((byte) 02);
        object.add((byte) 0x90);
        Byte[] arrayBytes = object.toArray(new Byte[object.size()]);
        byte checksum = 0;
        for (int i = 0; i < arrayBytes.length; i++) {
            checksum += arrayBytes[i];
        }
        int v2 = checksum & 0xFF;
        object.add((byte) v2);
        object.add((byte) 22);
        byte[] bytes_song_byte = new byte[object.size()];
        for (int i = 0; i < object.size(); i++) {
            bytes_song_byte[i] = object.get(i);
        }
        sendData(MainActivity.this, bytes_song_byte);
    }

    //Test history list adapter
    public class List_BAC_Adapter extends RecyclerView.Adapter<List_BAC_Adapter.MyViewHolde_list> {
        private List<BACRecord> notificationList;
        private Context ctx;
        //Check Internet connection
        Boolean isInternetPresent = false;
        ConnectionDetector connectionDetector;

        public List_BAC_Adapter(Context ctx, List<BACRecord> nList) {
            this.notificationList = nList;
            this.ctx = ctx;
            connectionDetector = new ConnectionDetector(ctx);
        }

        @NonNull
        @Override
        public MyViewHolde_list onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_adapter_record, parent, false);
            return new MyViewHolde_list(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolde_list holder, int position) {
            notification = notificationList.get(position);
            isInternetPresent = connectionDetector.isConnectingToInternet();

            String number = notification.getTime();
            if (number.substring(0, 2).equals("00")) {
                replaceTime = "12" + number.substring(2);
            } else {
                replaceTime = number;
            }
            holder.date.setText(notification.getDate());
            holder.time.setText(replaceTime);
            if (notification.getStatus().equalsIgnoreCase("PASS")) {
                holder.bacLabel.setImageResource(R.drawable.ic_success);
            } else {
                holder.bacLabel.setImageResource(R.drawable.ic_fail);
            }
            DecimalFormat precision = new DecimalFormat("0.000");
            holder.bac.setText("" + precision.format(notification.getBac()));
            if (notification.getStatus().equalsIgnoreCase("FAIL")) {
                if (notification.getBac() == 0.0) {
                    holder.bacLabel.setImageResource(R.drawable.ic_fail);
                    holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.connected_rect));
                } else if (notification.getBac() >= 0.01 && notification.getBac() <= 0.04) {
                    holder.bacLabel.setImageResource(R.drawable.ic_fail);
                    holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.gray_rect));
                } else if (notification.getBac() >= 0.05 && notification.getBac() <= 0.07) {
                    holder.bacLabel.setImageResource(R.drawable.ic_fail);
                    holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.gray_rect));
                } else if (notification.getBac() >= 0.08) {
                    holder.bacLabel.setImageResource(R.drawable.ic_fail);
                    holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.red_rect));
                }
            } else {
                if (notification.getBac() == 0.0) {
                    holder.bacLabel.setImageResource(R.drawable.ic_success);
                    holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.connected_status));
                } else if (notification.getBac() >= 0.01 && notification.getBac() <= 0.04) {
                    holder.bacLabel.setImageResource(R.drawable.ic_success);
                    holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.gray_background));
                } else if (notification.getBac() >= 0.05 && notification.getBac() <= 0.07) {
                    holder.bacLabel.setImageResource(R.drawable.ic_success);
                    holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.gray_background));
                } else if (notification.getBac() >= 0.08) {
                    holder.bacLabel.setImageResource(R.drawable.ic_success);
                    holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.red_background));
                }
            }
        }

        @Override
        public int getItemCount() {
            return notificationList.size();
        }

        public class MyViewHolde_list extends RecyclerView.ViewHolder {
            private TextView date, time, bac;
            private ImageView bacLabel;
            private LinearLayout listLayout, progressBarLayout;
            private ProgressBar progressBar;

            public MyViewHolde_list(View view) {
                super(view);
                date = (TextView) view.findViewById(R.id.date);
                time = (TextView) view.findViewById(R.id.time);
                bac = (TextView) view.findViewById(R.id.bac);
                bacLabel = (ImageView) view.findViewById(R.id.bacLabel);
                listLayout = (LinearLayout) view.findViewById(R.id.list_layout);
                progressBarLayout = (LinearLayout) view.findViewById(R.id.progressBar_layout);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            }
        }
    }

    /**
     * Step 1: Check Google Play services
     */
    @SuppressLint("LongLogTag")
    private void appUpdationProcess() {
        //Check whether this user has installed Google play service which is being used by Location updates.
        if (isGooglePlayServicesAvailable()) {
            Log.e("isGooglePlayServicesAvailable", "YES");
            //Passing null to indicate that it is executing for the first time.
            startStep2(null);
        } else {
        }
    }

    /**
     * Return the availability of GooglePlayServices
     */
    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    /**
     * Step 2: Check & Prompt Internet connection
     */
    private Boolean startStep2(DialogInterface dialog) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            promptInternetConnect();
            return false;
        }
        if (dialog != null) {
            dialog.dismiss();
        }
        if (checkPermissions()) { //Yes permissions are granted by the user. Go to the next step.
            startStep3();
        } else {  //No user has not granted the permissions yet. Request now.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions();
            }
        }
        return true;
    }

    /**
     * Show A Dialog with button to refresh the internet state.
     */
    private void promptInternetConnect() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getResources().getString(R.string.text_no_internet));
        builder.setMessage(getResources().getString(R.string.text_active_internet_connection));
        String positiveText = getResources().getString(R.string.text_refresh);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Block the Application Execution until user grants the permissions
                        if (startStep2(dialog)) {
                            //Now make sure about location permission.
                            if (checkPermissions()) {
                                //Step 2: Start the Location Monitor Service
                                //Everything is there to start the service.
                                startStep3();
                            } else if (!checkPermissions()) {
                                requestPermissions();
                            }
                        }
                    }
                });
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState1 = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionState2 = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Step 3: Start the Location Monitor Service
     */
    private void startStep3() {
        if (!mAlreadyStartedService) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                MainActivity.this.startForegroundService(new Intent(MainActivity.this, LocationService.class));
            } else {
                MainActivity.this.startService(new Intent(MainActivity.this, LocationService.class));
            }
            mAlreadyStartedService = true;
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If img_user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStep3();
            } else {
                showSnackbar(R.string.permission_denied_explanation, R.string.settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Build intent that displays the App settings screen.
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            }
        }
    }

    /**
     * Start permissions requests.
     */
    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);

        boolean shouldProvideRationale2 =
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (shouldProvideRationale || shouldProvideRationale2) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    // Shows a {@link Snackbar}.
    private void showSnackbar(final int mainTextStringId, final int actionStringId, View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    //show the alert dialog when test failed
    private void showTestFailedAlertDialog() {
        showTestAlertDialog(getApplicationContext(), getResources().getString(R.string.text_you_have_failed_to_provide_a_test), false);
    }

    //show the alert dialog
    public void showTestAlertDialog(Context context, String message, Boolean status) {
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setMessage(message);
        alertDialog.setButton(getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
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

    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // set side menu position
            displayView(position);
        }

        private void displayView(int position) {
            if (faceValue) {
                switch (position) {
                    case 0:
                        Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
                        startActivity(historyIntent);
                        break;
                    case 1:
                        Intent activeIntent = new Intent(MainActivity.this, ChecbacActiveActivity.class);
                        startActivity(activeIntent);
                        break;
                    case 2:
                        Log.e("LOG_FRSLIDE", "YRS");
                        if (isInternetPresent) {
                            Log.e("LOG_FRSLIDE", "Inside");
                            Intent i = new Intent(MainActivity.this, EnrollActivity.class);
                            i.putExtra("user", ParseUser.getCurrentUser().getObjectId());
                            i.putExtra("Intent", "Main");
                            startActivity(i);
                            finish();
                        } else {
                            ShowNoInternetDialog();
                        }
                        break;
                    case 3:
                        Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Open the MainActivity");
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "CheckBAC");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "MainActivity");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
                        mFirebaseAnalytics.setMinimumSessionDuration(20000);
                        mFirebaseAnalytics.setSessionTimeoutDuration(500);
                        startActivity(aboutIntent);
                        break;
                    case 4:
                        Intent supportIntent = new Intent(MainActivity.this, SupportActivity.class);
                        startActivity(supportIntent);
                        break;
                    case 5:
                        Intent legalIntent = new Intent(MainActivity.this, LegalActivity.class);
                        startActivity(legalIntent);
                        break;
                    case 6:
                        Intent requirementsIntent = new Intent(MainActivity.this, RequirementsActivity.class);
                        startActivity(requirementsIntent);
                        break;
                    case 7:
                        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(settingsIntent);
                        break;
                    case 8:
                        logoutAlerDialog();
                        break;
                    default:
                        break;
                }
            } else {
                switch (position) {
                    case 0:
                        Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
                        startActivity(historyIntent);
                        break;
                    case 1:
                        Intent activeIntent = new Intent(MainActivity.this, ChecbacActiveActivity.class);
                        startActivity(activeIntent);
                        break;
                    case 2:
                        Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Open the MainActivity");
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "CheckBAC");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "MainActivity");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
                        mFirebaseAnalytics.setMinimumSessionDuration(20000);
                        mFirebaseAnalytics.setSessionTimeoutDuration(500);
                        startActivity(aboutIntent);
                        break;
                    case 3:
                        Intent supportIntent = new Intent(MainActivity.this, SupportActivity.class);
                        startActivity(supportIntent);
                        break;
                    case 4:
                        Intent legalIntent = new Intent(MainActivity.this, LegalActivity.class);
                        startActivity(legalIntent);
                        break;
                    case 5:
                        Intent requirementsIntent = new Intent(MainActivity.this, RequirementsActivity.class);
                        startActivity(requirementsIntent);
                        break;
                    case 6:
                        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(settingsIntent);
                        break;
                    case 7:
                        logoutAlerDialog();
                        break;
                    default:
                        break;
                }
            }
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            mDrawerLayout.closeDrawer(sideLayout);
        }
    }

    public void logoutAlerDialog() {
        android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setCancelable(false);
        active_fr = (Boolean) Cache.getData(CatchValue.USER_ACTIVE_FR, MainActivity.this);
        Log.e("active_fr: ", "logoutAlertDialog: " + active_fr);

        if (active_fr) {
            alertDialogBuilder.setMessage("Are you sure you want to logout?");
        } else {
            alertDialogBuilder.setMessage("Logging out will lock you out of this app until your monitor can reset your password. You will be unable to take BAC tests and properly comply.");
        }
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (isInternetPresent) {
                            logout();
                        } else {
                            showAlertDialog(MainActivity.this, getResources().getString(R.string.text_no_internet_connection),
                                    getResources().getString(R.string.text_please_check_your_network), false);
                        }
                    }
                });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                    }
                });

        android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    //process of logout
    public void logout() {
        showProgressDialog();
        if (isInternetPresent) {
            active_fr = (Boolean) Cache.getData(CatchValue.USER_ACTIVE_FR, MainActivity.this);
            Log.e("@@@ ", " active_fr: " + active_fr);
            if (active_fr) {
                Log.e("@@@", "without_random");
                clearLogout("");
            } else {
                Log.e("@@@", "with_random");
                char[] chars = "0123456789".toCharArray();
                StringBuilder sb = new StringBuilder();
                Random random = new Random();
                for (int i = 0; i < passwordSize; i++) {
                    char c = chars[random.nextInt(chars.length)];
                    sb.append(c);
                }
                randomPassword = sb.toString();
                Log.e("Random: ", "Generated Alpha-Numeric Password : " + randomPassword);
                clearLogout(randomPassword);
            }
        } else {
            dismissProgressDialog();
            ShowNoInternetDialog();
        }
    }

    private void clearLogout(String randomPassword) {
        ParseUser parseUser = ParseUser.getCurrentUser();
        if (!randomPassword.equalsIgnoreCase("")) {
            parseUser.setPassword(randomPassword);
        }
        parseUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (null == e) {
                    ParseUser.logOutInBackground(new LogOutCallback() {
                        @Override
                        public void done(ParseException e) {
                            dismissProgressDialog();
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                MainActivity.this.stopService(new Intent(MainActivity.this, LocationService.class));
                            } else {
                                MainActivity.this.stopService(new Intent(MainActivity.this, LocationService.class));
                            }
                            CHECKBACDB.removeBACResultList();
                            sessionManager.logoutUser();
                            Cache.putData(CatchValue.USER_ACTIVE_FR, MainActivity.this, false, Cache.CACHE_LOCATION_DISK);
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Your password has been changed, please contact your monitor to get a new password
                                    loginIntent();
                                }
                            }, 2000);
                        }
                    });
                } else {
                }
            }
        });
    }

    @Override
    public void activateBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            Log.e(" ### ", " request_enable_bt");
            mBluetoothAdapter.enable();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mBluetoothAdapter.startDiscovery();
            try {
                baCtrackAPI = new BACtrackAPI(MainActivity.this, baCtrackAPICallbacks);
                baCtrackAPI.startScan();
            } catch (BluetoothLENotSupportedException e) {
                Log.e(TAG, "BluetoothLENotSupportedException", e);
                e.printStackTrace();
            } catch (BluetoothNotEnabledException e) {
                Log.e(TAG, "BluetoothNotEnabledException", e);
            }
            MainActivity.this.addListener(this);
        } else if (requestCode == BREATHTEST_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (Cache.getData(CatchValue.TIMER, MainActivity.this).equals(false)) {
                } else {
                    Cache.putData(CatchValue.TIMER, MainActivity.this, false, Cache.CACHE_LOCATION_DISK);
                    if(timer != null) {
                        timer.cancel();
                    }
                    if (alertDialog != null) {
                        alertDialog.dismiss();
                    }
                }
                bacValue = data.getDoubleExtra(BAC_VALUE, 0);
                deviceName = data.getStringExtra(DEVICE_NAME);
                testCountValue = data.getStringExtra(TEST_COUNT);
                batteryStatus = data.getStringExtra(BATTERY_STATUS);
                notificationLayout.setVisibility(View.GONE);
                notificationTimer.setVisibility(View.GONE);
                connectLayout.setVisibility(View.VISIBLE);
                String msg = "My BAC is " + bacValue + ".";
                Location location = currentLocation;
                Date myDate = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
                calendar.setTime(myDate);
                Date time = calendar.getTime();
                SimpleDateFormat outputFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZ");
                String resultDATE_Time = outputFmt.format(time);
                String[] splited = resultDATE_Time.split("\\s+");
                Log.e("Dat_format", "" + resultDATE_Time);
                resultDate = splited[0];
                resultTime = splited[1];
                dummy = splited[2];
                try {
                    final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
                    final Date dateObj = sdf.parse(resultTime);
                    resultTime = new SimpleDateFormat("KK:mm a").format(dateObj);
                } catch (java.text.ParseException e1) {
                    e1.printStackTrace();
                }
                latitudeFile = location != null ? location.getLatitude() : 0;
                longitudeFile = location != null ? location.getLongitude() : 0;
                videoFile = data.hasExtra(BaseCameraActivity.VIDEO_FILE) ? data.getStringExtra(BaseCameraActivity.VIDEO_FILE) : null;
                Log.e("videofile", videoFile);
                faceMatchBAC = true;
                if (isInternetPresent) {
                    //Internet isn't available
                    resultStatus = "PASS";
                    CHECKBACDB.addChecBACResultList(new BACRecord(resultDate, resultTime,
                            bacValue, latitudeFile, longitudeFile, videoFile, resultStatus, faceMatchBAC, deviceName));
                    getLocalRecords();
                    //Internet is available

                    if (faceValue && !videoFile.equalsIgnoreCase(null)) {
                        checkFaceMatch(bacValue, latitudeFile, longitudeFile, videoFile, deviceName, false, "");
                    } else {
                        faceRatio = "No";
                        ReportService.sendBusinessReportVideo(getApplicationContext(), bacValue,
                                latitudeFile, longitudeFile, videoFile, faceRatio, "", true, deviceName, "");
                    }
                } else {

                    Log.e("video_file: ", " ### " + videoFile);

                    //Internet isn't available
                    resultStatus = "FAIL";
                    CHECKBACDB.addChecBACResultList(new BACRecord(resultDate, resultTime,
                            bacValue, latitudeFile, longitudeFile, videoFile, resultStatus, faceMatchBAC, deviceName));
                    getLocalRecords();
                }
                Log.e("@@@", "result_ok");
                checkBACResult(bacValue);
                //bacRecordAdapter.notifyDataSetChanged();
            } else if (resultCode == RESULT_CANCELED) {
                Log.e("@@@", "result_canceled");
                if (Cache.getData(CatchValue.TIMER, MainActivity.this).equals(false)) {
                } else {
                    Cache.putData(CatchValue.TIMER, MainActivity.this, false, Cache.CACHE_LOCATION_DISK);
                }
                MainActivity.this.removeListener(this);
                disconnect();
                BACtrackCountdown(0);
                try {
                    baCtrackAPI = new BACtrackAPI(MainActivity.this, baCtrackAPICallbacks);
                    baCtrackAPI.startScan();
                } catch (BluetoothLENotSupportedException e) {
                } catch (BluetoothNotEnabledException e) {
                }
                MainActivity.this.addListener(this);
            }

        } else if (requestCode == PANIC_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String loc = ".";
                if (currentLocation != null) {
                    loc = ". My current location is " + "http://maps.google.com/maps?q=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() + ".";
                }
                // String message = "Hey "+ designNatedDriver+". I'm intoxicated and pressed the panic button. Please come and get me"+loc;
                String message = "I've activated the Panic Button and need immediate assistance" + loc + " Please call me immediately and come to my location.";
                Log.d(TAG, "Designated Driver has been notified " + " | msg " + message);
                if (data.hasExtra(BaseCameraActivity.PIC_FILE)) {
                    ReportService.sendImage(getApplicationContext(), data.getStringExtra(BaseCameraActivity.PIC_FILE), message);
                }
                if (data.hasExtra(BaseCameraActivity.VIDEO_FILE)) {
                    ReportService.sendVideo(getApplicationContext(), data.getStringExtra(BaseCameraActivity.VIDEO_FILE), message);
                }
            } else {
                Log.d(TAG, "Got RESULT_CANCELED. Panic video activity was cancelled");
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        Log.e("currentLocation", "" + currentLocation.getLongitude() + "---" + currentLocation.getLatitude());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public Location getCurrentLocation() {
        return currentLocation;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void panicActivated() {
        //pass the panic activity
        Intent intent = new Intent(MainActivity.this, PanicActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivityForResult(intent, PANIC_REQUEST_CODE);
    }

    //unregistered the bluetooth
    private void unregisterBluetooth() {
    }

    @Override
    public void BACtrackConnected(BACTrackDeviceType bacTrackDeviceType) {

    }

    @Override
    public void BACtrackDidConnect(String s) {

    }

    @Override
    public void BACtrackDisconnected() {

    }

    @Override
    public void BACtrackConnectionTimeout() {

    }

    @Override
    public void BACtrackFoundBreathalyzer(final BluetoothDevice bluetoothDevice) {
        Log.e("bac_track", "found: ### ");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                connectLayout.startAnimation(startAnimation);
                deviceStatusText.setText(getResources().getString(R.string.text_status_connected));
                deviceStatusText.setBackground(getResources().getDrawable(R.drawable.connected_status));
                deviceNameText.setText(bluetoothDevice.getName());
                if (bluetoothDevice.getName().equalsIgnoreCase("SmartBreathalyzer")) {
                    deviceNameText.setText("BACtrack");
                }
                statusLayout.setVisibility(View.GONE);
                notiStatus = (Boolean) Cache.getData(CatchValue.NOTIFICATION, MainActivity.this);
                startTestText.setVisibility(View.VISIBLE);
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                stopScan();
            }
        });

        //Take the test BACtrack device connection
        startTestText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationLayout.setVisibility(View.GONE);
                if (notiStatus.equals(true)) {
                    alertDialog.dismiss();
                }
                //pass the breathtestactivity class
                final Intent intent = new Intent(MainActivity.this, BreathTestActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra(DEVICE, bluetoothDevice);
                Log.e("bluetoothDevice:", "" + bluetoothDevice);
                startActivityForResult(intent, BREATHTEST_REQUEST_CODE);
            }
        });
    }

    @Override
    public void BACtrackCountdown(int i) {

    }

    @Override
    public void BACtrackStart() {

    }

    @Override
    public void BACtrackBlow() {

    }

    @Override
    public void BACtrackAnalyzing() {

    }

    @Override
    public void BACtrackResults(float v) {

    }

    @Override
    public void BACtrackFirmwareVersion(String s) {

    }

    @Override
    public void BACtrackSerial(String s) {

    }

    @Override
    public void BACtrackUseCount(int i) {

    }

    @Override
    public void BACtrackBatteryVoltage(float v) {
        Log.e("Bac_voltage", "" + String.valueOf(v));
    }

    @Override
    public void BACtrackBatteryLevel(int i) {
        Log.e("Bac_level", "" + String.valueOf(i));
    }

    @Override
    public void BACtrackError(int i) {

    }

    //Asking the app updation when app is installing into playstore
    private boolean web_update() {
        try {
            String curVersion = MainActivity.this.getPackageManager().getPackageInfo(package_name, 0).versionName;
            String newVersion = curVersion;
            newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + package_name + "&hl=en")
                    .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get()
                    .select("div[itemprop=softwareVersion]")
                    .first()
                    .ownText();
            return (value(curVersion) < value(newVersion)) ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private long value(String string) {
        string = string.trim();
        if (string.contains(".")) {
            final int index = string.lastIndexOf(".");
            return value(string.substring(0, index)) * 100 + value(string.substring(index + 1));
        } else {
            return Long.valueOf(string);
        }
    }

    //show the alert dialog for app updation
    private void Alert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_library, null);
        TextView alert_title = (TextView) dialogView.findViewById(R.id.custom_dialog_library_title_textview);
        TextView alert_message = (TextView) dialogView.findViewById(R.id.custom_dialog_library_message_textview);
        Button Bt_action = (Button) dialogView.findViewById(R.id.custom_dialog_library_ok_button);
        Bt_action.setText(getResources().getString(R.string.text_update_now));
        Button Bt_dismiss = (Button) dialogView.findViewById(R.id.custom_dialog_library_cancel_button);
        Bt_dismiss.setVisibility(View.GONE);
        builder.setView(dialogView);
        alert_title.setText(title);
        alert_message.setText(message);
        builder.setCancelable(false);
        Bt_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + package_name)));
                } catch (ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + package_name)));
                }
            }
        });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
                    finish();
                return false;
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    private BACtrackAPICallbacks baCtrackAPICallbacks = new BACtrackAPICallbacks() {
        @Override
        public void BACtrackConnected(BACTrackDeviceType bacTrackDeviceType) {
            for (BACtrackAPICallbacks baCtrackAPICallbacks : listeners) {
                baCtrackAPICallbacks.BACtrackConnected(bacTrackDeviceType);
            }
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

        @Override
        public void BACtrackDidConnect(String s) {
            for (BACtrackAPICallbacks baCtrackAPICallbacks : listeners) {
                baCtrackAPICallbacks.BACtrackDidConnect(s);
            }
        }

        @Override
        public void BACtrackDisconnected() {
            for (BACtrackAPICallbacks baCtrackAPICallbacks : listeners) {
                baCtrackAPICallbacks.BACtrackDisconnected();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    deviceStatusText.setText(getResources().getString(R.string.text_connect_breathalyzer));
                    deviceStatusText.setBackground(getResources().getDrawable(R.drawable.connect_status));
                    deviceNameText.setText(getResources().getString(R.string.frame_1));
                    deviceNameText.setTextSize(16);
                    statusLayout.setVisibility(View.VISIBLE);
                    startTestText.setVisibility(View.GONE);
                    try {
                        baCtrackAPI = new BACtrackAPI(MainActivity.this, baCtrackAPICallbacks);
                        baCtrackAPI.startScan();
                    } catch (BluetoothLENotSupportedException e) {
                    } catch (BluetoothNotEnabledException e) {
                    }


                    mBluetoothAdapter.enable();
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                    mBluetoothAdapter.startDiscovery();

                }
            });
        }

        @Override
        public void BACtrackConnectionTimeout() {
            for (BACtrackAPICallbacks baCtrackAPICallbacks : listeners) {
                baCtrackAPICallbacks.BACtrackConnectionTimeout();
            }
        }

        @Override
        public void BACtrackFoundBreathalyzer(final BluetoothDevice bluetoothDevice) {
            for (BACtrackAPICallbacks baCtrackAPICallbacks : listeners) {
                baCtrackAPICallbacks.BACtrackFoundBreathalyzer(bluetoothDevice);
            }

            baCtrackAPI.connectToDevice(bluetoothDevice);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    connectLayout.startAnimation(startAnimation);
                    deviceStatusText.setText(getResources().getString(R.string.text_status_connected));
                    deviceStatusText.setBackground(getResources().getDrawable(R.drawable.connected_status));
                    deviceNameText.setText(bluetoothDevice.getName());
                    if (bluetoothDevice.getName().equalsIgnoreCase("SmartBreathalyzer")) {
                        deviceNameText.setText("BACtrack");
                    }
                    statusLayout.setVisibility(View.GONE);
                    notiStatus = (Boolean) Cache.getData(CatchValue.NOTIFICATION, MainActivity.this);
                    startTestText.setVisibility(View.VISIBLE);
                }
            });

            startTestText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notificationLayout.setVisibility(View.GONE);
                    if (notiStatus.equals(true)) {
                        alertDialog.dismiss();
                    }
                    //pass the breathtestactivity class
                    final Intent intent = new Intent(MainActivity.this, BreathTestActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra(DEVICE, bluetoothDevice);
                    Log.e("bluetoothDevice:", "" + bluetoothDevice);
                    startActivityForResult(intent, BREATHTEST_REQUEST_CODE);
                }
            });

            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mBluetoothAdapter.cancelDiscovery();
        }

        @Override
        public void BACtrackCountdown(int i) {
            for (BACtrackAPICallbacks baCtrackAPICallbacks : listeners) {
                baCtrackAPICallbacks.BACtrackCountdown(i);
            }
        }

        @Override
        public void BACtrackStart() {
            for (BACtrackAPICallbacks baCtrackAPICallbacks : listeners) {
                baCtrackAPICallbacks.BACtrackStart();
            }
            Log.e("bac_track", "start: @@@ ");
        }

        @Override
        public void BACtrackBlow() {
            for (BACtrackAPICallbacks baCtrackAPICallbacks : listeners) {
                baCtrackAPICallbacks.BACtrackBlow();
            }
        }

        @Override
        public void BACtrackAnalyzing() {
            for (BACtrackAPICallbacks baCtrackAPICallbacks : listeners) {
                baCtrackAPICallbacks.BACtrackAnalyzing();
            }
        }

        @Override
        public void BACtrackResults(float v) {
            for (BACtrackAPICallbacks baCtrackAPICallbacks : listeners) {
                baCtrackAPICallbacks.BACtrackResults(v);
            }
        }

        @Override
        public void BACtrackFirmwareVersion(String s) {
            for (BACtrackAPICallbacks baCtrackAPICallbacks : listeners) {
                baCtrackAPICallbacks.BACtrackFirmwareVersion(s);
            }
        }

        @Override
        public void BACtrackSerial(String s) {
            for (BACtrackAPICallbacks baCtrackAPICallbacks : listeners) {
                baCtrackAPICallbacks.BACtrackSerial(s);
            }
        }

        @Override
        public void BACtrackUseCount(int i) {
            for (BACtrackAPICallbacks baCtrackAPICallbacks : listeners) {
                baCtrackAPICallbacks.BACtrackUseCount(i);
            }
        }

        @Override
        public void BACtrackBatteryVoltage(float v) {
            for (BACtrackAPICallbacks baCtrackAPICallbacks : listeners) {
                baCtrackAPICallbacks.BACtrackBatteryVoltage(v);
            }
            double voltage = getVoltage();
            Log.e("voltage", "BACtrack Voltage: " + voltage);
            Log.e("voltage", "BACtrack Voltage: " + v);
            if (baCtrackAPI != null) {
                boolean result = baCtrackAPI.getBreathalyzerBatteryVoltage();
                BACtrackBatteryLevel(0);
            } else {
            }
        }

        @Override
        public void BACtrackBatteryLevel(int i) {
            for (BACtrackAPICallbacks baCtrackAPICallbacks : listeners) {
                baCtrackAPICallbacks.BACtrackBatteryLevel(i);
            }
        }

        @Override
        public void BACtrackError(int i) {
            for (BACtrackAPICallbacks baCtrackAPICallbacks : listeners) {
                baCtrackAPICallbacks.BACtrackError(i);
            }
        }
    };

    public void addListener(BACtrackAPICallbacks baCtrackAPICallbacks) {
        listeners.add(baCtrackAPICallbacks);
    }

    public void removeListener(BACtrackAPICallbacks baCtrackAPICallbacks) {
        listeners.remove(baCtrackAPICallbacks);
    }

    public int getVoltage() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent b = this.registerReceiver(null, ifilter);
        return b.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        if (baCtrackAPI != null)
                            baCtrackAPI.stopScan();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        try {
                            baCtrackAPI = new BACtrackAPI(MainActivity.this, baCtrackAPICallbacks);
                        } catch (BluetoothLENotSupportedException e) {
                            Log.e(TAG, "BluetoothLENotSupportedException", e);
                        } catch (BluetoothNotEnabledException e) {
                            Log.e(TAG, "BluetoothNotEnabledException", e);
                        }
                        baCtrackAPI.startScan();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "Turning Bluetooth on...");
                        break;
                }
            }
        }
    };
    //start the BACtrack scanning
    public void startTest() {
        Log.e("bac_track: ", "start_test: $$$");
        if (baCtrackAPI != null) {
            baCtrackAPI.connectToNearestBreathalyzer();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    boolean result = baCtrackAPI.startCountdown();
                }
            }, 7000);
        } else {
            Log.e(TAG, "baCtrackAPI was null. Cant startTest");
        }
    }

    //disconnect the BACtrack device.
    public void disconnect() {
        if (baCtrackAPI != null) {
            Log.d(TAG, "baCtrackAPI disconnect");
            baCtrackAPI.disconnect();
        } else {
            Log.d(TAG, "baCtrackAPI was null. Cant disconnect");
        }
    }

    //check the bluetooth permissions
    private boolean checkReadBluetoothPermission() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, PackageManager.PERMISSION_GRANTED);
            return true;
        } else {
            return false;
        }
    }

    //check the run-time permissions
    private boolean checkPermission() {
        result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CONTACTS);
        result1 = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        result2 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        result3 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        result4 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        result5 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);
        result6 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        result7 = ContextCompat.checkSelfPermission(getApplicationContext(), SEND_SMS);
        Log.e("Resuly_checkpermissions", result1 + "----" + result2 + "----" + result3 + "----" + result4 + "----" + result6);
        if (result1 != 0) {
            CameaStatus = false;
        } else {
            CameaStatus = true;
        }
        if (result2 != 0) {
            LocationStatus = false;
        } else {
            LocationStatus = true;
        }
        if (result3 != 0) {
            StorageStatus = false;
        } else {
            StorageStatus = true;
        }
        if (result4 != 0) {
            StorageStatus = false;
        } else {
            StorageStatus = true;
        }
        if (result6 != 0) {
            MicrophoneStatus = false;
        } else {
            MicrophoneStatus = true;
        }
        if (result1 != 0 || result2 != 0 || result3 != 0 || result4 != 0 || result6 != 0) {
            Log.e("LOg_inside", "YES");
            if (isInternetPresent) {
                try {
                    ParseObject BACEntry = new ParseObject("AppPermissionReports");
                    BACEntry.put("userId", ParseUser.getCurrentUser());
                    BACEntry.put("LocationStatus", LocationStatus);
                    BACEntry.put("StorageStatus", StorageStatus);
                    BACEntry.put("CameraStatus", CameaStatus);
                    BACEntry.put("MicrophoneStatus", MicrophoneStatus);
                    BACEntry.put("NotificationStatus", true);
                    //data will be save in background for server
                    BACEntry.saveInBackground();
                    Log.e("LOg_inside", "send_success!");
                    Crashlytics.setString("domain", "MainActivity");
                    Crashlytics.setInt("code", 200);
                    Crashlytics.setString("UserName", ParseUser.getCurrentUser().getEmail());
                    Crashlytics.setString("Time", crashTime);
                    Crashlytics.setString("ModuleName", "AppPermissions");
                    Crashlytics.setString("deviceType", "Android");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("score", "Error: " + e.getMessage());
                    Crashlytics.logException(e);
                    Crashlytics.setString("domain", "MainActivity");
                    Crashlytics.setString("UserName", ParseUser.getCurrentUser().getEmail());
                    Crashlytics.setString("Time", crashTime);
                    Crashlytics.setString("ModuleName", "AppPermissions");
                    Crashlytics.setString("ErrorDescription", e.getMessage());
                    Crashlytics.setString("deviceType", "Android");
                }
            } else {}
        }
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED &&
                result3 == PackageManager.PERMISSION_GRANTED && result4 == PackageManager.PERMISSION_GRANTED && result5 == PackageManager.PERMISSION_GRANTED &&
                result6 == PackageManager.PERMISSION_GRANTED && result7 == PackageManager.PERMISSION_GRANTED/*&& result8== PackageManager.PERMISSION_GRANTED*/;
    }

    //request to the run-time permissions
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS, CAMERA, ACCESS_FINE_LOCATION, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, READ_PHONE_STATE, RECORD_AUDIO, SEND_SMS}, PERMISSION_REQUEST_CODE);
    }

    //show the progress dialog
    public void showProgressDialog() {
        progressDialog = new ProgressDialog(MainActivity.this);
        if (progressDialog == null) {
            progressDialog.setMessage(getResources().getString(R.string.text_loading));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(getResources().getString(R.string.text_loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    //disable the progress dialog
    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    //disable the ChecBAC Active and FR
    private void menuItems(Boolean faceValue) {
        dismissProgressDialog();
        if (faceValue) {
            menuItems.add(new SideMenu(getResources().getString(R.string.text_bac_test_history), R.mipmap.history_icon));
            menuItems.add(new SideMenu(getResources().getString(R.string.text_checkbac_active), R.mipmap.notification_ic));
            menuItems.add(new SideMenu(getResources().getString(R.string.text_calibrate_facial_recognition), R.mipmap.facial_recognition_icon));
            menuItems.add(new SideMenu(getResources().getString(R.string.text_about), R.mipmap.ic_about));
            menuItems.add(new SideMenu(getResources().getString(R.string.text_support), R.mipmap.support_icon));
            menuItems.add(new SideMenu(getResources().getString(R.string.text_legal), R.mipmap.privacy_policy_icon));
            menuItems.add(new SideMenu(getResources().getString(R.string.text_requirements), R.mipmap.mobile_app_icon));
            menuItems.add(new SideMenu(getResources().getString(R.string.text_settings), R.mipmap.ic_settings));
            menuItems.add(new SideMenu(getResources().getString(R.string.text_logout), R.mipmap.ic_signout));
        } else {
            menuItems.add(new SideMenu(getResources().getString(R.string.text_bac_test_history), R.mipmap.history_icon));
            menuItems.add(new SideMenu(getResources().getString(R.string.text_checkbac_active), R.mipmap.notification_ic));
            menuItems.add(new SideMenu(getResources().getString(R.string.text_about), R.mipmap.ic_about));
            menuItems.add(new SideMenu(getResources().getString(R.string.text_support), R.mipmap.support_icon));
            menuItems.add(new SideMenu(getResources().getString(R.string.text_legal), R.mipmap.privacy_policy_icon));
            menuItems.add(new SideMenu(getResources().getString(R.string.text_requirements), R.mipmap.mobile_app_icon));
            menuItems.add(new SideMenu(getResources().getString(R.string.text_settings), R.mipmap.ic_settings));
            menuItems.add(new SideMenu(getResources().getString(R.string.text_logout), R.mipmap.ic_signout));
        }
        mMenuAdapter = new DrawerMenuAdapter(MainActivity.this, menuItems);
        mDrawerList.setAdapter(mMenuAdapter);
        sideMenuBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(sideLayout);
            }
        });
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage(getResources().getString(R.string.text_are_you_want_to_exit))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.text_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.text_no), null)
                .show();
    }

    //Alert dialog for internet isn't available
    public void ShowNoInternetDialog() {
        showAlertDialog(MainActivity.this, getResources().getString(R.string.text_no_internet_connection),
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

    private void loginIntent() {
        Intent intent = new Intent(MainActivity.this, LoginUserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void alertMessageNoGPS() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.e(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.e(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
                        try {
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.e(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    private void showUserModeAlertDialog() {
        showUserAlert(MainActivity.this, "Alert Message", "User is not in active mode. Logging out will lock you out of this app until your monitor can reset your password. You will be unable to take BAC tests and properly comply.!", false);
    }

    private void showUserAlert(Context context, String title, String message, Boolean status) {
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon((status) ? R.mipmap.ic_action_checked : R.mipmap.ic_action_warning);
        alertDialog.setButton(getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                showProgressDialog();
                clearLogout("");
            }
        });
        alertDialog.show();
        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
        textView.setTextSize(16);
    }

    private void showUserLogoutAlert(Context context, String title, String message, Boolean status) {
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon((status) ? R.mipmap.ic_action_checked : R.mipmap.ic_action_warning);
        alertDialog.setButton(getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dismissProgressDialog();
                loginIntent();
            }
        });
        alertDialog.show();
        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
        textView.setTextSize(16);
    }

    public void checkFaceMatch(Double bacValue, Double latitudeFile, Double longitudeFile, String path,
                               String deviceName, Boolean frStatus, String dateAsString) {

        Log.e("MainActivity:", "video_file: ^^^ " + path);

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(13000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        //check the image is available or not in video file
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bmFrame.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String b64=Base64.encodeToString(b, Base64.DEFAULT);
        String image = b64.replace("\n", "");
        Log.e("image", image);
        //sending image
        try {
            String userName = ParseUser.getCurrentUser().getUsername();
            Log.e("report_service: ", "user_name: " + userName);
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = "http://35.202.161.76:5000/api/compareface";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", userName);
            jsonBody.put("base64string", image);
            final String requestBody = jsonBody.toString();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.e("VOLLEY", response.toString());
                    try {
                        JSONObject response_jsonObject = new JSONObject(String.valueOf(response));
                        String status = response_jsonObject.getString("status");
                        String code = response_jsonObject.getString("code");
                        Log.e("report_service: ", "status: " + status);
                        if (code.equalsIgnoreCase("200") || status.equalsIgnoreCase("True")) {
                            value = response_jsonObject.getString("value");
                            faceRatio = "YES";
                            faceMatchFailedAtBAC = false;
                            if (frStatus) {
                                ReportService.sendBusinessReportVideo(getApplicationContext(), bacValue,
                                        latitudeFile, longitudeFile, path, faceRatio, value, faceMatchFailedAtBAC, deviceName, dateAsString);
                            } else {
                                ReportService.sendBusinessReportVideo(getApplicationContext(), bacValue,
                                        latitudeFile, longitudeFile, path, faceRatio, value, faceMatchFailedAtBAC, deviceName, "");
                            }
                        } else if (code.equalsIgnoreCase("200") || status.equalsIgnoreCase("False")) {
                            value = response_jsonObject.getString("value");
                            faceRatio = "YES";
                            faceMatchFailedAtBAC = true;
                            if (frStatus) {
                                ReportService.sendBusinessReportVideo(getApplicationContext(), bacValue,
                                        latitudeFile, longitudeFile, path, faceRatio, value, faceMatchFailedAtBAC, deviceName, dateAsString);
                            } else {
                                ReportService.sendBusinessReportVideo(getApplicationContext(), bacValue,
                                        latitudeFile, longitudeFile, path, faceRatio, value, faceMatchFailedAtBAC, deviceName, "");
                            }
                        }else if (code.equalsIgnoreCase("500") || status.equalsIgnoreCase("Failed")){
                            value = response_jsonObject.getString("value");
                            faceRatio = "YES";
                            faceMatchFailedAtBAC = true;
                            if (frStatus) {
                                ReportService.sendBusinessReportVideo(getApplicationContext(), bacValue,
                                        latitudeFile, longitudeFile, path, faceRatio, value, faceMatchFailedAtBAC, deviceName, dateAsString);
                            } else {
                                ReportService.sendBusinessReportVideo(getApplicationContext(), bacValue,
                                        latitudeFile, longitudeFile, path, faceRatio, value, faceMatchFailedAtBAC, deviceName, "");
                            }
                        } else {
                            value = response_jsonObject.getString("value");
                            faceRatio = "YES";
                            faceMatchFailedAtBAC = true;
                            if (frStatus) {
                                ReportService.sendBusinessReportVideo(getApplicationContext(), bacValue,
                                        latitudeFile, longitudeFile, path, faceRatio, value, faceMatchFailedAtBAC, deviceName, dateAsString);
                            } else {
                                ReportService.sendBusinessReportVideo(getApplicationContext(), bacValue,
                                        latitudeFile, longitudeFile, path, faceRatio, value, faceMatchFailedAtBAC, deviceName, "");
                            }
                        }
                        Log.e("LOG_VALUE","" +value);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                    Log.e(" ### onErrorResponse: ", error.toString());
                    value = "500";
                    faceRatio = "YES";
                    faceMatchFailedAtBAC = true;
                    if (frStatus) {
                        ReportService.sendBusinessReportVideo(getApplicationContext(), bacValue,
                                latitudeFile, longitudeFile, path, faceRatio, value, faceMatchFailedAtBAC, deviceName, dateAsString);
                    } else {
                        ReportService.sendBusinessReportVideo(getApplicationContext(), bacValue,
                                latitudeFile, longitudeFile, path, faceRatio, value, faceMatchFailedAtBAC, deviceName, "");
                    }
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @SuppressLint("LongLogTag")
                @Override
                protected com.android.volley.Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        Log.e("response", String.valueOf(response.headers));
                        responseString = String.valueOf(response.statusCode);
                        Log.e("response", responseString);
                        Log.e(" *** parseNetworkResponse: ", responseString);
                    }
                    Log.e("Success_response", String.valueOf(HttpHeaderParser.parseCacheHeaders(response)).getBytes().toString());
                    return super.parseNetworkResponse(response);
                }
            };
            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(" ^^^ ", " catch: "+ e.getMessage());
        }
    }

    private void checkBACResult(Double bacValue) {
        dismissProgressDialog();
        ll_home.setVisibility(View.GONE);
        ll_result.setVisibility(View.VISIBLE);
        if (batteryStatus.equalsIgnoreCase("true")) {
            batteryImage.setImageResource(R.drawable.battery_low);
        } else {
            batteryImage.setImageResource(R.drawable.battery_full);
        }
        //convert to the decimal format
        DecimalFormat precision = new DecimalFormat("0.000");
        result_text.setText(precision.format(bacValue));
        if (bacValue == 0.00) {
            result_layout.setBackgroundColor(getResources().getColor(R.color.app_default));
        } else if (bacValue >= 0.01 && bacValue <= 0.07) {
            result_layout.setBackgroundColor(getResources().getColor(R.color.orange));
        } else if (bacValue >= 0.08) {
            result_layout.setBackgroundColor(getResources().getColor(R.color.dark_red));
        }
    }
}