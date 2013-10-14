####################################################
Combo OBDA Tool v0.1
Theory of Artificial Intelligence Group, University of Bremen, Germany
####################################################

==================
DIRECTORY STRUCTURE
==================
Combo.jar: Main library
combo.sh: The command line interface to Combo
eugentobulk.sh: The command line interface for generating a bulk load file from EUGen data
owlfilestobulk.sh: The command line interface for a generic bulk load file generator
config/: Config files for Combo
COPYING.txt: The license
examples/: Example queries
lib/: The required Java libraries
README.txt: This README file
udf/: C++ source code and Makefile for generating filters as user-defined functions (UDFs).

==================
INSTALLATION
==================

Besides a standard Java installation (Java 6 or above), Combo requires the following software:
1. IBM DB2,
2. GNU C++ compiler,
3. GNU Make,
4. Boost C++ libraries (http://www.boost.org/).

A free version of Item 1, namely IBM DB2 Express-C, can be downloaded from IBM's webpage. For our tests, we used Express-C version 9.7. 
Items 2-4 can most probably be installed via your Linux distribution's standard package management interface. 
If you are a Mac user, then you need XCode for 2 and 3. For 4, it makes sense to install MacPorts (http://www.macports.org/).
We have not tested the system on Windows.

Once you believe that these software are installed successfully, check that Combo can actually compile user-defined functions in DB2.
This is as simple as running the command

make filter

in udf/ directory of your Combo distribution. 
This command will most probably produce errors in your first run even if you successfully installed GNU Make and the C++ compiler. 
In that case, open udf/Makefile with your favourite text editor. Set the variable DB2_INCLUDE_DIR accordingly. Try running the command again.
If you are still getting errors and they are about the Boost header files, try doing the suggestion in the Makefile.

Combo requires a database in your DB2 server to run. It manages its own tables, stored procedures, etc in this database. 
Because of this reason, it makes sense to create a new database just for Combo.
A database in DB2 can be created as follows.
1. If the DB2 server is not already running, then log in as your database user and start the DB2 server with the command 'db2start'.
2. Once the server is running, enter the following on the command line
   
db2 create database combapp
   
where combapp is the name of your database. Note that the command above may take a while to create the database.
Once you have the database ready, enter the database connection information for Combo to the file config/app.properties as follows.

dburl = # the JDBC URI for your database, e.g., jdbc:db2://localhost:50001/combapp
user = # the DB2 user, e.g., db2inst1
password = # password of the user

Combo often uses the LOAD command from DB2 for its operations. 
This command seems to require a hard disk with a decent amount of RPM to function properly.
Therefore, we strongly encourage you to use a proper server machine in order to run Combo.
As a side note, our relatively decent unibody aluminum laptop did not perform satisfactorily while running LOAD.

==================
USAGE
==================

BASICS

First, initialize your database with stored procedures and tables that Combo uses as follows

./combo.sh Combo.jar init

Note that this command also deletes all the data in the database dedicated to Combo.

The command line interface to Combo is pretty self-explanatory; you can run 

./combo.sh help

anytime to see the details of the commands. 

It is important that you run "combo.sh" from the directory where Combo.jar is located.

PROJECT MANAGEMENT

Combo allows the users to have multiple ontologies in the database through 'projects'. 
Before you load your own ontology to the Combo database, you need to create a project for it using

./combo.sh create <project_name>

Note that project_name should not contain spaces and be less than 20 characters long.

You are now ready to load your ontology to the database.
The detailed instructions for that are given in the LOADING DATA section.
For now, we assume that you have successfully loaded your data.
After the data is loaded, it is important to update the statistics of your project's tables via 

./combo.sh runstats <project_name>

for the database system to choose better plans for your queries.
   
After the data is loaded, the original ABox can be completed w.r.t. to TBox with the following command:

./combo.sh complete <project_name>

Note that this command adds the new assertions to the same project it is called with.
So if you want to preserve your original data, load this data to two different projects first, and then complete one of them.
We also suggest to run 'runstats' on the project once the completion is done.

If you want to take a back up of your project in the database to a bulk load file, run the following command

./combo.sh export --project <project_name> --file <backup_file_path>

You can later load this file to a project in the Combo database.

You can use the 'list' command for listing your projects and the 'delete' command to delete a given project.

LOADING DATA

You can load data to Combo in bulk format only using bulk load files. 
These bulk load files are generated by special Combo bulk load file generators.
1. eugentobulk.sh: This translates the data generated by EUGen to a Combo bulk load file.

command:
   ./eugentobulk.sh
        -tbox <the_path_to_the_owl_ontology>
        -datadir <where_the_EUGen_generated_input_data_is_located>
        -output <the_path_to_the_output_bulk_load_file>

options:
   -tbox The OWL file to use as the TBox. Ideally, this is an ontology coming from the EUGen distribution.
   -datadir EUGen writes the generated data to several OWL files in a directory. With this option, you specify that directory.
   -output This is the path to the bulk load file that is going to be generated.

example:
    ./eugentobulk.sh -tbox ../EUGen/ontologies/lubm-ex-20.owl -datadir /tmp/modlubm -output /tmp/combobulk/lubm20.combo

2. owlfilestobulk.sh: This translates a DL-Lite_R ontology in an OWL file to a Combo bulk load file.

command:
   ./owlfilestobulk.sh
        -dir <a_directory_containing_OWL_files>
        -output <the_path_to_the_output_bulk_load_file>

options:
   -dir A directory containing TBox and ABox files. Note that the generator goes through every file with the extension .owl in that directory.
   -output This is the path to the bulk load file that is going to be generated.

example:
    ./owlfilestobulk.sh -dir /tmp/data -output /tmp/combobulk/mydata.combo

Once you have a bulk load file, then you can load it to your project using

./combo.sh load --file <path_to_the_bulk_file> --project <project_name>

QUERYING DATA

Combo also supports querying your projects.
In particular, Combo supports the following rewriting operations.
1. Rewriting using a filter: Combo generates a filter (a UDF in the database) for a given conjunctive query (CQ) and project. 
   Then it outputs a rewriting of the CQ in SQL with the filter condition attached.
   Note that for this SQL query to return correct results, you must run it over completed data!
   Basic usage of the command is as follows

   ./combo.sh rwf --file <CQ_in_a_file> --project <project_name> --uri <uri_to_prepend_to_concept_and_role_names_in_the_query#>

   Please check the file examples/cq.txt for an example of a CQ; and use the 'help' command for checking further options to the command 'rwf'.

   Combo stores the binary file and the source code for a generated filter under the relevant directory in udf/tmp/.
   In order to clean previously generated filters, you can empty the directory udf/tmp/ and then run the following command

   ./combo.sh dropf

   which removes from the database the registration info of these UDFs.

2. Non-recursive Datalog rewritings: Combo can generate an equivalent SQL query from a given non-recursive Datalog program (and project).
   The generated SQL query, in general, does not correspond to a union of CQs (UCQs); it is a nested SQL query and it does not use Common Table Expressions feature of SQL, i.e., the WITH command.
   Run these rewritings only on pure, i.e., not completed, data.
   
   Basic usage of the command is as follows

   ./combo.sh rwd --file <Datalog_program_in_a_file> --project <project_name> --uri <uri_to_prepend_to_concept_and_role_names_in_the_query#>

   Please check the file examples/datalog.txt for an example of a non-recursive Datalog program; and use the 'help' command for checking further options to the command 'rwd'.

==================
CONTACT
==================

combo-obda-users@googlegroups.com