package com.fti.sisfo.tours;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.fti.sisfo.tours.bookmarks.SharedPreference;
import com.fti.sisfo.tours.tools.GPSTracker;
import com.fti.sisfo.tours.volley.ListAdapter;
import com.fti.sisfo.tours.volley.ListData;
import com.fti.sisfo.tours.volley.MySingleton;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SocialFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    ListView list;
    SharedPreference sharedPreference;
    ListAdapter adapter;
    SwipeRefreshLayout swipe;
    Handler handler;
    Runnable runnable;
    List<ListData> newsList = new ArrayList<ListData>();;
    public static final String TAG_NO       = "no";
    public static final String TAG_ID       = "id";
    public static final String TAG_NAME     = "name";
    public static final String TAG_PHONE    = "phone";
    public static final String TAG_ADDRESS  = "address";
    public static final String TAG_GAMBAR   = "gambar";
    public static final String TAG_THUMB   = "thumb";
    public static final String TAG_POINT    = "point";
    public static final String TAG_KATEGORI = "kategori";
    GPSTracker gps;
    private static String url_list;
    private static String url_image;
    public SharedPreferences setting;
    private int offSet = 0;
    int no;
    private static final String TAG = CategoryActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreference = new SharedPreference();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.social_layout, container, false);

        newsList.clear();
        swipe = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        list  = (ListView) view.findViewById(R.id.list_data);
        newsList.clear();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(getActivity(), MapActivity.class);
                intent.putExtra(TAG_ID, newsList.get(position).getId());
                intent.putExtra(TAG_NAME, newsList.get(position).getName());
                intent.putExtra(TAG_PHONE, newsList.get(position).getPhone());
                intent.putExtra(TAG_ADDRESS, newsList.get(position).getAddress());
                intent.putExtra(TAG_KATEGORI, newsList.get(position).getKategori());
                intent.putExtra(TAG_GAMBAR, newsList.get(position).getGambar());
                intent.putExtra(TAG_THUMB, newsList.get(position).getThumb());
                intent.putExtra(TAG_POINT, newsList.get(position).getPoint());
                startActivity(intent);
            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ImageView button = (ImageView) view.findViewById(R.id.imgbtn_favorite);
                String tag = button.getTag().toString();
                if (tag.equalsIgnoreCase("no")) {
                    sharedPreference.addFavorite(getActivity(), newsList.get(position));
                    Toast.makeText(getActivity(),
                            getActivity().getResources().getString(R.string.add_favr),
                            Toast.LENGTH_SHORT).show();

                    button.setTag("yes");
                    button.setImageResource(R.drawable.hearth_yes);
                } else {
                    sharedPreference.removeFavorite(getActivity(), newsList.get(position));
                    button.setTag("no");
                    button.setImageResource(R.drawable.hearth_no);
                    Toast.makeText(getActivity(),
                            getActivity().getResources().getString(R.string.remove_favr),
                            Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });


        adapter = new ListAdapter(getActivity(), newsList);
        list.setAdapter(adapter);
        swipe.setOnRefreshListener(this);

        swipe.post(new Runnable() {
                       @Override
                       public void run() {
                           swipe.setRefreshing(true);
                           newsList.clear();
                           adapter.notifyDataSetChanged();
                           callNews(0);
                       }
                   }
        );

        list.setOnScrollListener(new AbsListView.OnScrollListener() {

            private int currentVisibleItemCount;
            private int currentScrollState;
            private int currentFirstVisibleItem;
            private int totalItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                this.currentScrollState = scrollState;
                this.isScrollCompleted();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                this.currentFirstVisibleItem = firstVisibleItem;
                this.currentVisibleItemCount = visibleItemCount;
                this.totalItem = totalItemCount;
            }

            private void isScrollCompleted() {
                if (totalItem - currentFirstVisibleItem == currentVisibleItemCount
                        && this.currentScrollState == SCROLL_STATE_IDLE) {

                    swipe.setRefreshing(true);
                    handler = new Handler();

                    runnable = new Runnable() {
                        public void run() {
                            callNews(offSet);
                        }
                    };

                    handler.postDelayed(runnable, 3000);
                }
            }

        });

        return view;
    }

    public void showalert(String title, String message){
        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage(message)
                .create().show();
    }

    @Override
    public void onRefresh() {
        newsList.clear();
        adapter.notifyDataSetChanged();
        callNews(0);
    }

    public void callNews(final int page) {
        swipe.setRefreshing(true);
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
            url_list = setting.getString("server_url", "http://192.168.1.4/tours/") + "?way=api/index/getRecommendationByKategori&page=";
            url_image = setting.getString("server_url", "http://192.168.1.4/tours/") + "assets/image/";
            JsonArrayRequest arrReq = new JsonArrayRequest(url_list + page + Filterisasi,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {

                            if (response.length() > 0) {

                        /**/
                                // Parsing json
                                for (int i = 0; i < response.length(); i++) {
                                    try {

                                        JSONObject obj = response.getJSONObject(i);
                                        ListData news = new ListData();

                                        no = obj.getInt(TAG_NO);

                                        news.setId(obj.getInt(TAG_ID));
                                        news.setName(obj.getString(TAG_NAME));
                                        news.setPhone(obj.getString(TAG_PHONE));
                                        news.setAddress(obj.getString(TAG_ADDRESS));
                                        news.setKategori(obj.getString(TAG_KATEGORI));

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
                                        newsList.add(news);

                                        //if (page > offSet)
                                        offSet = page + 1;

                                        Log.d(TAG, "offSet " + offSet);

                                    } catch (JSONException e) {
                                        Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                                    }

                                    // notifying list adapter about data changes
                                    // so that it renders the list view with updated data
                                    adapter.notifyDataSetChanged();
                                }

                            }
                            swipe.setRefreshing(false);
                        }

                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    swipe.setRefreshing(false);
                }
            });


            MySingleton.getInstance(getActivity()).addToRequestQueue(arrReq);
        } else {
            swipe.setRefreshing(false);
        }
    }
}