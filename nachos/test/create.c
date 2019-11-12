/*
 * create.c
 * Test basic Open Functionality 
 */

#include "syscall.h"
#include "stdlib.h"

int main(int argc, char *argv[])
{
    int val = 0;
    val = create1();
    val = create2();
    return val;
}

// Create a file that doesnt exist
int create1()
{
    printf("\nRunning Open1\n\n");
    printf("Opening non existent file\n");
    char *name = "nonExistentFile.txt";

    int val = creat(name);
    if (val == -1)
        printf("BAD - Unable create \n");
    else
        printf("Created with file desc - %d\n",val);

    return 0;
}

// Create on existing file and print fileDescriptor
int create2()
{
    printf("\nRunning Open2\n\n");
    printf("Creating existing File test.txt\n");
    char *name = "test.txt";

    int val = creat(name);

    printf("The file descriptor is - %d\n", val);

    return 0;
}
