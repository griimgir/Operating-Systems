package nachos.threads;

import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    
    // Global Variables
    //private static Communicator phone;
    private static Condition waitingOnBoat;
    private static String boatLocation;
    private static String pilot;
    //private static String passenger;
    private static Lock boatLock;
    private static int adultsOnMolokai;
    private static int childrenOnMolokai;
    private static int adultsOnOahu;
    private static int childrenOnOahu;
    
    public static void selfTest()
    {
BoatGrader b = new BoatGrader();

System.out.println("\n ***Testing Boats with only 2 children***");
begin(0, 2, b);

// System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//   begin(1, 2, b);

//   System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//   begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
// Store the externally generated autograder in a class adults
// variable to be accessible by children.
bg = b;

// Instantiate global variables here
// phone = new Communicator();

int totalAdults = adults;
int totalChildren = children;

adultsOnOahu = adults;
childrenOnOahu = children;


boatLocation = "Oahu";
pilot = "EMPTY";
//passenger = "EMPTY";

boatLock = new Lock();
    waitingOnBoat = new Condition(boatLock);
    
// Counters to determine everyone is on Molokai
adultsOnMolokai = 0;
childrenOnMolokai = 0;

int i;

for(i = 1; i <= children; i++ ) {
 Runnable childThread = new Runnable() {
  public void run() {
   ChildItinerary();
  }
 };
 KThread t = new KThread(childThread);
 t.setName("Child " + i);
 t.fork();  
} 

for(i = 1; i <= adults; i++) {
 Runnable adultThread = new Runnable() {
  public void run() {
   AdultItinerary();
  }
 };
 KThread t = new KThread(adultThread);
 t.setName("Adult " + i);
 t.fork();  
}

KThread.yield();

if (childrenOnOahu == 0 && adultsOnOahu == 0 && childrenOnMolokai == totalChildren && adultsOnMolokai == totalAdults) {
 System.out.println("Finish Boat.begin()");
}

    }
   
    // Every time adult/child arrives, then increment the counter variable.
    // If children leave Molokai, then decrement childrenOnMolokai and increment children on Oahu. 
    static void AdultItinerary()
    {
        boatLock.acquire();

        if (boatLocation == "Oahu") {
            while (childrenOnOahu <= 1 && adultsOnOahu >= 1 && pilot == "EMPTY") {

                pilot = "adult";                //Setting location and pilot
                boatLocation = "Molokai";

                adultsOnOahu--;                 //Even though we can't have threads communicate to each other we can here
                bg.AdultRideToMolokai();        //because variables in the stack can't be shared between threads.
                adultsOnMolokai++;

                pilot = "EMPTY";                //emptying the boat
                //passenger = "EMPTY";

                boatLocation = "Oahu";
                break;

            }
            waitingOnBoat.wake();
            waitingOnBoat.sleep();
        }
        
        boatLock.release();
        
        KThread.finish();
    }

    static void ChildItinerary()
    {
     boatLock.acquire();
     
     while (true) {
      
      if (boatLocation == "Oahu") {
       
       if (pilot == "EMPTY") {
        
        if (childrenOnOahu > 1) {
         // should take two children 
         pilot = "child";
         childrenOnOahu--;
         childrenOnOahu--;
         bg.ChildRowToMolokai();
         bg.ChildRideToMolokai();
         childrenOnMolokai++;
         childrenOnMolokai++;
        }
        
        else if (childrenOnOahu == 1 && adultsOnOahu == 0) {
         // last child to Oahu, then finish 
         pilot = "child";
         childrenOnOahu--;
         bg.ChildRowToMolokai();
         childrenOnMolokai++;
        }
        
        if (childrenOnOahu == 1 && adultsOnOahu == 0)
         // If there was one child left then we are done 
         waitingOnBoat.wake();
         break;
       }
       
       else if (pilot == "child") {
        
        //passenger = "child";
        childrenOnOahu--;
    childrenOnOahu--;
    bg.ChildRowToMolokai();
    bg.ChildRideToMolokai();
    childrenOnMolokai++;
    childrenOnMolokai++;
       }
       
       boatLocation = "Molokai";
      }
      
      else if (boatLocation == "Molokai" && pilot == "EMPTY") {
       
       pilot = "child";
       childrenOnMolokai--;
       bg.ChildRowToOahu();
       childrenOnOahu++;
      }
     }
     
     boatLock.release();
     
     // Signals that we are done
     //phone.speak(1);
     
     KThread.finish();
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
