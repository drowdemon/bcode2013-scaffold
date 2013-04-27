package testPlayerRadio;

public class AStarProblem
{

	private int gx;
	private int gy;
	private boolean dirx;
	private boolean diry;
	public AStarProblem(int gx, int gy, boolean dirx, boolean diry)
	{
		this.gx = gx;
		this.gy = gy;
		this.dirx = dirx;
		this.diry = diry;
	}
	boolean goal(point p)
	{
		boolean goalx = false,goaly=false;
		if((p.x-gx>=0 && dirx==true) || (p.x-gx<=0 && dirx==false))
			goalx=true;
		if((p.y-gy>=0 && diry==true) || (p.y-gy<=0 && diry==false))
			goaly=true;
		return (goaly && goalx); //This disgusts me. A lot.
	}
	int heuristic(point p) //for now, just Euclidean distance, with some weighting
	{
		int dx=p.x-gx;
		if((dx>=0 && dirx==true) || (dx<=0 && dirx==false))
			dx=0;
		float dy=p.y-gy;
		if((dy>=0 && diry==true) || (dy<=0 && diry==false))
			dy=0;
		return (int)(Math.hypot(dx, dy))/2;
	}
}
