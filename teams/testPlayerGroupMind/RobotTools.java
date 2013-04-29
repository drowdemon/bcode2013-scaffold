package testPlayerGroupMind;

import java.util.ArrayList;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class RobotTools
{
	public static int loctoint(MapLocation loc) //converts a location to an integer VERY POORLY
	{
		return -loc.x*1000*2-loc.y*3;
	}
	public static MapLocation inttoloc(int n)
	{
		int y=((-1*n)%1000)/3;
		return new MapLocation((-1*n/2-y)/1000,y);
	}
	public static ArrayList<MapLocation> parseDirections(ArrayList<Direction> dirs, MapLocation start) //merges directions like NE,NW into N,N and then merges same directions into long lines and waypoints at the end of each one
	{
		for(int i=0; i<dirs.size()-1; i++) //converting directions that net a certain effect to that simpler effect, as explained above
		{
			Direction d1=dirs.get(i);
			Direction d2=dirs.get(i+1);
			if(d1!=d2)
			{
				if(d1.ordinal()>d2.ordinal())
				{
					Direction temp=d1;
					d1=d2;
					d2=temp;
				}
				if(d1.ordinal()-d2.ordinal()==-2)
				{	
					dirs.set(i, d1.rotateRight());
					dirs.set(i+1, d1.rotateRight());
				}
				else if(d1.ordinal()-d2.ordinal()==-6)
				{
					dirs.set(i, d1.rotateLeft());
					dirs.set(i+1, d1.rotateLeft());
				}
			}	
		}
		
		ArrayList<MapLocation> ret=new ArrayList<MapLocation>(); //merges same directions into waypoints
		ret.add(start);
		int len=1;
		for(int i=1; i<dirs.size(); i++)
		{
			if(dirs.get(i)!=dirs.get(i-1)) //end of streak
			{
				ret.add(ret.get(ret.size()-1).add(dirs.get(i-1), len));
				len=1;
			}
			else
				len++;
		}
		return ret;
	}
}
