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

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.deepwares.checkpointdwi.R;
import com.deepwares.checkpointdwi.activities.MainActivity;
import com.deepwares.checkpointdwi.network.ConnectionDetector;
import com.deepwares.checkpointdwi.session.Cache;
import com.deepwares.checkpointdwi.session.CatchValue;
import com.google.android.gms.location.LocationRequest;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.support.v4.app.NotificationCompat.PRIORITY_MIN;

public class LocationService extends Service {

    public static final String BROADCAST_ACTION = "Hello_World";
    private static final int TWO_MINUTES = 1000 * 50;
    public LocationManager locationManager;
    public MyLocationListener listener;
    private static final String TAG = LocationService.class.getSimpleName();
    public Location previousBestLocation = null;
    Intent intent;
    int counter = 0;
    ConnectionDetector connectionDetector;
    Boolean isInternetPresent = false;
    Timer timer = new Timer();
    private Date currentTime, startedAt_time;
    Date crashCurrentDate;
    String crashTime, crashDate;
    String notificationText = null, notificationText1 = null;
    boolean notificationStatus;
    String notification_date;
    String notification_time;
    final private static String NOTIFICATION_CHANNEL = "notification_channel";
    private NotificationManager mNotificationManager;
    private int START_FOREGROUND_ID = 101;
    private Boolean gpsStatus = false;

    // location accuracy settings, add for battery usage
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
        //createNotificationChannel();
        startServiceOreoCondition();

