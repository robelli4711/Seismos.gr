package com.seismos.pentesigma.seismos;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Recycler_View_Adapter extends RecyclerView.Adapter<Recycler_View_Adapter.ViewHolder> {

    private Context context;
    private int refreshtype;
    private List<Data_Events> data_events = new ArrayList<Data_Events>();
    private OnItemClickListener mItemClickListener;

    public Recycler_View_Adapter(FragmentActivity mActivity) {
    }

    public Recycler_View_Adapter(Context context, int refreshtype, List<Data_Events> data, Application application) {
        data_events = data;
        this.context = context;
        this.refreshtype = refreshtype;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        final View sView = mInflater.inflate(R.layout.row_layout, parent, false);
        return new ViewHolder(sView);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

//        holder.rowcontainer.setBackgroundColor(setEventColor(data_events.get(position).getMagnitude()));
        holder.title.setText(makeTitle(data_events.get(position).getTitle()));
        holder.subtitle.setText(makeSubTitle(data_events.get(position).getTitle()));
        holder.depth.setText(String.format("Depth: %s km", data_events.get(position).getDepth()));
        holder.coordinates.setText(String.format("Lat: %s Lon: %s", data_events.get(position).getLatitude(), data_events.get(position).getLongitude()));
        holder.magnitude.setText(String.valueOf(data_events.get(position).getMagnitude()));
        holder.magnitude.setTextColor(setEventColor(data_events.get(position).getMagnitude()));

        holder.distance.setText(String.format("Distance: %.1f km", getDistance(data_events.get(position).getLatitude(), data_events.get(position).getLongitude())));
    }

    @Override
    public int getItemCount() {
        return data_events.size();
    }


    public void SetOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }


    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, R.anim.anticipate_overshoot_interpolator);
        viewHolder.itemView.setAnimation(animAnticipateOvershoot);
    }

    private String makeTitle(String title) {

        int start = title.indexOf(",");
        int end = title.lastIndexOf(",");
        String str = title.substring(start + 2, end);

        return str;
    }

    private String makeSubTitle(String title) {

        int i = title.lastIndexOf(",");
        String str = title.substring(i + 2, title.length());

        return str;
    }


    private int setEventColor(Double mag) {

        if(mag < 2)
           return Color.GREEN;

        if(mag >=2 && mag < 3)
           return Color.YELLOW;

        if(mag >=3 && mag < 4)
           return Color.MAGENTA;

        if(mag >=4)
           return Color.RED;

        return Color.LTGRAY;
    }

    private double getDistance(double lat, double lon) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        double myLat = Double.parseDouble(prefs.getString("myLocation_Lat", "0.0"));
        double myLon = Double.parseDouble(prefs.getString("myLocation_Lon", "0.0"));

        float[] results = new float[1];
        Location.distanceBetween(lat, lon, myLat, myLon, results);

        return (double)results[0] / 1000;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title, subtitle;
        TextView depth, coordinates, magnitude, distance;
        CardView cardview;
        View rowcontainer;

        public ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            subtitle = (TextView) view.findViewById(R.id.subtitle);
            depth = (TextView) view.findViewById(R.id.Depth);
            coordinates = (TextView) view.findViewById(R.id.coordinates);
            magnitude = (TextView)view.findViewById(R.id.magnitude_left_big_1);
            distance = (TextView)view.findViewById(R.id.distance);

            title.setOnClickListener(this);
            subtitle.setOnClickListener(this);
            depth.setOnClickListener(this);
            coordinates.setOnClickListener(this);
            magnitude.setOnClickListener(this);
            distance.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            setupMap(v, getAdapterPosition(), v.getId());
        }

        private void setupMap(View view, int position, long id) {

            String strDescription;

            // get Preferences and set ShowMap to false
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("showmap", false);
            editor.commit();

            DataSource_Events events = new DataSource_Events(context);
            strDescription = events.getEventDescription(position, refreshtype);
            String strTitle = events.getEventTitle(position, refreshtype);

            // Show Map in a own Activity
            Intent intent = new Intent(context, MapContainerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            context.startActivity(intent);

            // explicit create lat, lon
            double lat = events.makeLat(strDescription);
            double lon = events.makeLon(strDescription);
            double mag = events.makeMag(strTitle);
            double dep = events.makeDepth(strDescription);

            // write to SharedPreferences
            editor.putFloat("latitude", (float) lat);
            editor.putFloat("longitude", (float) lon);
            editor.putFloat("magnitude", (float) mag);
            editor.putFloat("depth", (float) dep);
            editor.putString("title", strTitle);
            editor.commit();
        }
    }
}
