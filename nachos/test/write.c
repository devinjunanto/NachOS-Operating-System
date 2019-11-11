/*
 * write1.c
 *
 * Write a string to stdout, one byte at a time.  Does not require any
 * of the other system calls to be implemented.
 * Geoff Voelker
 * 11/9/15
 */

#include "syscall.h"

int main(int argc, char *argv[])
{
	int val = 0;
	val = write1();
	val = write2();
	val = write3();
	return val;
}

int write1()
{
	printf("\nRunning Write1\n\n");
	char *str = "\nroses are red\nviolets are blue\nI hate Nachos\nand so do you\n\n";

	while (*str)
	{
		int r = write(1, str, 1);
		if (r != 1)
		{
			printf("failed to write character (r = %d)\n", r);
			exit(-1);
		}
		str++;
	}

	return 0;
}

//Write to file test
int write2()
{
	printf("\nRunning Write2\n\n");
	char *str = "\nroses are red\nviolets are blue\nI hate Nachos\nand so do you\n\n";

	while (*str)
	{
		int fileDesc = creat("writeTest.txt");
		int r = write(fileDesc, str, 1);
		if (r != 1)
		{
			printf("failed to write character (r = %d)\n", r);
			exit(-1);
		}
		str++;
	}

	return 0;
}

int write3()
{
	char *str = "a";

	for (int i = 0; i < 1030; i++)
	{
		str = str + 'a';
	}

	printf(str);
}