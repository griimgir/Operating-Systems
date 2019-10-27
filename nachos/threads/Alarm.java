package nachos.threads;

import nachos.machine.*;
import java.util.TreeSet;
/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	// CREATING A PRIVATE TREESET THAT CAN BE ONLY ACCESSES W/IN THIS CLASS
	// TREESET BECAUSE IT'S AN ASCENDING SET, IMPLYING IT IS ALREADY SORTED 
	// AND UNIQUE
	private TreeSet<KThread> standbyQueue = new TreeSet<KThread>();
	
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
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread
	 * that should be run.
	 */
	public void timerInterrupt() {
		
		// if ( standbyQueue is not empty){
		if(!standbyQueue.isEmpty())									
		{
		//  systemCurrentTime = read time from system;
			long currentSystemTime = Machine.timer().getTime();		
			
			// while ( systemCurrentTime is at least wakeUpTime){
			while (currentSystemTime >= wakeTime)		

			{
				// place into standbyQueue’s readyQueue;
				standbyQueue.pollFirst().ready();		
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
		
		// while ( wakeUpTime  > systemCurrentTime){
		while (wakeTime > Machine.timer().getTime())
		{
			
			// ADD CURRENT THREAD TO standybyQueue
			standbyQueue.add(KThread.currentThread());
			KThread.yield();
			
			// ?????
//			KThread.sleep();
		}
	}
}
