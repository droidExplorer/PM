package com.webmyne.paylabasmerchant.ui;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.webmyne.paylabasmerchant.R;
import com.webmyne.paylabasmerchant.model.AffilateUser;
import com.webmyne.paylabasmerchant.model.AppConstants;
import com.webmyne.paylabasmerchant.model.Country;
import com.webmyne.paylabasmerchant.model.PaymentStep1;
import com.webmyne.paylabasmerchant.ui.widget.CallWebService;
import com.webmyne.paylabasmerchant.ui.widget.CircleDialog;
import com.webmyne.paylabasmerchant.ui.widget.InternationalNumberValidation;
import com.webmyne.paylabasmerchant.ui.widget.SimpleToast;
import com.webmyne.paylabasmerchant.util.LanguageStringUtil;
import com.webmyne.paylabasmerchant.util.PrefUtils;
import com.webmyne.paylabasmerchant.util.RegionUtils;

import org.json.JSONObject;

import java.util.ArrayList;

public class FragmentCashOUT extends Fragment {

    private EditText edMobileNumber,edCashoutAmount,edFormId,edPin;
    private TextView btnNext,LiveRate;
    private Spinner spCountry,spIdentityProof;
    private Cashout cashoutobj;
    private ArrayList<Country> countries;
    private AffilateUser affilateUser;
    ArrayList<String> identityProofTypesList;
    boolean isEnglisSelected;
    CharSequence ch=".";
    public static FragmentCashOUT newInstance(String param1, String param2) {
        FragmentCashOUT fragment = new FragmentCashOUT();
        return fragment;
    }

    public FragmentCashOUT() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_cash_out, container, false);

        initView(convertView);
        //edRechargeMobileNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());



        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isEmptyField(edMobileNumber)){
                   SimpleToast.error(getActivity(), getResources().getString(R.string.err_mobile));
                }else if(InternationalNumberValidation.isPossibleNumber(edMobileNumber.getText().toString().toString(), countries.get(spCountry.getSelectedItemPosition()).ShortCode.toString().trim())==false){
                    SimpleToast.error(getActivity(),getResources().getString(R.string.code_combinegcfragment_ERRORENTERMOBILENO));
                }else if(InternationalNumberValidation.isValidNumber(edMobileNumber.getText().toString().toString(), countries.get(spCountry.getSelectedItemPosition()).ShortCode.toString().trim())==false){
                    SimpleToast.error(getActivity(),getResources().getString(R.string.code_combinegcfragment_ERRORENTERMOBILENO));
                }
                else if(edCashoutAmount.length()<6){
                    SimpleToast.error(getActivity(), getResources().getString(R.string.err_entercashout));
                }
                else if(isEmptyField(edFormId)){
                    SimpleToast.error(getActivity(), getResources().getString(R.string.err_enter_formid));
                }
                else if(isEmptyField(edPin)){
                    SimpleToast.error(getActivity(), getResources().getString(R.string.err_enter_pin));
                }
                else{
                    processPay();
                }
            }
        });
        return convertView;
    }

    private void initView(View convertView){

        edMobileNumber = (EditText)convertView.findViewById(R.id.edMobileNumber);
        edFormId= (EditText)convertView.findViewById(R.id.edFormId);
        edCashoutAmount= (EditText)convertView.findViewById(R.id.edCashoutAmount);
        edPin= (EditText)convertView.findViewById(R.id.edPin);
       // LiveRate= (TextView)convertView.findViewById(R.id.LiveRate);
        btnNext = (TextView)convertView.findViewById(R.id.btnNext);
        spCountry= (Spinner)convertView.findViewById(R.id.spCountry);
        spIdentityProof= (Spinner)convertView.findViewById(R.id.spIdentityProof);

        edCashoutAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//original pattern
//if(!s.toString().matches("^\\ (\\d{1,3}(\\,\\d{3})*|(\\d+))(\\.\\d{2})?$"))
                if(!s.toString().matches("^\\ (\\d{1,3}(\\d{3})*|(\\d+))(\\"+ch+"\\d{2})?$"))
                {
                    //original pattern
                    //String userInput= ""+s.toString().replaceAll("[^\\d]", "");
                    String userInput= ""+s.toString().replaceAll("[^\\d]+", "");

                    StringBuilder cashAmountBuilder = new StringBuilder(userInput);

                    while (cashAmountBuilder.length() > 3 && cashAmountBuilder.charAt(0) == '0') {
                        cashAmountBuilder.deleteCharAt(0);
                    }
                    while (cashAmountBuilder.length() < 3) {
                        cashAmountBuilder.insert(0, '0');
                    }
                    cashAmountBuilder.insert(cashAmountBuilder.length()-2, ch);
                    cashAmountBuilder.insert(0, ' ');

                    edCashoutAmount.setText(cashAmountBuilder.toString());
                    // keeps the cursor always to the right
                    Selection.setSelection(edCashoutAmount.getText(), cashAmountBuilder.toString().length());

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        getLiveCurrencyRate();

        isEnglisSelected= PrefUtils.isEnglishSelected(getActivity());

        if(isEnglisSelected)
            ch=",";
        else
            ch=".";

        setCountryCode();
        identityProofTypesList=new ArrayList<String>();
        identityProofTypesList.add(getResources().getString(R.string.NATIONAID));
        identityProofTypesList.add(getResources().getString(R.string.PASSPORT));
        identityProofTypesList.add(getResources().getString(R.string.DRIVINGLICENSE));
        identityProofTypesList.add(getResources().getString(R.string.SOCIALSECURITYNO));
        spIdentityProof.setAdapter(new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,identityProofTypesList));
    }


