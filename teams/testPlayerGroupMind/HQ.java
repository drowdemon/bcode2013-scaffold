package testPlayerGroupMind;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.Team;

public class HQ extends BaseRobot
{
	private int firstguyID;
	private boolean makingunits;
	private int delaymakingunits;
	private int searching;
	private int miningRad; 
	private int spread;
	private MapLocation currgoal;
	private int goalIndex;
	boolean endreading;
	private int countdownNewLoc;
	private Direction unitCreationDir;
	public HQ(RobotController myRC)
	{
		super(myRC);
		goalIndex=-1;
		firstguyID=0;
		makingunits=true;
		delaymakingunits=5;
		searching=0;
		miningRad=0; //0->16
		spread=0;
		endreading=false;
		countdownNewLoc=0;
		//Direction dir=rc.getLocation().directionTo(enemyHQ).opposite();
		Direction dir=rc.getLocation().directionTo(enemyHQ);
		Team t=rc.senseMine(myHQ.add(dir));
		while(!(t==null || t==rc.getTeam()))
		{
			dir=dir.rotateLeft(); //TODO Possibly choose which way to turn
			t=rc.senseMine(myHQ.add(dir));
		}
		rc.setIndicatorString(0, dir.toString());
		unitCreationDir=dir;
		if(rc.canMove(dir))
		{
			try
			{
				rc.spawn(dir.opposite());
			}catch(GameActionException E)
			{
				System.out.println("Caught exception at spawn moment initial");
			}
		}
		//Note: active in constructor. But not anymore - spawned.
	}

