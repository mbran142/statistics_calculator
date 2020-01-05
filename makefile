#Command to run: java -Djava.library.path=. {Program} (no extension)
J_NAME = StatisticsCalculator
C_NAME = StatisticsSimulation
SO_NAME = libstats.so

#JAVA_HOME = $(JAVA_HOME)
C_OBJS = $(C_NAME).c
H_OBJ = $(C_NAME).h
J_OBJS = $(J_NAME).java
CLASS_OBJ = *.class

all : $(H_OBJ) $(SO_NAME)

$(SO_NAME) : $(C_OBJS) $(H_OBJ)
	-gcc -fPIC -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/linux" -shared -g -o $@ $<

$(H_OBJ) : $(CLASS_OBJ)
	-javah $(J_NAME)

$(CLASS_OBJ) : $(J_OBJS)
	-javac $<

clean :
	-rm $(OBJ_NAME) $(SO_NAME) $(CLASS_OBJ)