        connectionDetector = new ConnectionDetector(this);
        isInternetPresent = connectionDetector.isConnectingToInternet();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 6000, 50, listener); //50
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000, 50, listener); //50
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("STOP_SERVICE", "DONE");
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }

    private void startServiceOreoCondition() {
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_service";
            String CHANNEL_NAME = "My Background Service";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setCategory(Notification.CATEGORY_SERVICE).setSmallIcon(R.drawable.ic_launcher).setPriority(PRIORITY_MIN).build();
            startForeground(101, notification);
        }
    }

    public class MyLocationListener implements LocationListener {
        @SuppressLint("LongLogTag")
        public void onLocationChanged(final Location loc) {
            Log.e("**************************************", "Location changed");
            Thread t = new Thread(new Runnable() {
                public void run() {
                    if (isInternetPresent) {
                        checkGPSStatus();
                    } else {}
                }
            });
            t.start();
            if (isBetterLocation(loc, previousBestLocation)) {
                loc.getLatitude();
                loc.getLongitude();
                intent.putExtra("Latitude", loc.getLatitude());
                intent.putExtra("Longitude", loc.getLongitude());
                intent.putExtra("Provider", loc.getProvider());
                if (loc != null) {
                    if (isInternetPresent) {
                        double speed = loc.getSpeed();
                        Log.e("Speed_test", String.valueOf(speed));
                        //Send result to activities
                        sendMessageToUI(loc.getLatitude(), loc.getLongitude(), speed);
                    } else {}
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

            if (speed >= 15) { //15
                final ParseUser user = ParseUser.getCurrentUser();
                if (user != null) {
                    ParseQuery<ParseObject> notificationListQuery = ParseQuery.getQuery("_User");
                    notificationListQuery.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
                    notificationListQuery.findInBackground(new FindCallback<ParseObject>() {
                        public void done(List<ParseObject> scoreList, ParseException e) {
                            if (e == null) {
                                if (scoreList.size() > 0) {
                                    for (ParseObject comment : scoreList) {
                                        if (comment.getBoolean("hasActiveEnabled")) {
                                            Cache.putData(CatchValue.NOTIFICATION, LocationService.this, true, Cache.CACHE_LOCATION_DISK);
                                            currentTime = new Date();
                                            crashCurrentDate = new Date();
                                            crashDate = new SimpleDateFormat("MMM dd, yyyy").format(crashCurrentDate);
                                            crashTime = new SimpleDateFormat("HH:mm").format(crashCurrentDate);
                                            notificationText = getResources().getString(R.string.text_you_are_in_motion);
                                            notificationText1 = getResources().getString(R.string.text_you_failed_for_test);
                                            if (isInternetPresent) {
                                                sendSpeedNotification(lat, lng, speed);
                                            } else {}
                                        }
                                    }
                                }
                            } else {
                                Log.d("score", "Error: " + e.getMessage());
                            }
                        }
                    });
                }
            }
        }

        private void sendSpeedNotification(double lat, double lng, double speed) {
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
                    if (Cache.getData(CatchValue.LOCKOUT, LocationService.this).equals(true)) {
                        Log.e("loCkOUTPERIOD", "YES");
                        long diff_elapsed = currentTime.getTime() - startedAt_time.getTime();
                        long seconds = diff_elapsed / 1000;
                        long minutes = seconds / 60;
                        Log.e("notificatin_status: ", " @@@ " + minutes);
                        if (minutes >= 60) { //60
                            Cache.putData(CatchValue.DATE, LocationService.this, currentTime, Cache.CACHE_LOCATION_DISK);
                            Cache.putData(CatchValue.NOTI_LATI, LocationService.this, lat, Cache.CACHE_LOCATION_DISK);
                            Cache.putData(CatchValue.NOTI_LONGI, LocationService.this, lng, Cache.CACHE_LOCATION_DISK);
                            Cache.putData(CatchValue.LOCKOUT, LocationService.this, false, Cache.CACHE_LOCATION_DISK);
                            if (isInternetPresent) {
                                startNotification(lat, lng, notificationText);
                            } else {}
                        }
                    } else {
                        Log.e("KnoCKOUT_PERIOD", "YES");
                        long diff_elapsed = currentTime.getTime() - startedAt_time.getTime();
                        long seconds = diff_elapsed / 1000;
                        long minutes = seconds / 60;
                        Log.e("notificatin_status: ", " @@@ " + minutes);
                        if (minutes >= 30) { //30
                            Log.e("minutes: ", "Knockout_period: " + minutes);
                            Cache.putData(CatchValue.DATE, LocationService.this, currentTime, Cache.CACHE_LOCATION_DISK);
                            Cache.putData(CatchValue.NOTI_LATI, LocationService.this, lat, Cache.CACHE_LOCATION_DISK);
                            Cache.putData(CatchValue.NOTI_LONGI, LocationService.this, lng, Cache.CACHE_LOCATION_DISK);
                            if (isInternetPresent) {
                                startNotification(lat, lng, notificationText);
                                final int interval = 300000; // 5 minutes
//                                final int interval = 120000; // 2 minutes
                                Handler handler = new Handler();
                                Runnable runnable = new Runnable(){
                                    public void run() {
                                        Log.e("notification_status: ", "check_notification @@@@@@@");
                                        checkNotification(lat, lng, notificationText1);
                                    }
                                };
                                handler.postAtTime(runnable, System.currentTimeMillis()+interval);
                                handler.postDelayed(runnable, interval);
                            }
                        }
                        Log.e("notificatin_status: ", " ### " + minutes);
                    }
                } else {
                    Cache.putData(CatchValue.DATE, LocationService.this, currentTime, Cache.CACHE_LOCATION_DISK);
                    Cache.putData(CatchValue.NOTI_LATI, LocationService.this, lat, Cache.CACHE_LOCATION_DISK);
                    Cache.putData(CatchValue.NOTI_LONGI, LocationService.this, lng, Cache.CACHE_LOCATION_DISK);
                    if (isInternetPresent) {
                        startNotification(lat, lng, notificationText);
                    } else {}
                }
            } catch (ParseException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                Crashlytics.setString("domain", "LocationMonitoringService");
                Crashlytics.setInt("code", e.getCode());
                Crashlytics.setString("Time", crashTime);
                Crashlytics.setString("ModuleName", "ActiveNotifyData");
                Crashlytics.setString("ErrorDescription", e.getMessage());
                Crashlytics.setString("deviceType", "Android");
            }
        }

        private void checkNotification(double lat, double lng, String notificationText1){
            if (notificationStatus == false) {
                if (isInternetPresent) {
                    Log.e("notification_status: ", "check_notification ########");
                    startNotification(lat, lng, notificationText1);
                } else {}
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
                notificationParse.save();
                Log.e("location_service: ", "successfully_update_the_records: " + "YES");
            } catch (Exception e) {
                Log.e("location_service: ", "successfully_update_the_records: " + "NO");
                e.printStackTrace();
                Log.e("score", "Error: " + e.getMessage());
                Crashlytics.logException(e);
                Crashlytics.setString("domain", "LocationService");
                Crashlytics.setString("Time", crashTime);
                Crashlytics.setString("ModuleName", "StartNotification");
                Crashlytics.setString("ErrorDescription", e.getMessage());
                Crashlytics.setString("deviceType", "Android");
            }
            Log.e("report_service", "object_id: " + notificationParse.getObjectId());
            //Store the notification object to session
            Cache.putData(CatchValue.OBJECTID, LocationService.this, notificationParse.getObjectId(), Cache.CACHE_LOCATION_DISK);

            if (isInternetPresent) {
                sendNotification(notificationText);
            } else {}
        }

        private void sendNotification(String notificationText) {
            Log.e("notificatin_text: ", " ### " + notificationText);
            // Add as notification for android oreo version
            Random random = new Random();
            int m = random.nextInt(9999 - 1000) + 1000;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent notificationIntent = new Intent(LocationService.this, MainActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(LocationService.this);
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(notificationIntent);
                PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(m, PendingIntent.FLAG_UPDATE_CURRENT);
                Notification.Builder notificationBuilder = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    notificationBuilder = new Notification.Builder(LocationService.this, NOTIFICATION_CHANNEL)
                            .setContentTitle(getResources().getString(R.string.app_name))
                            .setContentText(notificationText)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setStyle(new Notification.BigTextStyle().bigText(notificationText))
                            .setAutoCancel(true)
                            .setContentIntent(notificationPendingIntent);
                }
                getNotificationManager().notify(m, notificationBuilder.build());
            } else {
                NotificationCompat.Builder builder =
                        (NotificationCompat.Builder) new NotificationCompat.Builder(LocationService.this)
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

                Intent notificationIntent = new Intent(LocationService.this, MainActivity.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent contentIntent = PendingIntent.getActivity(LocationService.this, 0, notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent);

                // Add as notification
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(m, builder.build());
            }
            Cache.putData(CatchValue.NOTIFICATION, LocationService.this, true, Cache.CACHE_LOCATION_DISK);
        }

        //Last location details send to the server
        private void lastLocation(double lat, double lng) {
            if (isInternetPresent) {
                try {
                    final ParseUser user = ParseUser.getCurrentUser();
                    user.put("lastReportedLocation", new ParseGeoPoint(lat, lng));
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(com.parse.ParseException e) {
                            if (e == null) {
                                Log.e("SaveInBackground", "Success");
                                Crashlytics.setString("domain", "LocationService");
                                Crashlytics.setInt("code", 200);
                                Crashlytics.setString("ModuleName", "LastLocation");
                                Crashlytics.setString("deviceType", "Android");
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("score", "Error: " + e.getMessage());
                    Crashlytics.logException(e);
                    Crashlytics.setString("domain", "LocationService");
                    Crashlytics.setString("ModuleName", "LastLocation");
                    Crashlytics.setString("ErrorDescription", e.getMessage());
                    Crashlytics.setString("deviceType", "Android");
                }
            } else {}
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }

    private void checkGPSStatus() {
        Date date = new Date();
        String resultDate = new SimpleDateFormat("MMM dd, yyyy").format(date);
        String resultTime = new SimpleDateFormat("HH:mm").format(date);
        String dateTime = resultDate + ":" + resultTime;
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        final ParseUser user = ParseUser.getCurrentUser();
        if (user != null) {
            gpsStatus = user.getBoolean("GPSStatus");
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (gpsStatus != null) {
                if (!gpsStatus) {
                    if (isInternetPresent) {
                        try {
                            gpsStatus = true;
                            user.put("GPSStatus", gpsStatus);
                            user.put("GPSEnable", dateTime);
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(com.parse.ParseException e) {
                                    if (e == null) {
                                        Log.e("SaveInBackground", "Success");
                                        Crashlytics.setString("domain", "LocationService");
                                        Crashlytics.setInt("code", 200);
                                        Crashlytics.setString("Time", crashTime);
                                        Crashlytics.setString("ModuleName", "CheckGpsStatus");
                                        Crashlytics.setString("deviceType", "Android");
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("score", "Error: " + e.getMessage());
                            Crashlytics.logException(e);
                            Crashlytics.setString("domain", "LocationService");
                            Crashlytics.setString("Time", crashTime);
                            Crashlytics.setString("ModuleName", "CheckGpsStatus");
                            Crashlytics.setString("ErrorDescription", e.getMessage());
                            Crashlytics.setString("deviceType", "Android");
                        }
                    } else {}
                }
            } else {}
        } else {
            if (gpsStatus != null) {
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
                                        Crashlytics.setString("domain", "LocationService");
                                        Crashlytics.setInt("code", 200);
                                        Crashlytics.setString("Time", crashTime);
                                        Crashlytics.setString("ModuleName", "CheckGpsStatus");
                                        Crashlytics.setString("deviceType", "Android");
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("score", "Error: " + e.getMessage());
                            Crashlytics.logException(e);
                            Crashlytics.setString("domain", "LocationService");
                            Crashlytics.setString("Time", crashTime);
                            Crashlytics.setString("ModuleName", "CheckGpsStatus");
                            Crashlytics.setString("ErrorDescription", e.getMessage());
                            Crashlytics.setString("deviceType", "Android");
                        }
                    } else {}
                }
            } else {}
        }
    }

    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }
}