package nachos.threads;

import nachos.machine.*;
import java.util.Iterator;
import java.util.Comparator;
import java.util.TreeSet;
/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */



 // extend the comparator to sort for increasing wake time
class order implements Comparator <KThread>
{
	 public int compare(KThread ahead, KThread curr)
	 {	
	 	if(ahead.alarmT > curr.alarmT){ return 1;}
	 	else if (ahead.alarmT < curr.alarmT){ return -1;}
	 	else {return -1;}
	 }

//	 @Override
//	 public int check(final KThread o1, final KThread o2) {
//	 	if(o1.alarmT > o2.alarmT){ return 1;}
//	 	else if (o1.alarmT < o2.alarmT){ return -1;}
//	 	else {return -1;}
//	 }
}

public class Alarm {
	// CREATING A PRIVATE TREESET THAT CAN BE ONLY ACCESSES W/IN THIS CLASS
	// TREESET BECAUSE IT'S AN ASCENDING SET, IMPLYING IT IS ALREADY SORTED 
	// AND UNIQUE
	private TreeSet<KThread> standbyQueue = new TreeSet<KThread>(new order());
	
	// INITIALIZING wakeTime TO BE USED IN THE timerInterrupt AND waitUntil METHOD
	private long wakeTime = 0L;

	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 *
	 * <p><b>Note</b>: Nachos will not function correctly with more than one
	 * alarm.
	 */

	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() { timerInterrupt(); }
		});
		
		//testing new lock for waitUntil
		
	}
	
	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread
	 * that should be run.
	 */
	public void timerInterrupt() {
		//  systemCurrentTime = read time from system;
		long currTime = Machine.timer().getTime();
		Iterator<KThread> i = standbyQueue.iterator();
		// if ( standbyQueue is not empty){
		while(i.hasNext()) 
		{
			KThread front = i.next();
			long currAlarm = front.alarmT;
			// while ( currAlarm less than or equal to  currTime){
			if(currAlarm <= currTime)
			{
				// place into standbyQueue’s readyQueue;
    			front.ready();
    			i.remove();
			}else { 
				break;
			}
		}
		KThread.currentThread().yield();
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks,
	 * waking it up in the timer interrupt handler. The thread must be
	 * woken up (placed in the scheduler ready set) during the first timer
	 * interrupt where
	 *
	 * <p><blockquote>
	 * (current time) >= (WaitUntil called time)+(x)
	 * </blockquote>
	 *
	 * @param	x	the minimum number of clock ticks to wait.
	 *
	 * @see	nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		// for now, cheat just to get something working (busy waiting is bad)
		
		// ASSIGN TO PRIVATE VARIABLE INSTANCE TO BE USED WITHIN THE CLASS
		this.wakeTime = Machine.timer().getTime() + x;	
		KThread.currentThread().alarmT = wakeTime;
		// ADD CURRENT THREAD TO standybyQueue
		standbyQueue.add(KThread.currentThread());
		KThread.yield();
		// KThread.sleep();
	}
}