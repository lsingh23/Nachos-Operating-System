package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import java.util.LinkedList;

/**
 * A kernel that can support multiple user processes.
 */
public class UserKernel extends ThreadedKernel {
    /**
     * Allocate a new user kernel.
     */
    public UserKernel() {
	super();
    }

    /**
     * Initialize this kernel. Creates a synchronized console and sets the
     * processor's exception handler.
     */
    public void initialize(String[] args) {
	super.initialize(args);

	console = new SynchConsole(Machine.console());

    for(int i = 0; i < Machine.processor().getNumPhysPages(); i++){
        openPages.add(i);
    }
	
	Machine.processor().setExceptionHandler(new Runnable() {
		public void run() { exceptionHandler(); }
	    });
    }

    public static LinkedList<Integer> allocatePages(int requestedNum){
        boolean interruptStatus = Machine.interrupt().disable();
        LinkedList<Integer> allocatePages = null;
        
        if(openPages.size() > 0 && requestedNum > 0 && requestedNum <= openPages.size()){
            allocatePages = new LinkedList<>();
            for(int i = 0; i < requestedNum; i++)
                allocatePages.add(openPages.remove());
        }
        Machine.interrupt().restore(interruptStatus);
        return allocatePages;
    }

    public static void deallocatePages(LinkedList<Integer> allocatePages){
        boolean interruptStatus = Machine.interrupt().disable();
        openPages.addAll(allocatePages);
        Machine.interrupt().restore(interruptStatus);
    }


    /**
     * Test the console device.
     */	
    public void selfTest() {
	super.selfTest();

    if(Lib.test(test1)){
        Memory1Test();
        Memory2Test();
    }

    if(Lib.test(test2))
        UserProcess.selfTest();
         
    

	System.out.println("\nTesting the console device. Typed characters");
	System.out.println("will be echoed until q is typed.");

	char c;

	do {
	    c = (char) console.readByte(true);
	    console.writeByte(c);
	}
	while (c != 'q');

	
    }

    private void Memory1Test() {
        System.out.println("--------------------------------- User Kernel Test  1 ---------------------------------");
        boolean works = true;
        int size = openPages.size();

        if(size < 1) 
            works = false;

        LinkedList<Integer> test = allocatePages(8);

        if(test == null || test.size() != 8)
            works = false;

        deallocatePages(test);

        if(openPages.size() != size)
            works = false;

        if(works == true)
            System.out.println("User Kernel Test 1 Works\n\n");
        else
            System.out.println("User Kernel Test 1 does not works\n\n");
        

    }

    private void Memory2Test(){
        System.out.println("--------------------------------- User Kernel Test  2 ---------------------------------");
        System.out.println("openPages.size() when no pages are allocated : " + openPages.size());
        LinkedList<Integer> p1 = UserKernel.allocatePages(12);
        System.out.println("p1 taking 12 pages. Remaining open pages: " + openPages.size());
        System.out.println("p1 allocated pages: " + p1);

        LinkedList<Integer>p2 = UserKernel.allocatePages(16);
        System.out.println("p2 taking 16 pages. Remaining open pages: " + openPages.size());
        System.out.println("p2 allocated pages: " +p2);

        LinkedList<Integer> p3 = UserKernel.allocatePages(20);
        System.out.println("p3 taking 20 pages. Remaining open pages: " + openPages.size());
        System.out.println("p3 allocated pages: " + p3);

        LinkedList<Integer> p4 = UserKernel.allocatePages(8);
        System.out.println("p4 taking 8 pages. Remaining open pages: " + openPages.size());
        System.out.println("p4 allocated pages: " + p4);

        System.out.println();
        UserKernel.deallocatePages(p1);
        System.out.println("p1 release their 12 pages. Remaining open pages: " + openPages.size());
        UserKernel.deallocatePages(p2);
        System.out.println("p2 release their 16 pages. Remaining open pages: " + openPages.size());
        System.out.println();

        LinkedList<Integer> p5 = UserKernel.allocatePages(16);
        System.out.println("p5 taking 16 pages. Remaining open pages: " + openPages.size());
        System.out.println("p5 allocated pages: " + p5);
        System.out.println();

        System.out.println("Attempt to allocate 22 pages: ");
        if(UserKernel.allocatePages(22) == null){
            System.out.println("INVALID REQUEST.");
            System.out.println("Open pages: " + openPages.size());
        }
        System.out.println();


        System.out.println("Deallocate the rest of the test processes");
        UserKernel.deallocatePages(p3);
        UserKernel.deallocatePages(p4);
        UserKernel.deallocatePages(p5);
        System.out.println("Number of open pages: " + openPages.size());
        System.out.println("------------- allocatePages and deallocatePages work as expected. ---------------");
           

    }

    /**
     * Returns the current process.
     *
     * @return	the current process, or <tt>null</tt> if no process is current.
     */
    public static UserProcess currentProcess() {
	if (!(KThread.currentThread() instanceof UThread))
	    return null;
	
	return ((UThread) KThread.currentThread()).process;
    }

    /**
     * The exception handler. This handler is called by the processor whenever
     * a user instruction causes a processor exception.
     *
     * <p>
     * When the exception handler is invoked, interrupts are enabled, and the
     * processor's cause register contains an integer identifying the cause of
     * the exception (see the <tt>exceptionZZZ</tt> constants in the
     * <tt>Processor</tt> class). If the exception involves a bad virtual
     * address (e.g. page fault, TLB miss, read-only, bus error, or address
     * error), the processor's BadVAddr register identifies the virtual address
     * that caused the exception.
     */
    public void exceptionHandler() {
	Lib.assertTrue(KThread.currentThread() instanceof UThread);

	UserProcess process = ((UThread) KThread.currentThread()).process;
	int cause = Machine.processor().readRegister(Processor.regCause);
	process.handleException(cause);
    }

    /**
     * Start running user programs, by creating a process and running a shell
     * program in it. The name of the shell program it must run is returned by
     * <tt>Machine.getShellProgramName()</tt>.
     *
     * @see	nachos.machine.Machine#getShellProgramName
     */
    public void run() {
	super.run();

	UserProcess process = UserProcess.newUserProcess();
	rootProcess = process;	
	String shellProgram = Machine.getShellProgramName();	
	Lib.assertTrue(process.execute(shellProgram, new String[] { }));

	KThread.currentThread().finish();
    }

    /**
     * Terminate this kernel. Never returns.
     */
    public void terminate() {
	super.terminate();
    }

    /** Globally accessible reference to the synchronized console. */
    public static SynchConsole console;
    static UserProcess rootProcess;

    // dummy variables to make javac smarter
    private static Coff dummy1 = null;
    private static LinkedList<Integer> openPages = new LinkedList<>();
    private final static char test1 = '1';
    private static final char test2 = '2';
}
