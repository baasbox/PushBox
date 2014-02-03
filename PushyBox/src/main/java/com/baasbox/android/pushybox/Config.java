package com.baasbox.android.pushybox;

import com.baasbox.android.BaasBox;
import com.google.android.gms.common.Scopes;

/**
 * Created by Andrea Tortorella on 28/01/14.
 */
public class Config {
    //setup your configuration here

    public final static BaasBox.Config BAASBOX_CONFIG = null;
    public final static String SECRET="your secret key";
    public final static String AUTH_SCOPE = "oauth2:"+ Scopes.PLUS_PROFILE+" "+Scopes.PLUS_LOGIN;

    public final static String SENDER_ID = "your sender id";

}
