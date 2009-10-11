import java.awt.Color;

public class Square {
	Color color;
	int length;
	int posx;
	int posy;
	int row;
	int col;
	double alpha;
	
	Square(Color color, int length, int posx, int posy)
	{
		this.color = color;
		this.length = length;
		this.posx = posx;
		this.posy = posy;
	}
	
	void SetAlpha(double alpha)
	{
		this.alpha = alpha;
	}
	
}
