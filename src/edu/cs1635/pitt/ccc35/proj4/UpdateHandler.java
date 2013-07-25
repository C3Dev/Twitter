/* Author: Corey Crooks
 * Email: ccc35@pitt.edu
 * Purpose: Update data to the views to appear in timeline
 * 
 */

package edu.cs1635.pitt.ccc35.proj4;


import java.io.InputStream;
import java.net.URL;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateHandler extends SimpleCursorAdapter {

	// reset your auth key
	public final static String CONSUMER_KEY = "GNZqtiH0HtDouR4RzvuSYg";//alter
	//key secret
	public final static String SECRET_KEY = "pItEcthzX3kqaMAbiR0MYy1fGnDyTNIF0xzqvGVxjw";//alter
	
	
	static final String[] from = { "update_text", "user_screen", "update_time", "user_img" };
	
	static final int[] to = { R.id.updateText, R.id.userScreen, R.id.updateTime, R.id.userImg };
	
	private String TAG = "UpdateHandler";

	
	public UpdateHandler(Context context, Cursor c) {
		super(context, R.layout.update_layout, c, from, to);
	}

	
	@Override
	public void bindView(View row, Context context, Cursor cursor) {
		super.bindView(row, context, cursor);

		

		try 
		{
			
			URL profileURL = new URL(cursor.getString(cursor.getColumnIndex("user_img")));
			
			ImageView profPic = (ImageView)row.findViewById(R.id.userImg);
			profPic.setImageDrawable(Drawable.createFromStream((InputStream)profileURL.getContent(), ""));
		}
		catch(Exception te) { Log.e(TAG, te.getMessage()); }

		
		long createdAt = cursor.getLong(cursor.getColumnIndex("update_time"));
		
		TextView textCreatedAt = (TextView) row.findViewById(R.id.updateTime);
		
		textCreatedAt.setText(DateUtils.getRelativeTimeSpanString(createdAt)+" ");

		long statusID = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
		
		String statusName = cursor.getString(cursor.getColumnIndex("user_screen"));
		
		Data tweetData = new Data(statusID, statusName);

		
		row.findViewById(R.id.retweet).setTag(tweetData);
		row.findViewById(R.id.reply).setTag(tweetData);
		
		row.findViewById(R.id.retweet).setOnClickListener(tweetListener);
		row.findViewById(R.id.reply).setOnClickListener(tweetListener);
		
		row.findViewById(R.id.userScreen).setOnClickListener(tweetListener);
	}

	
	private OnClickListener tweetListener = new OnClickListener() {
		
		public void onClick(View v) {
			
			switch(v.getId()) {
			
			case R.id.reply:
				
				Intent replyIntent = new Intent(v.getContext(), TweetHandler.class);
				
				Data theData = (Data)v.getTag();
				
				replyIntent.putExtra("idTweet", theData.getID());
				
				replyIntent.putExtra("usrTweet", theData.getUser());
				
				v.getContext().startActivity(replyIntent);
				break;
				
			case R.id.retweet:
				
				Context appCont = v.getContext();
				
				SharedPreferences tweetPrefs = appCont.getSharedPreferences("TwitNicePrefs", 0);
				String userToken = tweetPrefs.getString("user_token", null);
				String userSecret = tweetPrefs.getString("user_secret", null);
				
				Configuration twitConf = new ConfigurationBuilder()
				.setOAuthConsumerKey(CONSUMER_KEY)
				.setOAuthConsumerSecret(SECRET_KEY)
				.setOAuthAccessToken(userToken)
				.setOAuthAccessTokenSecret(userSecret)
				.build();
				
				Twitter retweetTwitter = new TwitterFactory(twitConf).getInstance();
				
				Data tweetData = (Data)v.getTag();
				try 
				{
					
					retweetTwitter.retweetStatus(tweetData.getID());
					
					CharSequence text = "Retweeted!";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(appCont, text, duration);
					toast.show();
				}
				catch(TwitterException te) {Log.e(TAG, te.getMessage());}
				break;
				
			case R.id.userScreen:
				
				TextView tv = (TextView)v.findViewById(R.id.userScreen);
				String userScreenName = tv.getText().toString();
				
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
						Uri.parse("http://twitter.com/"+userScreenName));
				v.getContext().startActivity(browserIntent);
				break;
			default:
				break;
			}
		}
	};
}
