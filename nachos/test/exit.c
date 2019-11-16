/* 
 * exit1.c
 *
 * It does not get simpler than this...
 */

#include "syscall.h"

int main(int argc, char *argv[])
{
    return exit1();
}

int exit1()
{
    exit(123);
}