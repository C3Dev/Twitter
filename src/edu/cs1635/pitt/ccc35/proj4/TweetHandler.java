/* Author: Corey Crooks
 * Email: ccc35@pitt.edu
 * Purpose: Handles sending tweets, return to timeline
 * 
 */

package edu.cs1635.pitt.ccc35.proj4;


import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class TweetHandler extends Activity implements OnClickListener {

		
	private SharedPreferences tweetPrefs;

	private Twitter tweetTwitter;
	// change
	public final static String CONSUMER_KEY = "GNZqtiH0HtDouR4RzvuSYg";//alter
		
	public final static String SECRET_KEY = "pItEcthzX3kqaMAbiR0MYy1fGnDyTNIF0xzqvGVxjw";//alter
		
	private long idTweet = 0;
		
	private String tweetName = "";
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.tweet_layout);  
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		setupTweet();
	}
	
	
	private void setupTweet() {
    	
        tweetPrefs = getSharedPreferences("TwitNicePrefs", 0);
        
        	
        String userToken = tweetPrefs.getString("user_token", null);
    	String userSecret = tweetPrefs.getString("user_secret", null);
    	
    		
    	Configuration twitConf = new ConfigurationBuilder()
    	    .setOAuthConsumerKey(CONSUMER_KEY)
    	    .setOAuthConsumerSecret(SECRET_KEY)
    	    .setOAuthAccessToken(userToken)
    	    .setOAuthAccessTokenSecret(userSecret)
    	    .build();
    		
    	tweetTwitter = new TwitterFactory(twitConf).getInstance();
    	
    		
        Bundle extras = getIntent().getExtras();
	    if(extras !=null)
	    {
	    	idTweet = extras.getLong("idTweet");
	    	
	    	tweetName = extras.getString("usrTweet");
	    		
	    	EditText theReply = (EditText)findViewById(R.id.tweettext);
	    		
	    	theReply.setText("@"+tweetName+" ");
	    		
	    	theReply.setSelection(theReply.getText().length());

	    }
	    else 
	    {
	    	EditText theReply = (EditText)findViewById(R.id.tweettext);
	    	theReply.setText("");
	    }
        
	    
        LinearLayout tweetClicker = (LinearLayout)findViewById(R.id.homebtn);
    	tweetClicker.setOnClickListener(this);
        
    	Button tweetButton = (Button)findViewById(R.id.dotweet);
    	tweetButton.setOnClickListener(this);
		
	}
	
	
	public void onClick(View v) {
		
		EditText tweetTxt = (EditText)findViewById(R.id.tweettext);
		
		switch(v.getId()) {
    	case R.id.dotweet:
    		
        	String toTweet = tweetTxt.getText().toString();
        	try {
        		
        		if(tweetName.length()>0) {
        			tweetTwitter.updateStatus(new StatusUpdate(toTweet).inReplyToStatusId(idTweet));
        		}
        		
        		else {
        			tweetTwitter.updateStatus(toTweet);
        		}
        			
        		tweetTxt.setText("");
        			
        	}
        	catch(TwitterException te) { Log.e("TweetHandler", te.getMessage()); }
    		break;
    	case R.id.homebtn:
    		
    		tweetTxt.setText("");
    		break;
    	default:
    		break;
    	}
				finish();
	}
	
}
