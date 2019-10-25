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
		       
	Lib.assertTrue(priority >= priorityMinimum && priority <= priorityMaximum);
	
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
    	    	
    	PriorityQueue(boolean transferPriority) {
	    this.transferPriority = transferPriority;
	    
	    if(transferPriority == true){
	    	
	    }
	}

	public void waitForAccess(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).waitForAccess(this);
	}

	public void acquire(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).acquire(this);
	}

	
	/*
	Built from the nextThread() in RoundrobinScheduler
	with an included sortQueue function to ensure we always
	have the highest priority thread in front to pull from
	*/
	public KThread nextThread() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me
	    
	    //Check that there is a thread waiting in the queue
	    if(queue.isEmpty()){
	    	return null;
	    }else{
	    	
	    	//Sort the queue to ensure the highest priority thread is on top
	    	sortQueue();
	    	
	    	//Use LinkedLists removeFirst() function to select highest priority thread and remove it from the queue.
	    	return (KThread) queue.removeFirst();	
	    }
	}

	/**
	 * Return the next thread that <tt>nextThread()</tt> would return,
	 * without modifying the state of this queue.
	 *
	 * @return	the next thread that <tt>nextThread()</tt> would
	 *		return.
	 */
	
	
	/*
	 *Adapted from nextThread() with the difference being we only check
	 *the thread that is at the front of the queue and don't actually
	 *remove it from the queue.
	 */
	protected ThreadState pickNextThread() {
	    // implement me
		
	    //Check that there is a thread waiting in the queue
		if(queue.isEmpty()){
			return null;
			
		}else{
			
	    	//Sort the queue to ensure the highest priority thread is on top
			sortQueue();
			
			//Use the peekFirst() function to see the first thread in the queue without needing to remove it.
			return getThreadState(queue.peekFirst());
		}	
	}
	
	public void print() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me (if you want)
	    //I really really don't want to
	}

	/**
	 * <tt>true</tt> if this queue should transfer priority from waiting
	 * threads to the owning thread.
	 */
	public boolean transferPriority;
	
	
	/*
     * Helper function for picking a nextThread() and pickNextThread()
     * Uses Collections.sort() to sort the queue based on priority
     * This method of easily sorting I found on GeeksforGeeks
     */
	public void sortQueue(){
		
		//Uses Collection Sort to sort the thread each time this method is called.
		Collections.sort(queue, new Comparator<KThread>(){	
			
			//The compare function for the comparator, using default return values
			public int compare(KThread first,KThread second){
				
				//Setting threadStates for different threads to a variable
				ThreadState threadOne = getThreadState(first);
				ThreadState threadTwo = getThreadState(second);
	    	
					//Comparison if statements to find a proper return value
	    			if(threadOne.getEffectivePriority() == threadTwo.getEffectivePriority()){//Check if priorities are equal
	    				return 0;
	    			}
	    			if(threadOne.getEffectivePriority() < threadTwo.getEffectivePriority()){//Check which thread has a higher priority
	    				return 1;
	    			}else{
	    				return -1;
	    		}
	    	}
	    	});
	    }
	
	public LinkedList<KThread> queue = new LinkedList<KThread>(); //Create the queue which we will put the threads on.

	
    }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {
	/**
	 * Allocate a new <tt>ThreadState</tt> object and associate it with the
	 * specified thread.
	 *
	 * @param	thread	the thread this state belongs to.
	 */
	public ThreadState(KThread thread) {
	    this.thread = thread;
	    
	    setPriority(priorityDefault);
	}

	/**
	 * Return the priority of the associated thread.
	 *
	 * @return	the priority of the associated thread.
	 */
	public int getPriority() {
	    return priority;
	}
	public LinkedList<KThread> queue = new LinkedList<KThread>();

	public PriorityQueue newQueue;
	/**
	 * Return the effective priority of the associated thread.
	 *
	 * @return	the effective priority of the associated thread.
	 */
	
	
	public int getEffectivePriority() {
	    // implement me
		
		//Set temp variables to hold priority, thread, and the timer.
		int tempPriority = 		this.priority;
		KThread tempThread = 	this.thread;
		double tempTimer = 		this.threadTimer;
		
		//Check is the current queue is empty
		if (threadQueue == null || threadQueue.queue.isEmpty()) {
			return priority;
		}	
		
		//for loop to balance out thread's effective priorities AS WELL AS sort what threads have been on the queue the longest
		for(int i = 0; i < threadQueue.queue.size(); i++) {
			
			//Ensure we don't increase the priority to be more than the max
			if(tempPriority < priorityMaximum){
				
				//Check that the two threads DO indeed have the same priority
				if(tempPriority == getThreadState(threadQueue.queue.get(i)).priority){
					
					//Make sure it is two different threads. (Included with a bug fix from GeeksForGeeks)
					if(tempThread != getThreadState(threadQueue.queue.get(i)).thread ){
						
						//Check that the thread has been on the queue longer
						if(tempTimer < getThreadState(threadQueue.queue.get(i)).threadTimer){
							
							//If a thread has the same priority, is less than the max, and has been on the queue longer, increase its priority
							return (tempPriority+1);
						}
					}
				}
			}
		}	
		return priority;	
	}

	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	public void setPriority(int priority) {
	    if (this.priority == priority)
		return;
	    this.priority = priority;
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
		
		//Disable interrupts
		Lib.assertTrue(Machine.interrupt().disabled());
		
		//Add thread to queue
		waitQueue.queue.add(thread);
		
		//Creating a timer along with the thread to keep track of which threads have been in the queue longer.
		threadTimer = Machine.timer().getTime();
		threadQueue = waitQueue;	
		
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
		Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me
		
		//Create a temporary timer of how long a thread has been waiting
		int tempTimer;
		tempTimer = 0;
		
		//Ensure the queue is not empty
		if(threadQueue != null){
			if(!threadQueue.queue.isEmpty()){
				
				//Check transferPriority is true
					if(waitQueue.transferPriority == true){
						
						//Make sure there is another thread on the queue, size of 2 or more
						if(2 <= threadQueue.queue.size()){
				
						//for loop to scan through the queue and sort the waiting times of the threads on it
						for(int i = 0; i < threadQueue.queue.size(); i++){
							if (getThreadState(threadQueue.queue.get(i)).threadTimer < getThreadState(threadQueue.queue.get(tempTimer)).threadTimer) {
								tempTimer = i;
								
							}
						}
						
						//Check that our priority isn't goint to go over the maximum priority that is allowed
						if(priorityMaximum > getThreadState(waitQueue.queue.get(tempTimer)).getPriority()){
							getThreadState(waitQueue.queue.get(tempTimer)).setPriority(getThreadState(thread).getPriority());
						}	
					}	
				}		
					
			//Remove the thread from the queue after it has run
	    waitQueue.queue.remove(thread);
	    
	    //Set the queue as Null when all threads are finished.
	    threadQueue = null; 	
			}	
		}
	}

	/** The thread with which this object is associated. */	   
	protected KThread thread;
	/** The priority of the associated thread. */
	protected int priority;
	
	//Set a new Priority Queue
	public PriorityQueue threadQueue;
	
	//Global timer variable to keep a track of how long a thread has been in the queue
	public double threadTimer = 0;


    }
   
    


    
}