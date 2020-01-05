#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "StatisticsDisplay.h"

#define DEFAULT_ONLY -1
#define SPEC_SIZE 8
#define UNREDUCED_NUMERATOR 0
#define UNREDUCED_DENOMINATOR 1
#define NUMERATOR 2
#define DENOMINATOR 3
#define DATA_SIZE 4
#define OUT_SIZE 300
#define BUF_SIZE 30

typedef struct Specifications {
	int amount;
	int sample;
	int specific;
	bool less;
	bool equal;
	bool greater;
	bool consecutive;
	bool print;
} specifications;

//convert input java specification array to a spec structure
void arrToSpec(int [], specifications*);

//runs the simulation based on specifications. returns data in form of long long array
long long *runSimulation(specifications, int[], int);

//checks if an iteration of a sequence matches the specs
int check(int*, specifications, int, int);

//rotates the sequence one time
void rotate_one(int*, int);

//rotates the seucnes
void rotate(int*, int);

//puts together the output msg
void constructOutputMsg(char[], long long[]);

JNIEnv* genv;
jobject gthisObj;

JNIEXPORT jstring JNICALL Java_StatisticsDisplay_doSimulation
	(JNIEnv* env, jobject thisObj, jintArray javaSpec, jintArray javaSeq)
{
	genv = env;
	gthisObj = thisObj;

	jint seqSize, *inSpec, *inSeq;
	
	inSpec = (*env)->GetIntArrayElements(env, javaSpec, NULL);
	inSeq = (*env)->GetIntArrayElements(env, javaSeq, NULL);
	seqSize = (*env)->GetArrayLength(env, javaSeq);

	specifications spec;
	arrToSpec(inSpec, &spec);

	long long *data = runSimulation(spec, inSeq, seqSize);

	char outMsg[1000];
	constructOutputMsg(outMsg, data);

	free(data);
	return (*env)->NewStringUTF(env, outMsg);
}

void constructOutputMsg(char outMsg[], long long data[]) {

	long long temp;
	char buf[BUF_SIZE];
	int bufIndex;
	int msgIndex = 0;

	for (int i = 0; i < DATA_SIZE; i++) {

		temp = data[i];
		bufIndex = 0;

		while (temp) {
			buf[bufIndex++] = temp % 10 + '0';
			temp /= 10;
		}
		
		while (bufIndex)
			outMsg[msgIndex++] = buf[--bufIndex];

		outMsg[msgIndex++] = ' ';
	}
	
	outMsg[msgIndex] = '\0';
}

long long *runSimulation(specifications spec, int seq[], int size) {

	long long simulations = 0;
	long long passed = 0;

	int* rotations = (int *) malloc (sizeof *rotations * size);
	rotations[0] = 1;

	for (int i = 1, rot = 1; i < size; i++) {

		if (seq[i] != seq[i - 1])
			rot++;

		rotations[i] = rot;
	}

	bool done = false;
	int scope = 0;
	int num = 1;

	//case in which every number in the sequence is the same
	if (rotations[size - 1] == 1) {
		passed = check(seq, spec, num, size);
		simulations = 1;
		done = true;
	}

	while (!done) {

		//get new scope
		scope = 1;

		while (scope > size && seq[0] == seq[scope]) 
			scope++;

		if (scope == 1) {
			while (scope > size && seq[1] == seq[scope])
				scope++;
			scope--;
		}

		for (int i = 0; i <= scope; i++) {
			passed += check(seq, spec, num++, size);
			simulations++;
			rotate_one(seq, scope);
		}

		scope++;
		if (scope == size)
			done = true;

		else while (!(--rotations[scope])) {

			rotate(seq, scope);
			scope++;

			if (scope == size) {
				done = true;
				break;
			}
		}

		//reseting scope resolutions
		if (!done) {
			rotate(seq, scope);

			for (int i = 1, rot = 1; i < scope; i++) {

				if (seq[i] != seq[i - 1])
					rot++;

				rotations[i] = rot;
			}
		}
	}

	free(rotations);

	long long *data = (long long*) malloc(sizeof *data * DATA_SIZE);
	data[NUMERATOR] = data[UNREDUCED_NUMERATOR] = passed;
	data[DENOMINATOR] = data[UNREDUCED_DENOMINATOR] = simulations;

	if (data[NUMERATOR] == 0)
		data[DENOMINATOR] = 1;

	//reduce fraction
	for (int i = 2; i <= data[NUMERATOR]; i++) {
		if (data[NUMERATOR] % i == 0 && data[DENOMINATOR] % i == 0) {
			data[NUMERATOR] /= i;
			data[DENOMINATOR] /= i--;
		}
	}

	return data;
}

