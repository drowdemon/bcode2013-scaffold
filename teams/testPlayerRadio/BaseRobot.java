package testPlayerRadio;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;

public abstract class BaseRobot
{
	protected RobotController rc;
	protected MapLocation enemyHQ;
	protected MapLocation myHQ;
	protected int myChannel;
	protected int channelBlock;
	protected static final int STARTCHANNEL=19858; //Important, but must be secret. For later, put in a second one or a bunch of them really to ensure that at least one will work. Used to give robots info of where the channel block and personal channels are
	public BaseRobot(RobotController myRC)
	{
		rc=myRC;
		try
		{
			initRadio();
		}
		catch (GameActionException e)
		{
			System.out.println("Error initializing radio");
			e.printStackTrace();
		}
		enemyHQ=rc.senseEnemyHQLocation();
		myHQ=rc.senseHQLocation();
	}
	public MapLocation getNearestEnemy(MapLocation loc, Robot enemies[]) //returns location of the enemy nearest to loc
	{
		int mindist=999999999;
		MapLocation closest = null;
		for(Robot r:enemies)
		{
			MapLocation enemyloc=null;
			try
			{
				enemyloc = rc.senseRobotInfo(r).location;
			}
			catch (GameActionException e)
			{
				System.out.println("Error sensing enemy robots");
				e.printStackTrace();
			}
			int dist=loc.distanceSquaredTo(enemyloc);
			if(dist<mindist)
			{
				mindist=dist;
				closest=enemyloc;
			}
		}
		return closest;
	}
	public abstract void run();
	public abstract void initRadio() throws GameActionException;
}