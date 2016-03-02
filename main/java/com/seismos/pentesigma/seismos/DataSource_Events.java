package com.seismos.pentesigma.seismos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DataSource_Events {

    Context context;

    // Database fields
    private SQLiteDatabase database;
    private SQLhelper dbHelper;
    private String[] allColumns = {
            SQLhelper.COLUMN_ID,
            SQLhelper.COLUMN_TITLE,
            SQLhelper.COLUMN_DESCRIPTION,
            SQLhelper.COLUMN_LATITUDE,
            SQLhelper.COLUMN_LONGITUDE,
            SQLhelper.COLUMN_MAGNITUDE,
            SQLhelper.COLUMN_DEPTH,
            SQLhelper.COLUMN_DATE,
            SQLhelper.COLUMN_TIME};

    public DataSource_Events(Context context) {
        this.context = context;
        dbHelper = new SQLhelper(context);
    }

    public void open() throws SQLException {
        dbHelper = new SQLhelper(context);

        database = dbHelper.getWritableDatabase();

        if (database == null) {
            dbHelper.onCreate(database);
            database = dbHelper.getWritableDatabase();
        }
    }

    public void close() {

        dbHelper = new SQLhelper(context);
        dbHelper.close();
    }

    public Data_Events createEvent(String event, String description) {

        ContentValues values = new ContentValues();
        values.put(SQLhelper.COLUMN_TITLE, event);
        values.put(SQLhelper.COLUMN_DESCRIPTION, description);
        values.put(SQLhelper.COLUMN_DATE, makeDate(description));
        values.put(SQLhelper.COLUMN_TIME, makeTime(description));
        values.put(SQLhelper.COLUMN_MAGNITUDE, makeMag(event));
        values.put(SQLhelper.COLUMN_LATITUDE, makeLat(description));
        values.put(SQLhelper.COLUMN_LONGITUDE, makeLon(description));
        values.put(SQLhelper.COLUMN_DEPTH, makeDepth(description));

        long insertId = database.insert(SQLhelper.TABLE_EVENTS, null, values);
        Cursor cursor = database.query(SQLhelper.TABLE_EVENTS,
                allColumns, SQLhelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToLast();

        Data_Events newEvent = cursorToEvent(cursor);
        cursor.close();
        return newEvent;
    }


    public void deleteAllEvents() throws SQLiteException {
        if (database == null) {
            open();
        }
        database.execSQL("delete from events");
    }


    public String getEventDescription(int position, int isDesc) {

        int i = 0;

        if (database == null) {
            open();
        }

        Cursor cursor;
        switch (isDesc) {
            case 1:
                cursor = database.query(SQLhelper.TABLE_EVENTS, allColumns, null, null, null, null, SQLhelper.COLUMN_TITLE + " DESC");
                break;
            case 2:
                cursor = database.query(SQLhelper.TABLE_EVENTS, allColumns, null, null, null, null, null);
                break;
            default:
                cursor = database.query(SQLhelper.TABLE_EVENTS, allColumns, null, null, null, null, null);
        }

        cursor.moveToFirst();
        while (i < position && !cursor.isAfterLast()) {
            cursor.moveToNext();
            i++;
        }

        Data_Events event = cursorToEvent(cursor);

        // make sure to close the cursor
        cursor.close();
        return event.getDescription();
    }

    public String getEventTitle(int position, int isDesc) {

        int i = 0;

        if (database == null) {
            open();
        }

        Cursor cursor;
        switch (isDesc) {
            case 1:
                cursor = database.query(SQLhelper.TABLE_EVENTS, allColumns, null, null, null, null, SQLhelper.COLUMN_TITLE + " DESC");
                break;
            case 2:
                cursor = database.query(SQLhelper.TABLE_EVENTS, allColumns, null, null, null, null, null);
                break;
            default:
                cursor = database.query(SQLhelper.TABLE_EVENTS, allColumns, null, null, null, null, null);
        }

        cursor.moveToFirst();
        while (i < position && !cursor.isAfterLast()) {
            cursor.moveToNext();
            i++;
        }

        Data_Events event = cursorToEvent(cursor);

        // make sure to close the cursor
        cursor.close();
        return event.getTitle();
    }


    public List<Data_Events> getAllEvents() {
        List<Data_Events> events = new ArrayList<Data_Events>();

        if (database == null) {
            open();
        }

        Cursor cursor = database.query(SQLhelper.TABLE_EVENTS, allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Data_Events event = cursorToEvent(cursor);
            events.add(event);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return events;
    }

    public List<Data_Events> getAllTodaysEvents()  {
        List<Data_Events> events = new ArrayList<Data_Events>();

        if (database == null) {
            open();
        }

        Cursor cursor = database.rawQuery("SELECT * FROM events WHERE Date = ?",
                new String[]{String.valueOf(makeTodayDate())});

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Data_Events event = cursorToEvent(cursor);
            events.add(event);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return events;
    }

    public List<Data_Events> getEventsRange(double mag_from, double mag_to)  {

        List<Data_Events> events = new ArrayList<Data_Events>();
        if (database == null) {
            open();
        }

        Cursor cursor = database.rawQuery("SELECT * FROM events WHERE Date = ? AND Magnitude >= ? AND Magnitude <= ?",
                new String[]{String.valueOf(makeTodayDate()), String.valueOf(mag_from), String.valueOf(mag_to)});

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Data_Events event = cursorToEvent(cursor);
            cursor.moveToNext();

            events.add(event);
        }
        // make sure to close the cursor
        cursor.close();
        return events;
    }

    public List<Data_Events> getEventsRange48(double mag_from, double mag_to) {

        List<Data_Events> events = new ArrayList<Data_Events>();

        if (database == null) {
            open();
        }

        Cursor cursor = database.rawQuery("SELECT * FROM events WHERE Magnitude >= ? AND Magnitude <= ?",
                new String[]{String.valueOf(mag_from), String.valueOf(mag_to)});

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Data_Events event = cursorToEvent(cursor);
            cursor.moveToNext();

            events.add(event);
        }

        // make sure to close the cursor
        cursor.close();
        return events;
    }

    public List<Data_Events> getAllEventsSortMagD() {
        List<Data_Events> events = new ArrayList<Data_Events>();

        if (database == null) {
            open();
        }

        Cursor cursor = database.query(SQLhelper.TABLE_EVENTS, allColumns, null, null, null, null, SQLhelper.COLUMN_TITLE + " DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Data_Events event = cursorToEvent(cursor);
            events.add(event);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return events;
    }

    private Data_Events cursorToEvent(Cursor cursor) {

        Data_Events event = new Data_Events(makeLocalEventTitle(cursor.getString(1), cursor.getString(2)),
                cursor.getString(2));

        event.setTitle(makeLocalEventTitle(cursor.getString(1), cursor.getString(2)));
        event.setDescription(cursor.getString(2));
        event.setLatitude(makeLat(cursor.getString(2)));
        event.setLongitude(makeLon(cursor.getString(2)));
        event.setMagnitude(makeMag(cursor.getString(1)));
        event.setDepth(makeDepth(cursor.getString(2)));
        event.setDate(makeDate(cursor.getString(2)));
        event.setTime(makeTime(cursor.getString(2)));

        return event;
    }

    public List<Data_Events> getActualEvent() {
        if (database == null) {
            open();
        }

        List<Data_Events> events = new ArrayList<Data_Events>();

        Cursor cursor = database.query(SQLhelper.TABLE_EVENTS, allColumns, null, null, null, null, "1");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Data_Events event = cursorToEvent(cursor);
            events.add(event);
            cursor.moveToNext();
        }

        return events;
    }

    public int count() {
        if (database == null) {
            open();
        }

        Cursor cursor = database.rawQuery("SELECT * FROM events", null);
        cursor.moveToLast();
        int i = cursor.getCount();
        cursor.close();
        return i;
    }

    public int countTodays()  {

        if (database == null) {
            open();
        }

        Cursor cursor = database.rawQuery("SELECT * FROM events WHERE Date = ?",
                new String[]{String.valueOf(makeTodayDate())});

        cursor.moveToLast();
        int i = cursor.getCount();
        cursor.close();

        return i;
    }

    public int countYesterdays()  {

        if (database == null) {
            open();
        }

        Cursor cursor = database.rawQuery("SELECT * FROM events WHERE Date < ? AND Date > ?",
            new String[]{String.valueOf(makeTodayDate()), makeTodayMinusDays(2)});

        cursor.moveToLast();
        int i = cursor.getCount();
        cursor.close();

        return i;
    }

    public ArrayList countTodaysHourly(int time)  {

        if (database == null) {
            open();
        }

        ArrayList<Integer> counter = new ArrayList<Integer>();
        String strCount;

        // init the counter array
        for (int i = 1; i < 25; i++) {
            counter.add(0);
        }

        Cursor cursor = null;
        switch(time) {
            case 24:
                cursor = database.rawQuery("SELECT * FROM events WHERE Date = ?",
                        new String[]{String.valueOf(makeTodayDate())});
                break;
            case 48:
                    cursor = database.rawQuery("SELECT * FROM events WHERE Date < ? AND Date > ?",
                        new String[]{String.valueOf(makeTodayDate()), makeTodayMinusDays(2)});
                break;
        }

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            Data_Events event = cursorToEvent(cursor);
            int i = Integer.parseInt(event.getTime().substring(0,2));
            int c = counter.get(i);
            c++;
            counter.set(i, c);
            cursor.moveToNext();
        }

        cursor.close();
        return counter;
    }

    public int countMagnitude(double mag_from, double mag_to)  {

        if (database == null) {
            open();
        }

        Cursor cursor = database.rawQuery("SELECT * FROM events WHERE Date = ? AND Magnitude >= ? AND Magnitude <= ?",
                new String[]{String.valueOf(makeTodayDate()), String.valueOf(mag_from), String.valueOf(mag_to)});

        cursor.moveToLast();
        int i = cursor.getCount();
        cursor.close();

        return i;
    }

    public int countMagnitude48(double mag_from, double mag_to)  {

        if (database == null) {
            open();
        }

        Cursor cursor = database.rawQuery("SELECT * FROM events WHERE Magnitude >= ? AND Magnitude <= ? AND Date < ? AND Date > ?",
                new String[]{String.valueOf(mag_from), String.valueOf(mag_to), String.valueOf(makeTodayDate()), makeTodayMinusDays(2)});

        cursor.moveToLast();
        int i = cursor.getCount();
        cursor.close();

        return i;
    }

    public double makeLat(String description) throws StringIndexOutOfBoundsException {

        String str = "0";
        try {
            str = description.substring(description.indexOf("Latitude:") + 10, description.length());
            int end = str.indexOf("N");
            str = str.substring(0, end);
            str.replace("N", " ");
        } catch (StringIndexOutOfBoundsException e) {
            Log.d("Seismos", e.getMessage());
        }

        double ret;
        try {
            ret = Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0;
        }
        return ret;
    }

    public double makeLon(String description) throws StringIndexOutOfBoundsException {

        String str = "0";
        try {
            str = description.substring(description.indexOf("Longitude:") + 10, description.length());
            int end = str.indexOf("E");
            str = str.substring(0, end);
            str.replace("E", " ");
        } catch (StringIndexOutOfBoundsException e) {
            Log.d("Seismos", e.getMessage());
        }

        double ret;
        try {
            ret = Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0;
        }
        return ret;
    }

    public double makeMag(String title) throws StringIndexOutOfBoundsException {

        String str = title.substring(2, title.indexOf(","));
        return Double.parseDouble(str);
    }

    public double makeDepth(String description) throws StringIndexOutOfBoundsException {

        String str = description.substring(description.indexOf("Depth:") + 7, description.length());
        str = str.substring(0, str.indexOf("km"));
        return Double.parseDouble(str);
    }

    public String makeDate(String description) {

        String str = description.substring(description.indexOf("Time:") + 6, description.length());
        str = str.substring(0, str.indexOf(" "));

        return str;
    }

    public String makeTime(String description) {
//        23.3 km NNW of Aegion<br> Time: 20-Feb-2016 08:50:09 (UTC) <br> Latitude: 38.45N <br> Longitude: 21.99E <br> Depth: 5km <br> M 0.5

        String str = description.substring(description.indexOf("UTC") - 10, description.length());
        str = str.substring(0, str.indexOf(" "));

        return str;
    }

    public String makeHour(String time) {

        return time.substring(0, time.indexOf(":"));
    }

    private String makeLocalTime(String description) {

        SimpleDateFormat simpledateformat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.US);
        Date stringDate = null;

        String datefromstring = description.substring(description.indexOf("Time:") + 6, description.indexOf("UTC") - 2);
        try {
            stringDate = simpledateformat.parse(datefromstring);
        } catch (ParseException e) { e.printStackTrace(); }

        Calendar cal = Calendar.getInstance();
        cal.setTime(stringDate);
        cal.add(Calendar.HOUR_OF_DAY, 2);

        return simpledateformat.format(cal.getTime());
    }

    public String makeLocalEventTitle(String title, String description) {

        String start = title.substring(0, title.indexOf(",") + 1);
        String end = title.substring(title.indexOf(" , "), title.length());

        return start + " " + makeLocalTime(description) + end;
    }

    private String makeTodayDate()  {

        Locale.setDefault(Locale.US);

        SimpleDateFormat sourceFormat = new SimpleDateFormat("dd-MMM-yyyy");
        sourceFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date parsed = null; // => Date is in UTC now
        try { parsed = sourceFormat.parse(sourceFormat.format(new Date()));
        } catch (ParseException e) { e.printStackTrace(); }
        return sourceFormat.format(parsed);
    }

    private String makeTodayMinusDays(int minusDays) {

        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);

        Calendar cal = Calendar.getInstance();
        cal = Calendar.getInstance();
        cal.add(Calendar.DATE, - minusDays);
        String d1 = dateformat.format(cal.getTime());

        return d1.replace(".", "");
    }
}
