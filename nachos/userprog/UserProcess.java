package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

import java.awt.image.Kernel;
import java.io.EOFException;

/**
 * Encapsulates the state of a user process that is not contained in its user
 * thread (or threads). This includes its address translation state, a file
 * table, and information about the program being executed.
 * 
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 * 
 * @see nachos.vm.VMProcess
 * @see nachos.network.NetProcess
 */
public class UserProcess {
	/**
	 * Allocate a new process.
	 */
	public UserProcess() {
		int numPhysPages = Machine.processor().getNumPhysPages();
		pageTable = new TranslationEntry[numPhysPages];
		for (int i = 0; i < numPhysPages; i++)
			pageTable[i] = new TranslationEntry(i, i, true, false, false, false);

		// When any process is started, its file descriptors 0 and 1 must refer to
		// standard input and standard output.
		files[0] = UserKernel.console.openForReading();
		files[1] = UserKernel.console.openForWriting();

		// acquire lock
	}

	/**
	 * Allocate and return a new process of the correct class. The class name is
	 * specified by the <tt>nachos.conf</tt> key <tt>Kernel.processClassName</tt>.
	 * 
	 * @return a new process of the correct class.
	 */
	public static UserProcess newUserProcess() {
		String name = Machine.getProcessClassName();

		// If Lib.constructObject is used, it quickly runs out
		// of file descriptors and throws an exception in
		// createClassLoader. Hack around it by hard-coding
		// creating new processes of the appropriate type.

		if (name.equals("nachos.userprog.UserProcess")) {
			return new UserProcess();
		} else if (name.equals("nachos.vm.VMProcess")) {
			return new VMProcess();
		} else {
			return (UserProcess) Lib.constructObject(Machine.getProcessClassName());
		}
	}

	/**
	 * Execute the specified program with the specified arguments. Attempts to load
	 * the program, and then forks a thread to run it.
	 * 
	 * @param name the name of the file containing the executable.
	 * @param args the arguments to pass to the executable.
	 * @return <tt>true</tt> if the program was successfully executed.
	 */
	public boolean execute(String name, String[] args) {
		if (!load(name, args))
			return false;

		thread = new UThread(this);
		thread.setName(name).fork();

		return true;
	}

	/**
	 * Save the state of this process in preparation for a context switch. Called by
	 * <tt>UThread.saveState()</tt>.
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
	 * Read a null-terminated string from this process's virtual memory. Read at
	 * most <tt>maxLength + 1</tt> bytes from the specified address, search for the
	 * null terminator, and convert it to a <tt>java.lang.String</tt>, without
	 * including the null terminator. If no null terminator is found, returns
	 * <tt>null</tt>.
	 * 
	 * @param vaddr     the starting virtual address of the null-terminated string.
	 * @param maxLength the maximum number of characters in the string, not
	 *                  including the null terminator.
	 * @return the string read, or <tt>null</tt> if no null terminator was found.
	 */
	public String readVirtualMemoryString(int vaddr, int maxLength) {
		Lib.assertTrue(maxLength >= 0);

		byte[] bytes = new byte[maxLength + 1];

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
	 * @param vaddr the first byte of virtual memory to read.
	 * @param data  the array where the data will be stored.
	 * @return the number of bytes successfully transferred.
	 */
	public int readVirtualMemory(int vaddr, byte[] data) {
		return readVirtualMemory(vaddr, data, 0, data.length);
	}

	/**
	 * Transfer data from this process's virtual memory to the specified array. This
	 * method handles address translation details. This method must <i>not</i>
	 * destroy the current process if an error occurs, but instead should return the
	 * number of bytes successfully copied (or zero if no data could be copied).
	 * 
	 * @param vaddr  the first byte of virtual memory to read.
	 * @param data   the array where the data will be stored.
	 * @param offset the first byte to write in the array.
	 * @param length the number of bytes to transfer from virtual memory to the
	 *               array.
	 * @return the number of bytes successfully transferred.
	 */
	public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {
		Lib.assertTrue(offset >= 0 && length >= 0 && offset + length <= data.length);

		byte[] memory = Machine.processor().getMemory();

		// for now, just assume that virtual addresses equal physical addresses
		if (vaddr < 0 || vaddr >= memory.length)
			return 0;

		int amount = Math.min(length, memory.length - vaddr);
		System.arraycopy(memory, vaddr, data, offset, amount);

		return amount;
	}

	/**
	 * Transfer all data from the specified array to this process's virtual memory.
	 * Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
	 * 
	 * @param vaddr the first byte of virtual memory to write.
	 * @param data  the array containing the data to transfer.
	 * @return the number of bytes successfully transferred.
	 */
	public int writeVirtualMemory(int vaddr, byte[] data) {
		return writeVirtualMemory(vaddr, data, 0, data.length);
	}

	/**
	 * Transfer data from the specified array to this process's virtual memory. This
	 * method handles address translation details. This method must <i>not</i>
	 * destroy the current process if an error occurs, but instead should return the
	 * number of bytes successfully copied (or zero if no data could be copied).
	 * 
	 * @param vaddr  the first byte of virtual memory to write.
	 * @param data   the array containing the data to transfer.
	 * @param offset the first byte to transfer from the array.
	 * @param length the number of bytes to transfer from the array to virtual
	 *               memory.
	 * @return the number of bytes successfully transferred.
	 */
	public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
		Lib.assertTrue(offset >= 0 && length >= 0 && offset + length <= data.length);

		byte[] memory = Machine.processor().getMemory();

		// for now, just assume that virtual addresses equal physical addresses
		if (vaddr < 0 || vaddr >= memory.length)
			return 0;

		int amount = Math.min(length, memory.length - vaddr);
		System.arraycopy(data, offset, memory, vaddr, amount);

		return amount;
	}

