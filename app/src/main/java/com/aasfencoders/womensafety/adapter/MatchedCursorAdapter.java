package com.aasfencoders.womensafety.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aasfencoders.womensafety.R;
import com.aasfencoders.womensafety.data.DataContract;

import java.util.Random;

// Adapter to populate values in the MatchedCursorAdapter in [matchedConnection.java]
public class MatchedCursorAdapter extends CursorAdapter {

    public MatchedCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    // inflate the layout with the desired XML
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.single_matched_contact_item, viewGroup, false);
    }

    //  bind views(children) to it
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // declare the view object
        TextView matched_name = (TextView) view.findViewById(R.id.person_matched_name);
        TextView matched_phone = (TextView) view.findViewById(R.id.person_matched_number);

        // declare each column Index of that data cursor
        int idColIndex = cursor.getColumnIndex(DataContract.DataEntry._ID);
        int nameColIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_NAME);
        int numberColIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_PHONE);

        // Get the Values present in those column
        int id = cursor.getInt(idColIndex);
        String name = cursor.getString(nameColIndex);
        String number = cursor.getString(numberColIndex);

        // Set those values
        ImageView image = view.findViewById(R.id.matched_view_image);
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(230), rnd.nextInt(230), rnd.nextInt(230));
        image.setBackgroundColor(color);
        matched_name.setText(name);
        matched_phone.setText(number);
    }
}
