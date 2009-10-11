import twitter4j.Query;
import twitter4j.Tweet;
import java.awt.Color;


public enum Emotion {
	LOVE  ("\"I love\" -\"I hate\"", 		  "love.png", 									 2, 2, new Color(255, 31, 122)), 
	HAPPY ("\"I'm glad\" OR \"I'm happy\" -\"sad\"", "happy.png", 							 3, 3, new Color(248, 222, 97)),
	WISH ("\"I wish\" OR \"I hope\"", "wish.png", 											 2, 2, new Color(199, 29, 255)),
	CONFUSED ("\"I don't know\" OR \"I dunno\" OR \"not sure\" OR confused", "confused.png", 4, 4, new Color(139, 237, 12)),
	SAD ("\"I'm sad\" OR \"I'm disappointed\" -\"happy\"", "sad.png", 						 1, 1, new Color(19, 168, 234)),
	ANGRY ("\"I hate\" OR angry -\"I love\"", "angry.png", 									 5, 5, new Color(237, 12, 29));
	
	private String image;
	private Query query;
	private float speedx;
	private float speedy;
	private Color color;
	
	private Emotion(String query, String image, float speedx, float speedy, Color color) {
		this.image = image;
		this.query = new Query(query + " -\"http\" -\"www\"");
		this.speedx = speedx;
		this.speedy = speedy;
		this.color = color;
	}
	
	public Query query() {
		return query;
	}
	
	public float speedx() {
		return speedx/3;
	}
	
	public float speedy() {
		return speedy/3;
	}
	
	public Color color() {
		return color;
	}
	
	public boolean isPresent() {
		return TuioController.feducialPresent(this.ordinal());
	}
	
	public String image() {
		return image;
	}
}