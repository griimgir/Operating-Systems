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
	Adapted from the nextThread() in RoundrobinScheduler
	with an included sortQueue function to ensure we always
	have the highest priority thread in front to pull from
	*/
	public KThread nextThread() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me
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
		if(queue.isEmpty()){
			return null;
		}else{
			sortQueue();
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
     * Helper function for picking a nextThread().
     * Uses Collections.sort() to sort the queue based on priority
     * This method of easily sorting I found on GeeksforGeeks
     */
	public void sortQueue(){
		Collections.sort(queue, new Comparator<KThread>(){	
			public int compare(KThread first,KThread second){
				ThreadState threadOne = getThreadState(first);
				ThreadState threadTwo = getThreadState(second);
	    	
	    			if(threadOne.getEffectivePriority() == threadTwo.getEffectivePriority()){
	    				return 0;
	    			}
	    			if(threadOne.getEffectivePriority() < threadTwo.getEffectivePriority()){
	    				return 1;
	    			}else{
	    				return -1;
	    		}
	    	}
	    	});
	    }
	
	public LinkedList<KThread> queue = new LinkedList<KThread>();

	
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
		int tempPriority = 		this.priority;
		KThread tempThread = 	this.thread;
		double tempTimer = 		this.threadTimer;
		
		if (threadQueue == null || threadQueue.queue.isEmpty()) {
			return priority;
		}	
		
		for(int i = 0; i < threadQueue.queue.size(); i++) {
			
			if(tempPriority != priorityMaximum){
				if(tempPriority == getThreadState(threadQueue.queue.get(i)).priority){
					if(tempThread != getThreadState(threadQueue.queue.get(i)).thread ){
						if(tempTimer < getThreadState(threadQueue.queue.get(i)).threadTimer){
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
	    
	    // implement me
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
		Lib.assertTrue(Machine.interrupt().disabled());
		
		PriorityQueue queue = waitQueue;
		
		waitQueue.queue.add(thread);
		

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
		int tempTimer=0;
		if(threadQueue != null){
			if(!threadQueue.queue.isEmpty()){
					if(waitQueue.transferPriority == true){
						if(2 <= threadQueue.queue.size()){
				
		
						for(int i=0; i<threadQueue.queue.size(); i++){
							if (getThreadState(threadQueue.queue.get(i)).threadTimer < getThreadState(threadQueue.queue.get(tempTimer)).threadTimer) {
								tempTimer = i;
								
							}
						}
						
						if(priorityMaximum != getThreadState(waitQueue.queue.get(tempTimer)).getPriority()){
							getThreadState(waitQueue.queue.get(tempTimer)).setPriority(getThreadState(thread).getPriority());

						}
						
					}
			
				}
				
			
			
		
	    waitQueue.queue.remove(thread);
	    threadQueue = null; 
	
		
		
		
			}	
		}
	}

	/** The thread with which this object is associated. */	   
	protected KThread thread;
	/** The priority of the associated thread. */
	protected int priority;
	public PriorityQueue threadQueue;
	public double threadTimer = 0;


    }
   
    


    
}