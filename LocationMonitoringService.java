package com.deepwares.checkpointdwi.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.deepwares.checkpointdwi.R;
import com.deepwares.checkpointdwi.activities.MainActivity;
import com.deepwares.checkpointdwi.network.ConnectionDetector;
import com.deepwares.checkpointdwi.session.Cache;
import com.deepwares.checkpointdwi.session.CatchValue;
import com.deepwares.checkpointdwi.slidemenu.SettingsActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;
import static android.support.v4.app.NotificationManagerCompat.IMPORTANCE_HIGH;


/**
 * Created by devdeeds.com on 27-09-2017.
 */

public class LocationMonitoringService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = LocationMonitoringService.class.getSimpleName();
    GoogleApiClient mLocationClient;
    @SuppressLint("RestrictedApi")
    LocationRequest mLocationRequest = new LocationRequest();
    public static final String ACTION_LOCATION_BROADCAST = LocationMonitoringService.class.getName() + "LocationBroadcast";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    private Date currentTime, startedAt_time;
    String notification_date;
    String notification_time;
    Date crashCurrentDate;
    String crashTime, crashDate;
    private boolean gpsStatus = false;
    Notification notification;
    final private static String NOTIFICATION_CHANNEL = "notification_channel";
    private NotificationManager mNotificationManager;
    private int START_FOREGROUND_ID = 101;
    ConnectionDetector connectionDetector;
    Boolean isInternetPresent = false;
    Timer timer = new Timer();
    boolean notificationStatus;
    String notificationText = null, notificationText1 = null;


    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest.setInterval(Constants.LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.FASTEST_LOCATION_INTERVAL);
        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY; //by default
        //PRIORITY_BALANCED_POWER_ACCURACY, PRIORITY_LOW_POWER, PRIORITY_NO_POWER are the other priority modes
        mLocationRequest.setPriority(priority);
        mLocationClient.connect();

        connectionDetector = new ConnectionDetector(this);
        isInternetPresent = connectionDetector.isConnectingToInternet();

