package nachos.userprog;
//Team1 L4 - Ej, Andy, Izza, Spencer, Jason, Jefferson
import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.util.*;
import java.io.EOFException;

/**
 * Encapsulates the state of a user process that is not contained in its
 * user thread (or threads). This includes its address translation state, a
 * file table, and information about the program being executed.
 *
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 *
 * @see	nachos.vm.VMProcess
 * @see	nachos.network.NetProcess
 */
	
public class UserProcess {
    /**
     * Allocate a new process.
     */
	
    public UserProcess() {

	    int physicalPages = Machine.processor().getNumPhysPages();
	    pageTable = new TranslationEntry[physicalPages];
	
	    for (int i = 0; i < physicalPages; i++) {

	        pageTable[i] = new TranslationEntry(i, i, true, false, false, false);
	    }
        
        this.processID = processes;	//number of processes
        processes++;
        
        processTally.put(processID, this);
        sephamore = new Semaphore(0);
        childID = new HashSet<Integer>();


        this.OpenFiles = new OpenFile[16];
        this.OpenFiles[0] = UserKernel.console.openForReading();
        this.OpenFiles[1] = UserKernel.console.openForWriting();
    } //end of Ej's part
    
    /**
     * Allocate and return a new process of the correct class. The class name
     * is specified by the <tt>nachos.conf</tt> key
     * <tt>Kernel.processClassName</tt>.
     *
     * @return	a new process of the correct class.
     */
    public static UserProcess newUserProcess() {

	    return (UserProcess)Lib.constructObject(Machine.getProcessClassName());
    }

    /**
     * Execute the specified program with the specified arguments. Attempts to
     * load the program, and then forks a thread to run it.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the program was successfully executed.
     */
    public boolean execute(String name, String[] args) {
	
        if (!load(name, args))
            return false;
        
        new UThread(this).setName(name).fork();

        return true;
    }

