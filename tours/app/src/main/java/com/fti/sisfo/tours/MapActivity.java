package com.fti.sisfo.tours;


import android.Manifest;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;


import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fti.sisfo.tours.bookmarks.SharedPreference;
import com.fti.sisfo.tours.signin.SignInActivity;

import com.fti.sisfo.tours.volley.ListAdapter;
import com.fti.sisfo.tours.volley.ListData;
import com.fti.sisfo.tours.volley.MySingleton;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static Intent intent;
    public static final String TAG_ID = "id";
    public static final String TAG_NAME = "name";
    public static final String TAG_PHONE = "phone";
    public static final String TAG_ADDRESS = "address";
    public static final String TAG_GAMBAR = "gambar";
    public static final String TAG_THUMB = "thumb";
    public static final String TAG_POINT = "point";
    public static final String TAG_KATEGORI = "kategori";
    public static final String TAG_CITY = "city";

    public SharedPreferences setting;
    private static LatLng sydney;

    private static RatingBar ratebar;
    private WebView mWebView;

    ListView list_related;
    ListView list_nearby;
    ListView list_viewer;

    List<ListData> relatedList = new ArrayList<>();
    List<ListData> nearbyList = new ArrayList<>();
    List<ListData> viewerList = new ArrayList<>();
    ListAdapter adapter_related;
    ListAdapter adapter_nearby;
    ListAdapter adapter_viewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        setting = PreferenceManager.getDefaultSharedPreferences(this);

        mWebView = (WebView) findViewById(R.id.rating_webview);
        mWebView.setVerticalScrollBarEnabled(true);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress)
            {
                MapActivity.this.setTitle("Loading...");
                MapActivity.this.setProgress(progress * 100);

                if(progress == 100)
                    MapActivity.this.setTitle(R.string.app_name);
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                view.loadUrl(url);
                return true;
            }
        });

        intent = getIntent();
        final String cheeseName = intent.getStringExtra(TAG_NAME)+" ("+intent.getStringExtra(TAG_KATEGORI)+")";
        final String image = intent.getStringExtra(TAG_GAMBAR);
        final String info = "Alamat. " + intent.getStringExtra(TAG_ADDRESS) + "\n\n" + intent.getStringExtra(TAG_PHONE);
        final String info_city =  intent.getStringExtra(TAG_CITY)+" City";


        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView textname = (TextView) findViewById(R.id.objek_name);
        final TextView textinfo = (TextView) findViewById(R.id.objek_info);
        final TextView textinfo_city = (TextView) findViewById(R.id.objek_info_city);
        final ImageView textimage = (ImageView) findViewById(R.id.objek_image);



        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        textname.setText(cheeseName);
        textinfo.setText(info);
        textinfo_city.setText(info_city);
        if (image != null) {
            Picasso.with(this).load(Uri.parse(image)).placeholder(R.drawable.wallpaper)
                    .error(R.drawable.wallpaper)
                    .into(textimage);
        }

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(cheeseName);

        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(@Nullable AppBarLayout appBarLayout) {
                return false;
            }
        });
        params.setBehavior(behavior);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton fab_route = (FloatingActionButton) findViewById(R.id.fab_route);
        FloatingActionButton fab_layer = (FloatingActionButton) findViewById(R.id.fab_layer);
        FloatingActionButton fab_rate = (FloatingActionButton) findViewById(R.id.fab_rate);

        fab_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder().target(sydney).zoom(15).bearing(90).tilt(30).build()
                ));
            }
        });

        fab_layer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMapTypeSelectorDialog();
            }
        });

        final View formrate = getLayoutInflater().inflate(R.layout.formrate, null);
        ratebar = (RatingBar) formrate.findViewById(R.id.ratingBar);
        final AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle("Rate This Object ?")
                .setView(formrate)
                .setIcon(R.drawable.ic_notifications_black_24dp)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ratenow();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).create();

        fab_rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.show();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        GoogleApiClient client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        
        list_related  = (ListView) findViewById(R.id.list_data_related);
        list_nearby  = (ListView) findViewById(R.id.list_data_nearby);
        list_viewer  = (ListView) findViewById(R.id.list_data_viewer);
        
        relatedList.clear();
        nearbyList.clear();
        viewerList.clear();

        list_related.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(MapActivity.this, MapActivity.class);
                intent.putExtra(TAG_ID, relatedList.get(position).getId());
                intent.putExtra(TAG_NAME, relatedList.get(position).getName());
                intent.putExtra(TAG_PHONE, relatedList.get(position).getPhone());
                intent.putExtra(TAG_ADDRESS, relatedList.get(position).getAddress());
                intent.putExtra(TAG_KATEGORI, relatedList.get(position).getKategori());
                intent.putExtra(TAG_GAMBAR, relatedList.get(position).getGambar());
                intent.putExtra(TAG_THUMB, relatedList.get(position).getThumb());
                intent.putExtra(TAG_POINT, relatedList.get(position).getPoint());
                intent.putExtra(TAG_CITY, relatedList.get(position).getCity());
                startActivity(intent);
            }
        });


        list_nearby.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(MapActivity.this, MapActivity.class);
                intent.putExtra(TAG_ID, nearbyList.get(position).getId());
                intent.putExtra(TAG_NAME, nearbyList.get(position).getName());
                intent.putExtra(TAG_PHONE, nearbyList.get(position).getPhone());
                intent.putExtra(TAG_ADDRESS, nearbyList.get(position).getAddress());
                intent.putExtra(TAG_KATEGORI, nearbyList.get(position).getKategori());
                intent.putExtra(TAG_GAMBAR, nearbyList.get(position).getGambar());
                intent.putExtra(TAG_THUMB, nearbyList.get(position).getThumb());
                intent.putExtra(TAG_POINT, nearbyList.get(position).getPoint());
                intent.putExtra(TAG_CITY, nearbyList.get(position).getCity());
                startActivity(intent);
            }
        });

        list_viewer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(MapActivity.this, MapActivity.class);
                intent.putExtra(TAG_ID, viewerList.get(position).getId());
                intent.putExtra(TAG_NAME, viewerList.get(position).getName());
                intent.putExtra(TAG_PHONE, viewerList.get(position).getPhone());
                intent.putExtra(TAG_ADDRESS, viewerList.get(position).getAddress());
                intent.putExtra(TAG_KATEGORI, viewerList.get(position).getKategori());
                intent.putExtra(TAG_GAMBAR, viewerList.get(position).getGambar());
                intent.putExtra(TAG_THUMB, viewerList.get(position).getThumb());
                intent.putExtra(TAG_POINT, viewerList.get(position).getPoint());
                intent.putExtra(TAG_CITY, viewerList.get(position).getCity());
                startActivity(intent);
            }
        });


        list_related.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ImageView button = (ImageView) view.findViewById(R.id.imgbtn_favorite);
                String tag = button.getTag().toString();
                SharedPreference shared = new SharedPreference();

                if (tag.equalsIgnoreCase("no")) {
                    shared.addFavorite(getBaseContext(), relatedList.get(position));
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.add_favr),
                            Toast.LENGTH_SHORT).show();

                    button.setTag("yes");
                    button.setImageResource(R.drawable.hearth_yes);
                } else {
                    shared.removeFavorite(getBaseContext(), relatedList.get(position));
                    button.setTag("no");
                    button.setImageResource(R.drawable.hearth_no);
                    Toast.makeText(getBaseContext() ,getResources().getString(R.string.remove_favr),Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

        list_viewer.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ImageView button = (ImageView) view.findViewById(R.id.imgbtn_favorite);
                String tag = button.getTag().toString();
                SharedPreference shared = new SharedPreference();

                if (tag.equalsIgnoreCase("no")) {
                    shared.addFavorite(getBaseContext(), viewerList.get(position));
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.add_favr),
                            Toast.LENGTH_SHORT).show();

                    button.setTag("yes");
                    button.setImageResource(R.drawable.hearth_yes);
                } else {
                    shared.removeFavorite(getBaseContext(), viewerList.get(position));
                    button.setTag("no");
                    button.setImageResource(R.drawable.hearth_no);
                    Toast.makeText(getBaseContext() ,getResources().getString(R.string.remove_favr),Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

        list_nearby.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ImageView button = (ImageView) view.findViewById(R.id.imgbtn_favorite);
                String tag = button.getTag().toString();
                SharedPreference shared = new SharedPreference();

                if (tag.equalsIgnoreCase("no")) {
                    shared.addFavorite(getBaseContext(), nearbyList.get(position));
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.add_favr),
                            Toast.LENGTH_SHORT).show();

                    button.setTag("yes");
                    button.setImageResource(R.drawable.hearth_yes);
                } else {
                    shared.removeFavorite(getBaseContext(), nearbyList.get(position));
                    button.setTag("no");
                    button.setImageResource(R.drawable.hearth_no);
                    Toast.makeText(getBaseContext() ,getResources().getString(R.string.remove_favr),Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

        adapter_related = new ListAdapter(this, relatedList);
        list_related.setAdapter(adapter_related);
        adapter_viewer = new ListAdapter(this, viewerList);
        list_viewer.setAdapter(adapter_viewer);
        adapter_nearby = new ListAdapter(this, nearbyList);
        list_nearby.setAdapter(adapter_nearby);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);

        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(MapActivity.this, "You have to accept to enjoy all app's services!", Toast.LENGTH_LONG).show();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }


        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setPadding(0, 90, 0, 0);
        // Add a marker in Sydney and move the camera
        String[] latLng = "-0.312704, 100.373881".split(",");


        if (intent.getStringExtra(TAG_POINT) != null) {
            latLng = intent.getStringExtra(TAG_POINT).replace("(", "").replace(")", "").split(",");
        }

        sydney = new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));

        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in " + intent.getStringExtra(TAG_POINT)).icon(BitmapDescriptorFactory.fromResource(
                getResources().getIdentifier("marker_" + intent.getStringExtra(TAG_KATEGORI), "drawable", getPackageName()))
        ));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(sydney)      // Sets the center of the map to Mountain View
                .zoom(15)                   // Sets the zoom
                .bearing(10)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the buildermMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        getpolygon();
        showRatingWeb();
        callNews(0);
    }

    private void showRatingWeb(){
        SharedPreferences prefer = getSharedPreferences("OBJEK", MODE_PRIVATE);
        SharedPreferences pref = getSharedPreferences("ACCOUNT", MODE_PRIVATE);

        String get =  "&objek_id=" + intent.getIntExtra(TAG_ID,0) +
                      "&kategori="+ intent.getStringExtra(TAG_KATEGORI) +
                      "&user_id="+ pref.getString("personID","") ;
        //showalert("info", Float.toString(ratebar.getRating()));


        final SharedPreferences.Editor editor = prefer.edit();
        if(prefer.getString("objek_id_2", "") != (intent.getIntExtra(TAG_ID,0)+"")) {

            get +=  "&objek_id_1=" + prefer.getString("objek_id_1","") +
                    "&objek_id_2=" + prefer.getString("objek_id_2","") +
                    "&kategori_1=" + prefer.getString("kategori_1","") +
                    "&kategori_2=" + prefer.getString("kategori_2","") ;

            editor.putString("objek_id_1", prefer.getString("objek_id_2", ""));
            editor.putString("kategori_1", prefer.getString("kategori_2", ""));
            editor.putString("objek_id_2",""+intent.getIntExtra(TAG_ID,0));
            editor.putString("kategori_2",intent.getStringExtra(TAG_KATEGORI));
            editor.apply();

        }
        String url  = setting.getString("server_url","http://192.168.1.4/tours/")+"?way=api/index/chartrating" + get;
        mWebView.loadUrl(url);
    }

    private static final CharSequence[] MAP_TYPE_ITEMS = {"Road Map", "Satellite", "Terrain", "Hybrid"};

    private void showMapTypeSelectorDialog() {
        // Prepare the dialog by setting up a Builder.
        final String fDialogTitle = "Select Map Type";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(fDialogTitle);

        // Find the current map type to pre-check the item representing the current state.
        int checkItem = mMap.getMapType() - 1;

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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void showalert(String title, String message){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage(message)
                .create().show();
    }

    private void ratenow() {
        showalert("Success","Thank for your feedback");

        SharedPreferences pref = getSharedPreferences("ACCOUNT", MODE_PRIVATE);
        if(!pref.getBoolean("hasLoggedIn",false)){
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
        String get =  "&rating="+Float.toString(ratebar.getRating())+"&objek_id=" + intent.getIntExtra(TAG_ID,0) +
                "&user_id="+pref.getString("personID","") + "&kategori="+ intent.getStringExtra(TAG_KATEGORI) ;
        //showalert("info", Float.toString(ratebar.getRating()));
        String url  = setting.getString("server_url","http://192.168.1.4/tours/")+"?way=api/index/rate" + get;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //mTextView.setText("Response is: "+ response.substring(0,500));

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showalert("Error","That didn't work!");
            }
        });

        //queue.add(stringRequest);
        //MySingleton.getInstance(this).addToRequestQueue(stringRequest);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getpolygon() {

        String get =  "&objek_id=" + intent.getIntExtra(TAG_ID,0) +
                "&kategori="+ intent.getStringExtra(TAG_KATEGORI) ;
        final String url  = setting.getString("server_url","http://192.168.1.4/tours/")+"?way=api/index/getpolygon" + get;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            GeoJsonLayer layer = new GeoJsonLayer(mMap, response);
                            layer.getDefaultPolygonStyle().setFillColor(0x60FFD47F);
                            layer.getDefaultPolygonStyle().setStrokeColor(0x90FF2A2A);
                            layer.addLayerToMap();

                            //Log.e("error", url);
                            //Toast.makeText(getBaseContext(), response.toString(), Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            Toast.makeText(getBaseContext(), "pantesan", Toast.LENGTH_LONG).show();
                        }

                        System.out.println(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("error", "failed to get GeoJson");
                        Log.e("error", url);
                    }
                });
        MySingleton.getInstance(this).addToRequestQueue(request);
        //RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        //requestQueue.add(request);
    }

    private static final String TAG = MapActivity.class.getSimpleName();
    private int offSet = 0;
    private static String relasi;
    private void callNews(final int page){
        final String url_image = setting.getString("server_url","http://192.168.1.4/tours/")+"assets/image/";
        final String url_list  = setting.getString("server_url","http://192.168.1.4/tours/")+"?way=api/index/getRecommendationByObjek&page=";
        String get =  "&objek_id=" + intent.getIntExtra(TAG_ID,0) +
                "&kategori="+ intent.getStringExtra(TAG_KATEGORI);
        if (offSet == 0) {
            JsonArrayRequest arrReq = new JsonArrayRequest(url_list + page + get,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Log.d(TAG, response.toString());
                            if (response.length() > 0) {
                                for (int i = 0; i < response.length(); i++) {

                                    try {
                                        JSONObject obj = response.getJSONObject(i);
                                        ListData news = new ListData();

                                        news.setId(obj.getInt(TAG_ID));
                                        news.setName(obj.getString(TAG_NAME));
                                        news.setPhone(obj.getString(TAG_PHONE));
                                        news.setAddress(obj.getString(TAG_ADDRESS));
                                        news.setKategori(obj.getString(TAG_KATEGORI));
                                        news.setCity(obj.getString(TAG_CITY));

                                        offSet = page + 1;
                                        if (obj.has(TAG_POINT)) {
                                            news.setPoint(obj.getString(TAG_POINT));
                                        }
                                        if (obj.getString(TAG_GAMBAR) != "null") {
                                            news.setGambar(url_image + obj.getString(TAG_GAMBAR));
                                        }
                                        if (obj.getString(TAG_THUMB) != "null") {
                                            news.setThumb(url_image + obj.getString(TAG_THUMB));
                                        }
                                        // adding news to news array
                                        if (obj.has("related")) {
                                            relasi = "related";
                                            relatedList.add(news);
                                            Log.d(TAG, "SINI " + offSet);
                                        } else if (obj.has("viewer")) {
                                            relasi = "viewer";
                                            viewerList.add(news);
                                        } else if (obj.has("nearby")) {
                                            relasi = "nearby";
                                            nearbyList.add(news);
                                        }


                                        //if (page > offSet)


                                        Log.d(TAG, "offSet " + offSet);

                                    } catch (JSONException e) {
                                        Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                                    }
                                    if(relasi == "related") {
                                        adapter_related.notifyDataSetChanged();
                                    } else if(relasi == "viewer") {
                                        adapter_viewer.notifyDataSetChanged();
                                    } else if(relasi == "nearby") {
                                        adapter_nearby.notifyDataSetChanged();
                                    }
                                }
                            }

                        }

                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                }
            });
            MySingleton.getInstance(this).addToRequestQueue(arrReq);
        }
    }
}
