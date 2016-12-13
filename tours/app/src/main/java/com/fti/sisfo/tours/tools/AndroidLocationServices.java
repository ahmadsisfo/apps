package com.fti.sisfo.tours.tools;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fti.sisfo.tours.MainActivity;
import com.fti.sisfo.tours.MapActivity;
import com.fti.sisfo.tours.R;

import com.fti.sisfo.tours.volley.MySingleton;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AndroidLocationServices extends Service {

    WakeLock wakeLock;

    public AndroidLocationServices() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);

        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoNotSleep");

        // Toast.makeText(getApplicationContext(), "Service Created",
        // Toast.LENGTH_SHORT).show();

        Log.e("Google", "Service Created");

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        super.onStartCommand(intent,1, startId);

        //new ToggleGPS(getApplicationContext()).turnGPSOn();

        // Toast.makeText(getApplicationContext(), "Service Started",
        // Toast.LENGTH_SHORT).show();
        Log.e("Google", "Service Started");

        LocationManager locationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(new MainActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                5000, 5, listener);
        return START_STICKY;
    }

    private LocationListener listener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub

            Log.e("Google", "Location Changed");

            if (location == null)
                return;

            if (isConnectingToInternet(getApplicationContext())) {

                try {
                    Log.e("latitude", location.getLatitude() + "");
                    Log.e("longitude", location.getLongitude() + "");

                    String FEED_URL = PreferenceManager.getDefaultSharedPreferences(
                            getApplicationContext()).getString("server_url","http://192.168.1.4/tours/")+"?way=api/index/tracking"+
                            "&latlng=" + location.getLongitude() + ","+location.getLatitude()+
                            "&tracking_mode="+ PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("tracking_mode","") +
                            "&user_id=" + getSharedPreferences("ACCOUNT", MODE_PRIVATE).getString("personID","");
                    String IMG_URL = PreferenceManager.getDefaultSharedPreferences(
                            getApplicationContext()).getString("server_url", "http://192.168.1.4/tours/") + "assets/image/";
                    Toast.makeText(getApplicationContext(), location.getLatitude()+" "+location.getLongitude(), Toast.LENGTH_SHORT).show();


                    callNews(FEED_URL, IMG_URL);
                    //new LocationWebService(getApplicationContext()).execute(FEED_URL);


                } catch(Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }
    };


    public static final String TAG_ID       = "id";
    public static final String TAG_NAME     = "name";
    public static final String TAG_PHONE    = "phone";
    public static final String TAG_ADDRESS  = "address";
    public static final String TAG_GAMBAR   = "gambar";
    public static final String TAG_THUMB   = "thumb";
    public static final String TAG_POINT    = "point";
    public static final String TAG_KATEGORI = "kategori";
    public static final String TAG_CITY     = "city";
    private static int NOTIF_ID =0;
    private static String NOTIF_NAME ="";
    public void callNews(String url, final String img_url) {

        JsonObjectRequest arrReq = new JsonObjectRequest(Request.Method.GET,url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        if(setting.getBoolean("notif_switch",true)) {
                            try {
                                if(NOTIF_NAME != (response.getString(TAG_NAME))){
                                    NOTIF_NAME = response.getString(TAG_NAME);

                                    Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                                    String name = "";
                                    intent.putExtra(TAG_ID, Integer.parseInt(response.getString(TAG_ID)));
                                    intent.putExtra(TAG_NAME, response.getString(TAG_NAME));
                                    intent.putExtra(TAG_PHONE, response.getString(TAG_PHONE));
                                    intent.putExtra(TAG_ADDRESS, response.getString(TAG_ADDRESS));
                                    intent.putExtra(TAG_KATEGORI, response.getString(TAG_KATEGORI));
                                    intent.putExtra(TAG_GAMBAR, img_url+response.getString(TAG_GAMBAR));
                                    intent.putExtra(TAG_THUMB, img_url+response.getString(TAG_THUMB));
                                    intent.putExtra(TAG_POINT, response.getString(TAG_POINT));
                                    intent.putExtra(TAG_CITY, response.getString(TAG_CITY));
                                    name = response.getString(TAG_NAME);
                                    PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), NOTIF_ID, intent, 0);

                                    Notification n = new Notification.Builder(getApplicationContext())
                                            .setContentTitle("(Tours) object is nearby")
                                            .setContentText(name)
                                            .setSmallIcon(R.mipmap.ic_launcher)
                                            .setContentIntent(pIntent)
                                            .setAutoCancel(true)
                                            .build();
                                    NotificationManager notificationManager =
                                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                    notificationManager.notify(NOTIF_ID, n);
                                    NOTIF_ID++;
                                }
                                Log.e("BERHASIL", response.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }


                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("CALL NEWS","get nearby empty");
            }
        });
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(arrReq);

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        new ToggleGPS(getApplicationContext()).turnGPSOff();
        wakeLock.release();

    }

    public static boolean isConnectingToInternet(Context _context) {
        ConnectivityManager connectivity = (ConnectivityManager) _context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (NetworkInfo anInfo : info)
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }

}