private void getLiveCurrencyRate(){
    try{
        JSONObject userObject = new JSONObject();
        AffilateUser user= PrefUtils.getMerchant(getActivity());
        userObject.put("FromCurrency","EUR");

       // Log.e("user local currency",user.LocalCurrency);

        userObject.put("Tocurrency",user.LocalCurrency);
        userObject.put("Culture", LanguageStringUtil.CultureString(getActivity()));

        Log.e("live currency object", userObject.toString());

        final CircleDialog circleDialog = new CircleDialog(getActivity(), 0);
        circleDialog.setCancelable(true);
        circleDialog.show();
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConstants.GET_LIVE_CURRENCY, userObject, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject jobj) {
                circleDialog.dismiss();
                String response = jobj.toString();
                Log.e("cash out  Response", "" + response);
                cashoutobj = new GsonBuilder().create().fromJson(jobj.toString(), Cashout.class);

          //      LiveRate.setText("1 EUR = "+cashoutobj.LiveRate+" "+cashoutobj.Tocurrency);


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                circleDialog.dismiss();
                Log.e("error response live curreency: ", error + "");
                SimpleToast.error(getActivity(), getResources().getString(R.string.er_network));
            }
        });
        req.setRetryPolicy(  new DefaultRetryPolicy(0,0,0));
        MyApplication.getInstance().addToRequestQueue(req);

    }
    catch(Exception e){
        Log.e("error in getlive currency out ",e.toString());
    }

}
private void processPay(){
        try{
            JSONObject userObject = new JSONObject();
            AffilateUser user= PrefUtils.getMerchant(getActivity());

            userObject.put("AffiliateID",user.UserID+"");

            String newvalue= edCashoutAmount.getText().toString().trim();
            newvalue = newvalue.replaceAll("\\,", ".");
            userObject.put("Amount",newvalue);

           // userObject.put("Currency","EUR");
            userObject.put("FormDetail",edFormId.getText().toString());
            userObject.put("FormDetailValue",spIdentityProof.getSelectedItemPosition()+1);
            userObject.put("PIN",edPin.getText().toString());
            userObject.put("UserCountryCode",countries.get(spCountry.getSelectedItemPosition()).CountryCode);
            userObject.put("UserMobileNo",edMobileNumber.getText().toString());
            userObject.put("Culture", LanguageStringUtil.CultureString(getActivity()));
            Log.e("cash out post object",userObject.toString());

            final CircleDialog circleDialog = new CircleDialog(getActivity(), 0);
            circleDialog.setCancelable(true);
            circleDialog.show();
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConstants.SEND_VC_FOR_CASHOUT, userObject, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject jobj) {
                    circleDialog.dismiss();
                    String response = jobj.toString();
                    Log.e("cash out  Response", "" + response);
                    cashoutobj = new GsonBuilder().create().fromJson(jobj.toString(), Cashout.class);

                    try{
                        JSONObject obj = new JSONObject(response);
                        if(obj.getString("ResponseCode").equalsIgnoreCase("1")){
                            SimpleToast.ok(getActivity(), getResources().getString(R.string.PaymentStep1_1));
                            showVerificationAlert();
                        }
                        else {

                                SimpleToast.error(getActivity(), obj.getString("ResponseMsg"));

                        }

                    } catch (Exception e) {
                        Log.e("error response cash out1: ", e.toString() + "");
                    }


                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    circleDialog.dismiss();
                    Log.e("error response cash out2: ", error + "");
                    SimpleToast.error(getActivity(), getResources().getString(R.string.er_network));
                }
            });


            req.setRetryPolicy(  new DefaultRetryPolicy(0,0,0));
            MyApplication.getInstance().addToRequestQueue(req);

        }
        catch(Exception e){
            Log.e("error in cash out ",e.toString());
        }

    }

