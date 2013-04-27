package testPlayerPathing;

import java.util.ArrayList;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.Team;

public abstract class BaseRobot
{
	protected RobotController rc;
	protected MapLocation enemyHQ;
	protected MapLocation myHQ;
	protected int myChannel;
	protected int channelBlock;
	protected int channelDelta;
	protected static final int STARTCHANNEL=41975; //Important, but must be secret. For later, put in a second one or a bunch of them really to ensure that at least one will work. Used to give robots info of where the channel block and personal channels are
	protected static final int CODE_COMPPATH=3;
	private boolean searched=false;
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
			Team t=rc.senseMine(enemyloc);
			if(dist<mindist && (t==null || t==r.getTeam()))
			{
				mindist=dist;
				closest=enemyloc;
			}
		}
		return closest;
	}
	protected void fullpathingBasetoBase()
	{
		if(searched==false)
		{
			System.out.println("Searching");
			AStarProblem prob = new AStarProblem(enemyHQ.x, enemyHQ.y, (enemyHQ.x-myHQ.x)>0, (enemyHQ.y-myHQ.y)>0);
			AStar pathing=new AStar(rc.getMapWidth(),rc.getMapHeight(),prob, rc.senseNonAlliedMineLocations(myHQ, 100000000));
			ArrayList<Direction> route=pathing.search(new point(myHQ.x,myHQ.y));
			if(route==null)
				System.out.println("Ooops. Failed to path.");
			else
			{
				/*for(Direction d:route)
					System.out.println("Directions: " + d.toString());*/
				ArrayList<MapLocation> waypoints=RobotTools.parseDirections(route,myHQ);
				/*for(Direction d:route)
					System.out.println("New Directions: " + d.toString());
				for(MapLocation m:waypoints)
					System.out.println("Waypoints: " + m.toString());*/
				int i=1;
				for(; i<waypoints.size()-1; i++) //can optimize by doing this in the parseDirections' loop
				{
					try
					{
						rc.broadcast((channelBlock+channelDelta*(i-1))%65535,RobotTools.loctoint(waypoints.get(i)));
					}
					catch (GameActionException e)
					{
						System.out.println("Error sending message");
						e.printStackTrace();
					}
				}
				//Now i=waypoints.size()
				try
				{
					rc.broadcast((channelBlock+channelDelta*(i-1))%65535,RobotTools.loctoint(waypoints.get(i))-2000000000); //2*10^9-null terminator
				}
				catch (GameActionException e)
				{
					System.out.println("Error sending message");
					e.printStackTrace();
				}
			}
			searched=true;
		}
	}
	public abstract boolean run();
	public abstract void run2();
	public abstract void initRadio() throws GameActionException;
}