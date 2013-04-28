package testPlayerGroupMind;

import battlecode.common.Direction;

public class AStarPoint extends point
{
	int weight;
	int weightandheuristic;
	Direction fromdir;
	point from;
	public AStarPoint(int x, int y, int w, Direction d, int fx, int fy, int wh)
	{
		super(x, y);
		fromdir=d;
		weight=w;
		weightandheuristic=wh;
		from = new point(fx,fy);
	}
	public AStarPoint(int x, int y, Direction d)
	{
		super(x, y);
		fromdir=d;
		weightandheuristic=weight=999999999;
		from=null;
	}
}