private void processPay2(final String Vfcode){
        try{
            JSONObject userObject = new JSONObject();
            AffilateUser user= PrefUtils.getMerchant(getActivity());

            userObject.put("AffiliateID",user.UserID+"");

            String newvalue= edCashoutAmount.getText().toString().trim();
            newvalue = newvalue.replaceAll("\\,", ".");
            userObject.put("Amount",newvalue);
            // userObject.put("Currency","EUR");

            userObject.put("FormDetail",edFormId.getText().toString());
            userObject.put("PIN",edPin.getText().toString());
            userObject.put("UserCountryCode",countries.get(spCountry.getSelectedItemPosition()).CountryCode);
            userObject.put("UserMobileNo",edMobileNumber.getText().toString());
            userObject.put("VerificationCode",Vfcode);
            userObject.put("Culture", LanguageStringUtil.CultureString(getActivity()));

            Log.e("cash out2 post object",userObject.toString());

            final CircleDialog circleDialog = new CircleDialog(getActivity(), 0);
            circleDialog.setCancelable(true);
            circleDialog.show();
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConstants.CASH_OUT, userObject, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject jobj) {
                    circleDialog.dismiss();
                    String response = jobj.toString();
                    Log.e("cash out2  Response", "" + response);


                    try{
                        JSONObject obj = new JSONObject(response);
                        if(obj.getString("ResponseCode").equalsIgnoreCase("1")){
                            SimpleToast.ok(getActivity(), getResources().getString(R.string.cash_outdone));

                        }
                        else {
                            SimpleToast.error(getActivity(), obj.getString("ResponseMsg"));

                        }
                        getActivity().finish();

                    } catch (Exception e) {
                        Log.e("error response cash out1: ", e.toString() + "");
                    }


                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    circleDialog.dismiss();
                    Log.e("error response cash out2: ", error + "");
                    SimpleToast.error(getActivity(), getResources().getString(R.string.er_network));
                }
            });


            req.setRetryPolicy(  new DefaultRetryPolicy(0,0,0));
            MyApplication.getInstance().addToRequestQueue(req);

        }
        catch(Exception e){
            Log.e("error in cash out ",e.toString());
        }

    }

private void showVerificationAlert() {

        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.custom_alert_dialog, null);
        final  EditText etVerificationCode=(EditText)promptsView.findViewById(R.id.etVerificationCode);
        etVerificationCode.setText(cashoutobj.VerificationCode+"");

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setView(promptsView);

        alert.setPositiveButton("VERIFY", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(cashoutobj.VerificationCode.equalsIgnoreCase(etVerificationCode.getText().toString().trim())){
                    processPay2(etVerificationCode.getText().toString().trim());

                } else{
                    SimpleToast.error(getActivity(), getResources().getString(R.string.validation_empty_verification_code));

                }


            }
        });

        alert.setNegativeButton(getResources().getString(R.string.code_fragmentcashout_CANCELBUTON), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        alert.show();
    }


    private void setCountryCode() {
        new RegionUtils() {

            @Override
            public void response(ArrayList response) {
                countries=response;


                CountryCodeAdapter countryAdapter = new CountryCodeAdapter(getActivity(),R.layout.spinner_country, countries);
                spCountry.setAdapter(countryAdapter);
            }
        }.fetchCountry(getActivity());
    }

    public class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }
        @Override
        public void onFinish() {
            Log.e("counter","Time's up!");
            FragmentHome.isFromDetailPage=true;
                getActivity().finish();

        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

    }

    public class CountryCodeAdapter extends ArrayAdapter<Country> {
        Context context;
        int layoutResourceId;
        ArrayList<Country> values;
        // int android.R.Layout.
        public CountryCodeAdapter(Context context, int resource, ArrayList<Country> objects) {
            super(context, resource, objects);
            this.context = context;
            this.values=objects;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {

            TextView txt = new TextView(getActivity());
            txt.setPadding(16,16,16,16);
            txt.setGravity(Gravity.CENTER_VERTICAL);
            txt.setText(values.get(position).CountryName+" +"+String.valueOf(values.get(position).CountryCode));

            txt.setText(values.get(position).CountryName+" +"+String.valueOf(values.get(position).CountryCode));
            if(values.get(position).ShortCode == null || values.get(position).ShortCode.equalsIgnoreCase("") ||values.get(position).ShortCode.equalsIgnoreCase("NULL") ){
            }else{
                try {
                  /*  Class res = R.drawable.class;
                    Field field = res.getField(values.get(position).ShortCode.toLowerCase().toString()+".png");
                    int drawableId = field.getInt(null);*/
                    int idd = getResources().getIdentifier("com.webmyne.paylabasmerchant:drawable/" + values.get(position).ShortCode.toString().trim().toLowerCase(), null, null);
                    txt.setCompoundDrawablesWithIntrinsicBounds(idd,0,0,0);
                }
                catch (Exception e) {
                    Log.e("MyTag", "Failure to get drawable id.", e);
                }



            }
            return  txt;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView txt = new TextView(getActivity());
            txt.setGravity(Gravity.CENTER_VERTICAL);
            txt.setPadding(16,16,16,16);
            txt.setText("+"+String.valueOf(values.get(position).CountryCode)+" "+values.get(position).CountryName);


            return  txt;
        }
    }




 public boolean isEmptyField(EditText param1){

        boolean isEmpty = false;
        if(param1.getText() == null || param1.getText().toString().equalsIgnoreCase("")){
            isEmpty = true;
        }
        return isEmpty;
    }



private class Cashout{
    @SerializedName("VerificationCode")
    public String VerificationCode;

    @SerializedName("FromCurrency")
    public String FromCurrency;

    @SerializedName("LiveRate")
    public String LiveRate;
    @SerializedName("Tocurrency")
    public String Tocurrency;


}
    // end of main class
}
