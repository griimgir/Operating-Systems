package nachos.threads;
import nachos.machine.*;

import java.util.*;

/**
 * A scheduler that chooses threads using a lottery.
 * <p/>
 * <p/>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 * <p/>
 * <p/>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 * <p/>
 * <p/>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */

public class LotteryScheduler extends Scheduler {
	/**
	 * Allocate a new lottery scheduler.
	 */
	public LotteryScheduler() {
	}

	/**
	 * Allocate a new lottery thread queue.
	 *
	 * @param    transferPriority    <tt>true</tt> if this queue should
	 * transfer tickets from waiting threads
	 * to the owning thread.
	 * @return a new lottery thread queue.
	 */

	//Taken directly from our priority scheduler in project 1
	public ThreadQueue newThreadQueue(boolean transferPriority) {
		return new LotteryQueue(transferPriority);
	}
	
	//Taken directly from our priority scheduler in project 1
    public int getPriority(KThread thread) {
    	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getPriority();
    }

	//Taken directly from our priority scheduler in project 1
    public int getEffectivePriority(KThread thread) {
    	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getEffectivePriority();
    }

	//Taken directly from our priority scheduler in project 1
    public void setPriority(KThread thread, int priority) {
    	Lib.assertTrue(Machine.interrupt().disabled());		       
    	Lib.assertTrue(priority >= priorityMinimum && priority <= priorityMaximum);
    	getThreadState(thread).setPriority(priority);
    }

    

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
	public static final int priorityMaximum = Integer.MAX_VALUE;


	//Taken directly from our priority scheduler in project 1
	protected ThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
		    thread.schedulingState = new ThreadState(thread);

		return (ThreadState) thread.schedulingState;
	    }


	
	protected class LotteryQueue extends ThreadQueue {
		
    	// to transfer a threads priority or not
    	public boolean transferPriority;
    	
    	//New queue of threads
    	public LinkedList <ThreadState> threadList = new LinkedList<ThreadState>();
    	
		//Owner of the thread
		public ThreadState threadHead = null;


		LotteryQueue(boolean transferPriority) {
			this.transferPriority = transferPriority;
		}
		
		//Adapted from our priority Scheduler
		public void waitForAccess(KThread thread) {
			
			//Disable interrupts
			Lib.assertTrue(Machine.interrupt().disabled());
			
			//Variable to hold our thread state
		    ThreadState tempState = getThreadState(thread);

		    //Call waitForAccess given our new temp thread
			tempState.waitForAccess(this);
			
		}

		//Taken from our priority Scheduler
		public void acquire(KThread thread) {
			//Disable interrupts
		    Lib.assertTrue(Machine.interrupt().disabled());
		    
		    //Helper variable
		    ThreadState tempState = getThreadState(thread);
		    
		    //Call acquire with helper
		    tempState.acquire(this);
		}

		
		//Function to run a lottery and pick a new thread
		public KThread nextThread() {
			//Disable interrupts
			Lib.assertTrue(Machine.interrupt().disabled());
			
			//Number of tickets in circulation
			int numTickets = 0;
			
			//Extra tickets available
			int extraTickets = 0;
			
			//Variable to find winner, given the number of tickets in circulation
			int getWinner = Lib.random(numTickets + 1);
			
			//Owner of thread
			ThreadState threadHead = null;
			
			//Winning thread to put at queue front
			KThread winningThread = null;
			
			
			//Check that the queue is occupied
			if (threadList == null) {
				return null;
			}

			//Assign tickets
			for (int i = 0; i < threadList.size(); i++) {
				
				threadHead = this.threadList.get(i);
				numTickets += threadHead.getEffectivePriority();
			}
	
			//Check the winning tickets number
			for (int i = 0; i < threadList.size(); i++) {
				
				threadHead = this.threadList.get(i);
				
				extraTickets += threadHead.getEffectivePriority();
				
				if (getWinner <= extraTickets) {
					
					winningThread = threadHead.thread;
					
					break;
				}
			}

			//Double check we didn't assign winning ticket to a null thread 
			if(winningThread != null) {
				
				threadList.remove(threadHead);
			}

			return winningThread;

		}

		//Print function needed
		public void print() {
			Lib.assertTrue(Machine.interrupt().disabled());

		}

	}

	protected class ThreadState {
		//variable adapted from priority scheduler
		//Variable to hold thread
		protected KThread thread;

		//variable to hold priority taken from priority scheduler 
		protected int priority;
		
		//effectivepriority variable taken from priority scheduler 
		protected int effectivePriority;
		
		//queue for the lottery
		protected LotteryQueue queue;

		//waiting threads queue, taken from priority scheduler 

		//Adapted from priority Scheduler
		public ThreadState(KThread thread) {
			this.thread = thread;
			this.priority = priorityDefault; 
			queue = new LotteryQueue(true);
			
		}

		//Simply returns threads priority
		public int getPriority() {
			return priority;
		}

		//Adapted from priority Scheduler
		public int getEffectivePriority() {
			effectivePriority = priority;
			
			//For loop to get effective priorities
			for (int i = 0; i < queue.threadList.size(); i++) {
				
				effectivePriority += queue.threadList.get(i).effectivePriority;
				
			} 

			return effectivePriority;
		}


		//Taken from Priority Scheduler
		public void setPriority(int priority) {
			//Check if the priorities even need to be set
		    if (this.priority == priority) {
		    	return;
		    }
		    
		    //Set the priorities based on the priority we are given to set
		    //This method of using math was found on GeeksForGeeks
			this.priority = Math.min(Math.max(priority, priorityMinimum), priorityMaximum);
			
			this.effectivePriority = priorityMinimum - 1;
		}

		
		public void waitForAccess(LotteryQueue waitQueue) {
			waitQueue.threadList.add(this);

			if (waitQueue.threadHead != null) {
				if(waitQueue.threadHead != this){
				waitQueue.threadHead.queue.waitForAccess(this.thread);
				}
				
			}


		}

		//Return thread owner
		public void acquire(LotteryQueue waitQueue) {
			waitQueue.threadHead = this;
		}


	}
}