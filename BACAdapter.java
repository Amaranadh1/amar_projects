package com.deepwares.checkpointdwi.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.deepwares.checkpointdwi.R;
import com.deepwares.checkpointdwi.entities.BACRecord;
import com.deepwares.checkpointdwi.network.ConnectionDetector;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by deesunda on 10/4/2014.
 */
public class BACAdapter extends RecyclerView.Adapter<BACAdapter.MyViewHolder> {

    private List<BACRecord> notificationList;
    private Context ctx;
    BACRecord notification;

    //Check Internet
    Boolean isInternetPresent = false;
    ConnectionDetector connectionDetector;

    public BACAdapter(Context ctx, List<BACRecord> nList) {
        this.notificationList = nList;
        this.ctx = ctx;
        connectionDetector = new ConnectionDetector(ctx);
    }

    public BACAdapter(List<BACRecord> nList) {
        this.notificationList = nList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bac_record, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final BACAdapter.MyViewHolder holder, int position) {

        notification = notificationList.get(position);
        isInternetPresent = connectionDetector.isConnectingToInternet();
        holder.date.setText(notification.getDate());
        holder.time.setText(notification.getTime());

        if (notification.getStatus().equalsIgnoreCase("PASS")) {
            holder.bacLabel.setImageResource(R.drawable.ic_success);
        } else {
            holder.bacLabel.setImageResource(R.drawable.ic_fail);
        }

        DecimalFormat precision = new DecimalFormat("0.000");
        holder.bac.setText(precision.format(notification.getBac()));

        Log.e(" notification_value",notification.getStatus()+"----"+notification.getBac() );
        if (notification.getStatus() == "FAIL") {
            if (notification.getBac() == 0.000) {
                holder.bacLabel.setImageResource(R.drawable.ic_fail);
                holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.connected_rect));
            } else if (notification.getBac() >= 0.01 && notification.getBac() <= 0.04) {
                holder.bacLabel.setImageResource(R.drawable.ic_fail);
                holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.gray_rect));
            } else if (notification.getBac() >= 0.05 && notification.getBac() <= 0.07) {
                holder.bacLabel.setImageResource(R.drawable.ic_fail);
                holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.gray_rect));
            } else if (notification.getBac() >= 0.08) {
                holder.bacLabel.setImageResource(R.drawable.ic_fail);
                holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.red_rect));
            }

        } else {

            if (notification.getBac() == 0.000) {
                holder.bacLabel.setImageResource(R.drawable.ic_success);
                holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.connected_status));
            } else if (notification.getBac() >= 0.01 && notification.getBac() <= 0.04) {
                holder.bacLabel.setImageResource(R.drawable.ic_success);
                holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.gray_background));
            } else if (notification.getBac() >= 0.05 && notification.getBac() <= 0.07) {
                holder.bacLabel.setImageResource(R.drawable.ic_success);
                holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.gray_background));
            } else if (notification.getBac() >= 0.08) {
                holder.bacLabel.setImageResource(R.drawable.ic_success);
                holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.red_background));
            }
        }



       /* DecimalFormat precision = new DecimalFormat("0.000");
        holder.bac.setText(precision.format(notification.getBac()));

        if (notification.getBac() == 0.0) {
            holder.bacLabel.setTextColor(ctx.getResources().getColor(R.color.app_default));
            holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.connected_status));
        } else if (notification.getBac() >= 0.01 && notification.getBac() <= 0.04) {
            holder.bacLabel.setTextColor(ctx.getResources().getColor(R.color.app_default));
            holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.gray_background));
        } else if (notification.getBac() >= 0.05 && notification.getBac() <= 0.07) {
            holder.bacLabel.setTextColor(ctx.getResources().getColor(R.color.app_default));
            holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.gray_background));
        } else if (notification.getBac() >= 0.08) {
            holder.bacLabel.setTextColor(ctx.getResources().getColor(R.color.app_default));
            holder.bac.setBackground(ctx.getResources().getDrawable(R.drawable.red_background));
        }*/
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView date, time, bac;
        private ImageView bacLabel;
        private LinearLayout listLayout, progressBarLayout;
        private ProgressBar progressBar;

        public MyViewHolder(View view) {
            super(view);

            date = (TextView) view.findViewById(R.id.date);
            time = (TextView) view.findViewById(R.id.time);
            bac = (TextView) view.findViewById(R.id.bac);
            bacLabel = (ImageView) view.findViewById(R.id.bacLabel);
            listLayout = (LinearLayout) view.findViewById(R.id.list_layout);
            progressBarLayout = (LinearLayout) view.findViewById(R.id.progressBar_layout);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        }
    }
}


