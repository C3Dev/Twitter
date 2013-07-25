/* Author: Corey Crooks
 * Email: ccc35@pitt.edu
 * Purpose: store updates for timeline
 * 
 */
package edu.cs1635.pitt.ccc35.proj4;



import twitter4j.Status;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;


public class DatabaseHelper extends SQLiteOpenHelper {

		
	private static final int DB_VERSION = 1;
		
	private static final String DB_NAME = "home.db";

	private static final String H_COL = BaseColumns._ID;
		
	private static final String UPDATE = "update_text";
		
	private static final String USRCOL = "user_screen";

	private static final String TIME = "update_time";
		
	private static final String USER_IMG = "user_img";
	
		
	private static final String DB_CREATE = "CREATE TABLE home (" + H_COL + " INTEGER NOT NULL " +
			"PRIMARY KEY, " + UPDATE + " TEXT, " + USRCOL + " TEXT, " +
					TIME + " INTEGER, " + USER_IMG + " TEXT);";

  DatabaseHelper(Context context) {
      super(context, DB_NAME, null, DB_VERSION);
  }

 
  @Override
  public void onCreate(SQLiteDatabase db) {
  	Log.v("DatabaseHelper", "creating db");
      db.execSQL(DB_CREATE);
  }
  
 
  @Override
	public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
  	Log.v("DatabaseHelper", "upgrading db");
		db.execSQL("DROP TABLE IF EXISTS home");
		db.execSQL("VACUUM");
		onCreate(db);
	}

  
  public static ContentValues getValues(Status status) {
  	Log.v("DatabaseHelper", "converting values");
  	
  		
      ContentValues val = new ContentValues();
      try {
      		
	        val.put(H_COL, status.getId());
	        val.put(UPDATE, status.getText());
	        val.put(USRCOL, status.getUser().getScreenName());
	        val.put(TIME, status.getCreatedAt().getTime());
	        val.put(USER_IMG, status.getUser().getProfileImageURL().toString());
      }
      catch(Exception te) { Log.e("DatabaseHelper", te.getMessage()); }
      	
      return val;
    }

}
