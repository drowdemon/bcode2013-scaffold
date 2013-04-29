package testPlayerGroupMind;

import java.util.ArrayList;

import battlecode.common.Clock;
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
	private boolean searched=false;
	protected MapLocation rallypt;
	
	//For Soldiers:
	protected final static int CODE_COMPPATH=32;
	protected final static int CHECK_NEWLOC=256;
	protected final static int RUSH=8; //Soldier states
	protected final static int MINE=4;
	protected final static int ATTACKANDMINE=12;
	protected final static int DEFEND=16;
	//Several unassigned states: all of them are 0(?),4,8,12,16,20,24,28
	protected final static int STATEFLAGCHECK=28; 
	protected final static int SPREADFLAGCHECK=3; //possible: 0,1,2,3
	protected final static int MININGFLAGCHECK=192; //Possible: 0,64,128,192. Translation:0->16,64->25,192->36,256,49
	//For HQ
	protected final static int PATHING=42;
	protected final static int DONEPATHING = 84;
	
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
		rallypt=myHQ.add(myHQ.directionTo(enemyHQ),4);
	}
	public MapLocation getNearestEnemy(MapLocation loc, Robot enemies[]) //returns location of the enemy nearest to loc, who isn't on a mine
	{
		MapLocation retEnemy=null;
		int mindist=999999999;
		//int count=0;
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
			/*if(dist<=2)
				count++;*/
			if(dist<mindist && (t==null || t==r.getTeam()))
			{
				mindist=dist;
				retEnemy=enemyloc;
			}
		}
		return retEnemy;
	}
	protected void fullpathingBasetoBase() //complete pathing algorithm, A* search
	{
		if(searched==false)
		{
			System.out.println("Searching");
			//AStarProblem prob = new AStarProblem(enemyHQ.x, enemyHQ.y, (enemyHQ.x-myHQ.x)>0, (enemyHQ.y-myHQ.y)>0); //initilizes the problem with the goal, and in which direction am I past the goal
			AStar pathing=new AStar(rc.getMapWidth(),rc.getMapHeight(),new AStarProblem(enemyHQ.x, enemyHQ.y, (enemyHQ.x-myHQ.x)>0, (enemyHQ.y-myHQ.y)>0), rc.senseNonAlliedMineLocations(myHQ, 100000000)); //gives it the map dimensions, the above (now inline), and the mine locations
			ArrayList<Direction> route=pathing.search(new point(myHQ.x,myHQ.y));  //starts the search
			if(route==null)
				System.out.println("Ooops. Failed to path.");
			else
			{
				/*for(Direction d:route)
					System.out.println("Directions: " + d.toString());*/
				ArrayList<MapLocation> waypoints=RobotTools.parseDirections(route,myHQ); //simplify the directions
				/*for(Direction d:route)
					System.out.println("New Directions: " + d.toString());
				for(MapLocation m:waypoints)
					System.out.println("Waypoints: " + m.toString());*/
				int i=1;
				for(; i<waypoints.size()-1; i++) //TODO optimize by doing this in the parseDirections' loop. //Broadcasts all this data
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
			try
			{
				rc.broadcast((rc.senseObjectAtLocation(myHQ).getID()*28+13466+channelBlock)%65535, DONEPATHING); //tells the HQ that pathing has been completed
			}
			catch (GameActionException e)
			{
				e.printStackTrace();
			}
		}
	}
	public abstract boolean run();
	public abstract void run2();
	public abstract void initRadio() throws GameActionException;
}