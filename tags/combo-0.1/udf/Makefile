# If the Boost header files are not automatically detected by the C++ compiler, then you can try adding the following line to ~/.profile
#
# export CPLUS_INCLUDE_PATH=/opt/local/include
#
# where the value is the directory that the Boost header files reside.

# The following variable must be set to the correct path of the DB2 header files 
DB2_INCLUDE_DIR=/opt/ibm/db2/V9.7/include

# Change it to -m32 if you are not using a 64-bit environment
ARCHITECTURE=-m64

CXX=g++

test: CXX += -DDEBUG -g

test: filter.o
	${CXX} filtertest.cpp ${ARCHITECTURE} -o runtests filter.o 

filter: filter.o db2interface.o
	${CXX} -shared -lpthread -o libDB2Filter.dylib filter.o db2interface.o

filter.o: filter.cpp filter.h globalvars.h 
	${CXX} -c filter.cpp -fpic -D_REENTRANT ${ARCHITECTURE} -o filter.o

db2interface.o: db2interface.cpp filter.h ${DB2_INCLUDE_DIR}/sqludf.h
	${CXX} -c db2interface.cpp -I${DB2_INCLUDE_DIR} -fpic -D_REENTRANT ${ARCHITECTURE} -o db2interface.o

fake_filter: db2interface_fake.o
	cc -shared -lpthread -o libDB2FakeFilter.dylib db2interface_fake.o

db2interface_fake.o: db2interface_fake.c
	cc -c db2interface_fake.c -fpic -D_REENTRANT ${ARCHITECTURE} -o db2interface_fake.o

clean:
	rm -f *.o *.dylib runtests