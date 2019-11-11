/*
 * open1.c
 * Test basic Open Functionality - open a file that doesnt exists on disk
 */

#include "syscall.h"
#include "stdlib.h"

int main(int argc, char *argv[])
{
    printf("\nRunning Open1\n\n");
    printf("Opening non existent file");
    char *name = "nonExistentFile.txt";

    int val = open(name);
    if (val == -1)
        printf("OKAY - unable to open file");
    else
        printf("BAD - -1 not returned");

    return 0;
}