package nachos.threads;

import nachos.machine.*;

//help for this communicator class was provided by an upperclassman, Hoa in Networks class. 
//Further recources were used online and other lab help from other lab mates.
/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */

public class Communicator {
    /**
     * Allocate a new communicator.
     */
	
	
	private Lock lock;
	//private Condition speaker;	//Test without Condition2
	private Condition2 speaker;		//now using condition2
	private Condition2 spoke;		//speaker spoke a word for a listener to capture
	private Condition2 listener;
	private Condition2 heard;	//listenet has captured the word from speaker
	private int result;	//word expained in pseudo-code
	
	//I can use this to just make any bool variable to be set to false
	public boolean initiator(boolean bool) {
		bool = false;
		return bool;
	}
	
	
	//I can use this to make bool variables to be set to true
	public boolean initiatorTRUE(boolean bool) {
		bool = true;
		return bool;
	}
	
    public Communicator() {
    	// CREATING NEW LOCK 'lock' TO MAKE SPEAK AND LISTEN 
    	lock = new Lock();
    	// different from pseudo-code: Condition2 vs Condition
    	
    	/* this.speaker = new Condition2(lk); */
    	//this.speaker = new Condition2(lock);
    	speaker = new Condition2(lock);
    	/* this.listener = new Condition2(lk); */
    	//this.listener = new Condition2(lock);
    	listener = new Condition2(lock);
    	
    	spoke = new Condition2(lock);
    	heard = new Condition2(lock);
    	
    	Sbool = initiator(Sbool);
    	Lbool = initiator(Lbool);
    	read = initiator(read);
    	
    	
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    	
    	
    public void speak(int word) {
    	
    	// acquire lock
    	lock.acquire();
    	
    	// while another thread is speaking
    	while ( Sbool == true )
    	{
    		// listener sleeps
    		/* speaker.sleep */
    		speaker.sleep();
    	}
    	
    	// saving the word to current instance of communicator
    	result = word;
    	//this.word = result;
//    	listener.wake(); 
//    	speaker.sleep();
    	Sbool = initiatorTRUE(Sbool);
    	//researched online for this
    	while(read == false || Lbool == false) {
    		heard.wake();
    		spoke.sleep();
    		//System.out.println(" made it");
    	}read = Sbool = Lbool = initiator(read);
    	//wakes everything up
    	speaker.wake();
    	listener.wake();
    	
    	// release lock
    	lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    
//    private int result;
    public int listen() {
		//System.out.println("Listen() made it");
    	lock.acquire();
//    	while(listen() == 0)
//    	{
//    		listener.sleep();
//    	} 
    	while(Lbool == true) {
    		listener.sleep();
    		//System.out.println("Listener thread is put to sleep");
    	}Lbool = initiatorTRUE(Lbool); //if not Lbool is set to true until reset
    		
    	while(Sbool == false) {
    		heard.sleep();
    	}read = initiatorTRUE(read);
//    	word = null;   CANNOT SET A INT TO NULL UNLESS USING OPTIONAL VARIABLE LIBRARY
// 		SEE https://www.tutorialspoint.com/java8/java8_optional_class.htm
    	//speaker.wake();
    	spoke.wake();
    	lock.release();
	return result;
    }
    
    //instead of a linked list like previous attempt we can track listen and speak with true and false
    //self note: if it fails, Hoa said to use int to increment track
    private boolean Lbool;
    private boolean Sbool;
    private boolean read;
}