	/**
	 * Load the executable with the specified name into this process, and prepare to
	 * pass it the specified arguments. Opens the executable, reads its header
	 * information, and copies sections and arguments into this process's virtual
	 * memory.
	 * 
	 * @param name the name of the file containing the executable.
	 * @param args the arguments to pass to the executable.
	 * @return <tt>true</tt> if the executable was successfully loaded.
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
		} catch (EOFException e) {
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
		initialSP = numPages * pageSize;

		// and finally reserve 1 page for arguments
		numPages++;

		if (!loadSections())
			return false;

		// store arguments in last page
		int entryOffset = (numPages - 1) * pageSize;
		int stringOffset = entryOffset + args.length * 4;

		this.argc = args.length;
		this.argv = entryOffset;

		for (int i = 0; i < argv.length; i++) {
			byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
			Lib.assertTrue(writeVirtualMemory(entryOffset, stringOffsetBytes) == 4);
			entryOffset += 4;
			Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) == argv[i].length);
			stringOffset += argv[i].length;
			Lib.assertTrue(writeVirtualMemory(stringOffset, new byte[] { 0 }) == 1);
			stringOffset += 1;
		}

		return true;
	}

	/**
	 * Allocates memory for this process, and loads the COFF sections into memory.
	 * If this returns successfully, the process will definitely be run (this is the
	 * last step in process initialization that can fail).
	 * 
	 * @return <tt>true</tt> if the sections were successfully loaded.
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

			Lib.debug(dbgProcess,
					"\tinitializing " + section.getName() + " section (" + section.getLength() + " pages)");

			for (int i = 0; i < section.getLength(); i++) {
				int vpn = section.getFirstVPN() + i;

				// for now, just assume virtual addresses=physical addresses
				section.loadPage(i, vpn);
			}
		}

		return true;
	}

	/**
	 * Release any resources allocated by <tt>loadSections()</tt>.
	 */
	protected void unloadSections() {
	}

