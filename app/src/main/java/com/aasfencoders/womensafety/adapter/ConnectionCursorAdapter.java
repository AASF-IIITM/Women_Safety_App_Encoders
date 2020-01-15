package com.aasfencoders.womensafety.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.aasfencoders.womensafety.R;
import com.aasfencoders.womensafety.data.DataContract;

public class ConnectionCursorAdapter extends CursorAdapter {

    public ConnectionCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.single_active_connection, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView matched_name = (TextView) view.findViewById(R.id.person_connection_name);
        TextView matched_phone = (TextView) view.findViewById(R.id.person_connection_number);

        int idColIndex = cursor.getColumnIndex(DataContract.DataEntry._ID);
        int nameColIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_NAME);
        int numberColIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_PHONE);

        int id = cursor.getInt(idColIndex);
        String name = cursor.getString(nameColIndex);
        String number = cursor.getString(numberColIndex);

        matched_name.setText(name);
        matched_phone.setText(number);

    }

}
