package com.aasfencoders.womensafety.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aasfencoders.womensafety.Class.ContactNameClass;
import com.aasfencoders.womensafety.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ContactAdapter extends ArrayAdapter<ContactNameClass> {

    Context mContext;
    private int contact[];

    public ContactAdapter(@NonNull Context context, ArrayList<ContactNameClass> contactList , int arr[]) {
        super(context, 0, contactList );
        mContext = context;
        contact = arr;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(mContext).inflate(R.layout.single_contactname_item, parent, false);
        }
        final ContactNameClass currentContact = getItem(position);



        TextView name = listItemView.findViewById(R.id.single_contactname_textview);
        name.setText(currentContact.getName());
        TextView image = listItemView.findViewById(R.id.single_contactname_image);
        image.setText(currentContact.getName().substring(0,1).toUpperCase());

        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(230), rnd.nextInt(230), rnd.nextInt(230));
//        GradientDrawable shape = (GradientDrawable)image.getBackground();
//        shape.setColor(color);
        (image.getBackground()).setColorFilter(color, PorterDuff.Mode.SRC_IN);
        color = Color.argb(255,255,255,255);
        image.setTextColor(color);

        if(Arrays.binarySearch(contact,position)>=0){
            image.setVisibility(View.INVISIBLE);
            name.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
            name.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
        else
        {
            image.setVisibility(View.VISIBLE);
            name.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
            name.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        }

        return listItemView;
    }
}
