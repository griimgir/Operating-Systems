package nachos.threads;
import nachos.ag.BoatGrader;
import nachos.machine.*;

public class Boat {

    static BoatGrader bg;
    
    // Global Variables
    //private static Communicator phone;
    private static Condition waitingForBoat;		// Wakes the thread at the head of waiting queue, i.e the next sleeping adult/child
    private static String boatLocation;
    private static String pilot;
    private static Lock boatLock;
    private static int adultsOnMolokai;
    private static int childrenOnMolokai;
    private static int adultsOnOahu;
    private static int childrenOnOahu;
    
public static void selfTest() {
    
	BoatGrader b = new BoatGrader();
	
	System.out.println("\n ***Testing Boats with only 2 children***");
	begin(0, 2, b);

	//System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
    //	begin(1, 2, b);

    //System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
    //	begin(3, 3, b);
}

public static void begin( int adults, int children, BoatGrader b ) {
    
	// Store the externally generated autograder in a class adults
	// variable to be accessible by children.
	bg = b;

	// Instantiate global variables here
	int totalAdults = adults;
	int totalChildren = children;
	
	adultsOnOahu = adults;
	childrenOnOahu = children;
	
	
	boatLocation = "Oahu";
	pilot = "EMPTY";
	
	boatLock = new Lock();
    waitingForBoat = new Condition(boatLock);
    
	// Counters to determine everyone is on Molokai
	adultsOnMolokai = 0;
	childrenOnMolokai = 0;
	
	int i = 0;
	
	boolean disable = Machine.interrupt().disable();
	
	for(i = 0; i < children; i++ ) {
		Runnable childThread = new Runnable() {
			public void run() {
				ChildItinerary();
			}
		};
		KThread t = new KThread(childThread);
		t.setName("Child " + i);
		t.fork();		
	} 

	for(i = 0; i < adults; i++) {
		Runnable adultThread = new Runnable() {
			public void run() {
				AdultItinerary();
			}
		};
		KThread t = new KThread(adultThread);
		t.setName("Adult " + i);
		t.fork();		
	}
	
	Machine.interrupt().restore(disable);
	
	KThread.yield();
	
	if (childrenOnOahu == 0 && adultsOnOahu == 0 && childrenOnMolokai == totalChildren && adultsOnMolokai == totalAdults) {
		System.out.println("Finish Boat.begin()");
		//KThread.finish();
	}
	
}
   
    // Every time adult/child arrives, then increment the counter variable.
    // If children leave Molokai, then decrement childrenOnMolokai and increment children on Oahu. 
    static void AdultItinerary() {
    
        boatLock.acquire();						// Count all the adults on Oahu
        
        adultsOnOahu++;
        waitingForBoat.sleep();
        
        boatLock.release();
        
        //KThread.yield();
        
        boatLock.acquire();

        if (boatLocation == "Oahu") {
            if (childrenOnOahu <= 1 && adultsOnOahu >= 1 && pilot == "EMPTY") {
            	
                pilot = "adult";                // Setting pilot

                adultsOnOahu--;                 // Even though we can't have threads communicate to each other we can here
                bg.AdultRowToMolokai();         // because variables in the stack can't be shared between threads.
                adultsOnMolokai++;

                boatLocation = "Molokai";
                
                pilot = "EMPTY";                // Emptying the boat
            }
            
            waitingForBoat.wake();				// Wakes up a thread in the waiting queue, if there are any. Places a thread on the ready queue
            waitingForBoat.sleep();				// Tells the current thread to suspend all activities and says the thread is done
        }
        
        boatLock.release();
        
        KThread.finish();						// Terminates the currently running thread by destroying it and then calling sleep
    }

    static void ChildItinerary() {
    
    	boatLock.acquire();						// Count all the children on Oahu
    	
    	childrenOnOahu++;
    	waitingForBoat.sleep();
    	
    	boatLock.release();
    	
    	//KThread.yield();
    	
    	boatLock.acquire();
    	
    	while (childrenOnOahu > 0) {
    		
    		// When the boat is on Oahu
			if (boatLocation == "Oahu") {
	    		
				// No one is on the boat
				if (pilot == "EMPTY") {
	    				
					// should take two children 
					if (childrenOnOahu >= 2) {
	    					
						pilot = "child";
						waitingForBoat.wake();		// Wake up a second child thread
						waitingForBoat.sleep();		// Make the current child thread go to sleep
					}
				}
	    			
				else if (pilot == "child") {
					
					// Grabs a child as a passenger send both children to Molokai
					childrenOnOahu--;
					childrenOnOahu--;
					bg.ChildRowToMolokai();
					bg.ChildRideToMolokai();
					childrenOnMolokai++;
					childrenOnMolokai++;
					pilot = "EMPTY";
				}
	    			
				boatLocation = "Molokai";
			}
	    		
			else if (boatLocation == "Molokai" && pilot == "EMPTY" && childrenOnOahu >= 1) {
	    			
				pilot = "child";
				
				if (childrenOnOahu == 1 && adultsOnOahu == 0) {
					
					childrenOnMolokai--;
					bg.ChildRowToOahu();
					childrenOnOahu++;
					
					waitingForBoat.wake();
					waitingForBoat.sleep();
				}
	
				childrenOnMolokai--;
				bg.ChildRowToOahu();
				childrenOnOahu++;
				pilot = "EMPTY";
			}
	    	
		boatLock.release();
		
		waitingForBoat.wake();
		waitingForBoat.sleep();
	    	
		KThread.finish();
	}
}
    
    /*static void SampleItinerary()
    {
	// Please note that this isn't a valid solution (you can't fit
	// all of them on the boat). Please also note that you may not
	// have a single thread calculate a solution and then just play
	// it back at the autograder -- you will be caught.
	System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
	bg.AdultRowToMolokai();
	bg.ChildRideToMolokai();
	bg.AdultRideToMolokai();
	bg.ChildRideToMolokai();
    }
    */
}
