package com.seismos.pentesigma.seismos;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;


public class ChartFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private PieChart mChart;
    private PieChart mChart2;
    private LineChart mChart3;
    private HorizontalBarChart mChart4;

    private OnFragmentInteractionListener mListener;


    public static ChartFragment newInstance(String param1, String param2) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //noinspection StatementWithEmptyBody
        if (getArguments() != null) {
//            String mParam1 = getArguments().getString(ARG_PARAM1);
//            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_charts, container, false);
        mChart = (PieChart) v.findViewById(R.id.chart01);
        mChart2 = (PieChart) v.findViewById(R.id.chart02);
        mChart3 = (LineChart) v.findViewById(R.id.chart03);
        mChart4 = (HorizontalBarChart) v.findViewById(R.id.chart005);

        // setup Summary Bar Chart
        try {
            setBarData();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mChart4.setTouchEnabled(false);
        mChart4.animateY(1000, Easing.EasingOption.EaseInOutQuad);

        // setup Pie Charts
        setPieChartParameters(mChart, "Today");
        try {
            setPieData(24);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mChart.animateY(1000, Easing.EasingOption.EaseInOutQuad);

        setPieChartParameters(mChart2, "Yesterday");
        try {
            setPieData(48);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mChart2.animateY(1000, Easing.EasingOption.EaseInOutQuad);

        // setup Line Charts
        try {
            setLineData(24);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return v;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void setPieData(int time) throws ParseException {

        int ic = 0;
        DataSource_Events events = new DataSource_Events(this.getContext());
        ArrayList<Entry> yValues1 = new ArrayList<Entry>();
        ArrayList<String> xValues = new ArrayList<String>();

        for (int i = 0; i < 9; i++) {

            switch (time) {
                case 24:
                    ic = events.countMagnitude((double) i, (double) i + 0.9);
                    break;
                case 48:
                    ic = events.countMagnitude48((double) i, (double) i + 0.9);
                    break;
            }

            if(ic == 0)
                continue;

            yValues1.add(new Entry((float)ic, i));
            xValues.add(String.format("m %s (%s)", String.valueOf(i), ic));
        }

        // setup Colorlist
        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        PieDataSet dataSet = new PieDataSet(yValues1, "Mag");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        dataSet.setColors(colors);
        PieData data = new PieData(xValues, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(8f);
        data.setValueTextColor(Color.BLACK);

        switch (time) {
            case 24:
                mChart.setData(data);
                mChart.highlightValues(null);
                mChart.invalidate();
                break;
            case 48:
                mChart2.setData(data);
                mChart2.highlightValues(null);
                mChart2.invalidate();
                break;
        }
    }

    private void setLineData(int time) throws ParseException {

        DataSource_Events events = new DataSource_Events(getContext());

        // creating list of entry
        ArrayList<Integer> ev24 = events.countTodaysHourly(24);
        ArrayList<Integer> ev48 = events.countTodaysHourly(48);
        ArrayList<Entry> entries24 = new ArrayList<Entry>();
        ArrayList<Entry> entries48 = new ArrayList<Entry>();

        for(int i = 0; i<24; i++) {
            entries24.add(new Entry(ev24.get(i), i+1));
            entries48.add(new Entry(ev48.get(i), i+1));
        }

        LineDataSet dataset24 = new LineDataSet(entries24, "hourly # of events Today");
        LineDataSet dataset48 = new LineDataSet(entries48, "hourly # of events Yesterday");

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(dataset24);
        dataSets.add(dataset48);
        ((LineDataSet) dataSets.get(0)).setColor(Color.RED);
        ((LineDataSet) dataSets.get(0)).setCircleColor(Color.RED);
        ((LineDataSet) dataSets.get(1)).setColor(Color.BLUE);
        ((LineDataSet) dataSets.get(1)).setCircleColor(Color.BLUE);

        // creating labels
        ArrayList<String> labels = new ArrayList<String>();

        // setup Hours Axis
        for(int i=1; i<25;i++) {
            labels.add(String.valueOf(i));
        }

        LineData data = new LineData(labels, dataSets);
        mChart3.setData(data); // set the data and list of lables into chart
        mChart3.invalidate();
        mChart3.setDescription("");
    }


    private void setBarData() throws ParseException {

        DataSource_Events events = new DataSource_Events(getContext());
        ArrayList<BarDataSet> dataSets = null;

        ArrayList<BarEntry> valueSet1 = new ArrayList<BarEntry>();
        BarEntry v1e1 = new BarEntry((float) events.countTodays() + events.countYesterdays(), 0); // Total Overall
        valueSet1.add(v1e1);

        ArrayList<BarEntry> valueSet2 = new ArrayList<BarEntry>();
        BarEntry v2e1 = new BarEntry((float)events.countTodays(), 0); // Today
        valueSet2.add(v2e1);

        ArrayList<BarEntry> valueSet3 = new ArrayList<BarEntry>();
        BarEntry v3e1 = new BarEntry((float)events.countYesterdays(), 0); // Yesterday
        valueSet3.add(v3e1);

        // setup Bar Datas
        BarDataSet barDataSet1 = new BarDataSet(valueSet1, "Total #");
        BarDataSet barDataSet2 = new BarDataSet(valueSet2, "Today #");
        BarDataSet barDataSet3 = new BarDataSet(valueSet3, "Yesterday #");

        // setup Br Colors
        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS) { colors.add(c); }
        colors.add(ColorTemplate.getHoloBlue());

        barDataSet1.setColors(Arrays.asList(new Integer[]{colors.get(1)}));
        barDataSet2.setColors(Arrays.asList(new Integer[]{colors.get(2)}));
        barDataSet3.setColors(Arrays.asList(new Integer[]{colors.get(3)}));

        // setup main Chart from fragments
        dataSets = new ArrayList<>();
        dataSets.add(barDataSet1);
        dataSets.add(barDataSet3);
        dataSets.add(barDataSet2);

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("");

        BarData data = new BarData(labels, dataSets);
        mChart4.setData(data); // set the data and list of lables into chart
        mChart4.invalidate();
        mChart4.setDescription("");
    }

    private void setPieChartParameters(PieChart chart, String title) {

        chart.setUsePercentValues(true);
        chart.setDescription("");
        chart.setExtraOffsets(5, 10, 5, 5);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setCenterText(title);
        chart.setCenterTextSize(18);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColorTransparent(true);
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);
        chart.setHoleRadius(58f);
        chart.setTransparentCircleRadius(61f);
        chart.setDrawCenterText(true);
        chart.setRotationAngle(10);
        chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(true);
    }
}
