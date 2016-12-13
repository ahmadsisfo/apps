package com.fti.sisfo.tours.bookmarks;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.fti.sisfo.tours.R;
import com.fti.sisfo.tours.volley.ListData;
import com.fti.sisfo.tours.volley.MySingleton;

public class ObjekListAdapter extends ArrayAdapter<ListData> {

    private Context context;
    List<ListData> Objeks;
    SharedPreference sharedPreference;
    ImageLoader imageLoader;

    public ObjekListAdapter(Context context, List<ListData> Objeks) {
        super(context, R.layout.fragment_item_list, Objeks);
        this.context = context;
        this.Objeks = Objeks;
        sharedPreference = new SharedPreference();
    }

    private class ViewHolder {
        TextView ObjekNameTxt;
        TextView ObjekPhoneTxt;
        ImageView favoriteImg;
    }

    @Override
    public int getCount() {
        return Objeks.size();
    }

    @Override
    public ListData getItem(int position) {
        return Objeks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.fragment_item, null);
            holder = new ViewHolder();
            holder.ObjekNameTxt = (TextView) convertView.findViewById(R.id.Listtitle);
            holder.ObjekPhoneTxt = (TextView) convertView.findViewById(R.id.Listphone);
            //holder.ObjekPriceTxt = (TextView) convertView.findViewById(R.id.txt_pdt_price);
            holder.favoriteImg = (ImageView) convertView.findViewById(R.id.imgbtn_favorite);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (imageLoader == null)
            imageLoader = MySingleton.getInstance(convertView.getContext()).getImageLoader();

        ListData listData = getItem(position);
        holder.ObjekNameTxt.setText(listData.getName());
        holder.ObjekPhoneTxt.setText(listData.getAddress());
        NetworkImageView thumbNail = (NetworkImageView) convertView.findViewById(R.id.ListthumbImage);
        //holder.ObjekPriceTxt.setText(listData.getPrice() + "");
        thumbNail.setImageUrl(listData.getThumb(), imageLoader);
        /*If a Objek exists in shared preferences then set heart_red drawable
         * and set a tag*/
        if (checkFavoriteItem(listData)) {
            holder.favoriteImg.setImageResource(R.drawable.hearth_yes);
            holder.favoriteImg.setTag("red");
        } else {
            holder.favoriteImg.setImageResource(R.drawable.hearth_no);
            holder.favoriteImg.setTag("grey");
        }

        return convertView;
    }

    /*Checks whether a particular Objek exists in SharedPreferences*/
    public boolean checkFavoriteItem(ListData checkObjek) {
        boolean check = false;
        List<ListData> favorites = sharedPreference.getFavorites(context);
        if (favorites != null) {
            for (ListData Objek : favorites) {
                if (Objek.equals(checkObjek)) {
                    check = true;
                    break;
                }
            }
        }
        return check;
    }

    @Override
    public void add(ListData Objek) {
        super.add(Objek);
        Objeks.add(Objek);
        notifyDataSetChanged();
    }

    @Override
    public void remove(ListData Objek) {
        super.remove(Objek);
        Objeks.remove(Objek);
        notifyDataSetChanged();
    }
}