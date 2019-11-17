/* 
 * exit1.c
 *
 * It does not get simpler than this...
 */

#include "syscall.h"

int main(int argc, char *argv[])
{
    printf("HERE in exit1");
    exit(123); //return 0;
}