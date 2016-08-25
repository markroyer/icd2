
## Errors 

### Runtime Errors

If you are trying to run this program on linux with openjdk8, please be aware that there is a bug that prevents it from running properly (see <https://www.eclipse.org/forums/index.php/t/1071717/>).  In the meantime go and download the Oracle JDK.

### Build Errors

Sometimes you can get build errors like the following.

	[WARNING] Mirror tool: Problems resolving provisioning plan.: [Unable to
	satisfy dependency from edu.umaine.cs.icd2.product 1.0.0.201512052217 to
	org.eclipse.e4.rcp.feature.group [1.4.0.v20150903-1804].]
	[WARNING] More information on the preceding warning(s) can be found here:
	[WARNING] - http://wiki.eclipse.org/Tycho_Messages_Explained#Mirror_tool

This was initially fixed by just changing to an older version of tycho.  But ultimately, the fix was to remove the version information for the feature org.eclipse.rcp in the edu.umaine.cs.icd2.product file.

## Microsoft Libraries

Currently you need to have the dll files in the lib directory copied to the 

	C:\Windows\System32

directory in order for he HDFJava libraries to correctly load at runtime.  This is a result of using Java Native Interface in the HDFJava librarires.