    /**
     * Save the state of this process in preparation for a context switch.
     * Called by <tt>UThread.saveState()</tt>.
     */
    public void saveState() {
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {

	    Machine.processor().setPageTable(pageTable);
    }

    /**
     * Read a null-terminated string from this process's virtual memory. Read
     * at most <tt>maxLength + 1</tt> bytes from the specified address, search
     * for the null terminator, and convert it to a <tt>java.lang.String</tt>,
     * without including the null terminator. If no null terminator is found,
     * returns <tt>null</tt>.
     *
     * @param	vaddr	the starting virtual address of the null-terminated
     *			string.
     * @param	maxLength	the maximum number of characters in the string,
     *				not including the null terminator.
     * @return	the string read, or <tt>null</tt> if no null terminator was
     *		found.
     */
    public String readVirtualMemoryString(int vaddr, int maxLength) {

        Lib.assertTrue(maxLength >= 0);

        byte[] bytes = new byte[maxLength+1];

        int bytesRead = readVirtualMemory(vaddr, bytes);

        for (int length = 0; length < bytesRead; length++) {

            if (bytes[length] == 0)
            return new String(bytes, 0, length);
        }

        return null;
    }

    /**
     * Transfer data from this process's virtual memory to all of the specified
     * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data) {

	    return readVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from this process's virtual memory to the specified array.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @param	offset	the first byte to write in the array.
     * @param	length	the number of bytes to transfer from virtual memory to
     *			the array.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {

        Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);

        byte[] memory = Machine.processor().getMemory();
        
        // for now, just assume that virtual addresses equal physical addresses
        int bytesTransferred = 0;
        int pageSize = 1024;
        
        while(length > 0 && offset < data.length) {
            int virtualPage = vaddr / pageSize;
            int offsetAddress = vaddr % pageSize;
        
        if(virtualPage >= pageTable.length || virtualPage < 0) {
            break;
        }
            
        TranslationEntry TableEntry = pageTable[virtualPage];
        if (TableEntry == null || TableEntry.valid == false) {
            break;
        }
        TableEntry.used = true;
        
        int physicalPage = TableEntry.ppn;
        int physicalAddr = (physicalPage * pageSize) + offsetAddress;
        
        int amount = Math.min(data.length-offset, Math.min(length, pageSize-offsetAddress));
        System.arraycopy(memory, physicalAddr, data, offset, bytesTransferred);
        offset = amount +1;
        vaddr = amount +1;
        length = amount +1;
        bytesTransferred = amount+1;
        }
        
        return bytesTransferred;
    }

    /**
     * Transfer all data from the specified array to this process's virtual
     * memory.
     * Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @return	the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data) {
	
        return writeVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from the specified array to this process's virtual memory.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @param	offset	the first byte to transfer from the array.
     * @param	length	the number of bytes to transfer from the array to
     *			virtual memory.
     * @return	the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
    	
        Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);

    	byte[] memory = Machine.processor().getMemory();
    	
    	int pageSize = 1024;
    	int bytesTransferred = 0;
    	
    	while(length > 0 && offset < data.length) {
    		int virtualPage = vaddr / pageSize;
    		int offsetAddress = vaddr % pageSize;
    	
    		if(virtualPage >= pageTable.length || virtualPage < 0) {
    			break;
    		}
    		
    	TranslationEntry TableEntry = pageTable[virtualPage];
    	if (TableEntry == null || TableEntry.valid == false) {
    		break;
    	}
    	TableEntry.used = true;
    	
    	int physicalPage = TableEntry.ppn;
    	int physicalAddr = (physicalPage * pageSize) + offsetAddress;
    	
    	int amount = Math.min(data.length-offset, Math.min(length, pageSize-offsetAddress));
    	System.arraycopy(memory, physicalAddr, data, offset, bytesTransferred);
    	offset = amount +1;     //offset
    	vaddr = amount +1;		//virtual address
    	length = amount +1;		//length
    	bytesTransferred = amount+1;		//amount of bytes successfully transferred
    	}
    	
    	return bytesTransferred;           //return the amount of bytes transferred
       
    }

    /**
     * Load the executable with the specified name into this process, and
     * prepare to pass it the specified arguments. Opens the executable, reads
     * its header information, and copies sections and arguments into this
     * process's virtual memory.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the executable was successfully loaded.
     */
    private boolean load(String name, String[] args) {
            
        Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");
        
        OpenFile executable = ThreadedKernel.fileSystem.open(name, false);

        if (executable == null) {
            
            Lib.debug(dbgProcess, "\topen failed");
            return false;
        }

        try {
            
            coff = new Coff(executable);
        }

        catch (EOFException e) {
            
            executable.close();
            Lib.debug(dbgProcess, "\tcoff load failed");
            return false;
	    }

	    // make sure the sections are contiguous and start at page 0
        numPages = 0;
        for (int s = 0; s < coff.getNumSections(); s++) {
            
            CoffSection section = coff.getSection(s);
            if (section.getFirstVPN() != numPages) {
            coff.close();
            Lib.debug(dbgProcess, "\tfragmented executable");
            return false;
            }

            numPages += section.getLength();
        }

        // make sure the argv array will fit in one page
        byte[][] argv = new byte[args.length][];
        int argsSize = 0;
        for (int i = 0; i < args.length; i++) {
            
            argv[i] = args[i].getBytes();
            // 4 bytes for argv[] pointer; then string plus one for null byte
            argsSize += 4 + argv[i].length + 1;
        }

        if (argsSize > pageSize) {
        
            coff.close();
            Lib.debug(dbgProcess, "\targuments too long");
            return false;
        }

        // program counter initially points at the program entry point
        initialPC = coff.getEntryPoint();	

        // next comes the stack; stack pointer initially points to top of it
        numPages += stackPages;
        initialSP = numPages*pageSize;

        // and finally reserve 1 page for arguments
        numPages++;

        if (!loadSections())
            return false;

        // store arguments in last page
        int entryOffset = (numPages-1)*pageSize;
        int stringOffset = entryOffset + args.length*4;

        this.argc = args.length;
        this.argv = entryOffset;
        
        for (int i = 0; i < argv.length; i++) {

            byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
            Lib.assertTrue(writeVirtualMemory(entryOffset,stringOffsetBytes) == 4);
            entryOffset += 4;
            Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) == argv[i].length);
            stringOffset += argv[i].length;
            Lib.assertTrue(writeVirtualMemory(stringOffset,new byte[] { 0 }) == 1);
            stringOffset += 1;
        }

