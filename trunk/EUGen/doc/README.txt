####################################################
Extended University Data Generator v0.1
based on Univ-Bench Artificial Data Generator (UBA) V1.7 at http://swat.cse.lehigh.edu/projects/lubm/
Theory of Artificial Intelligence Group, University of Bremen, Germany
####################################################

==================
DIRECTORY STRUCTURE
==================
profile.txt: The data generator profile.
README.txt: This README file.
COPYING.txt: The license.
EUGen.jar: The data generator. 

==================
USAGE
==================

command:
   java -classpath $CLASSPATH de.unibremen.informatik.tdki.ExtendedGenerator
      	[-univ <univ_num>]
	[-index <starting_index>]
	[-seed <seed>]
	[-daml]
        [-hole <incompleteness_percentage>]
        [-subclass <no_of_subject_subclasses>]
        [-dir <output_directory>]
	-onto <ontology_url>

options:
   -univ number of universities to generate; 1 by default
   -index starting index of the universities; 0 by default
   -seed seed used for random data generation; 0 by default
   -daml generate DAML+OIL data; OWL data by default
   -hole the incompleteness percentage of the data; 5 by default
   -subclass the no of subject subclasses for Department, Course, Student, and Professor; 20 by default
   -dir the output directory to place the generated files
   -onto url of the univ-bench ontology

note:
    EUGen.jar should be on $CLASSPATH.

==================
CONTACT
==================

Inanc Seylan	seylan@informatik.uni-bremen.de
