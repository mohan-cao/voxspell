# VoxSpell Spelling Aid #

Installation instructions:

1. Unzip the file into any directory you wish (preferably your user directory)
2. Open the `exec.sh` file. Set up is automatic.
3. If you choose to use your own spelling list file, please replace the existing spelling list file in the same directory. (MUST BE THE SAME NAME AND DIRECTORY)

Compilation instructions:

1. Create a new JavaFX project. (you can search Google if you don't know how to do this)
2. Place the source files under the correct package hierarchy, including the spelling list, and other stuff

The hierarchy sort of goes like this

```
/
	java file
		/application (main layout files and the Model of the MVC)
		/controller (controllers that take user input and process it 
		/resources (resources such as images, videos, audio etc, and stored statistics classes)
	spelling-lists.txt
```
