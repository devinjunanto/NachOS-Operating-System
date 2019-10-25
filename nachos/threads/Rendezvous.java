package nachos.threads;

import nachos.machine.*;
import java.util.*; //import util and priorityQueue

/**
 * A <i>Rendezvous</i> allows threads to synchronously exchange values.
 */
public class Rendezvous {
    /**
     * Allocate a new Rendezvous.
     */
    public Rendezvous () {
        waitQueue = new LinkedHashMap<Integer, RendezvousThread>();
    }
    private LinkedHashMap<Integer, RendezvousThread> waitQueue; // waitQueue to hold values
    //private Lock lock;
    //private Condition cv;

    private class RendezvousThread{
        private int value;
        private Condition cv;
        private Lock lock;
        public RendezvousThread (int value, Condition cv, Lock lock){
            this.value = value;
            this.cv = cv;
            this.lock = lock;
        }
    }

    /**
     * Synchronously exchange a value with another thread.  The first
     * thread A (with value X) to exhange will block waiting for
     * another thread B (with value Y).  When thread B arrives, it
     * will unblock A and the threads will exchange values: value Y
     * will be returned to thread A, and value X will be returned to
     * thread B.
     *
     * Different integer tags are used as different, parallel
     * synchronization points (i.e., threads synchronizing at
     * different tags do not interact with each other).  The same tag
     * can also be used repeatedly for multiple exchanges.
     *
     * @param tag the synchronization tag.
     * @param value the integer to exchange.
     */
    public int exchange (int tag, int value) {
        if(!waitQueue.isEmpty() && waitQueue.containsKey(tag)){
            // WaitQueue contains tag
            RendezvousThread rtOld = waitQueue.remove(tag);
            int val = rtOld.value;

            Condition cv = new Condition(rtOld.lock);
            RendezvousThread rtNew = new RendezvousThread(value, cv, rtOld.lock);
            waitQueue.put(tag, rtNew);

            rtOld.lock.acquire();
            rtOld.cv.wake();
            rtOld.lock.release();
            return val;
        }else{
            // WaitQueue doesnt contain tag
            Lock lock = new Lock();
            lock.acquire();
            Condition cv = new Condition(lock);
            RendezvousThread rtNew = new RendezvousThread(value, cv, lock);
            
            waitQueue.put(tag, rtNew); // Add to waitQueue and then sleep
            cv.sleep();
            RendezvousThread rtOld = waitQueue.remove(tag);
            return rtOld.value;
        }
    }

    public static void rendezTest1() {
        System.out.println("\n\n\nRENDEZVOUS TEST 1\n\n\n");
        final Rendezvous r = new Rendezvous();

        KThread t1 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = -1;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == 1, "Was expecting " + 1 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t1.setName("t1");
        KThread t2 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = 1;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == -1, "Was expecting " + -1 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t2.setName("t2");

        t1.fork(); t2.fork();
        // assumes join is implemented correctly
        t1.join(); t2.join();
    }

    public static void rendezTest2() {
        System.out.println("\n\n\nRENDEZVOUS TEST 2\n\n\n");
        final Rendezvous r = new Rendezvous();

        KThread t1 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = -1;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == -2, "Was expecting " + -2 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t1.setName("t1");

        KThread t2 = new KThread( new Runnable () {
            public void run() {
                int tag = 1;
                int send = 1;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == 2, "Was expecting " + 2 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t2.setName("t2");  

        KThread t3 = new KThread( new Runnable () {
            public void run() {
                int tag = 1;
                int send = 2;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == 1, "Was expecting " + 1 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t3.setName("t3");

        KThread t4 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = -2;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == -1, "Was expecting " + -1 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t4.setName("t4");

        t1.fork(); t2.fork(); t3.fork(); t4.fork();
        // assumes join is implemented correctly
        t1.join(); t2.join(); t3.join(); t4.join();
    }

    public static void rendezTest3() {
        System.out.println("\n\n\nRENDEZVOUS TEST 3\n\n\n");
        final Rendezvous r1 = new Rendezvous();
        final Rendezvous r2 = new Rendezvous();

        //All threads call exchange with same tag, but through different instances of rendezvous

        KThread t1 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = -1;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r1.exchange (tag, send);
                Lib.assertTrue (recv == 2, "Was expecting " + 2 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t1.setName("t1");

        KThread t2 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = 1;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r2.exchange (tag, send);
                Lib.assertTrue (recv == -2, "Was expecting " + -2 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t2.setName("t2");  

        KThread t3 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = 2;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r1.exchange (tag, send);
                Lib.assertTrue (recv == -1, "Was expecting " + -1 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t3.setName("t3");

        KThread t4 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = -2;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r2.exchange (tag, send);
                Lib.assertTrue (recv == 1, "Was expecting " + 1 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t4.setName("t4");

        t1.fork(); t2.fork(); t3.fork(); t4.fork();
        // assumes join is implemented correctly
        t1.join(); t2.join(); t3.join(); t4.join();
    }

    public static void rendezTest4() {
        System.out.println("\n\n\nRENDEZVOUS TEST 4\n\n\n");
        final Rendezvous r = new Rendezvous();

        KThread t1 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = -1;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == 1, "Was expecting " + 1 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t1.setName("t1");

        KThread t2 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = 1;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == -1, "Was expecting " + -1 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t2.setName("t2");  

        KThread t3 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = 2;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == -2, "Was expecting " + -2 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t3.setName("t3");

        KThread t4 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = -2;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == 2, "Was expecting " + 2 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t4.setName("t4");

        t1.fork(); t2.fork(); t1.join(); t2.join();
        // assumes join is implemented correctly
        t3.fork(); t4.fork(); t3.join(); t4.join();
    }
    
    public static void rendezTest5() {
        System.out.println("\n\n\nRENDEZVOUS TEST 5\n\n\n");
        final Rendezvous r = new Rendezvous();

        KThread t1 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = -1;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue ((recv == 2 || recv ==  1 || recv ==  -2), "Was expecting " + 1 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t1.setName("t1");

        KThread t2 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = 1;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue ((recv == 2 || recv ==  -2 || recv ==  -1), "Was expecting " + -1 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t2.setName("t2");  

        KThread t3 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = 2;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue ((recv == -2 || recv ==  1 || recv ==  -1), "Was expecting " + -2 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t3.setName("t3");

        KThread t4 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = -2;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue ((recv == 2 || recv ==  1 || recv ==  -1) , "Was expecting " + 2 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv);
            }
            });
        t4.setName("t4");

        t1.fork(); t2.fork(); t3.fork(); t4.fork();
        // assumes join is implemented correctly
        t1.join(); t2.join(); t3.join(); t4.join();
    }

    // Invoke Rendezvous.selfTest() from ThreadedKernel.selfTest()

    public static void selfTest() {
        // place calls to your Rendezvous tests that you implement here
        rendezTest1(); //Simple test
        rendezTest2(); //Test multiple tags and sync
        rendezTest3(); //Test multiple instances and sync
        rendezTest4(); //Test if same tag can also be used repeatedly for multiple exchanges
        rendezTest5(); //Test if same tag can also be used repeatedly for multiple exchanges
    }
}
