package com.wefly.wealert.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wefly.wealert.R;
import com.wefly.wealert.activities.AlertSentDetailsActivity;
import com.wefly.wealert.dbstore.AlertData_;
import com.wefly.wealert.dbstore.Piece;
import com.wefly.wealert.dbstore.Recipient;
import com.wefly.wealert.services.models.AlertData;
import com.wefly.wealert.utils.AppController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import io.objectbox.Box;
import io.objectbox.query.Query;

public class AlertListAdapter extends BaseAdapter {
    private Context context;
    private List<AlertData> dataList;
    private LayoutInflater inflater;
    Box<com.wefly.wealert.dbstore.AlertData> Alertbox = AppController.boxStore.boxFor(com.wefly.wealert.dbstore.AlertData.class);

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
        CircleImageView image = view.findViewById(R.id.piece);
        TextView title = view.findViewById(R.id.alert_title);
        TextView content = view.findViewById(R.id.alert_content);
        TextView date = view.findViewById(R.id.date);

        AlertData item = dataList.get(i);
        image.setImageResource(R.drawable.add_pic);
        title.setText(item.getTitre());
        if (item.getContenu().length() > 35) {
            content.setText(item.getContenu().substring(0, 35) + "...");
        } else {
            content.setText(item.getContenu());
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        try {
            Date dt = format.parse(item.getDate_de_creation());
            date.setText(dt.toString());
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        view.setTag(item.getId());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long id = Long.parseLong(view.getTag().toString());

                Query query = Alertbox.query()
                        .equal(AlertData_.raw_id, id).build();

                List<com.wefly.wealert.dbstore.AlertData> alerts = query.find();

                for (com.wefly.wealert.dbstore.AlertData n : alerts) {
                    Toast.makeText(context, "CLicked ID:" + id + " Alert ID:" + n.getRaw_id(), Toast.LENGTH_LONG).show();
                    Intent detail = new Intent(context, AlertSentDetailsActivity.class);
                    detail.putExtra("alert_id", n.id);
                    context.startActivity(detail);
//                   for (Piece x : n.pieces) {
//                       Toast.makeText(context, "Piece url:" + x.getUrl(), Toast.LENGTH_LONG).show();
//                   }
//
//                   for(Recipient r:n.destinataires){
//                       Toast.makeText(context, "Alert Recipient:" + r.getUsername(), Toast.LENGTH_LONG).show();
//                   }

                }
            }
        });

        return view;
    }
}
