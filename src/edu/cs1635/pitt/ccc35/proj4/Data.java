/***
 * Author: Corey Crooks
 * Email: ccc35@pitt.edu
 * Purpose: use of single tweet, store the tweet id and user 
 */
package edu.cs1635.pitt.ccc35.proj4;

public class Data {

		
	private long idTweet;
		
	private String usrTweet;
	
	public Data(long ID, String sName) {
			
		idTweet=ID;
		usrTweet=sName;
	}
	
	
	public long getID() {return idTweet;}
	
	public String getUser() {return usrTweet;}
}
