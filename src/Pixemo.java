import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import TUIO.TuioCursor;
import TUIO.TuioObject;
import TUIO.TuioPoint;
import TUIO.TuioProcessing;
import TUIO.TuioTime;

@SuppressWarnings("serial")
public class Pixemo extends PApplet {
	public static boolean USEDB = true; 
	private Color[] angrycolors = { new Color(255, 92, 92),
			new Color(234, 66, 83), new Color(209, 36, 61),
			new Color(255, 74, 100) };

	private PFont applegothic;
	private String colorMode = "default";

	private Color[] defaultcolors = { new Color(255, 122, 122),
			new Color(255, 173, 122), new Color(255, 222, 122),
			new Color(160, 255, 122), new Color(122, 191, 255) };
	private boolean dragging = false;
	private Hashtable<Emotion, TweetFetcher> fetchers = new Hashtable<Emotion, TweetFetcher>(
			Emotion.values().length);

	private List<Flock> flocks = new ArrayList<Flock>();
	private List<Pixi> pixies = new ArrayList<Pixi>();
	private int squareSize = 25;
	private int pixiSize = 30;
	private Pixi selected;
	private int windowX = 1300;
	private int windowY = 800;

	private Square[][] grid = new Square[windowX / squareSize][windowY
			/ squareSize];
	private Color[] lovecolors = { new Color(255, 159, 207),
			new Color(255, 199, 230), new Color(255, 176, 206),
			new Color(255, 102, 164) };

	public static void main(String args[]) {
		System.out.println("Fullscreen?");
		PApplet.main(new String[] { "--present", "--hide-stop",
				"--location=0,0", "Pixemo" });
	}

	public void init() {
		if (frame != null) {
			frame.removeNotify();// make the frame not displayable
			frame.setResizable(false);
			frame.setUndecorated(true);
			println("frame is at " + frame.getLocation());
			frame.addNotify();
		}
		super.init();
	}

	// called when a cursor is added to the scene
	public void addTuioCursor(TuioCursor tcur) {
		println("add cursor " + tcur.getCursorID() + " (" + tcur.getSessionID()
				+ ") " + tcur.getX() + " " + tcur.getY());
	}

	// called when an object is added to the scene
	public void addTuioObject(TuioObject tobj) {
		println("add object " + tobj.getSymbolID() + " (" + tobj.getSessionID()
				+ ") " + tobj.getX() + " " + tobj.getY() + " "
				+ tobj.getAngle());
	}

	private void agePixi(Pixi pixi) {
		System.out.println(Float.toString(millis()) + ","
				+ Float.toString(pixi.age));
		if ((millis() - pixi.age) > 10000) {
			pixi.showText = false;
		}
		if ((millis() - pixi.age) > 100000) {
			if (pixi.attachedTo != null) {
				pixi.attachedTo.attached.remove(pixi);
			}

			pixies.remove(pixi);

			for (Pixi p : pixi.attached) {
				p.attachedTo = null;
				homePixi(p); // attach pixi to a new pixi
			}

		}
	}

	public void draw() {
		setupPixies();
		drawPixies();

		drawTuioObjects();
		drawTuioVectors();
	}

	private void drawBackground() {
		int size;
		synchronized (pixies) {
			size = pixies.size();
		}

		if (size > 0) {
			int loveCount = 0;
			int hateCount = 0;

//			synchronized (pixies) {
//				for (Pixi pixi : pixies) {
//					switch (pixi.emotion) {
//					case LOVE:
//						loveCount++;
//					case ANGRY:
//						hateCount++;
//					}
//				}
//			}

			if (loveCount > hateCount && !(colorMode == "love")) {
				setupBackground(lovecolors);
				colorMode = "love";
			} else if (hateCount > loveCount && !(colorMode == "hate")) {
				setupBackground(angrycolors);
				colorMode = "hate";
			}
		} else if (!(colorMode == "default")) {
			setupBackground(defaultcolors);
			colorMode = "default";
		}

		for (Square[] squares : grid) {
			for (Square square : squares) {
//				stroke(255, 255, 255);
//				strokeWeight((float) 0);
				noStroke();
				rect(square.posx, square.posy, squareSize, squareSize);
				fill(square.color.getRed(), square.color.getGreen(),
						square.color.getBlue());
			}
		}

	}

