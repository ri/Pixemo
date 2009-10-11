import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import twitter4j.Tweet;
import TUIO.TuioCursor;


public class Pixi {
	public float posx;
	public float posy;
	public float speedx;
	public float speedy;
	
	public TuioCursor cursor;
	public Emotion emotion;
	public String keyword;
	public String tweet;
	public float age;
	public int attachedIndex = -1;
	public Ripple ripple;
	public List<Pixi> attached = new ArrayList<Pixi>();
	public Pixi attachedTo;
	public boolean isAttached = false;
	private Random rand = new Random();
	public int offset;
	public boolean showText = true;
	public Square[][] overlay = new Square[7][7];
	
	public Pixi(String tweet, Emotion emotion, float speedx, float speedy) {
		this.tweet = tweet;
		this.emotion = emotion;
		this.speedx = speedx;
		this.speedy = speedy;
		this.offset = rand.nextInt(5);

		changeLocation();
		changeDirection();
	}

	private void changeLocation() {
		posx = rand.nextInt(720) + 80;
		posy = rand.nextInt(550) + 50;
	}

	Pixi(String tweet, float posx, float posy, float speedx, float speedy,
			Emotion emotion) {
		this(tweet, emotion, speedx, speedy);

		this.posx = posx;
		this.posy = posy;
		this.speedx = speedx;
		this.speedy = speedy;
		
	}


	public void move(int posx, int posy) {
		this.posx = posx;
		this.posy = posy;
	}
	

	public void setAge(float age) {
		this.age = age;
	}

	public void changeDirection() {
		changeDirection(4 * randomDirection(), 2 * randomDirection());
	}

	public int randomDirection() {
		int speed = 1;
		if (emotion == Emotion.ANGRY)
			speed = 2; // angry's go twice as fast

		return speed * (rand.nextInt() % 2 == 0 ? 1 : -1);
	}

	public void changeDirection(float speedx, float speedy) {
		this.speedx = speedx;
		this.speedy = speedy;
	}
	
	public String image() {
		return emotion.image();
	}

	// false if no spaces left
	public boolean attachPixi(Pixi pixi) {
		boolean attachSuccess = false;
		if (attached.size() < 4) {
			attached.add(pixi);
			pixi.attachedIndex = attached.size() - 1;
			attachSuccess = true;
		}
		return attachSuccess;
	}

	public void followPixi() {

		if (attachedTo != null) {
			float xDiff = Math.abs(this.posx - attachedTo.posx);
			float yDiff = Math.abs(this.posy - attachedTo.posy);
			if (xDiff > 10 || yDiff > 10) {
				speedx = (attachedTo.posx - this.posx) / 20 + offset;
				speedy = (attachedTo.posy - this.posy) / 20 + offset;
			}
			else
			{
				speedx = this.emotion.speedx() + rand.nextInt(5);
				speedy = this.emotion.speedy() + rand.nextInt(5);
			}
		}
	}
	
	public void SetCursor(TuioCursor cursor)
	{
		this.cursor = cursor;
	}
	
	public void endDrag()
	{
		this.cursor = null;
	}

	public boolean shouldAttach() {
		return emotion == Emotion.LOVE;
	}

}
