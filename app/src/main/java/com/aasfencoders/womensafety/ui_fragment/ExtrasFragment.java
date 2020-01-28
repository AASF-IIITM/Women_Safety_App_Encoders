package com.aasfencoders.womensafety.ui_fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aasfencoders.womensafety.MainActivity;
import com.aasfencoders.womensafety.R;
import com.aasfencoders.womensafety.ShowPolice;

public class ExtrasFragment extends Fragment {

    private RadioGroup radioGroup;
    private SharedPreferences sharedPreferences;
    private Button showPolice;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_extra,container,false);
        radioGroup = view.findViewById(R.id.sim_radio_group);
        if(getContext() != null){
            sharedPreferences = getContext().getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE);
        }

        String checked = sharedPreferences.getString(getString(R.string.SIM), getString(R.string.SIMNO));

        showPolice = view.findViewById(R.id.showPolice);

        showPolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext() , ShowPolice.class);
                startActivity(intent);
            }
        });

        if(checked.equals(getString(R.string.SIM1))){
            radioGroup.check(R.id.sim1);
        }else if(checked.equals(getString(R.string.SIM2))){
            radioGroup.check(R.id.sim2);
        }else{
            radioGroup.check(R.id.sim_No);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.sim1:
                        sharedPreferences.edit().putString(getString(R.string.SIM), getString(R.string.SIM1)).apply();
                        break;
                    case R.id.sim2:
                        sharedPreferences.edit().putString(getString(R.string.SIM), getString(R.string.SIM2)).apply();
                        break;
                    case R.id.sim_No:
                        sharedPreferences.edit().putString(getString(R.string.SIM), getString(R.string.SIMNO)).apply();
                        break;
                }
            }
        });

        return view;
    }
}
