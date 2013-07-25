/* Author: Corey Crooks
 * Email: ccc35@pitt.edu
 * Purpose: Update the database, retrieves tweets and saves to database
 */

package edu.cs1635.pitt.ccc35.proj4;
import java.util.List;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


public class TweetViewer extends Service {

	// add own key
	public final static String CONSUMER_KEY = "GNZqtiH0HtDouR4RzvuSYg";
	
	public final static String SECRET_KEY = "pItEcthzX3kqaMAbiR0MYy1fGnDyTNIF0xzqvGVxjw";
	// add secret
	private String TAG = "TimelineService";
	
	private SharedPreferences nicePrefs;
	
	private Handler niceHandler;
	
	private DatabaseHelper niceHelper;
	
	private SQLiteDatabase niceDB;
	
	private TimelineUpdater niceUpdater;
	
	private Twitter timelineTwitter;
	
	private static int mins = 5;
	private static final long FETCH_DELAY = mins * (60*1000);
	
	@Override
	public void onCreate() {
		super.onCreate();
	
		nicePrefs = getSharedPreferences("TwitNicePrefs", 0);
	
		niceHelper = new DatabaseHelper(this);
		
		niceDB = niceHelper.getWritableDatabase();
		
		
		String userToken = nicePrefs.getString("user_token", null);
		String userSecret = nicePrefs.getString("user_secret", null);
		
		Configuration twitConf = new ConfigurationBuilder()
			.setOAuthConsumerKey(CONSUMER_KEY)
			.setOAuthConsumerSecret(SECRET_KEY)
			.setOAuthAccessToken(userToken)
			.setOAuthAccessTokenSecret(userSecret)
			.build();
			
		timelineTwitter = new TwitterFactory(twitConf).getInstance();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStart(intent, startId);
	
		niceHandler = new Handler();
	
		niceUpdater = new TimelineUpdater();
	
		niceHandler.post(niceUpdater);
		
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		niceHandler.removeCallbacks(niceUpdater);
		niceDB.close();
	}

	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * TimelineUpdater class implements the runnable interface
	 */
	class TimelineUpdater implements Runnable 
	{
		
		public void run() 
		{
			
			boolean statusChanges = false;
			try 
			{
				
				List<Status> homeTimeline = timelineTwitter.getHomeTimeline();
				
				
				for (Status statusUpdate : homeTimeline) 
				{
					
					ContentValues timelineValues = DatabaseHelper.getValues(statusUpdate);
					niceDB.insertOrThrow("home", null, timelineValues);
						
					statusChanges = true;
				}
			} 
			catch (Exception te) { Log.e(TAG, "Exception: " + te);
			}
		
			if (statusChanges) 
			{
					
				sendBroadcast(new Intent("TWITTER_UPDATES"));
			}
				//delay fetching new updates
			niceHandler.postDelayed(this, FETCH_DELAY);
		}
	}
}