package nachos.threads;


import nachos.machine.*;

public class ReactWater{

    public ReactWater() {
        hydrogenCount = 0;
        oxygenCount = 0;
        lock = new Lock();
        oxygenWaiting = new Condition2(lock);
        hydrogenWaiting = new Condition2(lock);
    }

    public void hReady() {
        lock.acquire();
        hydrogenCount++;

        if(oxygenCount < 1 || hydrogenCount < 2) {
            hydrogenWaiting.sleep();
        }
        makeWater();
        lock.release();
    }

    public void oReady() {
        lock.acquire();
        oxygenCount++;

        if(oxygenCount < 1 || hydrogenCount < 2) {
            oxygenWaiting.sleep();
        }
        makeWater();
        lock.release();
    }

    public void makeWater() {
        if(oxygenCount < 1 || hydrogenCount < 2) {
            return;
        }
        hydrogenWaiting.wake();
        hydrogenCount--;
        hydrogenWaiting.wake();
        hydrogenCount--;
        oxygenWaiting.wake();
        oxygenCount--;
        
        System.out.println("Water was made!");
    }

    private static class Oxygen implements Runnable{

        Oxygen(ReactWater reactWater){
            this.reactWater = reactWater;
        }

        public void run(){
            reactWater.oReady();
        }       

        private ReactWater reactWater = null;
    }

    private static class Hydrogen implements Runnable{

        Hydrogen(ReactWater reactWater){
            this.reactWater = reactWater;
        }
        
        public void run(){
            reactWater.hReady(); 
        }

        private ReactWater reactWater = null;
    }

    public static void selfTest(){

        ReactWater react = new ReactWater();
        ThreadedKernel.alarm.waitUntil(1000);

        System.out.println("------------------ReactWater Testing-------------------");
        System.out.println("---------------Test 1 React Water: 1 O and 2 H -------");
        KThread oxygen1 = new KThread(new Oxygen(react)).setName("Oxygen1");
        KThread hydrogen1 = new KThread(new Hydrogen(react)).setName("Hydrogen1");
        KThread hydrogen2 = new KThread(new Hydrogen(react)).setName("Hydrogen2");

        oxygen1.fork();
        hydrogen1.fork();
        hydrogen2.fork();

        ThreadedKernel.alarm.waitUntil(2000);

        System.out.println("---------------Test 2 React Water: 3 H and 1 O -------");
        KThread hydrogen3 = new KThread(new Hydrogen(react)).setName("Hydrogen3");
        KThread hydrogen4 = new KThread(new Hydrogen(react)).setName("Hydrogen4");
        KThread hydrogen5 = new KThread(new Hydrogen(react)).setName("Hydrogen5");
        KThread oxygen2 = new KThread(new Oxygen(react)).setName("Oxygen2");

        hydrogen3.fork();
        hydrogen4.fork();
        hydrogen5.fork();
        oxygen2.fork();

        ThreadedKernel.alarm.waitUntil(2000);

        System.out.println("--------------Test 3 React Water: 1 O, 4 H, 1 O-------");
        KThread oxygen3 = new KThread(new Oxygen(react)).setName("Oxygen3");
        KThread hydrogen6 = new KThread(new Hydrogen(react)).setName("Hydrogen6");
        KThread hydrogen7 = new KThread(new Hydrogen(react)).setName("Hydrogen7");
        KThread hydrogen8 = new KThread(new Hydrogen(react)).setName("Hydrogen8");
        KThread hydrogen9 = new KThread(new Hydrogen(react)).setName("Hydrogen9");
        KThread oxygen4 = new KThread(new Oxygen(react)).setName("Oxygen4");

        oxygen3.fork();
        hydrogen6.fork();
        hydrogen7.fork();
        hydrogen8.fork();
        hydrogen9.fork();
        oxygen4.fork();

        ThreadedKernel.alarm.waitUntil(2000);
       
    }

    private int hydrogenCount;
    private int oxygenCount;
    private Lock lock;
    private Condition2 oxygenWaiting;
    private Condition2 hydrogenWaiting;
}