// Tests for read - works on both stdin and on files;
//checks arguments for correctness (valid file descriptor, file buffer pointer, count);
//the data read is actually the data stored in the file;
//the number of bytes read may be less than count.

#include "syscall.h"
#include "stdlib.h"

int main(int argc, char *argv[])
{
    int val = 0;
    val = read1();
    //val = create2();
    return val;
}

// Create a file that doesnt exist
int read1()
{
    char buffer[80];
    
}