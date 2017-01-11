package com.app.myplaces.settings;

public abstract interface ISettingConstants {

	public static final String KEY_RADIUS = "radius";
	public static final String KEY_METRIC = "metric";
	public static final String KEY_PIORITY = "piority";
	public static final String KEY_TRAVEL_MODE = "travelMode";
	public static final String KEY_DONT_SHOW = "dontShow";
	public static final String KEY_INIT_PROVIDER = "key_init_provider";
	
	public static final String KEY_ACCESS_TOKEN = "access_token";
	public static final String KEY_ACCESS_EXPRIRES = "access_expires";

	public static final int REQUEST_CODE_SELECT_AD = 3500;
	public static final int RESULT_SELECT_AD = 350;

	public static final String APP_ID = "130970527083995";
	public static final String[] PERMISSIONS = new String[] { "publish_stream","user_photos","read_stream","offline_access"};
	
	public static final String KEY_FB_NAME ="fb_name";
	public static final String KEY_FB_EMAIL ="fb_email";
	public static final String KEY_FB_ID ="fb_id";
	public static final String KEY_IS_FB_ACCOUNT ="fb_account";
	public static final String KEY_TWITTER_SECRET = "tw_oauth_token_secret";
	public static final String KEY_TWITTER_TOKEN = "tw_oauth_token";
	public static final String KEY_TWITTER_SCREEN_NAME = "screen_name";
	public static final String CONSUMER_KEY = "E7AeFKfV1kjuxeOjk2A8qA";
	public static final String CONSUMER_SECRET = "QriRwK7IhzaCuvnLqDpBEPs3V27gyhqW44Cl1iPk0";
	public static final String KEY_REGISTRATION_ID = "regId";
	
	public static final String TWITPIC_KEY = "315ef4720622e11d25c259fef843e840";

	public static final String CALLBACK_URL = "oauth://t4jsample";
	public static final String IEXTRA_AUTH_URL = "auth_url";
	public static final String IEXTRA_OAUTH_VERIFIER = "oauth_verifier";
	public static final String IEXTRA_OAUTH_TOKEN = "oauth_token";
	
	public static final String TWITTER_REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
	public static final String TWITTER_ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
	public static final String TWITTER_AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";

}
