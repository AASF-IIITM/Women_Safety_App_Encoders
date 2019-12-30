package com.aasfencoders.womensafety.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aasfencoders.womensafety.Class.ContactNameClass;
import com.aasfencoders.womensafety.R;

import java.util.ArrayList;

public class ContactAdapter extends ArrayAdapter<ContactNameClass> {

    Context mContext;

    public ContactAdapter(@NonNull Context context, ArrayList<ContactNameClass> contactList) {
        super(context, 0, contactList);
        mContext = context;
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

        return listItemView;
    }
}