	/**
	 * Initialize the processor's registers in preparation for running the program
	 * loaded into this process. Set the PC register to point at the start function,
	 * set the stack pointer register to point at the top of the stack, set the A0
	 * and A1 registers to argc and argv, respectively, and initialize all other
	 * registers to 0.
	 */
	public void initRegisters() {
		Processor processor = Machine.processor();

		// by default, everything's 0
		for (int i = 0; i < processor.numUserRegisters; i++)
			processor.writeRegister(i, 0);

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

	/**
	 * Terminate the current process immediately. Any open file descriptor belonging
	 * to the process are closed. Any children of the process no longer have a
	 * parent process. status is returned to the parent process as this process's
	 * exit status and can be collected using the join syscall.
	 *
	 * exit() never returns.
	 */
	private int exitHandler(int status) {
		// Do not remove this call to the autoGrader...
		Machine.autoGrader().finishingCurrentProcess(status);
		// ...and leave it as the top of handleExit so that we
		// can grade your implementation.

		// Close all files
		for (int i = 0; i < maxSize; i++) {
			if (files[i] != null)
				closeHandler(i);
		}
		unloadSections();

		coff.close();

		KThread.finish();
		// Kernel.kernel.terminate();

		// if (pid == 0)
		// Kernel.kernel.terminate();
		// else
		// KThread.finish();

		return 0;
	}

	/**
	 * Attempt to open the named disk file, creating it if it does not exist, and
	 * return a file descriptor that can be used to access the file. If the file
	 * already exists, creat truncates it.
	 *
	 * Note that creat() can only be used to create files on disk; creat() will
	 * never return a file descriptor referring to a stream.
	 *
	 * Returns the new file descriptor, or -1 if an error occurred.
	 */
	private int createHandler(int a1) {
		return openCreateJointHandler(a1, true); // This is the same as opn except it will create file
	}

	/**
	 * Attempt to open the named file and return a file descriptor.
	 *
	 * Note that open() can only be used to open files on disk; open() will never
	 * return a file descriptor referring to a stream.
	 *
	 * Returns the new file descriptor, or -1 if an error occurred.
	 */
	private int openHandler(int a1) {
		return openCreateJointHandler(a1, false);
	}

	// Since they both have same functionality
	private int openCreateJointHandler(int fileLoc, boolean createFileIfTrue) {
		for (int i = 2; i < maxSize; i++) {
			// Find first space in array where there is an empty space
			if (files[i] == null) {
				// Read a null-terminated string from this process's virtual memory.
				// Read at most maxLength + 1 bytes from the specified address
				String fileNameFromMemory = readVirtualMemoryString(fileLoc, 256);
				// System.out.println("Attempting to Open - " + fileNameFromMemory);
				if (fileNameFromMemory == null)
					return -1;
				OpenFile openFile = ThreadedKernel.fileSystem.open(fileNameFromMemory, createFileIfTrue);

				if (openFile == null)
					return -1;
				// System.out.println("Successfully Opened - " + fileNameFromMemory);

				files[i] = openFile;
				return i;
			}
		}
		return -1;
	}

	/**
	 * Attempt to read up to count bytes into buffer from the file or stream
	 * referred to by fileDescriptor.
	 *
	 * On success, the number of bytes read is returned. If the file descriptor
	 * refers to a file on disk, the file position is advanced by this number.
	 *
	 * It is not necessarily an error if this number is smaller than the number of
	 * bytes requested. If the file descriptor refers to a file on disk, this
	 * indicates that the end of the file has been reached. If the file descriptor
	 * refers to a stream, this indicates that the fewer bytes are actually
	 * available right now than were requested, but more bytes may become available
	 * in the future. Note that read() never waits for a stream to have more data;
	 * it always returns as much as possible immediately.
	 *
	 * On error, -1 is returned, and the new file position is undefined. This can
	 * happen if fileDescriptor is invalid, if part of the buffer is read-only or
	 * invalid, or if a network stream has been terminated by the remote host and no
	 * more data is available.
	 */
	private int readHandler(int fileDescriptor, int pointer, int count) {
		int bytesLeftToRead = 0;
		int totalBytesRead = 0;
		int currentPos = 0;
		if (fileDescriptor >= maxSize || fileDescriptor < 0)
			return -1; // Base check for descriptor validity
		else if (files[fileDescriptor] == null)
			return -1; // File DNE
		else if (count < 0)
			return -1;
		OpenFile openFile = files[fileDescriptor];
		if (openFile == null)
			return -1;

		// System.out.println("Successfully opened file " + fileDescriptor + " to
		// READ");
		// System.out.println("Attempting to READ " + count + " bytes");

		bytesLeftToRead = count;
		totalBytesRead = 0;
		currentPos = pointer;

		while (bytesLeftToRead > 0) {
			byte[] buffer = new byte[pageSizeCopy];
			int numToLoad = Math.min(pageSizeCopy, bytesLeftToRead);
			int numLoaded = openFile.read(buffer, 0, numToLoad);
			if (numLoaded == -1)
				return -1;

			// Transfer data from the buffer array to currentPos process's virtual memory of
			// length numLoaded
			int bytesRead = writeVirtualMemory(currentPos, buffer, 0, numLoaded);
			bytesLeftToRead = bytesLeftToRead - bytesRead;
			totalBytesRead = totalBytesRead + bytesRead;
			currentPos = currentPos + bytesRead;

			if (bytesRead < numToLoad)
				break;
		}
		return totalBytesRead;
	}

	/**
	 * Helper function to handle write file operation
	 * 
	 * @param fileDescriptor
	 * @param *buffer
	 * @param int
	 * 
	 *                       Attempt to write up to count bytes from buffer to the
	 *                       file or stream referred to by fileDescriptor. write()
	 *                       can return before the bytes are actually flushed to the
	 *                       file or stream. A write to a stream can block, however,
	 *                       if kernel queues are temporarily full.
	 *
	 *                       On success, the number of bytes written is returned
	 *                       (zero indicates nothing was written), and the file
	 *                       position is advanced by this number. It IS an error if
	 *                       this number is smaller than the number of bytes
	 *                       requested. For disk files, this indicates that the disk
	 *                       is full. For streams, this indicates the stream was
	 *                       terminated by the remote host before all the data was
	 *                       transferred.
	 *
	 *                       On error, -1 is returned, and the new file position is
	 *                       undefined. This can happen if fileDescriptor is
	 *                       invalid, if part of the buffer is invalid, or if a
	 *                       network stream has already been terminated by the
	 *                       remote host.
	 * 
	 */
	private int writeHandler(int fileDescriptor, int pointer, int count) {
		// int bytesLeftToWrite;
		int totalBytesWritten = 0;
		int retVal = 0; // This is to be returned

		if (fileDescriptor >= maxSize || fileDescriptor < 0)
			return -1;
		else if (files[fileDescriptor] == null)
			return -1;
		else if (pointer <= 0)
			return -1;
		else if (count < 0)
			return -1;

		OpenFile openFile = files[fileDescriptor];
		if (openFile == null)
			return -1; // Check if file exists

		byte[] buffer = new byte[count];
		// int numToLoad = Math.min(bytesLeftToWrite, pageSizeCopy); // to prevent page
		// faults
		int numLoaded = readVirtualMemory(pointer, buffer, 0, count);
		if (numLoaded < 0)
			return -1;
		retVal = openFile.write(buffer, 0, numLoaded);

		if (count != retVal)
			return -1; // number of bytes written matches count.

		return retVal;
	}

	private int closeHandler(int description) {
		if (description >= maxSize || description < 0)
			return -1;
		if (files[description] != null) {
			files[description].close();
			files[description] = null;
			return 0;
		}
		return -1;

	}

	/**
	 * Delete a file from the file system.
	 *
	 * If another process has the file open, the underlying file system
	 * implementation in StubFileSystem will cleanly handle this situation (this
	 * process will ask the file system to remove the file, but the file will not
	 * actually be deleted by the file system until all other processes are done
	 * with the file).
	 *
	 * Returns 0 on success, or -1 if an error occurred.
	 */
	private int unlinkHandler(int virtualMem) {
		String fileName = readVirtualMemoryString(virtualMem, 256);
		if (fileName == null)
			return -1;
		if (ThreadedKernel.fileSystem.remove(fileName)) {
			return 0;
		}
		return -1;
	}

	// private int handleExec(int adder, int count, int pointer) {
	// String s = readVirtualMemoryString(adder, 256);

	// if (s == null)
	// return -1;
	// else if (count < 0 || argc > 16)
	// return -1;
	// int newCount = count * 4;
	// byte[] buffer = new byte[newCount];
	// int read = readVirtualMemory(pointer, buffer, 0, newCount);
	// if (read < buffer.length)
	// return -1;
	// int[] address = new int[count];
	// String[] s1 = new String[count];
	// for (int i = 0; i < count; i++) {
	// address[i] = Lib.bytesToInt(buffer, i * 4);
	// }
	// for (int j = 0; j < count; j++) {
	// s1[i] = readVirtualMemoryString(address[i], 256);
	// if (s1[i] == null)
	// return -1;
	// }

	// UserProcess child = newUserProcess();
	// child.parent = this;
	// int childID = -1;
	// UserKernel.pidLock.acquire();

	// if (child.execute(file, s1)) {
	// childID = child.pid;
	// childrenList.add(childID);
	// }

	// UserKernel.pidLock.release();
	// return childID;
	// }

	// private int handleJoin() {
	// return 0;
	// }

	private static final int syscallHalt = 0, syscallExit = 1, syscallExec = 2, syscallJoin = 3, syscallCreate = 4,
			syscallOpen = 5, syscallRead = 6, syscallWrite = 7, syscallClose = 8, syscallUnlink = 9;

	/**
	 * Handle a syscall exception. Called by <tt>handleException()</tt>. The
	 * <i>syscall</i> argument identifies which syscall the user executed:
	 * 
	 * <table>
	 * <tr>
	 * <td>syscall#</td>
	 * <td>syscall prototype</td>
	 * </tr>
	 * <tr>
	 * <td>0</td>
	 * <td><tt>void halt();</tt></td>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td><tt>void exit(int status);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>2</td>
	 * <td><tt>int  exec(char *name, int argc, char **argv);
	 * 								</tt></td>
	 * </tr>
	 * <tr>
	 * <td>3</td>
	 * <td><tt>int  join(int pid, int *status);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>4</td>
	 * <td><tt>int  creat(char *name);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>5</td>
	 * <td><tt>int  open(char *name);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>6</td>
	 * <td><tt>int  read(int fd, char *buffer, int size);
	 * 								</tt></td>
	 * </tr>
	 * <tr>
	 * <td>7</td>
	 * <td><tt>int  write(int fd, char *buffer, int size);
	 * 								</tt></td>
	 * </tr>
	 * <tr>
	 * <td>8</td>
	 * <td><tt>int  close(int fd);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>9</td>
	 * <td><tt>int  unlink(char *name);</tt></td>
	 * </tr>
	 * </table>
	 * 
	 * @param syscall the syscall number.
	 * @param a0      the first syscall argument.
	 * @param a1      the second syscall argument.
	 * @param a2      the third syscall argument.
	 * @param a3      the fourth syscall argument.
	 * @return the value to be returned to the user.
	 */
	public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
		switch (syscall) {
		case syscallHalt:
			return handleHalt();
		case syscallExit:
			return exitHandler(a0);
		case syscallWrite:
			return writeHandler(a0, a1, a2); // int fileDescriptor, void *buffer, int count
		case syscallCreate:
			return createHandler(a0); // char *name
		case syscallOpen:
			return openHandler(a0);
		case syscallClose:
			return closeHandler(a0);
		case syscallRead:
			return readHandler(a0, a1, a2);

		default:
			Lib.debug(dbgProcess, "Unknown syscall " + syscall);
			Lib.assertNotReached("Unknown system call!");
		}
		return 0;
	}

