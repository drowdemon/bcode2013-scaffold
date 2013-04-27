package testPlayerRadio;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;

public class Soldier extends BaseRobot
{

	MapLocation rallypt;
	public Soldier(RobotController myRC)
	{
		super(myRC);
		rallypt=new MapLocation((myHQ.x*3+enemyHQ.x)/4,(myHQ.y*3+enemyHQ.y)/4);
	}
	private void movetoloc(MapLocation loc)
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
	public void run()
	{
		Robot enemies[]=rc.senseNearbyGameObjects(Robot.class,10000000,rc.getTeam().opponent());
		/*int x = 0;
		try
		{
			x = rc.readBroadcast(channelBlock);
		}
		catch (GameActionException e)
		{
			System.out.println("Error Broadcasting");
			e.printStackTrace();
		}
		if(x==0)
		{
			if(Clock.getRoundNum()<200)
				movetoloc(rallypt);
			else
				movetoloc(enemyHQ);
		}
		else
		{
			movetoloc(RobotTools.inttoloc(x));
		}*/
		if(enemies.length==0)
		{
			if(Clock.getRoundNum()<200)
				movetoloc(rallypt);
			else
				movetoloc(enemyHQ);
		}
		else
			movetoloc(getNearestEnemy(rc.getLocation(),enemies));
	}

	public void initRadio() throws GameActionException
	{
		channelBlock=rc.readBroadcast(STARTCHANNEL);
		myChannel=(rc.getRobot().getID()*42+19857+channelBlock)%65535;
	}
	
}