package testPlayerRadio;

import java.util.ArrayList;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class AStar
{

	private SearchFringe map[][];
	private AStarProblem p;
	public AStar(int mapwidth, int mapheight, AStarProblem prob, MapLocation mines[])
	{
		map=new SearchFringe[mapheight][mapwidth];
		p=prob;
		for(int i=0; i<mapheight; i++)
		{
			for(int j=0; j<mapwidth; j++)
			{
				map[i][j]=new SearchFringe();
			}
		}
		for(MapLocation loc:mines)
			map[loc.y][loc.x].mine=1;
	}
	private AStarPoint[] getSquare(point center)
	{
		AStarPoint square[]=new AStarPoint[8];
		if(center.x+1<map.length && center.y+1<map.length)
			square[0]=new AStarPoint(center.x+1, center.y+1,center.x,center.y);
		else
			square[0]=null;
		if(center.x+1<map.length && center.y<map.length)
			square[1]=new AStarPoint(center.x+1, center.y,center.x,center.y);
		else
			square[1]=null;
		if(center.x+1<map.length && center.y-1>=0)
			square[2]=new AStarPoint(center.x+1, center.y-1,center.x,center.y);
		else
			square[2]=null;
		
		if(center.x<map.length && center.y+1<map.length)
			square[3]=new AStarPoint(center.x, center.y+1,center.x,center.y);
		else
			square[3]=null;
		if(center.x<map.length && center.y-1>=0)
			square[4]=new AStarPoint(center.x, center.y-1,center.x,center.y);
		else
			square[4]=null;
		
		if(center.x-1>=0 && center.y-1>=0)
			square[5]=new AStarPoint(center.x-1, center.y-1,center.x,center.y);
		else
			square[5]=null;
		if(center.x-1>=0 && center.y+1<map.length)
			square[6]=new AStarPoint(center.x-1, center.y+1,center.x,center.y);
		else
			square[6]=null;
		if(center.x-1>=0 && center.y<map.length)
			square[7]=new AStarPoint(center.x-1, center.y,center.x,center.y);
		else
			square[7]=null;
		return square;
	}
	public ArrayList<point> search(point start, RobotController rc)
	{
		ArrayList<AStarPoint> toSearch = new ArrayList<AStarPoint>();
		for(point p:getSquare(start))
			toSearch.add(new AStarPoint(p.x,p.y,1+map[p.y][p.x].mine,start.x,start.y,1+map[p.y][p.x].mine+this.p.heuristic(p)));
		map[start.y][start.x]=new SearchFringe(0);
		while(toSearch.size()>0)
		{
			AStarPoint currpt=null;
			int minindex=0;
			while(true)
			{
				int minval=toSearch.get(0).weightandheuristic;
				minindex=0;
				for(int i=1; i<toSearch.size(); i++)
				{
					int test=toSearch.get(i).weightandheuristic;
					if(test<minval)
					{
						minval=test;
						minindex=i;
					}
				}
				currpt=toSearch.get(minindex);
				if(map[currpt.y][currpt.x].weightToDate<currpt.weight)
				{
					toSearch.remove(minindex);
					continue;
				}
				break;
			}
			System.out.println("currpt: (" + currpt.x + "," + currpt.y + ")");
			map[currpt.y][currpt.x].weightToDate=currpt.weight;
			map[currpt.y][currpt.x].path=new ArrayList<point>();
			if(map[currpt.from.y][currpt.from.x].path!=null)
				map[currpt.y][currpt.x].path.addAll(map[currpt.from.y][currpt.from.x].path);
			map[currpt.y][currpt.x].path.add((point)currpt);
			rc.breakpoint();
			if(p.goal((point)(currpt)))
			{
				return map[currpt.y][currpt.x].path;
			}
			for(point p:getSquare(currpt))
			{
				if(p==null)
					continue;
				int newweight=currpt.weight+map[p.y][p.x].mine+1/(Math.abs(p.x-currpt.x)+Math.abs(p.y-currpt.y));
				if(newweight<map[p.y][p.x].weightToDate)
				{
					toSearch.add(new AStarPoint(p.x,p.y,newweight,currpt.x,currpt.y,newweight+this.p.heuristic(p)));
				}
			
			}
			toSearch.remove(minindex);
		}
		return null;
	}
}