	/**
	 * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt> .
	 * The <i>cause</i> argument identifies which exception occurred; see the
	 * <tt>Processor.exceptionZZZ</tt> constants.
	 * 
	 * @param cause the user exception that occurred.
	 */
	public void handleException(int cause) {
		Processor processor = Machine.processor();

		switch (cause) {
		case Processor.exceptionSyscall:
			int result = handleSyscall(processor.readRegister(Processor.regV0), processor.readRegister(Processor.regA0),
					processor.readRegister(Processor.regA1), processor.readRegister(Processor.regA2),
					processor.readRegister(Processor.regA3));
			processor.writeRegister(Processor.regV0, result);
			processor.advancePC();
			break;

		default:
			System.out.println("\nCause of exception - " + cause);
			System.out.println("data - \n" + Processor.exceptionNames[cause]);
			Lib.debug(dbgProcess, "Unexpected exception: " + Processor.exceptionNames[cause]);
			Lib.assertNotReached("Unexpected exception");
		}
	}

	// From Writeup
	// Each file that a process opens should have a unique file descriptor
	// associated with it (see syscall.h for details).
	// The file descriptor should be a non-negative integer that is simply used to
	// index into a table of currently-open files by that process.
	// Your implementation should have a file table size of 16, supporting up to 16
	// concurrently open files per process.
	// Note that a given file descriptor can be reused if the file associated with
	// it is closed,
	// and that different processes can use the same file descriptor value to refer
	// to different files
	// (e.g., in every process file descriptor 0 refers to stdin).

	// Your implementation should have a file table size of 16, supporting up to 16
	// concurrently open files per process.
	private int maxSize = 16;
	private OpenFile[] files = new OpenFile[maxSize]; // Array of files

	// TODO understand
	public int pid;
	public UserProcess parent;

	final int pageSizeCopy = 1024;
	// public LinkedList<Integer> childrenList = new LinkedList<Integer>();

	/** The program being run by this process. */
	protected Coff coff;

	/** This process's page table. */
	protected TranslationEntry[] pageTable;

	/** The number of contiguous pages occupied by the program. */
	protected int numPages;

	/** The number of pages in the program's stack. */
	protected final int stackPages = 8;

	/** The thread that executes the user-level program. */
	protected UThread thread;

	private int initialPC, initialSP;

	private int argc, argv;

	private static final int pageSize = Processor.pageSize;

	private static final char dbgProcess = 'a';
}
