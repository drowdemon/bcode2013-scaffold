package testPlayerRadio;

import battlecode.common.MapLocation;

public class RobotTools
{
	public static int loctoint(MapLocation loc)
	{
		return -loc.x*1000*2-loc.y*3;
	}
	public static MapLocation inttoloc(int n)
	{
		int y=((-1*n)%1000)/3;
		return new MapLocation((-1*n/2-y)/1000,y);
	}
}
