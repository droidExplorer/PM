package com.webmyne.paylabasmerchant.ui;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.GsonBuilder;
import com.webmyne.paylabasmerchant.R;
import com.webmyne.paylabasmerchant.model.AffilateServices;
import com.webmyne.paylabasmerchant.model.AffilateUser;
import com.webmyne.paylabasmerchant.model.AppConstants;
import com.webmyne.paylabasmerchant.model.Country;
import com.webmyne.paylabasmerchant.model.GCRedeemPerposes;
import com.webmyne.paylabasmerchant.model.LiveCurrency;
import com.webmyne.paylabasmerchant.model.PaymentStep1;
import com.webmyne.paylabasmerchant.model.RedeemGC;
import com.webmyne.paylabasmerchant.model.ServiceCharge;
import com.webmyne.paylabasmerchant.ui.widget.CallWebService;
import com.webmyne.paylabasmerchant.ui.widget.CircleDialog;
import com.webmyne.paylabasmerchant.ui.widget.InternationalNumberValidation;
import com.webmyne.paylabasmerchant.ui.widget.SimpleToast;
import com.webmyne.paylabasmerchant.util.DatabaseWrapper;
import com.webmyne.paylabasmerchant.util.LanguageStringUtil;
import com.webmyne.paylabasmerchant.util.PrefUtils;
import com.webmyne.paylabasmerchant.util.RegionUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.webmyne.paylabasmerchant.util.LogUtils.LOGE;
import static com.webmyne.paylabasmerchant.util.PrefUtils.setMerchant;


public class FragmentHome extends Fragment {

    private DatabaseWrapper db_wrapper;
    private ArrayList<Country> countries;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    //    private Spinner spPaymentType;
    private LinearLayout gcLayout;
    private TextView btnNext, btnReset,txtCurrency,txtOther;
    private String mParam1;
    private String mParam2;
    FrameLayout linearTools;
    //    private Spinner spServiceType;
    private Spinner spCountryCode;
    private ArrayAdapter<String> paymentTypeAdapter;
    private ArrayAdapter<String> serviceTypeAdapter;
    private ArrayList<AffilateServices> affilateServicesArrayList;
    private ArrayList<String> affilateServiceNames;
    private ArrayList<String> paymentTypeList;
    private CircleDialog circleDialog;
    private PaymentStep1 paymentStep1;
    private AffilateUser affilateUser;
    private AffilateUser affilateUserBalance;
    private EditText etMobileNumber, etAmount, etGiftCode;
    private String paymentType;
    private int paymentTypePosition, serviceTypePosition;
    private LinearLayout linearPaymentType;
    //    private String paymentType;
//    private int paymentTypePosition,serviceTypePosition;
    private LinearLayout linerPaymentType;
    public int selectedPaymentType = -1;
    public int selectedServiceType = -1;
    public int selectedOtherType = -1;
    public String selectedOtherPurposeType;
    private ArrayList colors_p;
    private LinearLayout linearServiceType, layoutOthers;
    private LinearLayout layoutGenerateGC,layoutTopup,layoutTransfer;
    private LinearLayout layoutCash,layoutGC,layoutWallet;

    private LinearLayout layoutOthers1, layoutGenerateGC1,layoutTopUp1,linearTransfer1;
    boolean isEnglisSelected;
    CharSequence ch=".";
    public ServiceCharge serviceChargeobj;


    private TextView txtTransfer,txtTopup,txtGenerate;
    private TextView txtWallet,txtGC,txtCash,txtConvRate;

    private LinearLayout linearMobileHome;
    private LiveCurrency livCurencyObj;

    public static boolean isFromDetailPage = false;

    private ArrayList<GCRedeemPerposes> affilateGCReedemPurposeList;

   /* final CharSequence[] items = {
            getResources().getString(R.string.ELECTICITYBILL), getResources().getString(R.string.GASBILL)
    };*/

    private String[] items ;
    private String ServiceID;

    private Boolean isGenerateGCActive,isMobileTopupActive;

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

        colors_p = new ArrayList();
        colors_p.add(getResources().getColor(R.color.color_giftcode_t));
        colors_p.add(getResources().getColor(R.color.color_mobiletopup_t));
        colors_p.add(getResources().getColor(R.color.color_moneytransfer_t));
        colors_p.add(getResources().getColor(R.color.all_track_color));

