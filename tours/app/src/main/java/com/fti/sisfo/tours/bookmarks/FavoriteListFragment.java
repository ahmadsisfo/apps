package com.fti.sisfo.tours.bookmarks;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.fti.sisfo.tours.MapActivity;
import com.fti.sisfo.tours.R;
import com.fti.sisfo.tours.volley.ListData;

public class FavoriteListFragment extends Fragment {

    public static final String TAG_NO       = "no";
    public static final String TAG_ID       = "id";
    public static final String TAG_NAME     = "name";
    public static final String TAG_PHONE    = "phone";
    public static final String TAG_ADDRESS  = "address";
    public static final String TAG_GAMBAR   = "gambar";
    public static final String TAG_THUMB   = "thumb";
    public static final String TAG_POINT    = "point";
    public static final String TAG_KATEGORI = "kategori";
    public static final String TAG_CITY = "city";

    public static final String ARG_ITEM_ID = "favorite_list";

    ListView favoriteList;
    SharedPreference sharedPreference;
    List<ListData> favorites;

    Activity activity;
    ObjekListAdapter ObjekListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container,
                false);
        // Get favorite items from SharedPreferences.

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.hide();

        SwipeRefreshLayout swipe = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        //swipe.setDistanceToTriggerSync(999999);
        swipe.setEnabled(false);
        sharedPreference = new SharedPreference();
        favorites = sharedPreference.getFavorites(activity);

        if (favorites == null) {
            showAlert(getResources().getString(R.string.no_favorites_items),
                    getResources().getString(R.string.no_favorites_msg));
        } else {

            if (favorites.size() == 0) {
                showAlert(
                        getResources().getString(R.string.no_favorites_items),
                        getResources().getString(R.string.no_favorites_msg));
            }

            favoriteList = (ListView) view.findViewById(R.id.list_data);
            if (favorites != null) {
                ObjekListAdapter = new ObjekListAdapter(activity, favorites);
                favoriteList.setAdapter(ObjekListAdapter);

                favoriteList.setOnItemClickListener(new OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View arg1,
                                            int position, long arg3) {
                        // TODO Auto-generated method stub
                        Intent intent = new Intent(getActivity(), MapActivity.class);
                        intent.putExtra(TAG_ID, favorites.get(position).getId());
                        intent.putExtra(TAG_NAME, favorites.get(position).getName());
                        intent.putExtra(TAG_PHONE, favorites.get(position).getPhone());
                        intent.putExtra(TAG_ADDRESS, favorites.get(position).getAddress());
                        intent.putExtra(TAG_KATEGORI, favorites.get(position).getKategori());
                        intent.putExtra(TAG_GAMBAR, favorites.get(position).getGambar());
                        intent.putExtra(TAG_THUMB, favorites.get(position).getThumb());
                        intent.putExtra(TAG_POINT, favorites.get(position).getPoint());
                        intent.putExtra(TAG_CITY, favorites.get(position).getCity());
                        startActivity(intent);
                    }
                });

                favoriteList.setOnItemLongClickListener(new OnItemLongClickListener() {

                            @Override
                            public boolean onItemLongClick(
                                    AdapterView<?> parent, View view,
                                    int position, long id) {

                                ImageView button = (ImageView) view
                                        .findViewById(R.id.imgbtn_favorite);

                                String tag = button.getTag().toString();
                                if (tag.equalsIgnoreCase("no")) {
                                    sharedPreference.addFavorite(activity,
                                            favorites.get(position));
                                    Toast.makeText(
                                            activity,
                                            activity.getResources().getString(
                                                    R.string.add_favr),
                                            Toast.LENGTH_SHORT).show();

                                    button.setTag("yes");
                                    button.setImageResource(R.drawable.hearth_yes);
                                } else {
                                    sharedPreference.removeFavorite(activity,
                                            favorites.get(position));
                                    button.setTag("no");
                                    button.setImageResource(R.drawable.hearth_no);
                                    ObjekListAdapter.remove(favorites
                                            .get(position));
                                    Toast.makeText(
                                            activity,
                                            activity.getResources().getString(
                                                    R.string.remove_favr),
                                            Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            }
                        });
            }
        }
        return view;
    }

    public void showAlert(String title, String message) {
        if (activity != null && !activity.isFinishing()) {
            AlertDialog alertDialog = new AlertDialog.Builder(activity)
                    .create();
            alertDialog.setTitle(title);
            alertDialog.setMessage(message);
            alertDialog.setCancelable(false);

            // setting OK Button
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            // activity.finish();
                            getFragmentManager().popBackStackImmediate();
                        }
                    });
            alertDialog.show();
        }
    }

    @Override
    public void onResume() {
        getActivity().setTitle(R.string.favorites);
        //getActivity().getActionBar().setTitle(R.string.favorites);
        super.onResume();
    }
}