	private void drawPixi(Pixi pixi) {
		// String pixiData = Float.toString(pixi.speedx) +
		// Float.toString(pixi.speedy);
		if (pixi.cursor != null) {
			checkCursor(pixi);
		}
		pixi.followPixi();

		if (pixi.showText) {
			showText(pixi);
		}
		PImage pixiImage = loadImage(pixi.image());
		// edge detection
		// if (pixi.isAttached)
		// {
		// pixi.posx = windowX-pixi.attachedTo.posx + 20;
		// pixi.posy = windowY-pixi.attachedTo.posy + 20;
		// }
		// else
		// {
		if ((pixi.posx + pixi.speedx / 2) > windowX - pixiSize
				|| (pixi.posx + pixi.speedx / 2) < 0) {
			pixi.speedx *= -1;
			// pixi.posx += pixi.speedx/2;
			// pixi.posy += pixi.speedy/2;
		}
		if ((pixi.posy + pixi.speedy / 2) > windowY - pixiSize
				|| (pixi.posy + pixi.speedy / 2) < 0) {
			pixi.speedy *= -1;
			// pixi.posx += pixi.speedx/2;
			// pixi.posy += pixi.speedy/2;
		}

		pixi.posx += pixi.speedx / 2;
		pixi.posy += pixi.speedy / 2;
		// }

		image(pixiImage, pixi.posx, pixi.posy);

	}

	public void drawOverlay(Pixi pixi) {
		// returns start point of square
		double centerx = Math.floor(pixi.posx / 25);
		double centery = Math.floor(pixi.posy / 25);
		double rootx = centerx - 3;
		double rooty = centery - 3;

		for (int x = 0; x < pixi.overlay.length; x++) {
			for (int y = 0; y < pixi.overlay[x].length; y++) {
				Square square = pixi.overlay[x][y];
				pixi.overlay[x][y] = new Square(pixi.emotion.color(), 25,
						(int) ((rootx + x) * 25), (int) ((rooty + y) * 25));
				if (((x == 1 || x == 5) && y > 0 && y < 6)
						|| ((y == 1 || y == 5) && x > 0 && x < 6)) {
					pixi.overlay[x][y].SetAlpha(120);
				} else if (x == 0 || y == 0 || x == 6 || y == 6) {
					pixi.overlay[x][y].SetAlpha(50);
				} else if (((x == 2 || x == 4) && y > 1 && y < 5)
						|| ((y == 2 || y == 4) && x > 1 && x < 5)) {
					pixi.overlay[x][y].SetAlpha(190);
				} else {
					pixi.overlay[x][y].SetAlpha(255);
				}
//				stroke(255, 255, 255);
//				strokeWeight((float) 0);
				noStroke();

				rect(pixi.overlay[x][y].posx, pixi.overlay[x][y].posy,
						squareSize, squareSize);
				fill(pixi.overlay[x][y].color.getRed(),
						pixi.overlay[x][y].color.getGreen(),
						pixi.overlay[x][y].color.getBlue(),
						(float) pixi.overlay[x][y].alpha);
			}
		}

	}

	private void showText(Pixi pixi) {
		PImage speech = loadImage("speechy.png");
		image(speech, pixi.posx - 130, pixi.posy - 100);
		fill(255, 255, 255);
		text(pixi.tweet, pixi.posx - 125, pixi.posy - 90, 190, 80);
		fill(255, 255, 255);
	}

	private void checkCursor(Pixi pixi) {
		int cursorX = pixi.cursor.getScreenX(windowX);
		int cursorY = pixi.cursor.getScreenY(windowY);

		int state = pixi.cursor.getTuioState();
		if (state == 4) {
			pixi.endDrag();
		} else {
			pixi.move((int) cursorX, (int) cursorY);
		}
	}

	private void drawPixies() {
		drawBackground();
		Pixi[] pixies = new Pixi[this.pixies.size()];
		for (Pixi pixi : this.pixies.toArray(pixies)) {
			drawOverlay(pixi);
		}

		for (Pixi pixi : this.pixies.toArray(pixies)) {
			drawPixi(pixi);
			agePixi(pixi);
		}
	}

	@SuppressWarnings("unchecked")
	private void drawTuioObjects() {
		Vector<TuioObject> tuioObjectList = TuioController.client
				.getTuioObjects();
		for (int i = 0; i < tuioObjectList.size(); i++) {
			TuioObject tobj = (TuioObject) tuioObjectList.elementAt(i);
			stroke(0);
			fill(0);
			pushMatrix();
			translate(tobj.getScreenX(width), tobj.getScreenY(height));
			rotate(tobj.getAngle());
			rect(-TuioController.objectSize() / 2,
					-TuioController.objectSize() / 2, TuioController
							.objectSize(), TuioController.objectSize());
			popMatrix();
			fill(255);
			text("" + tobj.getSymbolID(), tobj.getScreenX(width), tobj
					.getScreenY(height));

		}
	}

	private void drawTuioVectors() {
		Vector tuioCursorList = TuioController.client.getTuioCursors();
		for (int i = 0; i < tuioCursorList.size(); i++) {
			TuioCursor tcur = (TuioCursor) tuioCursorList.elementAt(i);
			Vector pointList = tcur.getPath();

			if (tcur.getTuioState() == 0) {
				cursorPixi(tcur);
			}

			if (pointList.size() > 0) {
				stroke(0, 0, 255);
				TuioPoint start_point = (TuioPoint) pointList.firstElement();
				;
				for (int j = 0; j < pointList.size(); j++) {
					TuioPoint end_point = (TuioPoint) pointList.elementAt(j);
					line(start_point.getScreenX(width), start_point
							.getScreenY(height), end_point.getScreenX(width),
							end_point.getScreenY(height));
					start_point = end_point;
				}

				stroke(192, 192, 192);
				fill(192, 192, 192);
				ellipse(tcur.getScreenX(width), tcur.getScreenY(height),
						TuioController.cursorSize(), TuioController
								.cursorSize());
				fill(0);
				text("" + tcur.getCursorID(), tcur.getScreenX(width) - 5, tcur
						.getScreenY(height) + 5);
			}
		}
	}