        filterService();
        paymentTypeList();


    }

    private void paymentTypeList() {

        paymentTypeList = new ArrayList<String>();
        paymentTypeList.add(getResources().getString(R.string.SELECTPAYMENTTYPE));
        paymentTypeList.add(AppConstants.wallet);
        paymentTypeList.add(AppConstants.gc);
        paymentTypeList.add(AppConstants.cash);

    }

    private void filterService() {
        //filter services
        affilateGCReedemPurposeList= new ArrayList<GCRedeemPerposes>();
        affilateServicesArrayList = new ArrayList<AffilateServices>();
        affilateServicesArrayList = PrefUtils.getMerchant(getActivity()).affilateServicesArrayList;
        affilateServiceNames = new ArrayList<String>();
        affilateServiceNames.add(getResources().getString(R.string.SELECTSERVICETYPE));

        for (AffilateServices affilateServices : affilateServicesArrayList) {
            Log.e("service name",""+affilateServices.ServiceName);
            Log.e("sis active",""+affilateServices.IsActive);

            if (affilateServices.IsActive == true) {
                affilateServiceNames.add(affilateServices.ServiceName.toString().trim());

            }


        }


        Log.e("service name size",""+affilateServiceNames.size());
        for(int i=0;i<affilateServiceNames.size();i++)
        {
            Log.e("service name",affilateServiceNames.get(i));

        }


        affilateGCReedemPurposeList = PrefUtils.getMerchant(getActivity()).gcredeemPerposesArrayList;
        Log.e("GC Purpose name size",""+affilateGCReedemPurposeList.size());
        items = new String[affilateGCReedemPurposeList.size()];
        for(int i=0;i<affilateGCReedemPurposeList.size();i++)
        {
            items[i] = affilateGCReedemPurposeList.get(i).RedeemPurpose;
            Log.e("purpose name",""+affilateGCReedemPurposeList.get(i).RedeemPurpose);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View convertview = inflater.inflate(R.layout.fragment_home, container, false);

        initView(convertview);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isMobileNumberEmpty()) {
                    SimpleToast.error(getActivity(), getResources().getString(R.string.validation_empty_mobile_number));
//                    Toast.makeText(getActivity(), getResources().getString(R.string.validation_empty_mobile_number), Toast.LENGTH_SHORT).show();
                } else if(InternationalNumberValidation.isPossibleNumber(etMobileNumber.getText().toString().toString(), countries.get(spCountryCode.getSelectedItemPosition()).ShortCode.toString().trim())==false){
                    SimpleToast.error(getActivity(), getResources().getString(R.string.code_combinegcfragment_ERROENTERVALIDMONILENUMBER));
                }else if(InternationalNumberValidation.isValidNumber(etMobileNumber.getText().toString().toString(), countries.get(spCountryCode.getSelectedItemPosition()).ShortCode.toString().trim())==false){
                    SimpleToast.error(getActivity(), getResources().getString(R.string.code_combinegcfragment_ERROENTERVALIDMONILENUMBER));
                }else if (selectedPaymentType == -1) {
                    SimpleToast.error(getActivity(), getResources().getString(R.string.validation_empty_payment_type_selection));
//                    Toast.makeText(getActivity(), getResources().getString(R.string.validation_empty_payment_type_selection), Toast.LENGTH_SHORT).show();
                } else if (selectedPaymentType == 1 && isGiftCodeEmpty()) {
                    SimpleToast.error(getActivity(), getResources().getString(R.string.validation_empty_gift_code));
//                    Toast.makeText(getActivity(), getResources().getString(R.string.validation_empty_gift_code), Toast.LENGTH_SHORT).show();
                } else if (selectedServiceType == -1) {
                    SimpleToast.error(getActivity(), getResources().getString(R.string.validation_empty_service_type));
//                    Toast.makeText(getActivity(), getResources().getString(R.string.validation_empty_service_type), Toast.LENGTH_SHORT).show();
                } else if (isAmountEmpty()) {
                    SimpleToast.error(getActivity(), getResources().getString(R.string.validation_empty_amount));
//                    Toast.makeText(getActivity(), getResources().getString(R.string.validation_empty_amount), Toast.LENGTH_SHORT).show();
                } /*else if(etAmount.length()<6){
                    etAmount.setError(getString(R.string.code_ERMSG));
                }*/


                else {
                      
                    if ((selectedPaymentType == 2)) {

                        if (selectedServiceType == 0) {

                            //TODO
                            affilateUser.tempAmount = etAmount.getText().toString().trim();
                            Country countryObject = (Country) spCountryCode.getSelectedItem();
                            affilateUser.tempCountryCode = countryObject.CountryCode + "";
                            affilateUser.tempMobileNumber = etMobileNumber.getText().toString();

                            affilateUser.tempPaymentVia = "Cash";

                            PrefUtils.setMerchant(getActivity(), affilateUser);
                            Intent intent = new Intent(getActivity(), MoneyTransferHomeActivity.class);
                            intent.putExtra("edamount",etAmount.getText().toString());
                            startActivity(intent);

                        } else if (selectedServiceType == 1) {
                            //TODO
                            affilateUser.tempAmount = etAmount.getText().toString().trim();
                            Country countryObject = (Country) spCountryCode.getSelectedItem();
                            affilateUser.tempCountryCode = countryObject.CountryCode + "";
                            affilateUser.tempMobileNumber = etMobileNumber.getText().toString();
                            affilateUser.tempPaymentVia = "Cash";
                            PrefUtils.setMerchant(getActivity(), affilateUser);
                            Intent intent = new Intent(getActivity(), MobileTopupActivity.class);
                            startActivity(intent);



                        } else if (selectedServiceType == 2) {
                            //TODO

                            Log.e("payment type is ","2");
                            affilateUser.tempAmount = etAmount.getText().toString().trim();
                            Country countryObject = (Country) spCountryCode.getSelectedItem();
                            affilateUser.tempCountryCode = countryObject.CountryCode + "";
                            affilateUser.tempMobileNumber = etMobileNumber.getText().toString();
                            affilateUser.tempPaymentVia = "Cash";
                            PrefUtils.setMerchant(getActivity(), affilateUser);
                            Intent intent = new Intent(getActivity(), NewGenerateGCActivity.class);
                            intent.putExtra("payment_via", selectedPaymentType);
                            startActivity(intent);
                        }

                    } else {

                         getServiceLimit(ServiceID);

                        /*if(validateCharges()) {
                            postPaymentRequest();
                        }*/
                    }
                }
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resetAll();

            }
        });

        setCountryCode();

        return convertview;
    }


    private boolean validateCharges(){

        boolean isComplete = false;


        String ednewamount= etAmount.getText().toString().trim();
        ednewamount = ednewamount.replaceAll("\\,", ".");


        Float value = Float.parseFloat(ednewamount);

        if(ServiceID.equals("10")){
            isComplete = true;
        }
        else {


            if (value < serviceChargeobj.MinLimit) {

                isComplete = false;
                etAmount.setError("Minimum Amount is " + affilateUser.LocalCurrency + " " + serviceChargeobj.MinLimit + " For This Service");

            } else if (value > serviceChargeobj.MaxLimit) {

                isComplete = false;
                etAmount.setError("Maximum Amount is " + affilateUser.LocalCurrency + " " + serviceChargeobj.MaxLimit + " For This Service");

            }/*else if(value>user_value){

            isComplete = false;
            edAmountAddMoney.setError(getString(R.string.code_INSUFFICENTBALACNE));

        }*/ else {
                isComplete = true;
            }
        }



        return isComplete;
    }



    private void resetAll() {
        etAmount.setText("");
        etMobileNumber.setText("");
        etGiftCode.setText("");

        layoutTransfer.setVisibility(View.VISIBLE);
        layoutTopup.setVisibility(View.VISIBLE);
        layoutGenerateGC.setVisibility(View.VISIBLE);
        layoutOthers.setVisibility(View.VISIBLE);
        layoutCash.setVisibility(View.VISIBLE);
        layoutWallet.setVisibility(View.VISIBLE);
        layoutGC.setVisibility(View.VISIBLE);

        txtTransfer.setVisibility(View.VISIBLE);
        txtTopup.setVisibility(View.VISIBLE);
        txtGenerate.setVisibility(View.VISIBLE);
        txtWallet.setVisibility(View.VISIBLE);
        txtCash.setVisibility(View.VISIBLE);
        txtGC.setVisibility(View.VISIBLE);

        resetPaymentLinear();
        resetServiceLinear();

     //   spCountryCode.setSelection(0);

    }

    private void setCountryCode() {


        new RegionUtils() {

            @Override
            public void response(ArrayList response) {
                countries = response;

                CountryCodeAdapter countryAdapter = new CountryCodeAdapter(getActivity(), R.layout.spinner_country, countries);
                spCountryCode.setAdapter(countryAdapter);
            }
        }.fetchCountry(getActivity());
    }

    private void initView(View convertview) {
        
        gcLayout = (LinearLayout) convertview.findViewById(R.id.gcLayout);
        btnNext = (TextView) convertview.findViewById(R.id.btnNext);
        btnReset = (TextView) convertview.findViewById(R.id.btnReset);
        etMobileNumber = (EditText) convertview.findViewById(R.id.etMobileNumber);
        etAmount = (EditText) convertview.findViewById(R.id.etAmount);
        etGiftCode = (EditText) convertview.findViewById(R.id.etGiftCode);
        linearPaymentType = (LinearLayout) convertview.findViewById(R.id.linearPaymentType);
        linearServiceType = (LinearLayout) convertview.findViewById(R.id.linearServiceType);
        layoutOthers = (LinearLayout) convertview.findViewById(R.id.layoutOthers);
        spCountryCode = (Spinner) convertview.findViewById(R.id.spCountryCode);

        layoutCash =(LinearLayout) convertview.findViewById(R.id.layoutCash);
        layoutGC = (LinearLayout) convertview.findViewById(R.id.layoutGC);
        layoutGenerateGC = (LinearLayout) convertview.findViewById(R.id.layoutGenerateGC);
        layoutTopup = (LinearLayout) convertview.findViewById(R.id.layoutTopUp);
        layoutTransfer = (LinearLayout) convertview.findViewById(R.id.layoutTransfer);
        layoutWallet = (LinearLayout) convertview.findViewById(R.id.layoutWallet);



        layoutOthers1= (LinearLayout) convertview.findViewById(R.id.layoutOthers1);
        layoutGenerateGC1= (LinearLayout) convertview.findViewById(R.id.layoutGenerateGC1);
        layoutTopUp1= (LinearLayout) convertview.findViewById(R.id.layoutTopUp1);
        linearTransfer1= (LinearLayout) convertview.findViewById(R.id.linearTransfer1);

        txtOther = (TextView)convertview.findViewById(R.id.txtOther);
        txtCash = (TextView)convertview.findViewById(R.id.txtCash);
        txtGC = (TextView)convertview.findViewById(R.id.txtGC);
        txtGenerate = (TextView)convertview.findViewById(R.id.txtGenerate);
        txtTopup = (TextView)convertview.findViewById(R.id.txtTopup);
        txtTransfer = (TextView)convertview.findViewById(R.id.txtTransfer);
        txtWallet = (TextView)convertview.findViewById(R.id.txtWallet);
        txtCurrency= (TextView)convertview.findViewById(R.id.txtCurrency);
        txtConvRate= (TextView)convertview.findViewById(R.id.txtConvRate);
        linearMobileHome = (LinearLayout)convertview.findViewById(R.id.linearMobileHome);


        etAmount.addTextChangedListener(new TextWatcher() {
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

                    etAmount.setText(cashAmountBuilder.toString());
                    // keeps the cursor always to the right
                    Selection.setSelection(etAmount.getText(), cashAmountBuilder.toString().length());

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().length()==0) {
                    txtConvRate.setText("");
                    txtConvRate.setVisibility(View.GONE);
                    }

                else {

                    if(selectedServiceType == 3){
                        // for reddem gc no live conversion rate is not display
                    }
                    else {
                        if (etAmount.getText().toString().trim().length()>=5) {
                            MyApplication.getInstance().cancelAll();
                            getLiveCurrencyRate();
                        }


                    }

                }
            }
        });

    }
    


    @Override
    public void onResume() {
        super.onResume();


        getBalanceAndDisplay();
        affilateUser= PrefUtils.getMerchant(getActivity());
        // String str = affilateUser.affilateServicesArrayList.get(0).ServiceName.toString();
        // position 2 is for Cash in service, 0 - for generate gidt code, 1 - fro mobile top only



        isEnglisSelected= PrefUtils.isEnglishSelected(getActivity());
        if(isEnglisSelected)
            ch=",";
        else
            ch=".";


        isGenerateGCActive = affilateUser.affilateServicesArrayList.get(0).IsActive;
        isMobileTopupActive= affilateUser.affilateServicesArrayList.get(1).IsActive;



        if (isFromDetailPage == true) {
            resetAll();
        }
        PrefUtils.ClearLiveRate(getActivity());
        txtConvRate.setVisibility(View.GONE);
        selectedOtherType=-1;
        txtOther.setText(getResources().getString(R.string.OTHERSERVICES));
        //getting the currency object
        String LocalCurrency = PrefUtils.getAffilateCurrency(getActivity());
        txtCurrency.setText(LocalCurrency);


        paymentTypeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, paymentTypeList);
