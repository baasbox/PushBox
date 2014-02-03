package com.baasbox.android.pushybox;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.baasbox.android.BaasBox;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.json.JsonObject;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by Andrea Tortorella on 28/01/14.
 */
public class AuthenticateActivity extends ActionBarActivity
    implements View.OnClickListener{
    private final static String TAG = "CODE_TAG";
    private TextView mYourName;
    private EditText mTo;
    private EditText mWhat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_client);
        findViewById(R.id.gplus_sign_in).setOnClickListener(this);
        findViewById(R.id.access).setOnClickListener(this);
        findViewById(R.id.send).setOnClickListener(this);
        mYourName = (TextView)findViewById(R.id.your_name);
        mTo = (EditText)findViewById(R.id.to_whom);
        mWhat = (EditText)findViewById(R.id.message);
    }


    private static String[] getAccountNames(Context context){
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        String[] names = new String[accounts.length];
        for (int i=0;i<names.length;i++){
            names[i]=accounts[i].name;
        }
        return names;
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.access){
            new GetRegistrationId(this).execute();
        }else if (v.getId()==R.id.send){
            sendNewMessage();
        } else {
            new GetGoogleTokenTask(this).execute();
        }
    }

    private void sendNewMessage(){
        String to= mTo.getText().toString();
        if (TextUtils.isEmpty(to)){
            to = BaasUser.current().getName();
        }
        String message = mWhat.getText().toString();
        BaasUser.withUserName(to).send(new JsonObject().putString("message",message),new BaasHandler<Void>() {
            @Override
            public void handle(BaasResult<Void> voidBaasResult) {
                Toast.makeText(AuthenticateActivity.this,"SENT MESSAGE",Toast.LENGTH_LONG).show();
            }
        });
    }
    private static class GetRegistrationId extends AsyncTask<Void,Void,String>{
        private AuthenticateActivity mContext;
        GetRegistrationId(AuthenticateActivity context){
            mContext =context;
        }

        @Override
        protected String doInBackground(Void... params) {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(mContext);
            try {
                String regId = gcm.register(Config.SENDER_ID);
                return regId;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s!=null){
                Log.d(TAG,"Push regid: "+s);
                BaasBox.getDefault().registerPush(s,new BaasHandler<Void>() {
                    @Override
                    public void handle(BaasResult<Void> voidBaasResult) {
                        Log.d(TAG,"registerd push"+voidBaasResult.toString());
                        mContext.mYourName.setText(BaasUser.current().getName());
                    }
                });
            }
        }
    }

    private static class GetGoogleTokenTask extends AsyncTask<Void,Void,String>{
        private Context mContext;

        GetGoogleTokenTask(Context context){
            mContext = context;
        }

        @Override
        protected String doInBackground(Void... params) {
            String[] accounts= getAccountNames(mContext);
            if (accounts.length==0){
                return null;
            } else {
                return getToken(mContext,accounts[0]);
            }
        }

        @Override
        protected void onPostExecute(String s) {

            if (s!=null){
                BaasUser.signupWithProvider(BaasUser.Social.GOOGLE,s, Config.SECRET,
                        new BaasHandler<BaasUser>() {
                    @Override
                    public void handle(BaasResult<BaasUser> res) {
                        //todo handle social signup
                    }
                });
            }
        }
    }


    private static String getToken(Context context,String accountName){
        try {
            String token = GoogleAuthUtil.getToken(context, accountName, Config.AUTH_SCOPE);
            return token;
        } catch (IOException e) {
           Log.d(TAG,e.getMessage());
            throw new RuntimeException(e);
        }  catch (UserRecoverableAuthException e) {
          Log.d(TAG,e.getMessage());
            context.startActivity(e.getIntent());

        } catch (GoogleAuthException e) {
            Log.d(TAG,e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }
}
