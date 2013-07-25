/* Author: Corey Crooks
 * Email: ccc35@pitt.edu
 * Purpose:use list view to provide adapter for tweets activities
 * 
 */

package edu.cs1635.pitt.ccc35.proj4;


import twitter4j.ProfileImage;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

public class TweetAdapter extends Activity implements OnClickListener {
	
	//change 
	public final static String CONSUMER_KEY = "GNZqtiH0HtDouR4RzvuSYg";
	//change 
	public final static String SECRET_KEY = "pItEcthzX3kqaMAbiR0MYy1fGnDyTNIF0xzqvGVxjw";//alter
	//url for app return 
	public final static String URL = "tnice-android:///";
	
	private String TAG = "TweetAdapter";
	
	
	private Twitter myTwitter;
	
	private RequestToken getToken;
	private SharedPreferences nicePrefs;
	
	private ListView homeTimeline;
	
	private DatabaseHelper timelineHelper;
	
	private SQLiteDatabase timelineDB;
	
	private Cursor timelineCursor;

	private UpdateHandler timelineAdapter;

	private BroadcastReceiver niceStatusReceiver;
	
	//use for size of profile picture
	ProfileImage.ImageSize imageSize = ProfileImage.NORMAL;
	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        
        nicePrefs = getSharedPreferences("TwitNicePrefs", 0);
       
        if(nicePrefs.getString("user_token", null)==null) {
        		
        	setContentView(R.layout.activity_tweet_adapter);
        		
            myTwitter = new TwitterFactory().getInstance();
            	
            myTwitter.setOAuthConsumer(CONSUMER_KEY, SECRET_KEY);
            	// try and catch for token auth
            try 
            {
            		
            	getToken = myTwitter.getOAuthRequestToken(URL);
            }
            catch(TwitterException te) { Log.e(TAG, "TE "+te.getMessage()); }
        	
        	Button signIn = (Button)findViewById(R.id.signin);
        	signIn.setOnClickListener(this);
        }
        else 
        {
        	
        	setupTimeline();
        }

    }
    
    
    public void onClick(View v) {
    	
    	switch(v.getId()) {
    	
    	case R.id.signin:
    			
    		String authURL = getToken.getAuthenticationURL();
    		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authURL)));
    		break;
    		
    	case R.id.tweetbtn:
    			
    		startActivity(new Intent(this, TweetHandler.class));
    		break;
    	default:
    		break;
    	}
    }
    
  
    @Override
    protected void onNewIntent(Intent intent) {
    	
        super.onNewIntent(intent);
       
    	Uri twitURI = intent.getData();
    	
    	if(twitURI!=null && twitURI.toString().startsWith(URL)) 
    	{
    	
	    	String oaVerifier = twitURI.getQueryParameter("oauth_verifier");
	    
	   	    try
	   	    {
	    	        
	   	    	AccessToken accToken = myTwitter.getOAuthAccessToken(getToken, oaVerifier);
	   	    		
	   	        nicePrefs.edit()
	   	            .putString("user_token", accToken.getToken())
	   	            .putString("user_secret", accToken.getTokenSecret())
	   	            .commit();
	    	        
	   	        setupTimeline();
	    	        
	   	    }
	   	    catch (TwitterException te)
	   	    { Log.e(TAG, "Failed to get access token: "+te.getMessage()); }
    	}
    }
    
    private void setupTimeline() {
    	
    	setContentView(R.layout.tweet_view_layout);
    		
    	LinearLayout tweetClicker = (LinearLayout)findViewById(R.id.tweetbtn);
    	tweetClicker.setOnClickListener(this);
   
    	try 
    	{
    			
    		homeTimeline = (ListView)findViewById(R.id.homeList);
    			
    		timelineHelper = new DatabaseHelper(this);
    	
    		timelineDB = timelineHelper.getReadableDatabase();
    		
    	    
    		timelineCursor = timelineDB.query("home", null, null, null, null, null, "update_time DESC");
    		
    		startManagingCursor(timelineCursor);
    	   
    		timelineAdapter = new UpdateHandler(this, timelineCursor);
    		
    		homeTimeline.setAdapter(timelineAdapter);
    		
    		niceStatusReceiver = new TwitterUpdateReceiver();
    	
    	    registerReceiver(niceStatusReceiver, new IntentFilter("TWITTER_UPDATES"));
    	    
			
    		this.getApplicationContext().startService(new Intent(this.getApplicationContext(), TweetViewer.class));
    	}
    	catch(Exception te) { Log.e(TAG, "Failed to fetch timeline: "+te.getMessage()); }
    }

	
	class TwitterUpdateReceiver extends BroadcastReceiver 
	{
		@Override
		public void onReceive(Context context, Intent intent) {
		
			int rowLimit = 100;
			if(DatabaseUtils.queryNumEntries(timelineDB, "home")>rowLimit) {
				String deleteQuery = "DELETE FROM home WHERE "+BaseColumns._ID+" NOT IN " +
						"(SELECT "+BaseColumns._ID+" FROM home ORDER BY "+"update_time DESC " +
								"limit "+rowLimit+")";	
				timelineDB.execSQL(deleteQuery);
			}		
			
			timelineCursor = timelineDB.query("home", null, null, null, null, null, "update_time DESC");
			startManagingCursor(timelineCursor);
			timelineAdapter = new UpdateHandler(context, timelineCursor);
			homeTimeline.setAdapter(timelineAdapter);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try 
		{
			
			stopService(new Intent(this, TweetViewer.class));
		
			unregisterReceiver(niceStatusReceiver);
		
			timelineDB.close();
		}
		catch(Exception se) { Log.e(TAG, "unable to stop service or receiver"); }
	}
}
