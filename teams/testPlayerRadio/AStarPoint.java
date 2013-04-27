package testPlayerRadio;

public class AStarPoint extends point
{
	int weight;
	int weightandheuristic;
	point from;
	public AStarPoint(int x, int y, int w, int fx, int fy, int wh)
	{
		super(x, y);
		from=new point(fx,fy);
		weight=w;
		weightandheuristic=wh;
	}
	public AStarPoint(int x, int y, int fx, int fy)
	{
		super(x, y);
		from=new point(fx,fy);
		weightandheuristic=weight=999999999;
	}
}
