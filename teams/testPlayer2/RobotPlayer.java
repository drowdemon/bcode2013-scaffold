package testPlayer2;

import battlecode.common.*;

public class RobotPlayer
{
	private static RobotController rc;
	private static MapLocation rallypt;
	private static MapLocation enemyHQ;
	private static MapLocation myHQ;
	public static void run(RobotController myRC) throws GameActionException
	{
		rc=myRC;
		enemyHQ=rc.senseEnemyHQLocation();
		myHQ=rc.senseHQLocation();
		rallypt=new MapLocation((myHQ.x*3+enemyHQ.x)/4,(myHQ.y*3+enemyHQ.y)/4);
		while(true)
		{
			if(rc.getType()==RobotType.HQ)
			{
				runHQ();
			}
			else if(rc.getType()==RobotType.SOLDIER)
			{
				Robot[] enemies=rc.senseNearbyGameObjects(Robot.class,10000000,rc.getTeam().opponent());
				if(enemies.length==0)
				{
					if(Clock.getRoundNum()<250)
						movetoloc(rallypt);
					else
						movetoloc(enemyHQ);
				}
				else
				{
					int mindist=999999999;
					MapLocation closest = null;
					MapLocation myloc=rc.getLocation();
					for(Robot r:enemies)
					{
						MapLocation enemyloc=rc.senseRobotInfo(r).location;
						int dist=myloc.distanceSquaredTo(enemyloc);
						if(dist<mindist)
						{
							mindist=dist;
							closest=enemyloc;
						}
					}
					movetoloc(closest);
				}
			}
			rc.yield();
		}
	}
	private static void movetoloc(MapLocation loc)
	{
		if(rc.isActive())
		{
			Direction dir=rc.getLocation().directionTo(loc);
			if(rc.getLocation().distanceSquaredTo(loc)==0)
				return;
			int trydirs[]={0,1,-1,2,-2};
			for(int d : trydirs)
			{
				Direction newd=Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(newd))
				{
					try
					{
						rc.move(newd);
					}
					catch (GameActionException e)
					{
						System.out.println("Thrown in soldier movement");
						e.printStackTrace();
					}
					break;
				}
			}
		}	
	}
	public static void runHQ()
	{
		if(rc.isActive())
		{
			Direction dir=rc.getLocation().directionTo(enemyHQ); 
			if(rc.canMove(dir))
			{
				try
				{
					rc.spawn(dir);
				}catch(GameActionException E)
				{
					System.out.println("Caught exception at spawn moment");
				}
			}
		}
	}
}