package com.fti.sisfo.tours;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.fti.sisfo.tours.tools.GPSTracker;
import com.fti.sisfo.tours.volley.ListData;
import com.fti.sisfo.tours.volley.MySingleton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class PrimaryFragment extends Fragment implements OnMapReadyCallback {
    private static GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager mLocationManager;
    private static double currentLat = 0;
    private static double currentLon = 0;
    private static Marker now;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.primary_layout, null);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.categorymap);
        mapFragment.getMapAsync(this);

        FloatingActionButton fab_layer = (FloatingActionButton) view.findViewById(R.id.fab_layer);
        fab_layer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMapTypeSelectorDialog();
            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);


        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-0.302680, 100.374982);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));


        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(getContext(), "You have to accept to enjoy all app's services!", Toast.LENGTH_LONG).show();
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }

        addListenerLocation();

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(sydney)      // Sets the center of the map to Mountain View
                .zoom(14)                   // Sets the zoom
                .bearing(10)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        callNews(0);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            public void onInfoWindowClick(Marker marker) {
                try{
                    Intent intent = new Intent(getActivity(), MapActivity.class);
                    intent.putExtra(TAG_ID, Integer.parseInt(mMarkers.get("id"+marker.getId())));
                    intent.putExtra(TAG_NAME, mMarkers.get("name"+marker.getId()));
                    intent.putExtra(TAG_PHONE, mMarkers.get("phone"+marker.getId()));
                    intent.putExtra(TAG_ADDRESS, mMarkers.get("address"+marker.getId()));
                    intent.putExtra(TAG_KATEGORI, mMarkers.get("kategori"+marker.getId()));
                    intent.putExtra(TAG_GAMBAR, mMarkers.get("gambar"+marker.getId()));
                    intent.putExtra(TAG_THUMB, mMarkers.get("thumb"+marker.getId()));
                    intent.putExtra(TAG_POINT, mMarkers.get("point"+marker.getId()));
                    intent.putExtra(TAG_CITY, mMarkers.get("city"+marker.getId()));
                    startActivity(intent);
                } catch (Exception e){
                    Log.e("Marker","This Is Current Location");
                }
            }
        });
    }

    private static final CharSequence[] MAP_TYPE_ITEMS = {"Road Map", "Satellite", "Terrain", "Hybrid" };

    private void showMapTypeSelectorDialog() {
        // Prepare the dialog by setting up a Builder.
        final String fDialogTitle = "Select Map Type";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(fDialogTitle);

        // Find the current map type to pre-check the item representing the current state.
        int checkItem = mMap.getMapType()-1 ;

        // Add an OnClickListener to the dialog, so that the selection will be handled.
        builder.setSingleChoiceItems(
                MAP_TYPE_ITEMS,
                checkItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        // Perform an action depending on which item was selected.
                        switch (item) {
                            case 0:
                                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                break;
                            case 1:
                                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                break;
                            case 2:
                                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                break;
                            default:
                                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        }
                        dialog.dismiss();
                    }
                }
        );

        // Build the dialog and show it.
        AlertDialog fMapTypeDialog = builder.create();
        fMapTypeDialog.setCanceledOnTouchOutside(true);
        fMapTypeDialog.show();
    }

    private void addListenerLocation() {
        mLocationManager = (LocationManager)
                getActivity().getSystemService(Context.LOCATION_SERVICE);
        android.location.LocationListener mLocationListener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (now != null) {
                    now.remove();
                }
                currentLat = location.getLatitude();
                currentLon = location.getLongitude();
                LatLng myLaLn = new LatLng(currentLat, currentLon);
                //Toast.makeText(getActivity().getBaseContext(), currentLat + "-" + currentLon, Toast.LENGTH_SHORT).show();
                MarkerOptions markerOptions = new MarkerOptions().position(myLaLn).title(
                        "Current Location").draggable(true);
                now = mMap.addMarker(markerOptions);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(markerOptions.getPosition()));

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
                if (ActivityCompat.checkSelfPermission(getActivity().getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (lastKnownLocation != null) {
                    currentLat = lastKnownLocation.getLatitude();
                    currentLon = lastKnownLocation.getLongitude();
                    Toast.makeText(getActivity().getBaseContext(), currentLat + "-" + currentLon, Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 500, 10, mLocationListener);
    }

    GPSTracker gps;
    public SharedPreferences setting;
    private int offSet = 0;
    private static final String TAG = CategoryActivity.class.getSimpleName();
    public static final String TAG_ID       = "id";
    public static final String TAG_NAME     = "name";
    public static final String TAG_PHONE    = "phone";
    public static final String TAG_ADDRESS  = "address";
    public static final String TAG_GAMBAR   = "gambar";
    public static final String TAG_THUMB   = "thumb";
    public static final String TAG_POINT    = "point";
    public static final String TAG_KATEGORI = "kategori";
    public static final String TAG_CITY = "city";

    public void callNews(final int page) {

        if(page == 0) {
            SharedPreferences pref = getActivity().getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE);
            String Filterisasi = "&kategori=" + getActivity().getIntent().getStringExtra("title") + "&user_id=" + pref.getString("personID", "");
            gps = new GPSTracker(getContext());
            if (gps.canGetLocation()) {
                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();
                Filterisasi += "&latlng=" + longitude + "," + latitude;
                //Toast.makeText(getActivity().getApplicationContext(),"Your Location is -\nLat: " + latitude + "\nLong: "+ longitude, Toast.LENGTH_LONG).show();
            } else {
                gps.showSettingsAlert();
            }
            setting = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String url_list = setting.getString("server_url", "http://192.168.1.4/tours/") + "?way=api/index/getRecommendationByKategori&page=";
            final String url_image = setting.getString("server_url", "http://192.168.1.4/tours/") + "assets/image/";
            JsonArrayRequest arrReq = new JsonArrayRequest(url_list + page + Filterisasi,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() > 0) {
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject obj = response.getJSONObject(i);
                                    String[] latLng = "-0.312704, 100.373881".split(",");
                                    if (obj.getString(TAG_POINT) != null) {
                                        latLng = obj.getString(TAG_POINT).replace("(", "").replace(")", "").split(",");
                                    }

                                    LatLng marker = new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));

                                    Marker mkr;
                                    if(i < 1) {
                                        mkr = mMap.addMarker(new MarkerOptions().position(marker).title(obj.getString(TAG_NAME)).icon(BitmapDescriptorFactory.fromResource(
                                                getResources().getIdentifier("marker_" + obj.getString(TAG_KATEGORI), "drawable", getActivity().getPackageName()))
                                        ));

                                    } else {
                                        mkr = mMap.addMarker(new MarkerOptions().position(marker).title(obj.getString(TAG_NAME)).icon(BitmapDescriptorFactory.fromResource(
                                                getResources().getIdentifier("marker2_" + obj.getString(TAG_KATEGORI), "drawable", getActivity().getPackageName()))
                                        ));
                                    }

                                    mMarkers.put("id"+mkr.getId(), ""+obj.getInt(TAG_ID));
                                    mMarkers.put("name"+mkr.getId(),obj.getString(TAG_NAME));
                                    mMarkers.put("phone"+mkr.getId(),obj.getString(TAG_PHONE));
                                    mMarkers.put("address"+mkr.getId(),obj.getString(TAG_ADDRESS));
                                    mMarkers.put("kategori"+mkr.getId(),obj.getString(TAG_KATEGORI));
                                    mMarkers.put("city"+mkr.getId(),obj.getString(TAG_CITY));
                                    if (obj.has(TAG_POINT)) {
                                        mMarkers.put("point"+mkr.getId(),obj.getString(TAG_POINT));
                                    }


                                    if (obj.getString(TAG_GAMBAR) != "null") {
                                        mMarkers.put("gambar"+mkr.getId(),url_image+obj.getString(TAG_GAMBAR));
                                    }
                                    if (obj.getString(TAG_THUMB) != "null") {
                                        mMarkers.put("thumb"+mkr.getId(),url_image+obj.getString(TAG_THUMB));
                                    }
                                    offSet = page + 1;
                                    Log.d(TAG, "offSet " + offSet);
                                } catch (JSONException e) {
                                    Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                                }
                                //adapter.notifyDataSetChanged();
                            }
                        }
                        //swipe.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    //swipe.setRefreshing(false);
                }
            });
            MySingleton.getInstance(getActivity()).addToRequestQueue(arrReq);
        }
    }

    private static HashMap<String, String> mMarkers = new HashMap<>();

}
