package com.fti.sisfo.tours;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.fti.sisfo.tours.volley.MySingleton;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.StreetViewPanorama.OnStreetViewPanoramaChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.rey.material.widget.Slider;
import com.rey.material.widget.Spinner;

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
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * This shows how to create a simple activity with streetview and a map
 */
public class StreetFragment extends Fragment
        implements OnMarkerDragListener, OnStreetViewPanoramaChangeListener,OnMapReadyCallback  {

    private static final String MARKER_POSITION_KEY = "MarkerPosition";

    // George St, Sydney
    private static final LatLng SYDNEY = new LatLng(-0.3053167,100.3694229);

    private StreetViewPanorama mStreetViewPanorama;

    private Marker mMarker;
    Marker now;
    LatLng markerPosition;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.street_layout, null);


        markerPosition = SYDNEY;

        /*SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);*/

        SupportStreetViewPanoramaFragment streetViewPanoramaFragment = (SupportStreetViewPanoramaFragment)
                this.getChildFragmentManager().findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(
                new OnStreetViewPanoramaReadyCallback() {
                    @Override
                    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
                        mStreetViewPanorama = panorama;
                        mStreetViewPanorama.setOnStreetViewPanoramaChangeListener(
                                StreetFragment.this);
                        // Only need to set the position once as the streetview fragment will maintain
                        // its state.
                        if (savedInstanceState == null) {
                            mStreetViewPanorama.setPosition(SYDNEY);
                        }
                    }
                });
        SupportMapFragment mapFragment =
                (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        /*mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                //map.setOnMarkerDragListener();
                // Creates a draggable marker. Long press to drag.
                mMarker = map.addMarker(new MarkerOptions()
                        .position(markerPosition)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman))
                        .draggable(true));
            }
        });*/

        mapFragment.getMapAsync(this);




        final View searchform = inflater.inflate(R.layout.formsearch, null);
        final Slider sl_radius = (Slider)searchform.findViewById(R.id.slider_radius);
        final TextView tv_discrete = (TextView)searchform.findViewById(R.id.slider_tv_discrete);
        tv_discrete.setText(String.format("value=%d km", sl_radius.getValue()));
        sl_radius.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider view, boolean fromUser, float oldPos, float newPos, int oldValue, int newValue) {
                tv_discrete.setText(String.format("value=%d km", newValue));
            }
        });
        final android.widget.RatingBar ratingmin = (android.widget.RatingBar) searchform.findViewById(R.id.ratingBar);
        final android.widget.EditText et_katakunci = (android.widget.EditText) searchform.findViewById(R.id.edittext_katakunci);
        final Spinner spn_kategori = (Spinner) searchform.findViewById(R.id.spinner_kategori);
        String[] items = {"all","wisata", "penginapan", "resto", "ukm"};
        ArrayAdapter<String> adapt = new ArrayAdapter<>(getActivity(), R.layout.row_spn, items);
        adapt.setDropDownViewResource(R.layout.row_spn_dropdown);
        spn_kategori.setAdapter(adapt);

        final AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setTitle("Search Filter")
                .setView(searchform)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Filterisasi  = "&kategori="+spn_kategori.getSelectedItem().toString();
                        Filterisasi += "&katakunci="+et_katakunci.getText().toString();
                        Filterisasi += "&ratingmin="+ratingmin.getRating();
                        Filterisasi += "&radius="+sl_radius.getValue();
                        Radius = sl_radius.getValue();
                        callNews(0);
                        mMarker = mMap.addMarker(new MarkerOptions()
                                .position(markerPosition)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman))
                                .draggable(true));
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).create();

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viw) {
                alert.show();
                /*Snackbar.make(viw, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });
        FloatingActionButton fab_route = (FloatingActionButton) view.findViewById(R.id.fab_route);
        fab_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder().target(now.getPosition()).zoom(18).bearing(10).tilt(30).build()
                ));
            }
        });

        FloatingActionButton fab_layer = (FloatingActionButton) view.findViewById(R.id.fab_layer);
        fab_layer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMapTypeSelectorDialog();
            }
        });

        getActivity().setTitle("Street Viewer");
        return view;
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(MARKER_POSITION_KEY, mMarker.getPosition());
    }

    @Override
    public void onStreetViewPanoramaChange(StreetViewPanoramaLocation location) {
        if (location != null) {
            mMarker.setPosition(location.position);
            now = mMarker;
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mStreetViewPanorama.setPosition(marker.getPosition(), 150);
        now = marker;
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    private GoogleMap mMap;
    private static final String TAG = CategoryActivity.class.getSimpleName();
    private static String Filterisasi = "";
    private static int Radius = 0;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        mMap = googleMap;
        mMap.setOnMarkerDragListener(this);
        mMap.setPadding(0, 100, 0, 0);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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

        callNews(0);
        mMarker = mMap.addMarker(new MarkerOptions()
                .position(markerPosition)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman))
                .draggable(true));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(markerPosition)      // Sets the center of the map to Mountain View
                .zoom(18)                   // Sets the zoom
                .bearing(10)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));



        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            public void onInfoWindowClick(Marker marker) {
                try {
                    Intent intent = new Intent(getActivity(), MapActivity.class);
                    intent.putExtra(TAG_ID, Integer.parseInt(mMarkers.get("id" + marker.getId())));
                    intent.putExtra(TAG_NAME, mMarkers.get("name" + marker.getId()));
                    intent.putExtra(TAG_PHONE, mMarkers.get("phone" + marker.getId()));
                    intent.putExtra(TAG_ADDRESS, mMarkers.get("address" + marker.getId()));
                    intent.putExtra(TAG_KATEGORI, mMarkers.get("kategori" + marker.getId()));
                    intent.putExtra(TAG_GAMBAR, mMarkers.get("gambar" + marker.getId()));
                    intent.putExtra(TAG_THUMB, mMarkers.get("thumb" + marker.getId()));
                    intent.putExtra(TAG_POINT, mMarkers.get("point" + marker.getId()));
                    intent.putExtra(TAG_CITY, mMarkers.get("city" + marker.getId()));
                    startActivity(intent);
                } catch (Exception e){
                    Log.e("Marker","This Is Current Location");
                }
            }
        });
    }

    public SharedPreferences setting;

    public static final String TAG_ID       = "id";
    public static final String TAG_NAME     = "name";
    public static final String TAG_PHONE    = "phone";
    public static final String TAG_ADDRESS  = "address";
    public static final String TAG_GAMBAR   = "gambar";
    public static final String TAG_THUMB   = "thumb";
    public static final String TAG_POINT    = "point";
    public static final String TAG_KATEGORI = "kategori";
    public static final String TAG_CITY     = "city";
    private static HashMap<String, String> mMarkers = new HashMap<>();

    public void callNews(final int page) {

        if(page == 0) {

            SharedPreferences pref = getActivity().getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE);
            Filterisasi += "&user_id=" + pref.getString("personID", "");
            Filterisasi += "&latlng=" + currentLon + "," + currentLat;
            setting = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String url_list = setting.getString("server_url", "http://192.168.1.4/tours/") + "?way=api/index/getallobjek&page=";
            final String url_image = setting.getString("server_url", "http://192.168.1.4/tours/") + "assets/image/";
            mMap.clear();

            if(Radius != 0){
                mMap.addCircle(new CircleOptions()
                        .center(new LatLng(currentLat, currentLon))
                        .radius(1000*Radius)
                        .strokeColor(0x9955AAFF)
                        .fillColor(0x5555AAFF));
            }
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
                                        if (obj.has("intersect")) {
                                            mkr = mMap.addMarker(new MarkerOptions().position(marker).title(obj.getString(TAG_NAME)).icon(BitmapDescriptorFactory.fromResource(
                                                    getResources().getIdentifier("marker_" + obj.getString(TAG_KATEGORI), "drawable", getActivity().getPackageName()))
                                            ));
                                        } else {
                                            mkr = mMap.addMarker(new MarkerOptions().position(marker).title(obj.getString(TAG_NAME)).icon(BitmapDescriptorFactory.fromResource(
                                                    getResources().getIdentifier("marker2_" + obj.getString(TAG_KATEGORI), "drawable", getActivity().getPackageName()))
                                            ));
                                        }

                                        mMarkers.put("id" + mkr.getId(), "" + obj.getInt(TAG_ID));
                                        mMarkers.put("name" + mkr.getId(), obj.getString(TAG_NAME));
                                        mMarkers.put("phone" + mkr.getId(), obj.getString(TAG_PHONE));
                                        mMarkers.put("address" + mkr.getId(), obj.getString(TAG_ADDRESS));
                                        mMarkers.put("kategori" + mkr.getId(), obj.getString(TAG_KATEGORI));
                                        mMarkers.put("city" + mkr.getId(), obj.getString(TAG_CITY));
                                        if (obj.has(TAG_POINT)) {
                                            mMarkers.put("point" + mkr.getId(), obj.getString(TAG_POINT));
                                        }
                                        if (obj.getString(TAG_GAMBAR) != "null") {
                                            mMarkers.put("gambar" + mkr.getId(), url_image+obj.getString(TAG_GAMBAR));
                                        }
                                        if (obj.getString(TAG_THUMB) != "null") {
                                            mMarkers.put("thumb" + mkr.getId(), url_image+obj.getString(TAG_THUMB));
                                        }


                                    } catch (JSONException e) {
                                        Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                                    }

                                    //adapter.notifyDataSetChanged();
                                }
                                addListenerLocation();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    addListenerLocation();
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                }
            });
            MySingleton.getInstance(getActivity()).addToRequestQueue(arrReq);
        }
    }


    private static double currentLat = 0;
    private static double currentLon = 0;
    private LocationManager mLocationManager;

    private void addListenerLocation() {
        mLocationManager = (LocationManager)
                getActivity().getSystemService(Context.LOCATION_SERVICE);
        android.location.LocationListener mLocationListener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                currentLat = location.getLatitude();
                currentLon = location.getLongitude();

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

}
