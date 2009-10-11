import java.util.Vector;

import TUIO.TuioObject;
import TUIO.TuioProcessing;


public final class TuioController {	
	//TUIO stuff
	public static TuioProcessing client;
	public static float cursor_size = 15;
	public static float object_size = 60;
	public static float table_size = 300;
	public static float scale_factor = 1;
	
	public static float objectSize() {
		return object_size*scale_factor;
	}
	
	public static float cursorSize() {
		return cursor_size*scale_factor;
	}
	
	@SuppressWarnings("unchecked")
	public static TuioObject getFeducial(int id) {
		Vector<TuioObject> tuioObjects = client.getTuioObjects();
		
		for(TuioObject obj : tuioObjects) {
			if(obj.getSymbolID() == id)
				return obj;
		}
		
		return null;
	}
	
	public static boolean feducialPresent(int id) {
		return getFeducial(id) != null;
	}
}
