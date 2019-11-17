Shardul Saiya A13964199
Devin Junanto A15754079

openCreateJointHandler
Find the first empty space in the array files
Read a null-terminated string from this process's virtual memory
Read at most maxLength + 1 bytes from the specified address
Return -1 if error
Open the file from memory

readHandler
Return -1 if the fileDescriptor error
Open the file
Transfer data from the buffer array to currentPos process's virtual memory of length numLoaded

writeHandler
Return -1 if error
Open the file
Write to file

closeHandler
Return -1 if error
Close the file

unlinkHandler
Return -1 if error
Unlink

loadSections
Acquire physical pages lock
Allocates the pageTable and the number of physical pages based on the size of numpages
Find next available page
Release the physical lock

readVirtualMemory
Read from virtual memory

writeVirtualMemory
Write to virtual memory

joinHandler
Return -1 if error
Parent acquire exit status from child

execHandler
Return -1 if error
Acquire virtual address
Create new child process
Execute user program

exitHandler
Close everything

The code works well for everything and passed the test, except for the joinHandler.

We use the test that the write up provided.

Devin wrote the code for read, write, close, exec, and unlink.
Shardul debugged and editted the read, write, close, exec, and unlink so it become readable.
Shardul also wrote the code creat, open, exit, loadSections, readVirtualMemory, and writeVirtualMemory.
We together wrote the code for join.