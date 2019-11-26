package nachos.threads;
import nachos.machine.*;
import java.util.*;
/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */


public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    }
    
    /**
     * Allocate a new priority thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer priority from waiting threads
     *					to the owning thread.
     * @return	a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	return new PriorityQueue(transferPriority);
    }

    public int getPriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	Lib.assertTrue(priority >= priorityMinimum &&
			priority <= priorityMaximum);
	getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMaximum)
	    return false;

	setPriority(thread, priority+1);

	Machine.interrupt().restore(intStatus);
	return true;
    }
    public boolean decreasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMinimum)
	    return false;

	setPriority(thread, priority-1);

	Machine.interrupt().restore(intStatus);
	return true;
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
    public static final int priorityMaximum = 7;    

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
	if (thread.schedulingState == null)
	    thread.schedulingState = new ThreadState(thread);

	return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class PriorityQueue extends ThreadQueue {
    	//T/F to transfer a threads priority or not
    	public boolean transferPriority;
    	
    	//New queue of threads
		public java.util.PriorityQueue<ThreadState> threadList;
		
		//Owner of the thread
		protected ThreadState threadHead = null;
		
	PriorityQueue(boolean transferPriority) {
	    this.transferPriority = transferPriority;
	    
	    //Check if transfer is authorized or not
	    if (transferPriority == false) {
	    	
	    	//If no: return thread with no changes
			this.threadList = new java.util.PriorityQueue<ThreadState>(11);
			
		} else {
			
			//If yes: use compare() and its helper function to adjust the state
			this.threadList = new java.util.PriorityQueue<ThreadState>(11, new compare());
		}
	}

	public void waitForAccess(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    
	    //Helper variable to easily get thread's state
	    ThreadState tempState = getThreadState(thread);
	    
	    //add the thread to the list
	    threadList.add(tempState);

	    //For loop to set all threads to min
		for(ThreadState i : threadList) {
			
			i.effectivePriority = (priorityMinimum - 1);
			
		}
		
	    tempState.waitForAccess(this);
	    
	}

	public void acquire(KThread thread) {
		//Disable interrupts
	    Lib.assertTrue(Machine.interrupt().disabled());
	    
	    //Helper variable
	    ThreadState tempState = getThreadState(thread);
	    
	    //Call acquire with helper
	    tempState.acquire(this);
	}

	public KThread nextThread() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me
	    
	    //Check if there is a thread in queue
	    if(pickNextThread() == null) {
	    	
	    	//If there isn't, return null and set a null flag on the queue
	    	threadHead = null;
	    	
	    	return null;
	    }else{
	    //If there's a thread on queue we poll to call the next one
	    threadHead = threadList.poll();
	    
	    //Call helper function getThread to access the thread
	    KThread tempThread = threadHead.getThread();

		acquire(tempThread);
		
		return tempThread; 
	
	    }
	}

	/**
	 * Return the next thread that <tt>nextThread()</tt> would return,
	 * without modifying the state of this queue.
	 *
	 * @return	the next thread that <tt>nextThread()</tt> would
	 *		return.
	 */
	
	protected ThreadState pickNextThread() {
	    // implement me
		
		//Look at the next thread in queue
		ThreadState nextThread = threadList.peek();
		
		//If next thread is null, we return null
		if(nextThread == null) {
			
			return null;
			
		}else {
			
			//if its not null we return the next thread
			return nextThread;
		}
	}
	
	public void print() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me (if you want)
	    //I REAAALLLLLLYYYYYY don't want to.
	}
	/**
	 * <tt>true</tt> if this queue should transfer priority from waiting
	 * threads to the owning thread.
	 */
    }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
    protected class ThreadState implements Comparable<ThreadState> {
    	
    	protected KThread thread;
    	protected int priority;
    	
    	//Initialize effectivePriority variable to be min possible
    	protected int effectivePriority = priorityMinimum - 1;
    	
    	//make a new priority queue
    	protected PriorityQueue queue;
    	
    	//Set of threads
    	protected LinkedList<PriorityQueue> waitThreads;
    	
    	
	/**
	 * Allocate a new <tt>ThreadState</tt> object and associate it with the
	 * specified thread.
	 *
	 * @param	thread	the thread this state belongs to.
	 */
	public ThreadState(KThread thread) {
		
		
		//Default initializing variables for when this method is called to default priority queue
		this.thread = thread;
		this.priority = priorityDefault;
		this.queue = null;
		this.waitThreads = new LinkedList<PriorityQueue>();
	}

	/**
	 * Return the priority of the associated thread.
	 *
	 * @return	the priority of the associated thread.
	 */
	
	//Helper function to easily get a threads priority
	public int getPriority() {
	    return priority;
	}

	/**
	 * Return the effective priority of the associated thread.
	 *
	 * @return	the effective priority of the associated thread.
	 */
	public int getEffectivePriority() {
		//Ensure our priority is minimum
		if (effectivePriority <= priorityMinimum - 1) {
			
			
			//I,J,K for loop to check which thread has a higher effective Priority
			for (PriorityQueue i : waitThreads) {
				
				for (ThreadState j : i.threadList) {
					
					if (j != this) {
						
						int k = j.getEffectivePriority();
					
						if (k > effectivePriority) {
							
							effectivePriority = k;
						}
					}
				}
			}
		}

		//Method of finding the higher priority, method found on GeeksForGeeks
		effectivePriority = Math.max(effectivePriority, priority);

		return effectivePriority;
	}
	
	//Method to return a thread if needed
	public KThread getThread() {
		return thread;
	}
	
	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	
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

	/**
	 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
	 * the associated thread) is invoked on the specified priority queue.
	 * The associated thread is therefore waiting for access to the
	 * resource guarded by <tt>waitQueue</tt>. This method is only called
	 * if the associated thread cannot immediately obtain access.
	 *
	 * @param	waitQueue	the queue that the associated thread is
	 *				now waiting on.
	 *
	 * @see	nachos.threads.ThreadQueue#waitForAccess
	 */
	
	public void waitForAccess(PriorityQueue waitQueue) {
	    // implement me
		
		//Set our queue
		this.queue = waitQueue;

	}

	/**
	 * Called when the associated thread has acquired access to whatever is
	 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
	 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
	 * <tt>thread</tt> is the associated thread), or as a result of
	 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
	 *
	 * @see	nachos.threads.ThreadQueue#acquire
	 * @see	nachos.threads.ThreadQueue#nextThread
	 */
	public void acquire(PriorityQueue waitQueue) {
	    // implement me
		
		//Add thread to the waiting threads
		waitThreads.add(waitQueue);

	}	
	
	//Helper function for the comparator, Threadstate comparison. Method was found on GeeksForGeeks
	public int compareTo(ThreadState a) {
		
		return new Integer(a.getPriority()).compareTo(this.getPriority());
	}

    }
    
    
    //Helper function for comparing the priority queue
    public static class compare implements Comparator<ThreadState> {
    	
		public int compare(ThreadState a, ThreadState b) {
			
			int x = a.getEffectivePriority();
			int y = b.getEffectivePriority();
			
			return new Integer(x).compareTo(y);
		}
	}
    
}