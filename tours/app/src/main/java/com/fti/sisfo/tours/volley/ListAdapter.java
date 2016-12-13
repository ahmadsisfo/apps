package com.fti.sisfo.tours.volley;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import com.fti.sisfo.tours.R;

public class ListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<ListData> listItems;
    ImageLoader imageLoader;

    public ListAdapter(Activity activity, List<ListData> listItems) {
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
            convertView = inflater.inflate(R.layout.fragment_item, null);

        if (imageLoader == null)
            imageLoader = MySingleton.getInstance(convertView.getContext()).getImageLoader();

        NetworkImageView thumbNail = (NetworkImageView) convertView.findViewById(R.id.ListthumbImage);
        TextView judul = (TextView) convertView.findViewById(R.id.Listtitle);
        TextView timestamp = (TextView) convertView.findViewById(R.id.Listphone);
        TextView kategori = (TextView) convertView.findViewById(R.id.kategori);
        //TextView isi = (TextView) convertView.findViewById(R.id.news_isi);

        ListData news = listItems.get(position);

        thumbNail.setImageUrl(news.getThumb(), imageLoader);
        judul.setText(news.getName());
        timestamp.setText(news.getPhone());
        kategori.setText(news.getKategori());
        //isi.setText(Html.fromHtml(news.getAddress()));

        return convertView;
    }

}