	public boolean run() //Tell the first guy to path
	{
		Robot allies[]=rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam()); 
		if(allies==null || allies.length==0)
			return false;
		else if(allies.length==1) //found the first spawned robot!
		{
			try
			{
				rc.broadcast((allies[0].getID()*28+13466+channelBlock)%65535,CODE_COMPPATH);
				firstguyID=allies[0].getID();
			}
			catch (GameActionException e)
			{
				System.out.println("Error broadcasting to first unit");
				e.printStackTrace();
			}
			return true;
		}
		else
		{
			System.out.println("MAJOR ERROR IN HQ first run!!!");
			return true;
		}
		
	}
	public void run2()
	{
		try
		{
			readMyData();
		}
		catch (GameActionException e1)
		{
			e1.printStackTrace();
		}
		Robot allies[]=rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam());
		MapLocation mymines[]=null;
		if(delaymakingunits<0)
			mymines=rc.senseMineLocations(rallypt, (miningRad/64+4)*(miningRad/64+4), rc.getTeam());
		if(rc.isActive())
		{
			if(rc.getTeamPower()-(int)((double)allies.length*1.5)>=20) //for some power usage
			{
				makeunit(); //make more guys
				makingunits=true;
				delaymakingunits=5;
			}
			else
			{
				makingunits=false;
				delaymakingunits--;
			}
		}
		int state=MINE;
		if(Clock.getRoundNum()>=350)
		{
			state=ATTACKANDMINE;
		}
		int msg=0;
		if(state==MINE)
			msg=getMiningMessage(mymines);
		else if(state==ATTACKANDMINE)
			msg=getAttackAndMineMessage(mymines);
		for(Robot r:allies)
		{
			if(searching==0 && r.getID()==firstguyID)
				msg|=CODE_COMPPATH;
			try
			{
				rc.broadcast((r.getID()*28+13466+channelBlock)%65535,(msg));
			}
			catch (GameActionException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private int getMiningMessage(MapLocation mymines[])
	{
		int msg=MINE;
		if(spread<2)
			spread=2;
		if(mymines!=null)
		{
			//rc.setIndicatorString(0, "Rad: " + miningRad);
			//rc.setIndicatorString(1, "mines: " + mymines.length);
			if((miningRad/64+4)*(miningRad/64+4)*3/2-mymines.length<3)
			{
				spread=3;
				if(miningRad<192)
				{
					miningRad+=64;
				}
			}
		}
		msg|=(spread|miningRad);
		return msg;
	}
	
	private int getAttackAndMineMessage(MapLocation mymines[]) //A little screwy right here
	{
		int msg=ATTACKANDMINE;
		//int sendspread=2;
		//int sendRad=0;
		if(countdownNewLoc>=0)
		{
			msg|=CHECK_NEWLOC;
			countdownNewLoc--; //a delay so that everyone recieves the above message.
		}
		if(mymines!=null)
		{
			if((miningRad/64+4)*(miningRad/64+4)*3/2-mymines.length<7+nearedgeweight(rallypt)) //mined area
			{
				if(miningRad==0) //if mined small area, mine a larger one
				{	
					miningRad=64;
					spread=2;
				}
				else //mined large area
				{
					spread=1; //reset to mine small area
					miningRad=0;
					msg|=CHECK_NEWLOC; //go to next place
					countdownNewLoc=15;
					if(goalIndex==-1 && searching==2 && !endreading) //if this is the first time I'm going somewhere new
					{
						goalIndex++; 
						currgoal=readNextLoc(goalIndex);
						if(currgoal==null || currgoal.y==-1)
						{	
							//System.out.println("WHY!!!!!!!!");
							currgoal=enemyHQ;
						}
						else
						{
							rallypt=nextrally(rallypt.distanceSquaredTo(currgoal));
						}
					}
					else if(goalIndex>=0) //If I've been going places
					{
						int dtogoal=rallypt.distanceSquaredTo(currgoal);
						if(dtogoal!=0) //If I'm not done, go a little to the goal
						{
							rallypt=nextrally(dtogoal);
						}
						else //I'm done, get next goal
						{
							goalIndex++;
							currgoal=readNextLoc(goalIndex);
							if(currgoal==null || currgoal.y==-1)
							{	
								//System.out.println("WHY!!!!!!!!");
								currgoal=enemyHQ;
							}
							else
							{
								rallypt=nextrally(rallypt.distanceSquaredTo(currgoal));
							}
						}
					}
					else //no pathing data, go to HQ
					{
						currgoal=enemyHQ;
						rallypt=nextrally(rallypt.distanceSquaredTo(currgoal));
					}
				}
			}
			else
			{
				rc.setIndicatorString(1,"Still mining");
			}
		}
		msg|=(spread|miningRad); //put it all together
		rc.setIndicatorString(0, rallypt.toString() + " " + ((currgoal!=null)?currgoal.toString():"null"));
		try
		{
			rc.broadcast(channelBlock-channelDelta, RobotTools.loctoint(rallypt));
		}
		catch (GameActionException e)
		{
			e.printStackTrace();
		}
		return msg;
	}
	
	public MapLocation nextrally(int dtogoal) //Gets the next point to go to, either all the way if its close or a bit of the way if its not
	{
		if(dtogoal>=49)
			rallypt=rallypt.add(rallypt.directionTo(currgoal),7);
		else
			rallypt=rallypt.add(rallypt.directionTo(currgoal),(int)Math.sqrt(dtogoal));
		rc.setIndicatorString(2, ""+dtogoal);
		return rallypt;
	}
	
	private int nearedgeweight(MapLocation loc) //if we're near the edge we can't mine everything. So basically mine very very little
	{
		if(rc.getMapWidth()-loc.x<5 || loc.x<5 || rc.getMapHeight()-loc.y<5 || loc.y<5)
		{
			if(miningRad==0)
				return 20;
			else if(miningRad==64)
				return 30;
			else //WTF?
				return 100;
		}
		else
			return 0;
	}
	
	private MapLocation readNextLoc(int numread) //reads the next location in the pathed directions
	{
		int possCommand=0;
		if(endreading==false)
		{
			try
			{
				possCommand=rc.readBroadcast((channelBlock+numread*channelDelta)%65535);
			}
			catch (GameActionException e)
			{
				System.out.println("Error reading channel");
				e.printStackTrace();
			}
			if(possCommand!=0) //If it does: COMPROMISED or no directions posted yet 
			{
				if(possCommand+2000000000<0 && possCommand/500000000<0) //Both tests just in case, not really required to do both. Means null terminator
				{
					endreading=true;
					possCommand+=2000000000;
				}
				else
					numread++;
				return RobotTools.inttoloc(possCommand);
			}
			else
				return new MapLocation(-1,-1); //COMPROMISED, or no directions currently
		}
		else
			return null;
	}
	
	private void readMyData() throws GameActionException //reads my own channel
	{
		int result=0;
		result=rc.readBroadcast(myChannel);
		if(result==PATHING)
			searching=1;
		if(result==DONEPATHING)
			searching=2;
	}
	
	public void initRadio() throws GameActionException //initializes the radio
	{
		channelBlock=(int)((Math.random()*65535)+493671)%65535;
		if(channelBlock==STARTCHANNEL)
			channelBlock+=437;
		myChannel=(rc.getRobot().getID()*28+13466+channelBlock)%65535;
		channelDelta=channelBlock%57*3;
		rc.broadcast(STARTCHANNEL, channelBlock);
	}
	
	private void makeunit() //creates a unit towards the enemy, or somewhere else in case of mines
	{
		if(rc.canMove(unitCreationDir)) //this direction computed in constructor so as not to charge mines
		{
			try
			{
				rc.spawn(unitCreationDir);
			}catch(GameActionException E)
			{
				System.out.println("Caught exception at spawn moment");
			}
		}
	}
}