//      spPaymentType.setAdapter(paymentTypeAdapter);
        serviceTypeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, affilateServiceNames);
//      spServiceType.setAdapter(serviceTypeAdapter);

        resetPaymentLinear();
        resetServiceLinear();
        setupPaymentLinear();
        setupServiceLinear();


     /*   final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        pager.setPageMargin(pageMargin);
    */
      /*  ((MyDrawerActivity)getActivity()).setToolTitle("Hi, User!");
        ((MyDrawerActivity)getActivity()).setToolSubTitle("Balance $0.00");
        ((MyDrawerActivity)getActivity()).setToolColor(Color.parseColor("#2977AC"));
*/


    }
    private void getBalanceAndDisplay() {
        affilateUserBalance= PrefUtils.getMerchant(getActivity());

        new CallWebService(AppConstants.USER_DETAILS+affilateUserBalance.UserID+"/"+LanguageStringUtil.CultureString(getActivity()),CallWebService.TYPE_JSONOBJECT) {

            @Override
            public void response(String jobj) {

                Log.e("on resume response k: ", jobj.toString() + "");

                affilateUserBalance = new GsonBuilder().create().fromJson(jobj.toString(), AffilateUser.class);

              //  AffilateUser user1= PrefUtils.getMerchant(getActivity());
                try{
                    ((MyDrawerActivity)getActivity()).setToolTitle("Hi, "+affilateUserBalance.FName);

                    ((MyDrawerActivity)getActivity()).setToolSubTitle("Balance "+getResources().getString(R.string.euro)+" "+LanguageStringUtil.languageString(getActivity(),affilateUserBalance.LemonwayBal));
                    ((MyDrawerActivity)getActivity()).hideToolLoading();


                    int posCountry = 0;
                    try {
                        for (int i = 0; i < countries.size(); i++) {
                            if (countries.get(i).CountryID == affilateUserBalance.CountryID) {
                                posCountry = i;
                                break;
                            }
                        }
                    }catch (Exception e){
                        Log.e("error ","recipient-prof is not loaded");
                    }
                    spCountryCode.setSelection(posCountry);



                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void error(VolleyError error) {
                try{
                    ((MyDrawerActivity)getActivity()).hideToolLoading();
                }catch(Exception e){}

            }
        }.start();

    }



    private void getLiveCurrencyRate(){
        try{
            JSONObject userObject = new JSONObject();
            AffilateUser user= PrefUtils.getMerchant(getActivity());

            // Log.e("user local currency",user.LocalCurrency);
            final String LocalCurrency = PrefUtils.getAffilateCurrency(getActivity());
            userObject.put("FromCurrency","EUR");
            userObject.put("Tocurrency",LocalCurrency);
            userObject.put("Culture", LanguageStringUtil.CultureString(getActivity()));

            final CircleDialog circleDialog = new CircleDialog(getActivity(), 0);
            circleDialog.setCancelable(true);
            circleDialog.show();
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConstants.GET_LIVE_CURRENCY, userObject, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject jobj) {
                    circleDialog.dismiss();
                    String response = jobj.toString();
                    Log.e("live currency  Response", "" + response);
                    livCurencyObj = new GsonBuilder().create().fromJson(jobj.toString(), LiveCurrency.class);

                    String newvalue1= etAmount.getText().toString().trim();
                    newvalue1 = newvalue1.replaceAll("\\,", ".");

                    float finalamt = Float.valueOf(newvalue1)/ Float.valueOf(livCurencyObj.LiveRate.toString());

                    PrefUtils.settLiveRate(getActivity(),livCurencyObj.LiveRate.toString());

                    double newValue=0.0d;
                    DecimalFormat df = new DecimalFormat("#.##");
                    newValue = Double.valueOf(df.format(finalamt));
                    txtConvRate.setVisibility(View.VISIBLE);
                    txtConvRate.setText(etAmount.getText().toString()+" "+ livCurencyObj.Tocurrency +" = "+ LanguageStringUtil.languageString(getActivity(), String.valueOf(newValue))+" EUR");


                //    txtConvRate.setText(etAmount.getText().toString()+" EUR"+" = "+ String.valueOf(newValue)+" "+livCurencyObj.Tocurrency);
                   // LiveRate.setText("1 EUR = "+cashoutobj.LiveRate+" "+cashoutobj.Tocurrency);


                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    circleDialog.dismiss();
                    Log.e("error curreency: ", error + "");
                    SimpleToast.error(getActivity(), getResources().getString(R.string.er_network));
                }
            });
            req.setRetryPolicy(  new DefaultRetryPolicy(0,0,0));
            MyApplication.getInstance().addToRequestQueue(req);

        }
        catch(Exception e){
            Log.e("error currency out ",e.toString());
        }

    }


    public void resetPaymentLinear() {
        selectedPaymentType = -1;


        for (int i = 0; i < linearPaymentType.getChildCount(); i++) {
            int k = i;
            LinearLayout linear = (LinearLayout) linearPaymentType.getChildAt(i);
            
            ImageView img = (ImageView) linear.getChildAt(0);
            linear.setBackgroundResource(R.drawable.circle_border_focused);
            linear.getBackground().setColorFilter((int) colors_p.get(k), PorterDuff.Mode.SRC_ATOP);
            img.setColorFilter((int) colors_p.get(k), PorterDuff.Mode.SRC_ATOP);
        }
    }

    public void resetServiceLinear() {
        selectedServiceType = -1;

        for (int i = 0; i < linearServiceType.getChildCount(); i++) {
            int k = i;

            LinearLayout linear = (LinearLayout) linearServiceType.getChildAt(i);
            LinearLayout linear2 = (LinearLayout) linear.getChildAt(0);

            ImageView img = (ImageView) linear2.getChildAt(0);
            linear2.setBackgroundResource(R.drawable.circle_border_focused);
            linear2.getBackground().setColorFilter((int) colors_p.get(k), PorterDuff.Mode.SRC_ATOP);
            img.setColorFilter((int) colors_p.get(k), PorterDuff.Mode.SRC_ATOP);
        }

        if(isMobileTopupActive == false){
            layoutTopup.setVisibility(View.GONE);
            txtTopup.setVisibility(View.GONE);
        }
        if(isGenerateGCActive == false){
            layoutGenerateGC.setVisibility(View.GONE);
            txtGenerate.setVisibility(View.GONE);
        }
        selectedOtherType=-1;
        txtOther.setText(getResources().getString(R.string.OTHERSERVICES));
        txtOther.setVisibility(View.VISIBLE);
    }

    private void setupPaymentLinear() {

        for (int i = 0; i < linearPaymentType.getChildCount(); i++) {
            LinearLayout linearChild = (LinearLayout) linearPaymentType.getChildAt(i);
            linearChild.setOnClickListener(linearPaymentListner);
        }


    }

    private void setupServiceLinear() {
        
        for (int i = 0; i <4; i++) {
            LinearLayout linearChild = (LinearLayout) linearServiceType.getChildAt(i);

            linearChild.setOnClickListener(linearServiceListner);
        }

        if(isMobileTopupActive == false){
            layoutTopup.setVisibility(View.GONE);
            txtTopup.setVisibility(View.GONE);
        }

        if(isGenerateGCActive == false){
            layoutGenerateGC.setVisibility(View.GONE);
            txtGenerate.setVisibility(View.GONE);
        }
          
        
    }

    View.OnClickListener linearPaymentListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            
            if(selectedServiceType == -1){
                Toast.makeText(getActivity(), getResources().getString(R.string.SELECTSERVICEFIIRST), Toast.LENGTH_SHORT).show();
            }else{
                LinearLayout linearChild = (LinearLayout) v;
                selectedPaymentType = linearPaymentType.indexOfChild(linearChild);
                setPaymentSelection(selectedPaymentType);
            }



        }
    };

    View.OnClickListener linearServiceListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            
            LinearLayout linearChild = (LinearLayout) v;

            selectedServiceType = linearServiceType.indexOfChild(linearChild);



            setServiceSelection(selectedServiceType);

        }
    };

    private void setServiceSelection(int selectedServiceType) {

        for (int i = 0; i < 4; i++) {

            LinearLayout linear = (LinearLayout) linearServiceType.getChildAt(i);
            LinearLayout linear2 = (LinearLayout) linear.getChildAt(0);
            ImageView iv = (ImageView) linear2.getChildAt(0);
            int z = i;
            if (z == selectedServiceType) {
                iv.setColorFilter(Color.WHITE);
                linear2.setBackgroundResource(R.drawable.circle_mask);
                linear2.getBackground().setColorFilter((int) colors_p.get(selectedServiceType), PorterDuff.Mode.SRC_ATOP);
            } else {
                iv.setColorFilter((int) colors_p.get(z), PorterDuff.Mode.SRC_ATOP);
                linear2.setBackgroundResource(R.drawable.circle_border_focused);
                linear2.getBackground().setColorFilter((int) colors_p.get(z), PorterDuff.Mode.SRC_ATOP);
            }
        }

        switch (selectedServiceType){

            case 0:

                ServiceID=AppConstants.Money_Transfer;

                // transfer
                layoutWallet.setVisibility(View.VISIBLE);
                layoutGC.setVisibility(View.GONE);
                layoutCash.setVisibility(View.VISIBLE);

                txtWallet.setVisibility(View.VISIBLE);
                txtGC.setVisibility(View.GONE);
                txtCash.setVisibility(View.VISIBLE);
                linearMobileHome.setVisibility(View.VISIBLE);

                etAmount.setText("");
                selectedOtherType=-1;
                txtOther.setText(getResources().getString(R.string.OTHERSERVICES));
                gcLayout.setVisibility(View.GONE);
                break;

            case 1:

                ServiceID=AppConstants.Mobile_Top_Up;

                //topup

                layoutWallet.setVisibility(View.VISIBLE);
                layoutGC.setVisibility(View.GONE);
                layoutCash.setVisibility(View.VISIBLE);

                txtWallet.setVisibility(View.VISIBLE);
                txtGC.setVisibility(View.GONE);
                txtCash.setVisibility(View.VISIBLE);

                linearMobileHome.setVisibility(View.VISIBLE);

                etAmount.setText("");
                selectedOtherType=-1;
                txtOther.setText(getResources().getString(R.string.OTHERSERVICES));
                gcLayout.setVisibility(View.GONE);
                break;

            case 2:

                ServiceID=AppConstants.Generate_New_Gift_Code;

                //generate
                layoutWallet.setVisibility(View.VISIBLE);
                layoutGC.setVisibility(View.GONE);
                layoutCash.setVisibility(View.VISIBLE);

                txtWallet.setVisibility(View.VISIBLE);
                txtGC.setVisibility(View.GONE);
                txtCash.setVisibility(View.VISIBLE);
                linearMobileHome.setVisibility(View.VISIBLE);

                etAmount.setText("");
                selectedOtherType=-1;
                txtOther.setText(getResources().getString(R.string.OTHERSERVICES));
                gcLayout.setVisibility(View.GONE);
                break;

            case 3:

                ServiceID=AppConstants.Redeem_Gift_code;

                //others

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.SELECTOTHERSERVICES));

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int pos) {
                        // Do something with the selection
                        //   mDoneButton.setText(items[item]);
                        selectedOtherType=affilateGCReedemPurposeList.get(pos).Id;
                        selectedOtherPurposeType=affilateGCReedemPurposeList.get(pos).RedeemPurpose;
                        txtOther.setText(items[pos].toUpperCase());

                    }
                });
                AlertDialog alert = builder.create();
                alert.setCancelable(false);
                alert.show();

                layoutWallet.setVisibility(View.GONE);
                layoutGC.setVisibility(View.VISIBLE);
                layoutCash.setVisibility(View.GONE);

                txtWallet.setVisibility(View.GONE);
                txtGC.setVisibility(View.VISIBLE);
                txtCash.setVisibility(View.GONE);

                linearMobileHome.setVisibility(View.VISIBLE);
                gcLayout.setVisibility(View.VISIBLE);
                etAmount.setText("");
                break;

        }


    }

    private void setPaymentSelection(int selectedPaymentType) {



        for (int i = 0; i < linearPaymentType.getChildCount(); i++) {

            LinearLayout linear = (LinearLayout) linearPaymentType.getChildAt(i);
            ImageView iv = (ImageView) linear.getChildAt(0);
            int z = i;
            if (z == selectedPaymentType) {
                iv.setColorFilter(Color.WHITE);
                linear.setBackgroundResource(R.drawable.circle_mask);
                linear.getBackground().setColorFilter((int) colors_p.get(selectedPaymentType), PorterDuff.Mode.SRC_ATOP);

            } else {

                iv.setColorFilter((int) colors_p.get(z), PorterDuff.Mode.SRC_ATOP);
                linear.setBackgroundResource(R.drawable.circle_border_focused);
                linear.getBackground().setColorFilter((int) colors_p.get(z), PorterDuff.Mode.SRC_ATOP);

            }
        }


      /*  switch (selectedPaymentType){

            case 0:
                    // for wallet
                layoutTransfer.setVisibility(View.VISIBLE);
                txtTransfer.setVisibility(View.VISIBLE);

                if(isMobileTopupActive == false){
                    layoutTopup.setVisibility(View.GONE);
                    txtTopup.setVisibility(View.GONE);
                }
                else{
                    layoutTopup.setVisibility(View.VISIBLE);
                    txtTopup.setVisibility(View.VISIBLE);
                }

                if(isGenerateGCActive == false){
                    layoutGenerateGC.setVisibility(View.GONE);
                    txtGenerate.setVisibility(View.GONE);
                }
                else{
                    layoutGenerateGC.setVisibility(View.VISIBLE);
                    txtGenerate.setVisibility(View.VISIBLE);
                }
                layoutTransfer.setVisibility(View.VISIBLE);
                txtTransfer.setVisibility(View.VISIBLE);

                //hide
                layoutOthers.setVisibility(View.GONE);
                txtOther.setVisibility(View.GONE);
                gcLayout.setVisibility(View.GONE);

                break;

            case 1:
                // for Giftcode
                //display
                layoutOthers.setVisibility(View.VISIBLE);
                txtOther.setVisibility(View.VISIBLE);

                gcLayout.setVisibility(View.VISIBLE);

                //hide
                layoutTopup.setVisibility(View.GONE);
                txtTopup.setVisibility(View.GONE);
                layoutGenerateGC.setVisibility(View.GONE);
                txtGenerate.setVisibility(View.GONE);
                layoutTransfer.setVisibility(View.GONE);
                txtTransfer.setVisibility(View.GONE);

                break;

            case 2:
            // for Cash
                layoutTransfer.setVisibility(View.VISIBLE);
                txtTransfer.setVisibility(View.VISIBLE);

            if(isMobileTopupActive == false){
                layoutTopup.setVisibility(View.GONE);
                txtTopup.setVisibility(View.GONE);
            }
            else{
                layoutTopup.setVisibility(View.VISIBLE);
                txtTopup.setVisibility(View.VISIBLE);
            }

            if(isGenerateGCActive == false){
                layoutGenerateGC.setVisibility(View.GONE);
                txtGenerate.setVisibility(View.GONE);
            }

            else{
                layoutGenerateGC.setVisibility(View.VISIBLE);
                txtGenerate.setVisibility(View.VISIBLE);
            }

            //hide
            layoutOthers.setVisibility(View.GONE);
            txtOther.setVisibility(View.GONE);
                gcLayout.setVisibility(View.GONE);


        }*/

      /*  if(isMobileTopupActive == false){
            layoutTopup.setVisibility(View.GONE);
            txtTopup.setVisibility(View.GONE);
        }
        if(isGenerateGCActive == false){
            layoutGenerateGC.setVisibility(View.GONE);
            txtGenerate.setVisibility(View.GONE);
        }*/

/*
        if (selectedPaymentType == 0) {
            gcLayout.setVisibility(View.VISIBLE);
        } else {
            gcLayout.setVisibility(View.GONE);
        }




        if (selectedPaymentType == 1) {
            gcLayout.setVisibility(View.VISIBLE);
        } else {
            gcLayout.setVisibility(View.GONE);
        }

        if (selectedPaymentType == 2) {
            layoutOthers.setVisibility(View.GONE);
            txtOther.setVisibility(View.GONE);
        } else {
            layoutOthers.setVisibility(View.VISIBLE);
        }*/

    }


    private void postPaymentRequest() {

        circleDialog = new CircleDialog(getActivity(), 0);
        circleDialog.setCancelable(true);
        circleDialog.show();

        JSONObject requestObject = new JSONObject();
        try {

            requestObject.put("AffiliateID", affilateUser.UserID + "");
            //  String LiveRate=PrefUtils.getLiveRate(getActivity());
            // Log.e("using live rate",LiveRate);
            // float finalamt = Float.valueOf(etAmount.getText().toString().trim()) * Float.valueOf(LiveRate);
            // double newPayableAMount=0.0d;
            // DecimalFormat df = new DecimalFormat("#.##");
            // newPayableAMount = Double.valueOf(df.format(finalamt));

            //     requestObject.put("Amount", String.valueOf(newPayableAMount));

            requestObject.put("Culture", LanguageStringUtil.CultureString(getActivity()));
            if (selectedServiceType == 1) {
                requestObject.put("Amount", "1");
            }
            else{
                String newvalue= etAmount.getText().toString().trim();
                newvalue = newvalue.replaceAll("\\,", ".");

                requestObject.put("Amount", newvalue + "");
            }



            if (selectedPaymentType == 1) {
                requestObject.put("GiftCode", etGiftCode.getText().toString()); //add if gift code select
            }




            if (selectedPaymentType == 1) {
                requestObject.put("PaymentVia", "GC"); //GC or Wallet
            } else if (selectedPaymentType == 0) {
                requestObject.put("PaymentVia", "Wallet");
            }
            Country countryObject = (Country) spCountryCode.getSelectedItem();
            requestObject.put("UserCountryCode", countryObject.CountryCode);
            requestObject.put("UserMobileNo", etMobileNumber.getText().toString().trim() + "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("object payment 1", requestObject.toString() + "");
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConstants.PAYMENT_STEP_1, requestObject, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject jobj) {
                circleDialog.dismiss();

                Log.e("response: ", jobj.toString() + "");
                paymentStep1 = new GsonBuilder().create().fromJson(jobj.toString(), PaymentStep1.class);
                try {
                    if (paymentStep1.ResponseCode.equalsIgnoreCase("1")) {

                        SimpleToast.ok(getActivity(), getResources().getString(R.string.PaymentStep1_1));
                        showVerificationAlert();


                    } else if (paymentStep1.ResponseCode.equalsIgnoreCase("2")) {
                        SimpleToast.error(getActivity(), getResources().getString(R.string.PaymentStep1_2));


                    } else if (paymentStep1.ResponseCode.equalsIgnoreCase("2")) {
                        SimpleToast.error(getActivity(), getResources().getString(R.string.PaymentStep1_2));
//                        Toast.makeText(getActivity(), getResources().getString(R.string.PaymentStep1_2), Toast.LENGTH_SHORT).show();
                    } else if (paymentStep1.ResponseCode.equalsIgnoreCase("-2")) {
                        SimpleToast.error(getActivity(), getResources().getString(R.string.PaymentStep1_m2));
//                        Toast.makeText(getActivity(), getResources().getString(R.string.PaymentStep1_m2), Toast.LENGTH_SHORT).show();
                    } else if (paymentStep1.ResponseCode.equalsIgnoreCase("-3")) {
                        SimpleToast.error(getActivity(), getResources().getString(R.string.PaymentStep1_m3));
//                        Toast.makeText(getActivity(), getResources().getString(R.string.PaymentStep1_m3), Toast.LENGTH_SHORT).show();
                    } else if (paymentStep1.ResponseCode.equalsIgnoreCase("-4")) {
                        SimpleToast.error(getActivity(), getResources().getString(R.string.PaymentStep1_m4));
//                        Toast.makeText(getActivity(), getResources().getString(R.string.PaymentStep1_m4), Toast.LENGTH_SHORT).show();
                    }
                    else if (paymentStep1.ResponseCode.equalsIgnoreCase("-6")) {
                          SimpleToast.error(getActivity(), paymentStep1.ResponseMsg);
//                        Toast.makeText(getActivity(), getResources().getString(R.string.PaymentStep1_m4), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        SimpleToast.error(getActivity(), getResources().getString(R.string.PaymentStep1_m5));
//                        Toast.makeText(getActivity(), getResources().getString(R.string.PaymentStep1_m5), Toast.LENGTH_SHORT).show();
                    }
               } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("exc",e.toString());
                    SimpleToast.error(getActivity(), "Error");
                }

            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                circleDialog.dismiss();
            }
        });

        req.setRetryPolicy(new DefaultRetryPolicy(0, 0, 0));
        MyApplication.getInstance().addToRequestQueue(req);

    }

    private void checkOTPTimeout(String OTP) {

        try{
            JSONObject userObject = new JSONObject();
            AffilateUser user= PrefUtils.getMerchant(getActivity());

            // Log.e("user local currency",user.LocalCurrency);
            final String LocalCurrency = PrefUtils.getAffilateCurrency(getActivity());
            userObject.put("OPT",OTP);
            userObject.put("UserID",paymentStep1.UserID);
            userObject.put("Culture", LanguageStringUtil.CultureString(getActivity()));

            Log.e("chk otp obj", "" + userObject.toString());
            final CircleDialog circleDialog = new CircleDialog(getActivity(), 0);
            circleDialog.setCancelable(true);
            circleDialog.show();
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConstants.OTP_TIME_OUT, userObject, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject jobj) {
                    circleDialog.dismiss();
                    String response = jobj.toString();
                    Log.e("chk otp Response", "" + response);

                    try {
                        JSONObject obj = new JSONObject(response);

                        if (obj.getString("ResponseCode").equalsIgnoreCase("1")) {
                             /*isok = true;
                            Log.e("isok",""+isok);*/

                            if (isRedeemGC()) { // Redeem GC
                                 processRedeemGC();

                            }
                            else if (selectedServiceType == 0) { // Money Transfer
                                //TODO
                                affilateUser.tempAmount = etAmount.getText().toString().trim();
                                Country countryObject = (Country) spCountryCode.getSelectedItem();
                                affilateUser.tempCountryCode = countryObject.CountryCode + "";
                                affilateUser.tempMobileNumber = etMobileNumber.getText().toString();
                                if (selectedPaymentType == 1) {
                                    affilateUser.tempPaymentVia = "GC";
                                    affilateUser.tempGiftCode = etGiftCode.getText().toString().trim();
                                } else {
                                    affilateUser.tempPaymentVia = "Wallet";
                                }
                                PrefUtils.setMerchant(getActivity(), affilateUser);
                                Intent intent = new Intent(getActivity(), MoneyTransferHomeActivity.class);
                                intent.putExtra("edamount",etAmount.getText().toString());
                                startActivity(intent);

                            } else if (selectedServiceType == 1) { // Mobile Topup
                                //TODO
                                affilateUser.tempAmount = etAmount.getText().toString().trim();
                                Country countryObject = (Country) spCountryCode.getSelectedItem();
                                affilateUser.tempCountryCode = countryObject.CountryCode + "";
                                affilateUser.tempMobileNumber = etMobileNumber.getText().toString();
                                if (selectedPaymentType == 1) {
                                    affilateUser.tempPaymentVia = "GC";
                                    affilateUser.tempGiftCode = etGiftCode.getText().toString().trim();
                                } else if (selectedPaymentType == 0) {
                                    affilateUser.tempPaymentVia = "Wallet";
                                } else if (selectedPaymentType == 2) {
                                    affilateUser.tempPaymentVia = "Cash";
                                }
                                PrefUtils.setMerchant(getActivity(), affilateUser);
                                Intent intent = new Intent(getActivity(), MobileTopupActivity.class);
                                startActivity(intent);
                            } else if (selectedServiceType == 2) { //Generate GC
                                //TODO
                                affilateUser.tempAmount = etAmount.getText().toString().trim();
                                Country countryObject = (Country) spCountryCode.getSelectedItem();
                                affilateUser.tempCountryCode = countryObject.CountryCode + "";
                                affilateUser.tempMobileNumber = etMobileNumber.getText().toString();
                                if (selectedPaymentType == 1) {
                                    affilateUser.tempPaymentVia = "GC";
                                    affilateUser.tempGiftCode = etGiftCode.getText().toString().trim();
                                } else if (selectedPaymentType == 0) {
                                    affilateUser.tempPaymentVia = "Wallet";
                                } else if (selectedPaymentType == 2) {
                                    affilateUser.tempPaymentVia = "Cash";
                                }
                                PrefUtils.setMerchant(getActivity(), affilateUser);
                                Intent intent = new Intent(getActivity(), NewGenerateGCActivity.class);
                                intent.putExtra("payment_via", selectedPaymentType);
                                startActivity(intent);

                            }


                        }
                        else {
                            Log.e("inside else ","else otp");
/*
                            isok = false;
                            Log.e("isok",""+isok);*/
                            SimpleToast.error(getActivity(), obj.getString("ResponseMsg"));

                            paymentStep1.VerificationCode="";
                            showResenVerificationCodealertboc();


                        }
                    }catch (Exception e){
                        Log.e("error response  otp:",""+e);
                    }



                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    circleDialog.dismiss();
                    Log.e("error chckotp: ", error + "");
                    SimpleToast.error(getActivity(), getResources().getString(R.string.er_network));
                }
            });
            req.setRetryPolicy(  new DefaultRetryPolicy(0,0,0));
            MyApplication.getInstance().addToRequestQueue(req);

        }
        catch(Exception e){
            Log.e("error chckotp ",e.toString());
        }


    }



