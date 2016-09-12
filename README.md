# VoxSpell Spelling Aid #

Installation instructions:

1. Unzip the file into any directory you wish (preferably your user directory)
2. Run the application by double clicking the jar file. Set up is automatic.
3. If you choose to use your own wordlist file, please replace the existing wordlist file in the same directory. (MUST BE THE SAME NAME AND DIRECTORY)

Compilation instructions:

1. Create a new JavaFX project. (you can search Google if you don't know how to do this)
2. Place the source files under the correct package hierarchy, including the wordlist/spelling list, and other stuff

The hierarchy sort of goes like this

```
/
	/src
		/application (main layout files and the Model of the MVC)
		/controller (controllers that take user input and process it 
		/resources (resources such as images, videos, audio etc, and stored statistics classes)
	spelling-lists.txt
	wordlist *(to be phased out, and replaced by spelling-lists.txt)
```