        return true;
    }

    /**
     * Allocates memory for this process, and loads the COFF sections into
     * memory. If this returns successfully, the process will definitely be
     * run (this is the last step in process initialization that can fail).
     *
     * @return	<tt>true</tt> if the sections were successfully loaded.
     */
    protected boolean loadSections() {

    	if (numPages > Machine.processor().getNumPhysPages()) {
    	
            coff.close();
    	    Lib.debug(dbgProcess, "\tinsufficient physical memory");
    	    return false;
    	}
    	

    	
    	// load sections
    	for (int s = 0; s < coff.getNumSections(); s++) {
    	    
            CoffSection section = coff.getSection(s);
    	    
    	    Lib.debug(dbgProcess, "\tinitializing " + section.getName()
    		      + " section (" + section.getLength() + " pages)");

    	    for (int i = 0; i < section.getLength(); i++) {
    	    	
                int vpn = section.getFirstVPN()+i;
    	    	
    	    		
    	    	TranslationEntry TableEntry = pageTable[vpn];
    		
    	    	TableEntry.used = true;
    	    	TableEntry.readOnly = section.isReadOnly();
    		
    	    	int ppn = TableEntry.ppn;
    	    	// for now, just assume virtual addresses=physical addresses
    	    	section.loadPage(i, ppn);
    	    }
    	}

    	pageTable = new TranslationEntry[numPages]; 
    	for (int i = 0; i < numPages; i++) { 

          int ppn = UserKernel.getAvailablePage(); 
	      pageTable[i] = new TranslationEntry(i, ppn, true, false, false, false);
    	}
    	    
    	return true;
    }

    /**
     * Release any resources allocated by <tt>loadSections()</tt>.
     */
    protected void unloadSections() {
        /**
         * Initialize the processor's registers in preparation for running the
         * program loaded into this process. Set the PC register to point at the
         * start function, set the stack pointer register to point at the top of
         * the stack, set the A0 and A1 registers to argc and argv, respectively,
         * and initialize all other registers to 0.
         */
  	  for (int i = 0; i < numPages; i++) {
	 	  pageTable[i].valid = false;
		  UserKernel.addAvailablePage(pageTable[i].ppn);
	  }
    }    

    /**
     * Initialize the processor's registers in preparation for running the
     * program loaded into this process. Set the PC register to point at the
     * start function, set the stack pointer register to point at the top of
     * the stack, set the A0 and A1 registers to argc and argv, respectively,
     * and initialize all other registers to 0.
     */
    public void initRegisters() {
	
        Processor processor = Machine.processor();

        // by default, everything's 0
        for (int i = 0; i < processor.numUserRegisters; i++) {

            processor.writeRegister(i, 0);
        }

        // initialize PC and SP according
        processor.writeRegister(Processor.regPC, initialPC);
        processor.writeRegister(Processor.regSP, initialSP);

        // initialize the first two argument registers to argc and argv
        processor.writeRegister(Processor.regA0, argc);
        processor.writeRegister(Processor.regA1, argv);
    }

    /**
     * Handle the halt() system call. 
     */
    private int handleHalt() {

	Machine.halt();
	
	Lib.assertNotReached("Machine.halt() did not halt machine!");
	return 0;
    }

        /*-----------------Spencer's and Izza's Section------------------*/
	//int processID;
	//int parentID;
	//int exitStatus;
	//int nextID;
	//int numOfProcess = nextID;
	

	private int exec(int file, int argc, int argv) {
    
    	String fileName = readVirtualMemoryString(file, 256);
		String[] stringBuffer = new String[argc];
		byte[] byteBuffer = new byte[4];
		
    	// variable to simplify upcoming conditional
    	boolean compatibleFile = fileName.endsWith(".coff");
    	if(fileName == null ||  !compatibleFile || argc < 0)
        {
            return -1;
        }
    	
    	for(int i = 0; i < argc; i++)
    	{
    		int temp = argv + i * 4;
    		stringBuffer[i] = readVirtualMemoryString(Lib.bytesToInt(byteBuffer,0),256);
    		if(readVirtualMemory(temp, byteBuffer) != 4 || stringBuffer[i] == null)
    		{
    			return -1;
    		}
    	}
    	UserProcess child = newUserProcess();
    	int childID = child.pid;
    	childrenProcessQueue.add(childID);
    	
    	saveState();
    	
		if (!child.execute(fileName, stringBuffer)) {
			return -1;
		}
    	return childID;
    }


    private void exit(int statusCode) {
    
    	this.statusCode = statusCode;
    	int fileNum = openFiles.length;

    	closeFiles(fileNum);
    	unloadSections();
    	cleanUpProcess();
    	semaphore.V();
    	
    	if(processTot.isEmpty())
    	{
    		Kernel.kernel.terminate();
    	}
    	
    	UThread.finish();	
    }
    
    // helper function for exit
    public void closeFiles(int fileNum) {
    
    	for(int i = 0; i < fileNum; i++)
    	{
    		OpenFiles[i].close();
    	}
    }
    
    // helper function for exit
    public void cleanUpProcess() {
    
    	processTot.remove(pid);
    	exitProcess.put(pid, this);
    }
    
    private int join(int pid, int statusCode)
    {
    	if(childrenProcessQueue.contains(pid) != true)
    	{
    		return -1;
    	}
    	childrenProcessQueue.remove(pid);
    	UserProcess child = processTot.get(pid);
    	if(child == null)
    	{
    		child = exitProcess.get(pid);
    		if(child == null)
    		{
    			return -1;
    		}
    	}
    	child.S.P();
    	writeVirtualMemory(statusCode, Lib.bytesFromInt(child.statusCode));
    	if(child.exitState == true)
    		return 1;
    	else return 0;
    }

    /*-----------------End of Spencer's and Izza's Section------------------*/

    /*-----------------EJ'S AND ANDY'S SECTION-------------------------*/
    
    /*create, close, open*/
    private int open(int file) {
    	if(file < 0) {
    		return -1;	//catch: return an error
    	}
    	
    	int description = -1;
    	for(int i = 2; i < this.OpenFiles.length; i++) {
    		if(this.OpenFiles[i] == null) {
    			description = i;
    			break;
    		}
    	}
    	if(description == -1) {
    		return -1;
    	}
    	
    	String readFileName = readVirtualMemoryString(file, 256);	//passing file with a max length of 256
    	if(readFileName == null) {
    		return -1;	//null error return
    	}
    	
    	OpenFile files = ThreadedKernel.fileSystem.open(readFileName, false);
    	if(files == null) {
    		return -1;
    	}
    	this.OpenFiles[description] = files;
    	return description;
    }
    
    private int creat(int file) {
    	if(file < 0) {
    		return -1;
    	}
    	
    	int description = -1;
    	for(int i = 2; i < this.OpenFiles.length; i++) {
    		if(this.OpenFiles[i] == null) {
    			description = i;
    			break;
    		}
    	}
    	if(description == -1) {
    		return -1;
    	}
    	
    	String readFileName = readVirtualMemoryString(file, 256);	//passing file with a max length of 256
    	if(readFileName == null) {
    		return -1;	//null error return
    	}
    	
    	OpenFile files = ThreadedKernel.fileSystem.open(readFileName, false);
    	if(files == null) {
    		return -1;
    	}
    	this.OpenFiles[description] = files;
    	return description;
    	
    }
    
    //recieved help from upperclassman (message by Ej)
    private int close(int description) {
		if (description >= this.OpenFiles.length || description < 0 || this.OpenFiles[description] == null)
			return -1;
		
		//recieved help from upperclassman	(message by Ej)
		OpenFile file = this.OpenFiles[description];

		this.OpenFiles[description] = null;

		file.close();

		return 0;    	
    }
    
    /*read, write, unlink*/
    private int read(int fileDesciptor, int buffer, int count) {

        // Checks if count is valid
        if (count < 0)
            return 1;

        // Checks if the file descriptor is valid
        if (fileDesciptor < 0 || fileDesciptor >= this.OpenFiles.length)
            return 1;

        // Checks if the file is valid
        if (this.OpenFiles[fileDesciptor] == null)
            return 1;

        OpenFile fileToRead = this.OpenFiles[fileDesciptor];            // Opens a file to be read

        byte[] Byte = new byte[count];                                  // Creates an array of bytes of size count
        int read_bytes = fileToRead.read(Byte, 0, count);             // reads in the bytes
        int write_bytes = writeVirtualMemory(buffer, Byte, 0, read_bytes);
        
        if(read_bytes != write_bytes) {	//catchE: when attempting to do anything else other
        	return -1;					//read, it will ignore and return as an error
        }
        
        return read_bytes;

    }
    
    private int write(int fileDesciptor, int buffer, int count) {

        // Checks if count is valid
        if (count < 0)
            return -1;

        // Checks if the file descriptor is valid
        if (fileDesciptor < 0 || fileDesciptor >= this.OpenFiles.length)
            return -1;

        // Checks if the file is valid
        if (this.OpenFiles[fileDesciptor] == null)
            return -1;

        OpenFile fileToWrite = this.OpenFiles[fileDesciptor];              // Opens a file to write

        byte[] Byte = new byte[count];                                      // Creates an array of bytes of size count
        int read_bytes = readVirtualMemory(buffer, Byte);             // reads in the bytes from memory

        // If the number of bytes read does not equal count, then there is an error
        if (read_bytes != count) 
            return -1;

        int write_bytes = fileToWrite.write(Byte, 0, read_bytes);           // writes the bytes to the file

        return write_bytes;
    }
    
    private int unlink(int name) {

        if (name < 0)                                                       // A check if the address is valid
            return -1;

        String filename = readVirtualMemoryString(name, 259);               // Retrieves the file name, allowing up to 259 characters

        if (filename == null)                                             // If the file name if the doesn't exist
            return -1;

        int i;
        for (i = 2; i < this.OpenFiles.length; i++) {                       // Checks each open file, if the process we want to unlink is opened 
                                                                            // by these, then remove the file
            if (OpenFiles[i].getName().equals(filename)) {

                OpenFiles[i] = null;
                break;
            }
        }

        if (!ThreadedKernel.fileSystem.remove(filename))                      // If the file cannot be removed then return -1
            return -1;

        return 0;                                                              // Success
    }
    /*-------------End of EJ'S AND ANDY'S SECTION--------------------*/

    private static final int
        syscallHalt = 0,
        syscallExit = 1,
        syscallExec = 2,
        syscallJoin = 3,
        syscallCreate = 4,
        syscallOpen = 5,
        syscallRead = 6,
        syscallWrite = 7,
        syscallClose = 8,
        syscallUnlink = 9;

    /**
     * Handle a syscall exception. Called by <tt>handleException()</tt>. The
     * <i>syscall</i> argument identifies which syscall the user executed:
     *
     * <table>
     * <tr><td>syscall#</td><td>syscall prototype</td></tr>
     * <tr><td>0</td><td><tt>void halt();</tt></td></tr>
     * <tr><td>1</td><td><tt>void exit(int status);</tt></td></tr>
     * <tr><td>2</td><td><tt>int  exec(char *name, int argc, char **argv);
     * 								</tt></td></tr>
     * <tr><td>3</td><td><tt>int  join(int pid, int *status);</tt></td></tr>
     * <tr><td>4</td><td><tt>int  creat(char *name);</tt></td></tr>
     * <tr><td>5</td><td><tt>int  open(char *name);</tt></td></tr>
     * <tr><td>6</td><td><tt>int  read(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>7</td><td><tt>int  write(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>8</td><td><tt>int  close(int fd);</tt></td></tr>
     * <tr><td>9</td><td><tt>int  unlink(char *name);</tt></td></tr>
     * </table>
     * 
     * @param	syscall	the syscall number.
     * @param	a0	the first syscall argument.
     * @param	a1	the second syscall argument.
     * @param	a2	the third syscall argument.
     * @param	a3	the fourth syscall argument.
     * @return	the value to be returned to the user.
     */
    public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {

        switch (syscall) {
        case syscallHalt:
            return handleHalt();
        
        /*Izza's and Spencer's Cases*/
        case syscallExit:
            exit(a0);
        
        case syscallExec:
            return exec(a0, a1, a2);

        case syscallJoin:
            return join(a0, a1);

        /*Ej's cases*/
        case syscallCreate:
            return creat(a0);
            
        case syscallClose:
            return close(a0);
            
        case syscallOpen:
            return open(a0);
            
        /*Andy's cases*/
        case syscallRead:
            return read(a0, a1, a2);
            
        case syscallWrite:
            return write(a0, a1, a2);
        
        case syscallUnlink:
            return unlink(a0);
            
            
        default:
            Lib.debug(dbgProcess, "Unknown syscall " + syscall);
            Lib.assertNotReached("Unknown system call!");
        }
        return 0;
    }

    /**
     * Handle a user exception. Called by
     * <tt>UserKernel.exceptionHandler()</tt>. The
     * <i>cause</i> argument identifies which exception occurred; see the
     * <tt>Processor.exceptionZZZ</tt> constants.
     *
     * @param	cause	the user exception that occurred.
     */
    public void handleException(int cause) {
        
        Processor processor = Machine.processor();

        switch (cause) {
        case Processor.exceptionSyscall:

            int result = handleSyscall(processor.readRegister(Processor.regV0),
                        processor.readRegister(Processor.regA0),
                        processor.readRegister(Processor.regA1),
                        processor.readRegister(Processor.regA2),
                        processor.readRegister(Processor.regA3)
                        );

            processor.writeRegister(Processor.regV0, result);
            processor.advancePC();

            break;				       
                        
        default:

            Lib.debug(dbgProcess, "Unexpected exception: " +
                Processor.exceptionNames[cause]);
            Lib.assertNotReached("Unexpected exception");
        }
    }
	
    /** The program being run by this process. */
    protected Coff coff;

    /** This process's page table. */
    protected TranslationEntry[] pageTable;
    /** The number of contiguous pages occupied by the program. */
    protected int numPages;

    /** The number of pages in the program's stack. */
    protected final int stackPages = 8;
    
    private int initialPC, initialSP;
    private int argc, argv;
	
    private static final int pageSize = Processor.pageSize;
    private static final char dbgProcess = 'a';

    /*--------------------------------------------------------------------------------*/
	
    //VARIABLE DECLERATIONS
    protected HashSet<Integer> childID;
	protected static Hashtable<Integer, UserProcess> processExitTally = new Hashtable<Integer, UserProcess>();
	protected static Hashtable<Integer, UserProcess> processTally = new Hashtable<Integer, UserProcess>();
	
	private static int processes = 0;
	private int processID;
	
	private OpenFile[] OpenFiles;
	
	protected boolean exited = true;
	protected Semaphore sephamore;
	protected int status;
}