int check(int* seq, specifications spec, int num, int size) {

	int count = 0;
	int maxCount = 0;

	if (!spec.consecutive) {

		for (int j = 0; j < (spec.sample - (spec.amount - 1)); j++) {

    		for (int h = j; h < spec.sample; h++) {

        		if (seq[j] == seq[h] && (spec.specific == DEFAULT_ONLY || spec.specific == seq[j]))
        		    count++;

			}

    maxCount = count > maxCount ? count : maxCount;
    count = 0;
		}
	}

	else {

		count =  1;
		maxCount = 1;

		for (int j = 0; j < spec.sample - 1; j++) {

			if (seq[j] == seq[j + 1] && (spec.specific == DEFAULT_ONLY || spec.specific == seq[j]))
				count++;
			
			else {
				maxCount = count > maxCount ? count : maxCount;
				count = 1;
			}
		}

		maxCount = count > maxCount ? count : maxCount;
	}

	bool check = (maxCount == spec.amount && spec.equal == 1)
				|| (maxCount < spec.amount && spec.less == 1)
				|| (maxCount > spec.amount && spec.greater == 1);

	if (spec.print) {

		char outMsg[OUT_SIZE];
		char buf[BUF_SIZE];
		int outIndex = 0, bufIndex, temp;
		outMsg[outIndex++] = '|';
		outMsg[outIndex++] = ' ';

		for (int i = 0; i < size; i++) {

			temp = seq[i];
			bufIndex = 0;

			if (!temp)
				buf[bufIndex++] = '0';
			else while (temp) {
				buf[bufIndex++] = temp % 10 + '0';
				temp /= 10;
			}
			
			while (bufIndex) {

				outMsg[outIndex++] = buf[--bufIndex];

				if (outIndex > OUT_SIZE - 5) {
					spec.print = false;
					jclass thisClass = (*genv)->GetObjectClass(genv, gthisObj);
					jmethodID consoleCallBack = (*genv)->GetMethodID(genv, thisClass, "printToConsole", "(Ljava/lang/String;)V");
					(*genv)->CallVoidMethod(genv, gthisObj, consoleCallBack, (*genv)->NewStringUTF(genv, "Sequence too long to print!\n"));
					return check ? 1 : 0;
				}
			}

			outMsg[outIndex++] = ' ';
			if (i == spec.sample - 1) {
				outMsg[outIndex++] = '|';
				outMsg[outIndex++] = ' ';
			}
		}

		if (check)
			outMsg[outIndex++] = '<';

		outMsg[outIndex++] = '\n';
		outMsg[outIndex] = '\0';

		jclass thisClass = (*genv)->GetObjectClass(genv, gthisObj);
		jmethodID consoleCallBack = (*genv)->GetMethodID(genv, thisClass, "printToConsole", "(Ljava/lang/String;)V");
		(*genv)->CallVoidMethod(genv, gthisObj, consoleCallBack, (*genv)->NewStringUTF(genv, outMsg));
	}

	return check ? 1 : 0;
}
 
void rotate_one(int* seq, int max) {

		int	temp = seq[max];

		for (int i = max - 1; i >= 0; i--)
			seq[i + 1] = seq[i];

		seq[0] = temp;
}

void rotate(int* seq, int max) {

	int num = 1;
	int temp;

	for (int i = max - 1; seq[i] == seq[max] && i >= 0; i--)
		num++;

	while (num) {

		temp = seq[max];

		for (int i = max - 1; i >= 0; i--)
			seq[i + 1] = seq[i];

		seq[0] = temp;
		num--;
	}
}

void arrToSpec(int inSpec[], specifications* spec) {

	spec->amount = inSpec[0]; //must be typed explicitly; between 1 and seqSize
	spec->sample = inSpec[1]; //default is seqSize
	spec->specific = inSpec[2]; //none selected is -1, otherwise selected
	spec->less = inSpec[3];
	spec->equal = inSpec[4];
	spec->greater = inSpec[5];
	spec->consecutive = inSpec[6];
	spec->print = inSpec[7];
}