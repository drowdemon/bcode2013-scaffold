package testPlayerBucketPathing;

import java.util.ArrayList;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class RobotTools
{
	public final static int BUCKETSIZE=5;
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
		/*for(int i=0; i<dirs.size()-1; i++) //converting directions that net a certain effect to that simpler effect, as explained above
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
		}*/
		
		ArrayList<MapLocation> ret=new ArrayList<MapLocation>(); //merges same directions into waypoints
		ret.add(start);
		int len=1;
		for(int i=1; i<dirs.size(); i++)
		{
			if(dirs.get(i)!=dirs.get(i-1)) //end of streak
			{
				ret.add(ret.get(ret.size()-1).add(dirs.get(i-1), len*BUCKETSIZE));
				len=1;
			}
			else
				len++;
		}
		if(len!=1)
			ret.add(ret.get(ret.size()-1).add(dirs.get(dirs.size()-1), len*BUCKETSIZE));
		for(MapLocation ml:ret)
		{
			System.out.println("Waypts: " + ml);
		}
		return ret;
	}
	public static void quickSort(MapLocation locs[], int start, int end, MapLocation comp)
	{
		if(start>=end)
			return;
		int mid=partition(locs,start,end,comp);
		quickSort(locs,start,mid,comp);
		quickSort(locs,mid+1,end,comp);
	}
	private static int partition(MapLocation locs[], int start, int end, MapLocation comp)
	{
		int mid=(int)(Math.random()*(end-start))+start;
		MapLocation temp=locs[start];
		locs[start]=locs[mid];
		locs[mid]=temp;
		int dtocomp=comp.distanceSquaredTo(locs[mid]);
		int middle=start+1;
		for(int i=start+1; i<end; i++)
		{
			if(comp.distanceSquaredTo(locs[i])<=dtocomp)
			{
				if(i==middle)
					middle++; //no need to swap, I'd be swapping it with itself
				else
				{
					temp=locs[middle]; //swap middle and i
					locs[middle]=locs[i];
					locs[i]=temp;
					middle++;
				}
			}
		}
		
		temp=locs[middle-1];
		locs[middle-1]=locs[mid];
		locs[mid]=temp;
		return mid; //mid is partition pt
	}
}
