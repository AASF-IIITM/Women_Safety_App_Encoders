package com.aasfencoders.womensafety.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aasfencoders.womensafety.Class.InviteSentClass;
import com.aasfencoders.womensafety.Class.ReceiveClass;
import com.aasfencoders.womensafety.R;

import java.util.ArrayList;

public class ReceiveAdapter extends ArrayAdapter<ReceiveClass> {

    private Context mContext;


    public ReceiveAdapter(@NonNull Context context, ArrayList<ReceiveClass> inviteList) {
        super(context, 0, inviteList);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(mContext).inflate(R.layout.single_receive_contact_item, parent, false);

        }

        ReceiveClass currentCall = getItem(position);

        TextView name = listItemView.findViewById(R.id.person_receive_name);
        TextView number = listItemView.findViewById(R.id.person_receive_number);

        name.setText(currentCall.getName());
        number.setText(currentCall.getNumber());

        return listItemView;
    }
}
