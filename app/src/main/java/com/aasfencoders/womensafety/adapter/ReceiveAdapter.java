package com.aasfencoders.womensafety.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aasfencoders.womensafety.Class.InviteSentClass;
import com.aasfencoders.womensafety.Class.ReceiveClass;
import com.aasfencoders.womensafety.R;
import com.aasfencoders.womensafety.receivedConnection;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReceiveAdapter extends ArrayAdapter<ReceiveClass> {

    private Context mContext;
    private FirebaseFunctions mFunctions;
    private ReceiveClass currentCall;
    private SharedPreferences sharedPreferences;


    public ReceiveAdapter(@NonNull Context context, ArrayList<ReceiveClass> inviteList) {
        super(context, 0, inviteList);
        mContext = context;
        mFunctions = FirebaseFunctions.getInstance();
        sharedPreferences = mContext.getSharedPreferences(mContext.getString(R.string.package_name), Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(mContext).inflate(R.layout.single_receive_contact_item, parent, false);

        }

        currentCall = getItem(position);

        TextView name = listItemView.findViewById(R.id.person_receive_name);
        TextView number = listItemView.findViewById(R.id.person_receive_number);

        Button accept = listItemView.findViewById(R.id.acceptButton);
        Button reject = listItemView.findViewById(R.id.rejectButton);

        name.setText(currentCall.getName());
        number.setText(currentCall.getNumber());

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callFunction(currentCall.getName() , currentCall.getNumber() , mContext.getString(R.string.accept));
            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callFunction(currentCall.getName() , currentCall.getNumber() , mContext.getString(R.string.reject));
            }
        });

        return listItemView;
    }

    private void callFunction(String source_name , String source_no , String selection){

        String target_no = sharedPreferences.getString(mContext.getString(R.string.userNumber), mContext.getString(R.string.error));
        String target_name = sharedPreferences.getString(mContext.getString(R.string.username), mContext.getString(R.string.error));

        if(source_name.equals(mContext.getString(R.string.error)) || source_no.equals(mContext.getString(R.string.error))){
            Toast.makeText(mContext,mContext.getString(R.string.errormessage),Toast.LENGTH_SHORT).show();
        }else{
            Map<String, Object> data = new HashMap<>();
            data.put("source_no", source_no);
            data.put("target_no", target_no);
            data.put("selection", selection);
            data.put("source_name", source_name);
            data.put("target_name", target_name);

            mFunctions
                    .getHttpsCallable("sent_status")
                    .call(data)
                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                        @Override
                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                            String result = (String) task.getResult().getData();
                            if(result != null) {
                                Toast.makeText(mContext,result,Toast.LENGTH_SHORT).show();
                            }
                            return result;
                        }
                    });
        }

    }
}
