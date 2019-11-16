/* 
 * exit1.c
 *
 * It does not get simpler than this...
 */

#include "syscall.h"

int main(int argc, char *argv[])
{
    exit1();
}

int exit1()
{
    printf("\n\nRunning exit1\n");
    exit(123);
}