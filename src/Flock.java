import java.util.*;

public class Flock {
	private List<Pixi> pixies = new ArrayList<Pixi>();
	private float speedx, speedy;
	public String emotion;

	Flock(String emotion, Pixi firstPixi)
	{
		this.emotion = emotion;
		pixies.add(0, firstPixi);
	}

	public void addPixi(Pixi pixi)
	{
		pixies.add(pixi);
	}
}
