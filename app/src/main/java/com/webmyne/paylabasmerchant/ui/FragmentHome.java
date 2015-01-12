package com.webmyne.paylabasmerchant.ui;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.webmyne.paylabasmerchant.R;
import com.webmyne.paylabasmerchant.model.AffilateServices;
import com.webmyne.paylabasmerchant.util.PrefUtils;

import java.util.ArrayList;


public class FragmentHome extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Spinner spPaymentType;
    private LinearLayout gcLayout;
    private TextView btnNext;
    private String mParam1;
    private String mParam2;
    FrameLayout linearTools;
    private Spinner spServiceType;
    private ArrayAdapter<String> serviceTypeAdapter;
    private ArrayList<AffilateServices> affilateServicesArrayList;
    private ArrayList<String> affilateServiceNames;

    public static FragmentHome newInstance(String param1, String param2) {
        FragmentHome fragment = new FragmentHome();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentHome() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        affilateServicesArrayList=new ArrayList<AffilateServices>();
        affilateServicesArrayList= PrefUtils.getMerchant(getActivity()).affilateServicesArrayList;
        affilateServiceNames=new ArrayList<String>();
        for(AffilateServices affilateServices: affilateServicesArrayList){
            if(affilateServices.IsActive==true){
                affilateServiceNames.add(affilateServices.ServiceName.toString().trim());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View convertview = inflater.inflate(R.layout.fragment_home, container, false);
        spPaymentType=(Spinner)convertview.findViewById(R.id.spPaymentType);
        gcLayout=(LinearLayout)convertview.findViewById(R.id.gcLayout);
        btnNext=(TextView)convertview.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showVerificationAlert();
            }


        });
        spServiceType=(Spinner)convertview.findViewById(R.id.spServiceType);
        serviceTypeAdapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, affilateServiceNames);
        spServiceType.setAdapter(serviceTypeAdapter);
        spPaymentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==1){
                    gcLayout.setVisibility(View.VISIBLE);
                } else {
                    gcLayout.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        return convertview;
    }

    private void showVerificationAlert() {



        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.custom_alert_dialog, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setView(promptsView);

        alert.setPositiveButton("VERIFY", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            dialog.dismiss();

//                FragmentManager manager = getActivity().getSupportFragmentManager();
//                FragmentTransaction ft = manager.beginTransaction();
//                ft.setCustomAnimations(R.anim.entry, R.anim.exit,R.anim.entry, R.anim.exit);
//                ft.replace(R.id.payment_fragment, new FragmentPaymentServiceSelection(), "paymenent_services");
//                ft.addToBackStack("");
//                ft.commit();
            }
        });

        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            dialog.dismiss();
            }
        });

        alert.show();
    }

//end of main class
}
