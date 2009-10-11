import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.sql.*;

public class TweetDB extends Thread {
	public static ConcurrentLinkedQueue<Pixi> tweets = new ConcurrentLinkedQueue<Pixi>();
	private Emotion emotion;
	private float speedx;
	private float speedy;
	private static Random random = new Random();
	
	public TweetDB(Emotion emotion)
	{
		this.emotion = emotion;
		this.speedx  = emotion.speedx();
		this.speedy  = emotion.speedy();

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
		List<String> tweets = doSearch();
		for(String t : tweets) { makePixi(t); }
	}

	private void makePixi(String t) {
		Pixi pixi = new Pixi(t, emotion, speedx, speedy);
		TweetFetcher.tweets.add(pixi);
		System.out.println(emotion.toString() + " found message: " + t);
		doSleep(400);
	}
	
	private List<String> doSearch() {		

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
			ms += TweetDB.random.nextInt((int)ms) - (int)ms/2 + 1;
			Thread.sleep(ms);
		} catch (InterruptedException e) {}
	}
}
