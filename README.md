# Gradescope-Autograder
Calls the UNC Grader from Gradescope
Written by Andrew Vitkus


The gradescope autograder interface works by taking a zip file with a few specified scripts and anything else needed. The setup.sh script is run when the docker container for that assignment's grader is created. This is where you specify any dependencies you need installed. The other script is run_autograder and is called to grade each assignment. When gradescope grades an assignment, the student submission will be stored in /autograder/submission and all extra files included in the zip file will be in /autograder/source.

I created two additional Java programs: one for setting up the grader's expected submission structure and one for converting the grader's output into gradescopes expected result. The run_autograder script starts by setting up the classpath to include checkstyle, object editor, and UNC checks. It then runs the AssignmentSetup project code to do the conversion to the grader's structure, taking the course name and assignment number as arguments. The code for this program has a bunch of named constants including the grader's main class and the grader jar file's name, so those will need to be updated to reflect the correct values or the jar created such that the hard-coded ones are correct. This program also writes a new script that will actually invoke the grader and the less intuitively named GradescopeRunner project.

After the run_autograder script finishes running AssignmentSetup, it makes the new grading script executable and runs it. This finishes setting up the grader then executes it. The last thing this new script does is run GradescopeRunner to parse the grader's output and create the json file that gradescope uses to get the final results.

The gradescope API doesn't provide the student's name to the autograder so I use grade, me (student). Since they use a single docker container for all grading, there is no connection between submissions since the container is reinitialized for each run.
