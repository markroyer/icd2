
# ICD2 Tests

## Eclipse testing

Run these tests in Eclipse as JUnit Plug-in tests.  Append to the
program arguments

``` -testproperties true ```

This will make it so that the program will automatically select the
correct setup for testing the program.

## Maven testing

To build the project and run the tests, type

``` mvn clean verify ```

in the `edu.umaine.cs.icd2.parent` directory.
 
