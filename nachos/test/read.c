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
    //printf(buffer);
    printf("just Read - %d/%d ",val, len);
    int file2 = creat("LONGwriteTestCopy.text");
    val = write(file2, buffer, len);
    printf("just wrote - %d/%d in file number ", val, len, file2 );
    return 0;
}

