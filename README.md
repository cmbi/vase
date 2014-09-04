# VASE


Visualization of Alignments Structures and Entropy

# Configuration:

The configuration file is at src/main/resources/config.properties

If you want the users to be able to submit custom structures, then you must set 'hsspcache' to a valid writable directory.

If you only want VASE to display the content of a series of VASE-format xml files, then you can set 'xmlonly' to true and set the 'cache' path to the directory where the files are located.

# Build & Install

VASE requires maven 2 to compile. (http://maven.apache.org/) You can build the war file by running "mvn package" from the root directory. Then deploy it.
