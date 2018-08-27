package com.wefly.wealert.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wefly.wealert.R;
import com.wefly.wealert.services.models.AlertData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AlertListAdapter extends BaseAdapter {
    private Context context;
    private List<AlertData> dataList;
    private LayoutInflater inflater;

    public AlertListAdapter(Context ctx, List<AlertData> alertDataList) {
        this.context = ctx;
        this.dataList = alertDataList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int i) {
        return dataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.alert_item, parent, false);
        CircleImageView image=view.findViewById(R.id.piece);
        TextView title=view.findViewById(R.id.alert_title);
        TextView content=view.findViewById(R.id.alert_content);
        TextView date=view.findViewById(R.id.date);

        AlertData item=dataList.get(i);
        image.setImageResource(R.drawable.add_pic);
        title.setText(item.getTitre());
        if(item.getContenu().length()>35) {
            content.setText(item.getContenu().substring(0,35)+"...");
        }else{
            content.setText(item.getContenu());
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.getDefault());
        try {
            Date dt = format.parse(item.getDate_de_creation());
            date.setText(dt.toString());
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return view;
    }
}
