package testPlayerPathing;

import java.util.ArrayList;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

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
			map[loc.y][loc.x].mine=3;
	}
	private AStarPoint[] getSquare(point center)
	{
		AStarPoint square[]=new AStarPoint[8];
		square[0]=getpt(center,new point(center.x+1,center.y+1));
		if(square[0]!=null)
			square[0].fromdir=Direction.SOUTH_EAST;
		square[1]=getpt(center,new point(center.x+1,center.y));
		if(square[1]!=null)
			square[1].fromdir=Direction.EAST;
		square[2]=getpt(center,new point(center.x+1,center.y-1));
		if(square[2]!=null)
			square[2].fromdir=Direction.NORTH_EAST;
		
		square[3]=getpt(center,new point(center.x, center.y+1));
		if(square[3]!=null)
			square[3].fromdir=Direction.SOUTH;
		square[4]=getpt(center,new point(center.x, center.y-1));
		if(square[4]!=null)
			square[4].fromdir=Direction.NORTH;
		
		square[5]=getpt(center,new point(center.x-1, center.y-1));
		if(square[5]!=null)
			square[5].fromdir=Direction.NORTH_WEST;
		square[6]=getpt(center,new point(center.x-1, center.y+1));
		if(square[6]!=null)
			square[6].fromdir=Direction.SOUTH_WEST;
		square[7]=getpt(center,new point(center.x-1, center.y));
		if(square[7]!=null)
			square[7].fromdir=Direction.WEST;
	
		return square;
	}
	private AStarPoint getpt(point center, point check)
	{
		if(check.x<map[0].length && check.y<map.length && check.x>=0 && check.y>=0)
		{
			//System.out.println("point: (" + check.x + "," + check.y + ")");
			//System.out.println("Next point heuristic " + p.heuristic(check));
			//if(p.heuristic(check)>=p.heuristic(center)) //new point is closer to goal than previous point
				return new AStarPoint(check.x, check.y, Direction.NONE);
			//else
				//return null;
		}
		else
			return null;
	}
	public ArrayList<Direction> search(point start)
	{
		ArrayList<AStarPoint> toSearch = new ArrayList<AStarPoint>();
		//System.out.println("Init point heuristic " + p.heuristic(start));
		//System.out.println("Dimensions X: " + map[0].length + " Y: " + map.length);
		for(AStarPoint p:getSquare(start))
		{	
			if(p==null)
				continue;
			toSearch.add(new AStarPoint(p.x,p.y,1+map[p.y][p.x].mine,p.fromdir,start.x,start.y,1+map[p.y][p.x].mine+this.p.heuristic(p)));
		}
		//System.out.println("Initial toSearch size: " + toSearch.size());
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
					if(toSearch.size()==0)
						return null;
					continue;
				}
				break;
			}
			//System.out.println("currpt: (" + currpt.x + "," + currpt.y + ")");
			map[currpt.y][currpt.x].weightToDate=currpt.weight;
			map[currpt.y][currpt.x].path=new ArrayList<Direction>();
			if(map[currpt.from.y][currpt.from.x].path!=null)
				map[currpt.y][currpt.x].path.addAll(map[currpt.from.y][currpt.from.x].path);
			map[currpt.y][currpt.x].path.add(currpt.fromdir);
			/*System.out.println("Map (" + currpt.x + "," + currpt.y + ")");
			for(int i=0; i<map[currpt.y][currpt.x].path.size(); i++)
			{
				System.out.println(map[currpt.y][currpt.x].path.get(i).toString());
			}
			rc.breakpoint();*/
			//rc.breakpoint();
			if(p.goal((point)(currpt)))
			{
				System.out.println("Goalstate: (" + currpt.x + "," + currpt.y + ")");
				return map[currpt.y][currpt.x].path;
			}
			for(AStarPoint p:getSquare(currpt))
			{
				if(p==null)
					continue;
				//int newweight=currpt.weight+map[p.y][p.x].mine+2/(Math.abs(p.x-currpt.x)+Math.abs(p.y-currpt.y));
				int newweight=currpt.weight+map[p.y][p.x].mine+1;
				if(newweight<map[p.y][p.x].weightToDate)
				{
					toSearch.add(new AStarPoint(p.x,p.y,newweight,p.fromdir,currpt.x,currpt.y,newweight+this.p.heuristic(p)));
				}
			
			}
			toSearch.remove(minindex);
		}
		return null;
	}
}
