package com.ilocky.example;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ilocky.ILockyPassport;

import java.util.List;

/**
 * Created by richard on 15/8/17.
 */
public class ILockyKeyAdapter extends ArrayAdapter<ILockyPassport> {
    private Context context;
    List<ILockyPassport> passports;
    public ILockyKeyAdapter(Context context, List<ILockyPassport> objects) {
        super(context, R.layout.element_key, objects);
        this.context=context;
        passports=objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            // inflate the layout
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.element_key, parent, false);

        }
        ILockyPassport passport=passports.get(position);
        String iLockyName = passport.getIlockyId();
        ((TextView)convertView.findViewById(R.id.tvKeyName)).setText(passport.getPassportName());
        ((TextView)convertView.findViewById(R.id.tviLockyName)).setText(iLockyName);
        if(passport.isCurrent()) {
            convertView.findViewById(R.id.flStatus).setBackgroundColor(Color.GREEN);
        } else if(passport.isFuture()) {
            convertView.findViewById(R.id.flStatus).setBackgroundColor(Color.YELLOW);
        } else if(passport.isPast()) {
            convertView.findViewById(R.id.flStatus).setBackgroundColor(Color.GRAY);
        }
        if(!passport.isValid()) {
            convertView.findViewById(R.id.flStatus).setBackgroundColor(Color.RED);
        }
        if(passport.getActionType()== ILockyPassport.ACTION_TYPE_INITIALIZING)
            convertView.findViewById(R.id.tvLongOpen).setVisibility(View.VISIBLE);
        else
            convertView.findViewById(R.id.tvLongOpen).setVisibility(View.INVISIBLE);

        int times=passport.getTimes();
        int timesLimit=passport.getTimesLimit();
        if(timesLimit==0) {
            ((TextView)convertView.findViewById(R.id.tvTimesLimit)).setText("∞");
        }
        else
            ((TextView)convertView.findViewById(R.id.tvTimesLimit)).setText(Integer.toString(timesLimit));
        ((TextView)convertView.findViewById(R.id.tvTimes)).setText(Integer.toString(times));
        return convertView;
    }
}