	private void cursorPixi(TuioCursor cursor) {
		long startTime = cursor.getStartTime().getMicroseconds();
		int cursorX = cursor.getScreenX(windowX);
		int cursorY = cursor.getScreenY(windowY);

		for (Pixi pixi : pixies) {
			if (cursorX > pixi.posx - 25 && cursorX < pixi.posx + 25
					&& cursorY > pixi.posy - 28 && cursorY < pixi.posy + 28) {
				pixi.SetCursor(cursor);
				break;

			}
		}

	}

	private void homePixi(Pixi pixi) {
		if (pixies.size() > 1) {
			Pixi attachTo;

			for (Pixi apixi : pixies) {
				if (apixi.attachPixi(pixi)) {
					attachTo = apixi;
					pixi.attachedTo = attachTo;
					break;

				}
			}
		}
	}

	public void mouseDragged() {
		if (selected != null && dragging) {
			selected.move(mouseX, mouseY);
			background(51);
			drawPixies();
		}
	}

	public void mousePressed() {
		for (Pixi pixi : pixies) {
			if (mouseX > pixi.posx - 25 && mouseX < pixi.posx + 25
					&& mouseY > pixi.posy - 28 && mouseY < pixi.posy + 28) {
				selected = pixi;
			}
		}
		dragging = true;
	}

	public void mouseReleased() {
		dragging = false;
		selected = null;
	}

	// called after each message bundle
	// representing the end of an image frame
	public void refresh(TuioTime bundleTime) {
		redraw();
	}

	// these callback methods are called whenever a TUIO event occurs

	// called when a cursor is removed from the scene
	public void removeTuioCursor(TuioCursor tcur) {
		println("remove cursor " + tcur.getCursorID() + " ("
				+ tcur.getSessionID() + ")");
	}

	// called when an object is removed from the scene
	public void removeTuioObject(TuioObject tobj) {
		println("remove object " + tobj.getSymbolID() + " ("
				+ tobj.getSessionID() + ")");
	}
	
	int WIDTH, HEIGHT;

	public void setup() {
		frame.setLocation(0,0);		
      GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice devices[] = environment.getScreenDevices();

	
		WIDTH =devices[0].getDisplayMode().getWidth();
        HEIGHT= devices[0].getDisplayMode().getHeight();


		size(WIDTH, HEIGHT);
        
		background(0);
		applegothic = loadFont("AppleGothic-12.vlw");
		textFont(applegothic, 12);
		setupBackground(defaultcolors);
		drawBackground();
		// TUIO stuff
		loop();
		frameRate(30);

		TuioController.scale_factor = height / TuioController.table_size;
		TuioController.client = new TuioProcessing(this);

		// Create the tweet fetchers for each emotion:
		for (Emotion e : Emotion.values()) {
			TweetFetcher tf = new TweetFetcher(e);
			tf.start(); // start the thread

			// Add the fetchers to the fetchers list with key based on emotion
			// so e.g.
			// fetchers.get(Emotion.ANGER) // will return Anger fetcher
			fetchers.put(e, tf);
		}
	}

	private void setupBackground(Color[] colors) {
		Random rand = new Random();

		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				int randNum = rand.nextInt(colors.length);
				grid[i][j] = new Square(colors[randNum], 25, i * 25, j * 25);

			}
		}
	}

	private void setupPixies() {
		Pixi p;

		while ((p = TweetFetcher.tweets.poll()) != null) {
			pixies.add(p);
			p.setAge(millis());
			if (p.shouldAttach())
				homePixi(p);
		}
	}

	// called when a cursor is moved
	public void updateTuioCursor(TuioCursor tcur) {
		println("update cursor " + tcur.getCursorID() + " ("
				+ tcur.getSessionID() + ") " + tcur.getX() + " " + tcur.getY()
				+ " " + tcur.getMotionSpeed() + " " + tcur.getMotionAccel());
	}

	// called when an object is moved
	public void updateTuioObject(TuioObject tobj) {
		// println("update object " + tobj.getSymbolID() + " ("
		// + tobj.getSessionID() + ") " + tobj.getX() + " " + tobj.getY()
		// + " " + tobj.getAngle() + " " + tobj.getMotionSpeed() + " "
		// + tobj.getRotationSpeed() + " " + tobj.getMotionAccel() + " "
		// + tobj.getRotationAccel());
	}
}
