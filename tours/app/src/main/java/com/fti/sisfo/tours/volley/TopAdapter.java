package com.fti.sisfo.tours.volley;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import com.fti.sisfo.tours.R;
import com.squareup.picasso.Picasso;

public class TopAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<ListData> listItems;
    ImageLoader imageLoader;

    public TopAdapter(Activity activity, List<ListData> listItems) {
        this.activity = activity;
        this.listItems = listItems;
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int location) {
        return listItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
            convertView = inflater.inflate(R.layout.layout_top, null);

        if (imageLoader == null)
            imageLoader = MySingleton.getInstance(convertView.getContext()).getImageLoader();

        TextView texttop = (TextView) convertView.findViewById(R.id.top_views);
        TextView textname = (TextView) convertView.findViewById(R.id.topviews_title);
        TextView textinfo = (TextView) convertView.findViewById(R.id.topviews_info);
        NetworkImageView thumbNail = (NetworkImageView) convertView.findViewById(R.id.ListthumbImage);

        ListData news = listItems.get(position);
        texttop.setText(news.getTop());
        textname.setText(news.getName()+" ("+news.getKategori()+")");
        textinfo.setText(news.getAddress()+ " - " +news.getPhone());
        /*if (post.getString("gambar") != null) {
            Picasso.with(this).load(Uri.parse(SERVER_IMG+post.getString("gambar"))).placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .into(textimage);
        }*/




        thumbNail.setImageUrl(news.getThumb(), imageLoader);


        return convertView;
    }

}

