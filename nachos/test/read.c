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
    val = read2();
    return val;
}

// Open an existing file and load its contents with count greater than text in file
int read1()
{
    int len = 500;
    char buffer[len];
    printf("\nRunning Read1\n\n");
    int fd = open("writeTest.txt");
    int val = read(fd, buffer, len);
    printf(buffer);
    return 0;
}

// read large file and print
int read2()
{
    int len = 3000;
    char buffer[len];
    printf("\nRunning Read1\n\n");
    int fd = open("LONGwriteTest.txt");
    int val = read(fd, buffer, len);
    printf(buffer);
    printf("Val - %d/2000 ",val);
    return 0;
}

