package com.webmyne.paylabasmerchant.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.GsonBuilder;
import com.webmyne.paylabasmerchant.R;
import com.webmyne.paylabasmerchant.model.AffilateUser;
import com.webmyne.paylabasmerchant.model.AppConstants;
import com.webmyne.paylabasmerchant.ui.widget.CircleDialog;
import com.webmyne.paylabasmerchant.ui.widget.SimpleToast;
import com.webmyne.paylabasmerchant.util.LanguageStringUtil;
import com.webmyne.paylabasmerchant.util.PrefUtils;

import static com.webmyne.paylabasmerchant.util.PrefUtils.isEnglishSelected;
import static com.webmyne.paylabasmerchant.util.PrefUtils.isLoggedIn;
import static com.webmyne.paylabasmerchant.util.PrefUtils.setLoggedIn;
import static com.webmyne.paylabasmerchant.util.LogUtils.LOGE;
import static com.webmyne.paylabasmerchant.util.PrefUtils.setMerchant;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;


public class LoginActivity extends ActionBarActivity {

    private TextView btnLoginNext;
    private EditText etMerchantId,etSecretId;
    private CircleDialog circleDialog;
    private AffilateUser affilateUser;
    private boolean isLogin=false;
    private ImageView imgUS,imgFrance;
    private boolean isEnglisSelected=false;
    private GoogleCloudMessaging gcm;
    private String regid;
    private String PROJECT_NUMBER = "92884720384";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // overridePendingTransition(R.anim.entry,R.anim.exit);
       setContentView(R.layout.activity_launcher);
       initView();
    }

    private void initView(){

        etMerchantId= (EditText) findViewById(R.id.etMerchantId);
        etSecretId= (EditText) findViewById(R.id.etSecretId);
        btnLoginNext= (TextView) findViewById(R.id.btnLoginNext);
        imgUS= (ImageView) findViewById(R.id.imgUS);
        imgFrance= (ImageView) findViewById(R.id.imgFrance);
    }



    @Override
    protected void onResume() {

        super.onResume();
        setLanguage();
        etMerchantId.setText("C35361266A");
        etSecretId.setText("1VC@ht");

        btnLoginNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("inside","btn click");


                if(isMerchantEmpty() || isSecretIdEmpty()){
//                    SnackBar bar = new SnackBar(Login.this,"Please enter merchant id and secret id");
//                    bar.show();
                    Log.e("inside","if");
                    SimpleToast.error(LoginActivity.this,getString(R.string.login_merchant_and_password_message));
//                    Toast.makeText(LoginActivity.this,"Please enter merchant id and Password",Toast.LENGTH_SHORT).show();
                } else {
                    circleDialog = new CircleDialog(LoginActivity.this, 0);
                    circleDialog.setCancelable(true);
                    circleDialog.show();
                    Log.e("inside", "else");

                    getRegId();
                }

            }
        });
        imgUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isEnglisSelected= PrefUtils.isEnglishSelected(LoginActivity.this);
                if(isEnglisSelected){
                    showLanguageAlert("en");
                }

            }
        });

        imgFrance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isEnglisSelected= PrefUtils.isEnglishSelected(LoginActivity.this);
                if(!isEnglisSelected){
                    showLanguageAlert("fr");
                }

            }
        });
    }

    private void setLanguage() {

        isEnglisSelected= PrefUtils.isEnglishSelected(LoginActivity.this);
        if(isEnglishSelected(LoginActivity.this)){
            imgUS.setColorFilter(Color.argb(128, 0, 0, 0));
            Configuration config = new Configuration();
            config.locale = Locale.FRANCE;
            getResources().updateConfiguration(config, null);
            etMerchantId.setHint("Merchand ID");
            etSecretId.setHint("mot de passe");
            btnLoginNext.setText("SUIVANT");

        } else {
            imgFrance.setColorFilter(Color.argb(128, 0, 0, 0));
            Configuration config = new Configuration();
            config.locale = Locale.ENGLISH;
            getResources().updateConfiguration(config, null);
            etMerchantId.setHint("Merchant ID");
            etSecretId.setHint("Password");
            btnLoginNext.setText("NEXT");
        }


        if(isLoggedIn(LoginActivity.this)){
            Intent intent =new Intent(LoginActivity.this,VerificationActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void showLanguageAlert(final String languageType){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Change Language");
        if(languageType.equalsIgnoreCase("en")){
            alert.setMessage("Are you sure, you want to change language to English");
        } else {
            alert.setMessage("Are you sure, you want to change language to French");
        }
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if(languageType.equalsIgnoreCase("en")){

                    PrefUtils.setEnglishSelected(LoginActivity.this,false);
                    imgUS.clearColorFilter();
                    imgFrance.setColorFilter(Color.argb(128, 0, 0, 0));
                } else {
                    PrefUtils.setEnglishSelected(LoginActivity.this,true);
                    imgFrance.clearColorFilter();
                    imgUS.setColorFilter(Color.argb(128, 0, 0, 0));
                }
                changeLanguage(languageType);
                dialog.dismiss();

            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });

        alert.show();

    }

    private void changeLanguage(String languageType){

        if(languageType.equalsIgnoreCase("en")){
            Log.e("eng","eng");
            Configuration config = new Configuration();
            config.locale = Locale.ENGLISH;
            getResources().updateConfiguration(config, null);
            etMerchantId.setHint("Merchant ID");
            etSecretId.setHint("Password");
            btnLoginNext.setText("NEXT");
        } else {
            Log.e("french","french");
            Configuration config = new Configuration();
            config.locale = Locale.FRANCE;
            getResources().updateConfiguration(config, null);
            etMerchantId.setHint("Merchand ID");
            etSecretId.setHint("mot de passe");
            btnLoginNext.setText("SUIVANT");
        }

    }

    public boolean isMerchantEmpty(){

        boolean isEmpty = false;

        if(etMerchantId.getText() == null || etMerchantId.getText().toString().equalsIgnoreCase("")){
            isEmpty = true;
        }
        return isEmpty;
    }

    public boolean isSecretIdEmpty(){

        boolean isEmpty = false;

        if(etSecretId.getText() == null || etSecretId.getText().toString().equalsIgnoreCase("")){
            isEmpty = true;
        }
        return isEmpty;

    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.entry,R.anim.exit);
    }


    private void checkMerchentLogin() {

        Log.e("inside","check merchent");

        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("MerchantID", etMerchantId.getText().toString().trim() + "");
            requestObject.put("Password", etSecretId.getText().toString().trim() + "");
            requestObject.put("Culture", LanguageStringUtil.CultureString(LoginActivity.this));
            Log.e("login ojb",requestObject.toString());
        } catch (Exception e){
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConstants.AFFILATE_LOGIN, requestObject, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject jobj) {
                circleDialog.dismiss();

                Log.e("response: ", jobj.toString() + "");

                affilateUser = new GsonBuilder().create().fromJson(jobj.toString(), AffilateUser.class);
                if(affilateUser.ResponseCode.equalsIgnoreCase("1")){
                    //store current user and domain in shared preferences
                    setMerchant(LoginActivity.this,affilateUser);

                    // setting the Local currency
                    PrefUtils.setAffilateCurrency(LoginActivity.this, affilateUser.LocalCurrency);

                    // set login true
                    setLoggedIn(LoginActivity.this, true);
                    Intent intent =new Intent(LoginActivity.this,VerificationActivity.class);
                    startActivity(intent);
                    finish();

                    } else {
//                    SimpleToast.error(LoginActivity.this, getString(R.string.network_error_message) +"Please try again");
                    // Toast.makeText(LoginActivity.this,"Network Error\n" +"Please try again",Toast.LENGTH_SHORT).show();
                    SimpleToast.error(LoginActivity.this, getString(R.string.INVALIDMOBILEORPASSWORD));
//                    Toast.makeText(LoginActivity.this,"Network Error\n" +"Please try again",Toast.LENGTH_SHORT).show();
                    }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                circleDialog.dismiss();
            }
        });

        req.setRetryPolicy(new DefaultRetryPolicy(0,0,0));
        MyApplication.getInstance().addToRequestQueue(req);

    }

    public void getRegId(){
/*

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
               try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(LoginActivity.this);
                    }

                    regid = gcm.register(PROJECT_NUMBER);


                    Log.e("GCM ID :", regid);

                    if(regid==null || regid==""){
                        AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
                        alert.setTitle(getString(R.string.ERROR));
                        alert.setMessage(getString(R.string.INTERNALSERVERERROR));
                        alert.setPositiveButton(getString(R.string.TRYAGIN), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getRegId();
                                dialog.dismiss();
                            }
                        });
                        alert.setNegativeButton(getString(R.string.EXIT), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                        alert.show();
                    } else {
                        // Store GCM ID in sharedpreference
                        SharedPreferences sharedPreferences=getSharedPreferences("GCM",MODE_PRIVATE);
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putString("GCM_ID",regid);
                        editor.commit();
*/


                        checkMerchentLogin();




                    }
               /* } catch (IOException ex) {
                    ex.printStackTrace();
                    Log.e("errr",ex.toString());

                }
                return null;
            }
        }.execute();

    } // end of getRegId
*/
}
