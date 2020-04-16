package com.rasmita.myplayer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class ItemListViewAdapter extends BaseAdapter {
    Context context;
    ArrayList<RowItem> items;
    Holder holder;

    public ItemListViewAdapter(MainActivity mainActivity, ArrayList<RowItem> rowItems) {
        this.context = mainActivity;
        this.items = rowItems;

    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    public class Holder {
        TextView title,desc;
        ImageView img_video;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final RowItem _pos = items.get(position);
        holder = new Holder();
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.videolist, parent, false);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.desc = (TextView) convertView.findViewById(R.id.description);
            holder.img_video = (ImageView) convertView.findViewById(R.id.imageView1);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.title.setTag(position);
        holder.img_video.setTag(position);
        holder.desc.setTag(position);
        holder.title.setText(_pos.getTitle());
        holder.desc.setText(_pos.getDesc());
        holder.img_video.setBackgroundResource(_pos.getImageId());
        return convertView;



    }
}