//        String CHANNEL_ONE_ID = "com.kjtech.app.N1";
//        String CHANNEL_ONE_NAME = "Channel One";
        String CHANNEL_ONE_ID = "com.deepwares.checkpointdwi";
        String CHANNEL_ONE_NAME = "CheckBAC";
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);

            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(getApplicationContext())
                    .setChannelId(CHANNEL_ONE_ID)
                    .build();

            startForeground(START_FOREGROUND_ID, notification);
        }

        /*Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notification.contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);*/

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
     * LOCATION CALLBACKS
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Log.d(TAG, "== Error On onConnected() Permission not granted");
            //Permission not granted by user so cancel the further execution.

            return;
        }

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);
        } catch (SecurityException ex) {
            Log.e(TAG, "Error creating location service: " + ex.getMessage());
        }
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended");
    }

    //to get the location change
    @Override
    public void onLocationChanged(Location location) {

        //check the gps is available or not

//        connectionDetector = new ConnectionDetector(this);
//        isInternetPresent = connectionDetector.isConnectingToInternet();

        Thread t = new Thread(new Runnable() {
            public void run() {
                checkGPSStatus();
            }
        });
        t.start();

        if (location != null) {
            try {
                //Send result to activities
                final ParseUser user = ParseUser.getCurrentUser();
                if (user != null) {
                    double speed = location.getSpeed();
                    Log.e("Speed_test", String.valueOf(speed));


                    try {
                        ParseQuery<ParseObject> notificationListQuery = ParseQuery.getQuery("_User");
                        notificationListQuery.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
                        notificationListQuery.findInBackground(new FindCallback<ParseObject>() {
                            public void done(List<ParseObject> scoreList, ParseException e) {
                                if (e == null) {
                                    if (scoreList.size() > 0) {
                                        for (ParseObject comment : scoreList) {
                                            if (comment.getBoolean("hasActiveEnabled")) {
                                                Log.e("location_monitoring: ", "speed: " + comment.getBoolean("hasActiveEnabled"));
                                                sendMessageToUI(location.getLatitude(), location.getLongitude(), speed);
                                            }
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
                }
            } catch (Exception e) {
                Log.e(TAG, "speed_test" + e.getMessage());
            }
        }
    }

    private void sendMessageToUI(double lat, double lng, double speed) {
        //send the last location details to server with every 1 hour
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                lastLocation(lat, lng);
            }
        }, 0, 3600 * 1000);

        //Set the notification channel for working the notification on android oreo version
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = null;
            channel = new NotificationChannel(NOTIFICATION_CHANNEL, "NotificationChannel",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setLightColor(Color.GREEN);
            channel.enableLights(true);
            channel.enableVibration(true);
            Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/danger_alaram");

            //set the sound for notification
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            channel.setSound(uri, att);

            //Notification channel is set to the notification manager
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            getNotificationManager().createNotificationChannel(channel);
        }

        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LATITUDE, lat);
        intent.putExtra(EXTRA_LONGITUDE, lng);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        if (speed >= 0) { //15
            Cache.putData(CatchValue.NOTIFICATION, LocationMonitoringService.this, true, Cache.CACHE_LOCATION_DISK);
            currentTime = new Date();
            crashCurrentDate = new Date();
            crashDate = new SimpleDateFormat("MMM dd, yyyy").format(crashCurrentDate);
            crashTime = new SimpleDateFormat("HH:mm").format(crashCurrentDate);

            notificationText = getResources().getString(R.string.text_you_are_in_motion);
            notificationText1 = getResources().getString(R.string.text_you_failed_for_test);

            if (isInternetPresent) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("ActiveNotify");
                query.whereEqualTo("user_id", ParseUser.getCurrentUser());
                query.orderByDescending("createdAt");
                query.setLimit(1);
                try {
                    List<ParseObject> gameScore = query.find();
                    if (gameScore.size() > 0) {
                        for (ParseObject comment : gameScore) {
                            startedAt_time = comment.getCreatedAt();
                            notificationStatus = comment.getBoolean("notification");
                            Log.e("notification_status: ", " " + notificationStatus);
                        }
                        if (Cache.getData(CatchValue.LOCKOUT, LocationMonitoringService.this).equals(true)) {
                            Log.e("loCkOUTPERIOD", "YES");
                            long diff_elapsed = currentTime.getTime() - startedAt_time.getTime();
                            long seconds = diff_elapsed / 1000;
                            long minutes = seconds / 60;
                            if (minutes > 3) { //60
                                Cache.putData(CatchValue.DATE, LocationMonitoringService.this, currentTime, Cache.CACHE_LOCATION_DISK);
                                Cache.putData(CatchValue.NOTI_LATI, LocationMonitoringService.this, lat, Cache.CACHE_LOCATION_DISK);
                                Cache.putData(CatchValue.NOTI_LONGI, LocationMonitoringService.this, lng, Cache.CACHE_LOCATION_DISK);
                                Cache.putData(CatchValue.LOCKOUT, LocationMonitoringService.this, false, Cache.CACHE_LOCATION_DISK);
                                startNotification(lat, lng, notificationText);
                            }
                        } else {
                            Log.e("KnoCKOUT_PERIOD", "YES");
                            long diff_elapsed = currentTime.getTime() - startedAt_time.getTime();
                            long seconds = diff_elapsed / 1000;
                            long minutes = seconds / 60;

                            Log.e("notificatin_status: ", " @@@ " +minutes);

                            if (minutes > 1) { //30
                                Log.e("minutes: ", "Knockout_period: " + minutes);
                                Cache.putData(CatchValue.DATE, LocationMonitoringService.this, currentTime, Cache.CACHE_LOCATION_DISK);
                                Cache.putData(CatchValue.NOTI_LATI, LocationMonitoringService.this, lat, Cache.CACHE_LOCATION_DISK);
                                Cache.putData(CatchValue.NOTI_LONGI, LocationMonitoringService.this, lng, Cache.CACHE_LOCATION_DISK);
                                startNotification(lat, lng, notificationText);
                            }

                            Log.e("notificatin_status: ", " ### " +minutes);
                            Log.e("notificatin_status: ", " $$$ " +notificationStatus);

                            if (minutes == 2) { //36
                                if (notificationStatus == false) {
                                    Log.e("minutes: ", "notification_minutes: " + minutes);
                                    startNotification(lat, lng, notificationText1);
                                }
                            }
                        }
                    } else {
                        Cache.putData(CatchValue.DATE, LocationMonitoringService.this, currentTime, Cache.CACHE_LOCATION_DISK);
                        Cache.putData(CatchValue.NOTI_LATI, LocationMonitoringService.this, lat, Cache.CACHE_LOCATION_DISK);
                        Cache.putData(CatchValue.NOTI_LONGI, LocationMonitoringService.this, lng, Cache.CACHE_LOCATION_DISK);
                        startNotification(lat, lng, notificationText);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    Crashlytics.setString("domain", "LocationMonitoringService");
                    Crashlytics.setInt("code", e.getCode());
                    Crashlytics.setString("UserName", ParseUser.getCurrentUser().getEmail());
                    Crashlytics.setString("Time", crashTime);
                    Crashlytics.setString("ModuleName", "ActiveNotifyData");
                    Crashlytics.setString("ErrorDescription", e.getMessage());
                    Crashlytics.setString("deviceType", "Android");
                }
            }
        }
    }

    //Last location details send to the server
    private void lastLocation(double lat, double lng) {

//        connectionDetector = new ConnectionDetector(this);
//        isInternetPresent = connectionDetector.isConnectingToInternet();


        if (isInternetPresent) {
            try {
                final ParseUser user = ParseUser.getCurrentUser();
                user.put("lastReportedLocation", new ParseGeoPoint(lat, lng));
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(com.parse.ParseException e) {
                        if (e == null) {
                            Log.e("SaveInBackground", "Success");
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    //send the notification
    private void startNotification(double lat, double lng, String notificationText) {

        //Set current date and time
        Date current = new Date();
        notification_date = new SimpleDateFormat("MMM dd, yyyy").format(current);
        notification_time = new SimpleDateFormat("h:mm a").format(current);
        final Double lattitude = lat;
        final Double longitude = lng;
        ParseObject notificationParse = new ParseObject("ActiveNotify");
        try {
            notificationParse.put("bac_value", "");
            notificationParse.put("user_id", ParseUser.getCurrentUser());
            notificationParse.put("startedAt", new ParseGeoPoint(lattitude != null ? lat : 30, longitude != null ? lng : -84));
            notificationParse.put("notification", false);
            notificationParse.saveInBackground();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Store the notification object to session
        Cache.putData(CatchValue.OBJECTID, LocationMonitoringService.this, notificationParse.getObjectId(), Cache.CACHE_LOCATION_DISK);

        // Add as notification for android oreo version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            // Construct a task stack.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Add the main Activity to the task stack as the parent.
            stackBuilder.addParentStack(MainActivity.class);
            // Push the content Intent onto the stack.
            stackBuilder.addNextIntent(notificationIntent);
            // Get a PendingIntent containing the entire back stack.
            PendingIntent notificationPendingIntent =
                    stackBuilder.getPendingIntent(START_FOREGROUND_ID, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder notificationBuilder = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                notificationBuilder = new Notification.Builder(this, NOTIFICATION_CHANNEL)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText(notificationText)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setStyle(new Notification.BigTextStyle().bigText(notificationText))
                        .setAutoCancel(true)
                        .setContentIntent(notificationPendingIntent);
            }

            getNotificationManager().notify(START_FOREGROUND_ID, notificationBuilder.build());

        } else {

            NotificationCompat.Builder builder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(getResources().getString(R.string.app_name))
                            .setContentText(notificationText)
                            .setVibrate(new long[]{1000, 1000})
                            .setLights(Color.RED, 3000, 3000)
                            .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                                    + "://" + getPackageName() + "/raw/danger_alaram"))
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                            .setAutoCancel(true);

            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            // Add as notification
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());
        }


        Cache.putData(CatchValue.NOTIFICATION, LocationMonitoringService.this, true, Cache.CACHE_LOCATION_DISK);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Failed to connect to Google API");
    }

    private void checkGPSStatus() {

        Date date = new Date();
        String resultDate = new SimpleDateFormat("MMM dd, yyyy").format(date);
        String resultTime = new SimpleDateFormat("HH:mm").format(date);
        String dateTime = resultDate + ":" + resultTime;

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        connectionDetector = new ConnectionDetector(this);
//        isInternetPresent = connectionDetector.isConnectingToInternet();
        if (isInternetPresent) {
            final ParseUser user = ParseUser.getCurrentUser();
            if (user != null) {
                gpsStatus = user.getBoolean("GPSStatus");
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (gpsStatus == false) {
                    connectionDetector = new ConnectionDetector(this);
                    try {
                        gpsStatus = true;
                        user.put("GPSStatus", gpsStatus);
                        user.put("GPSEnable", dateTime);
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(com.parse.ParseException e) {
                                if (e == null) {
                                    Log.e("SaveInBackground", "Success");
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (gpsStatus == true) {

                    if (isInternetPresent) {
                        try {
                            gpsStatus = false;
                            user.put("GPSStatus", gpsStatus);
                            user.put("GPSDisable", dateTime);
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(com.parse.ParseException e) {
                                    if (e == null) {
                                        Log.e("SaveInBackground", "Success");
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1,new Notification());
    }
}