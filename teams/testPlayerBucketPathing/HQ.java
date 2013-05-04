package testPlayerBucketPathing;

import java.util.ArrayList;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class HQ extends BaseRobot
{
	private int delaymakingunits;
	private int searching;
	private int miningRad; 
	private int spread;
	private MapLocation currgoal;
	private int goalIndex;
	private boolean endreading;
	private int countdownNewLoc;
	private Direction unitCreationDir;
	private MapLocation encs[];
	private int IDsofEncampmentCreators[];
	public HQ(RobotController myRC)
	{
		super(myRC);
		goalIndex=-1;
		delaymakingunits=5;
		searching=0;
		miningRad=0; //0->16
		spread=0;
		endreading=false;
		countdownNewLoc=0;
		IDsofEncampmentCreators=new int[2];
		new ArrayList<MapLocation>();
		//Direction dir=rc.getLocation().directionTo(enemyHQ).opposite();
		Direction dir=rc.getLocation().directionTo(enemyHQ);
		Team t=rc.senseMine(myHQ.add(dir));
		while(!(t==null || t==rc.getTeam()))
		{
			dir=dir.rotateLeft(); //TODO Possibly choose which way to turn
			t=rc.senseMine(myHQ.add(dir));
		}
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

	public boolean run()
	{
		if(rc.isActive())
		{
			Robot allies[]=rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam());
			if(allies.length<2)
				makeunit();
			else
			{
				encs=rc.senseAllEncampmentSquares();
				//System.out.println("Sorted");
				RobotTools.quickSort(encs, 0, encs.length, myHQ);
				int numtimes=0;
				for(int i=encs.length; --i>=0;)
				{
					if(myHQ.distanceSquaredTo(encs[i])>2 || i==0)
					{
						try
						{
							rc.broadcast(((IDsofEncampmentCreators[numtimes]=allies[numtimes].getID())*28+13466+channelBlock)%65535, CREATE_ENC|(CREATE_SHIELDS*(numtimes+3))); //this is a hack. It relies on CREATE_SUPPLIER being last and CREATE_GENERATOR being before it
							rc.broadcast((IDsofEncampmentCreators[numtimes]*28+13466+channelBlock+(channelDelta/3))%65535, RobotTools.loctoint(encs[i]));
							System.out.println(IDsofEncampmentCreators[numtimes]);
							System.out.println((CREATE_ENC|(CREATE_SHIELDS*(numtimes+3)))+"");
						}
						catch (GameActionException e)
						{
							e.printStackTrace();
						}
						if(++numtimes>=2)
						{
							//System.out.println("Finished with broadcasts");
							return true;
						}
					}
				}
			}
		}
		return false;
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
				delaymakingunits=5;
			}
			else
			{
				delaymakingunits--;
			}
		}
		int state=MINE;
		if(Clock.getRoundNum()>=200)
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
			try
			{
				if(r.getID()==IDsofEncampmentCreators[0] || r.getID()==IDsofEncampmentCreators[1])
					continue;
				RobotType thistype=rc.senseRobotInfo(r).type;
				if(thistype==RobotType.GENERATOR && searching==0)
					rc.broadcast((r.getID()*28+13466+channelBlock)%65535, CODE_COMPPATH);
				if(thistype!=RobotType.SOLDIER)
					continue;
			}
			catch (GameActionException e1)
			{
				e1.printStackTrace();
			}
			/*if(searching==0 && r.getID()==firstguyID)
				msg|=CODE_COMPPATH;*/
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
			//rc.setIndicatorString(1, (miningRad/64+4)*(miningRad/64+4)*3/2-mymines.length + " left, but I need: " + (7+nearedgeweight(rallypt)));
			if((miningRad/64+4)*(miningRad/64+4)*3/2-mymines.length<=7+nearedgeweight(rallypt)) //mined area
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
						if(currgoal!=null)
							rc.setIndicatorString(0, currgoal.toString());
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
		//	else
		//	{
		//		rc.setIndicatorString(1,"Still mining");
		//	}
		}
		msg|=(spread|miningRad); //put it all together
		rc.setIndicatorString(1, rallypt.toString() + " " + ((currgoal!=null)?currgoal.toString():"null"));
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
			//rc.setIndicatorString(0, miningRad+" ");
			if(miningRad==0)
				return 22;
			else if(miningRad==64)
				return 33;
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
				if((possCommand&TERMINATOR)>0)
				{
					endreading=true;
					possCommand&=(TERMINATOR-1);
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
		if((result&ENCEXISTS)>0)
		{
			//MapLocation loc=RobotTools.inttoloc(result&ENCEXISTS-1);
			//myEncs.add(loc);
			rc.broadcast(((result>>18)*28+13466+channelBlock)%65535,ACKNOLEDGED);
			rc.broadcast((rc.getRobot().getID()*28+13466+channelBlock)%65535,0);
			//System.out.println("Read you loud and clear. Over." + " Sending to: " + (result>>18));
		}
	}
	
	public void initRadio() throws GameActionException //initializes the radio
	{
		channelBlock=(int)((Math.random()*65535)+10846)%65535;
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
