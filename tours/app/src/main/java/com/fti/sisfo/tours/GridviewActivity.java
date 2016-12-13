package com.fti.sisfo.tours;


import android.content.Intent;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.fti.sisfo.tours.picasso.GridItem;
import com.fti.sisfo.tours.picasso.GridViewAdapter;
import com.fti.sisfo.tours.signin.SignInActivity;
import com.fti.sisfo.tours.volley.ListAdapter;
import com.fti.sisfo.tours.volley.ListData;
import com.fti.sisfo.tours.volley.MySingleton;
import com.fti.sisfo.tours.volley.TopAdapter;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ViewFlipper;

public class GridviewActivity extends Fragment {

    private static final String TAG = GridviewActivity.class.getSimpleName();

    public SharedPreferences setting;
    private ProgressBar mProgressBar;

    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;
    private String SERVER_IMG;
    private String FEED_URL;
    View view;

    List<ListData> newsList = new ArrayList<>();
    TopAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        view = inflater.inflate(R.layout.activity_gridview, null);

        setting = PreferenceManager.getDefaultSharedPreferences(getActivity());

        SERVER_IMG = setting.getString("server_url","http://192.168.1.4/tours/")+"assets/image/";
        FEED_URL = setting.getString("server_url", "http://192.168.1.4/tours/") + "?way=api/index/category";
        GridView mGridView = (GridView) view.findViewById(R.id.gridView);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);




        //Initialize with empty data
        mGridData = new ArrayList<>();
        mGridAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_layout, mGridData);
        mGridView.setAdapter(mGridAdapter);

        //Grid view click event
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                GridItem item = (GridItem) parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), CategoryActivity.class);
                intent.putExtra("title", item.getTitle()).putExtra("image", item.getImage());
                startActivity(intent);
            }
        });


        ListView list  = (ListView) view.findViewById(R.id.list_data);
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
                intent.putExtra(TAG_CITY, newsList.get(position).getCity());
                startActivity(intent);
            }
        });

        adapter = new TopAdapter(getActivity(), newsList);
        list.setAdapter(adapter);
        //Start download
        callNews();
        //new AsyncHttpTask().execute(FEED_URL);
        mProgressBar.setVisibility(View.VISIBLE);

        mViewFlipper = (ViewFlipper) view.findViewById(R.id.view_flipper);
        mViewFlipper.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                detector.onTouchEvent(event);
                return true;
            }
        });

        mViewFlipper.setAutoStart(true);
        mViewFlipper.setFlipInterval(4000);
        mViewFlipper.startFlipping();

        //animation listener
        mAnimationListener = new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
                //animation started event
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                //TODO animation stopped event
            }
        };




        getActivity().setTitle("Tours");
        return view;
    }



    public static final String TAG_ID       = "id";
    public static final String TAG_NAME     = "name";
    public static final String TAG_PHONE    = "phone";
    public static final String TAG_ADDRESS  = "address";
    public static final String TAG_GAMBAR   = "gambar";
    public static final String TAG_THUMB   = "thumb";
    public static final String TAG_POINT    = "point";
    public static final String TAG_KATEGORI = "kategori";
    public static final String TAG_CITY     = "city";
    private void callNews(){
        JsonArrayRequest arrReq = new JsonArrayRequest(FEED_URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());
                        if (response.length() > 0) {
                            GridItem item;
                            for (int i = 0; i < response.length(); i++) {
                                final JSONObject post = response.optJSONObject(i);
                                try {
                                    mGridAdapter.setGridData(mGridData);

                                    String title = post.optString("name");
                                    item = new GridItem();
                                    item.setTitle(title);
                                    if (post.getString("image") != null) {
                                        item.setImage(SERVER_IMG + post.getString("image"));
                                    }
                                    mGridData.add(item);
                                    Log.d(TAG, "offSet ");

                                } catch (JSONException e) {
                                    Log.e("CATCH","masuk sini");
                                    try {
                                        JSONObject obj = response.getJSONObject(i);
                                        ListData news = new ListData();

                                        news.setId(obj.getInt(TAG_ID));
                                        news.setName(obj.getString(TAG_NAME));
                                        news.setPhone(obj.getString(TAG_PHONE));
                                        news.setAddress(obj.getString(TAG_ADDRESS));
                                        news.setKategori(obj.getString(TAG_KATEGORI));
                                        news.setCity(obj.getString(TAG_CITY));

                                        if (obj.has(TAG_POINT)) {
                                            news.setPoint(obj.getString(TAG_POINT));
                                        }

                                        if (obj.has("top")) {
                                            news.setTop(obj.getString("top"));
                                        }

                                        if (obj.getString(TAG_GAMBAR) != "null") {
                                            news.setGambar(SERVER_IMG + obj.getString(TAG_GAMBAR));
                                        }

                                        if (obj.getString(TAG_THUMB) != "null") {
                                            news.setThumb(SERVER_IMG + obj.getString(TAG_THUMB));
                                        }

                                        newsList.add(news);
                                    }catch(Exception f){


                                    }
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                        mProgressBar.setVisibility(View.GONE);
                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getActivity().getApplicationContext(), "Failed to fetch data!", Toast.LENGTH_LONG).show();
                mProgressBar.setVisibility(View.GONE);
            }
        });
        MySingleton.getInstance(getActivity()).addToRequestQueue(arrReq);

    }

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private ViewFlipper mViewFlipper;
    private AnimationListener mAnimationListener;
    private Context mContext;


    @SuppressWarnings("deprecation")
    private final GestureDetector detector = new GestureDetector(new SwipeGestureDetector());


    class SwipeGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                // right to left swipe
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.left_in));
                    mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.left_out));
                    // controlling animation
                    mViewFlipper.getInAnimation().setAnimationListener(mAnimationListener);
                    mViewFlipper.showNext();
                    return true;
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.right_in));
                    mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(mContext,R.anim.right_out));
                    // controlling animation
                    mViewFlipper.getInAnimation().setAnimationListener(mAnimationListener);
                    mViewFlipper.showPrevious();
                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }
    }

}