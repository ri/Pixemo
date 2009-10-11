import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TweetFetcher extends Thread {
	public static ConcurrentLinkedQueue<Pixi> tweets = new ConcurrentLinkedQueue<Pixi>();
	private Query query;
	private Twitter twitter;
	private long sinceID = 0;
	private Emotion emotion;
	private float speedx;
	private float speedy;
	private static Random random = new Random();
	
	public TweetFetcher(Emotion emotion)
	{
		twitter      = new Twitter();
		Query query  = emotion.query();	
		this.query   = query;
		this.emotion = emotion;
		this.speedx  = emotion.speedx();
		this.speedy  = emotion.speedy();

		query.setPage(20);
		query.setRpp(1);
		
		setName(emotion.toString() + " fetcher");
	}
	
	public void run() {
		while(true) {
			if(emotion.isPresent())
				searchAndAddToQueue();
			doSleep(4000);
		}
	}
	
	public void searchAndAddToQueue()
	{
		if (Pixemo.USEDB)
		{
			List<String> tweets = dbSearch();
			for(String t : tweets) { makePixi(t); }
		}
		else
		{
			List<Tweet> tweets = doSearch();
			for(Tweet t : tweets) { makePixi(t.getText()); }
		}
	}

	private void makePixi(String t) {
		Pixi pixi = new Pixi(t, emotion, speedx, speedy);
		TweetFetcher.tweets.add(pixi);
		System.out.println(emotion.toString() + " found message: " + t);
		doSleep(400);
	}
	
	private List<Tweet> doSearch() {		
		try {
			query.setSinceId(sinceID);
			
			QueryResult results = twitter.search(query);
			List<Tweet> tweets	= results.getTweets();
			
			if (tweets.size() > 0) 
				sinceID = tweets.get(tweets.size()-1).getId();
			return results.getTweets();
		} catch (TwitterException e) {
			return new ArrayList<Tweet>();
		}
	}
	
	private List<String> dbSearch() {		

		Connection conn = null;
    	List<String> tweets = new ArrayList<String>();

        try
        {
            String userName = "root";
            String password = "";
            String url = "jdbc:mysql://localhost/Pixemo";
            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            conn = DriverManager.getConnection (url, userName, password);
            System.out.println ("Database connection established");
            
            
            Statement s = conn.createStatement ();
            s.executeQuery ("SELECT id, text, emotion FROM Tweets WHERE emotion ='" + emotion + "' " +
            		"ORDER BY RAND() LIMIT 1");
            ResultSet rs = s.getResultSet ();
            int count = 0;
            while (rs.next ())
            {
                String text = rs.getString ("text");
                System.out.println (
                        "text = " + text);
                tweets.add(text);
                ++count;
            }
            rs.close ();
            s.close ();
            System.out.println (count + " rows were retrieved");

        }
        catch (Exception e)
        {
            System.err.println ("Cannot connect to database server");
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    conn.close ();
                    System.out.println ("Database connection terminated");
                }
                catch (Exception e) { /* ignore close errors */ }
            }
        }
        return tweets;
	}
	private void doSleep(long ms) {
		try {
			ms += TweetFetcher.random.nextInt((int)ms) - (int)ms/2 + 1;
			Thread.sleep(ms);
		} catch (InterruptedException e) {}
	}
}