private void  showResenVerificationCodealertboc(){

        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.custom_resend_alert_dialog, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setView(promptsView);

        alert.setNeutralButton(getResources().getString(R.string.RESEND),new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
               // showVerificationAlert();
                  postPaymentRequest();
            }
        });
        alert.show();
    }

private void showVerificationAlert() {

        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.custom_alert_dialog, null);
        final EditText etVerificationCode = (EditText) promptsView.findViewById(R.id.etVerificationCode);
        etVerificationCode.setText(paymentStep1.VerificationCode + "");

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setView(promptsView);

        alert.setPositiveButton(getResources().getString(R.string.VERIFY), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (paymentStep1.VerificationCode.equalsIgnoreCase(etVerificationCode.getText().toString().trim())) {
                    // TODO goto next screen
                    dialog.dismiss();
                    checkOTPTimeout(etVerificationCode.getText().toString().trim());
                }


                else {
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


    /*
       Retreive image from assests and return in Bitmap format
    */
    private Bitmap getBitmapFromAsset(String strName) {
        AssetManager assetManager = getActivity().getAssets();
        InputStream istr = null;
        try {
            istr = assetManager.open(strName + ".png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        return bitmap;
    }

    private void processRedeemGC() {

        circleDialog = new CircleDialog(getActivity(), 0);
        circleDialog.setCancelable(true);
        circleDialog.show();

        JSONObject requestObject = new JSONObject();
        try {

          //  String LiveRate=PrefUtils.getLiveRate(getActivity());
           // Log.e("using live rate",LiveRate);
           // float finalamt = Float.valueOf(etAmount.getText().toString().trim()) * Float.valueOf(LiveRate);
           // double newPayableAMount=0.0d;
           // DecimalFormat df = new DecimalFormat("#.##");
           // newPayableAMount = Double.valueOf(df.format(finalamt));

       //     requestObject.put("Amount", String.valueOf(newPayableAMount));
            requestObject.put("Culture", LanguageStringUtil.CultureString(getActivity()));
            requestObject.put("AffiliateID", affilateUser.UserID + "");

            String newvalue= etAmount.getText().toString().trim();
            newvalue = newvalue.replaceAll("\\,", ".");

            requestObject.put("Amount", newvalue + "");

            requestObject.put("PurposeId", selectedOtherType + "");

            //requestObject.put("ServiceID", selectedOtherType + "");
            //requestObject.put("ServiceUse", selectedOtherPurposeType + "");

            requestObject.put("GiftCode", etGiftCode.getText().toString().trim() + "");
            Country countryObject = (Country) spCountryCode.getSelectedItem();
            requestObject.put("UserCountryCode", countryObject.CountryCode + "");
            requestObject.put("UserMobileNo", etMobileNumber.getText().toString().trim() + "");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("object request", requestObject.toString() + "");
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConstants.REDEEM_GC, requestObject, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject jobj) {
                circleDialog.dismiss();
                LOGE("response: ", jobj.toString() + "");
                Log.e("response redeem gc: ", jobj.toString() + "");
                RedeemGC redeemGC = new GsonBuilder().create().fromJson(jobj.toString(), RedeemGC.class);

                PrefUtils.ClearLiveRate(getActivity());

                if (redeemGC.ResponseCode.equalsIgnoreCase("1")) {
                    SimpleToast.ok(getActivity(), getResources().getString(R.string.RedeemGC1_1));
                } else if (redeemGC.ResponseCode.equalsIgnoreCase("2")) {
                    SimpleToast.ok(getActivity(), getResources().getString(R.string.RedeemGC1_2));
//                        Toast.makeText(getActivity(), getResources().getString(R.string.PaymentStep1_2), Toast.LENGTH_SHORT).show();
                } else if (redeemGC.ResponseCode.equalsIgnoreCase("-1")) {
                    SimpleToast.error(getActivity(), getResources().getString(R.string.RedeemGC1_m1));
//                        Toast.makeText(getActivity(), getResources().getString(R.string.PaymentStep1_2), Toast.LENGTH_SHORT).show();
                } else if (redeemGC.ResponseCode.equalsIgnoreCase("-2")) {
                    SimpleToast.error(getActivity(), getResources().getString(R.string.RedeemGC1_m2));
//                        Toast.makeText(getActivity(), getResources().getString(R.string.PaymentStep1_m2), Toast.LENGTH_SHORT).show();
                } else if (redeemGC.ResponseCode.equalsIgnoreCase("-3")) {
                    SimpleToast.error(getActivity(), getResources().getString(R.string.RedeemGC1_m3));
//                        Toast.makeText(getActivity(), getResources().getString(R.string.PaymentStep1_m3), Toast.LENGTH_SHORT).show();
                } else if (redeemGC.ResponseCode.equalsIgnoreCase("-4")) {

                    SimpleToast.error(getActivity(), getResources().getString(R.string.RedeemGC1_m4));
//                        Toast.makeText(getActivity(), getResources().getString(R.string.PaymentStep1_m4), Toast.LENGTH_SHORT).show();
                }
                else if (redeemGC.ResponseCode.equalsIgnoreCase("-6")) {

                    SimpleToast.error(getActivity(), redeemGC.ResponseMsg);
//                        Toast.makeText(getActivity(), getResources().getString(R.string.PaymentStep1_m4), Toast.LENGTH_SHORT).show();
                }else {
                    SimpleToast.error(getActivity(), getResources().getString(R.string.RedeemGC1_m5));
//                        Toast.makeText(getActivity(), getResources().getString(R.string.PaymentStep1_m5), Toast.LENGTH_SHORT).show();
                }

            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                circleDialog.dismiss();
            }
        });

        req.setRetryPolicy(new DefaultRetryPolicy(0, 0, 0));
        MyApplication.getInstance().addToRequestQueue(req);
    }
    private boolean isRedeemGC() {
        boolean isAvailable = false;
        if (selectedPaymentType == 1 && selectedServiceType == 3) {
            isAvailable = true;
        }


            return isAvailable;
    }


    public boolean isMobileNumberEmpty() {

        boolean isEmpty = false;

        if (etMobileNumber.getText() == null || etMobileNumber.getText().toString().equalsIgnoreCase("")) {
            isEmpty = true;
        }
        return isEmpty;
    }

    public boolean isGiftCodeEmpty() {

        boolean isEmpty = false;

        if (etGiftCode.getText() == null || etGiftCode.getText().toString().equalsIgnoreCase("")) {
            isEmpty = true;
        }
        return isEmpty;
    }

    public boolean isAmountEmpty() {

        boolean isEmpty = false;

        if(selectedServiceType == 1){
            isEmpty = false;
            return isEmpty;
        }else{
            if (etAmount.getText() == null || etAmount.getText().toString().equalsIgnoreCase("")) {
                isEmpty = true;
            }
        }


        return isEmpty;
    }


    public class CountryCodeAdapter extends ArrayAdapter<Country> {
        Context context;
        int layoutResourceId;
        ArrayList<Country> values;

        // int android.R.Layout.
        public CountryCodeAdapter(Context context, int resource, ArrayList<Country> objects) {
            super(context, resource, objects);
            this.context = context;
            this.values = objects;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {

            TextView txt = new TextView(getActivity());
            txt.setPadding(16, 16, 16, 16);
            txt.setGravity(Gravity.CENTER_VERTICAL);
            txt.setText(values.get(position).CountryName + " +" + String.valueOf(values.get(position).CountryCode));

            txt.setText(values.get(position).CountryName + " +" + String.valueOf(values.get(position).CountryCode));

            if (values.get(position).ShortCode == null || values.get(position).ShortCode.equalsIgnoreCase("") || values.get(position).ShortCode.equalsIgnoreCase("NULL")) {
            } else {
                try {
                  /*  Class res = R.drawable.class;
                    Field field = res.getField(values.get(position).ShortCode.toLowerCase().toString()+".png");
                    int drawableId = field.getInt(null);*/
                    int idd = getResources().getIdentifier("com.webmyne.paylabasmerchant:drawable/" + values.get(position).ShortCode.toString().trim().toLowerCase(), null, null);
                    txt.setCompoundDrawablesWithIntrinsicBounds(idd, 0, 0, 0);
                } catch (Exception e) {
                    Log.e("MyTag", "Failure to get drawable id.", e);
                }


            }
            return txt;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView txt = new TextView(getActivity());
            txt.setGravity(Gravity.CENTER_VERTICAL);
            txt.setPadding(16, 16, 16, 16);
            txt.setText("+" + String.valueOf(values.get(position).CountryCode));


            return txt;
        }
    }

    private String getServiceName(int selectedServiceType) {
        String serviceType;
        if (selectedServiceType == 0) {
            serviceType = "";
        } else if (selectedServiceType == 1) {
            serviceType = "";
        } else if (selectedServiceType == 2) {
            serviceType = "";
        } else {
            serviceType = "Other";
        }
        return serviceType;
    }



private void getServiceLimit(String ServiceId) {

    affilateUser= PrefUtils.getMerchant(getActivity());

    JSONObject object = null;
    try {
        object = new JSONObject();
        object.put("Culture", LanguageStringUtil.CultureString(getActivity()));
        object.put("ServiceID", ServiceId);
        object.put("AffiliateID", affilateUser.UserID);

        Log.e("obj service limit: ", "" + object.toString());

    } catch (Exception e) {
        e.printStackTrace();
    }

    final CircleDialog circleDialog = new CircleDialog(getActivity(), 0);
    circleDialog.setCancelable(true);
    circleDialog.show();

    JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConstants.AFFILATE_SERVICE_LIMIT, object, new Response.Listener<JSONObject>() {

        @Override
        public void onResponse(JSONObject jobj) {

            circleDialog.dismiss();
            String response = jobj.toString();
            Log.e("Response service limit: ", "" + response);
            try {
                JSONObject obj = new JSONObject(response);
                String responsecode = obj.getString("ResponseCode");

                if (responsecode.equalsIgnoreCase("1")) {

                    serviceChargeobj = new GsonBuilder().create().fromJson(response, ServiceCharge.class);
                    PrefUtils.setServiceLimit(getActivity(),serviceChargeobj);

                    if(validateCharges()) {
                            postPaymentRequest();
                        }

                } else {
                    SimpleToast.error(getActivity(),obj.getString("ResponseMsg"));

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }, new Response.ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {

            circleDialog.dismiss();
            SimpleToast.error(getActivity(), getString(R.string.NWWNE));


        }
    });

    req.setRetryPolicy(
            new DefaultRetryPolicy(0, 0, 0));

    MyApplication.getInstance().addToRequestQueue(req);

}


//end of main class
}
