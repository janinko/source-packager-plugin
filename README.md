# Jenkins Source Packager

This plugin creates zip package of source codes and file with commands and environment variables used for building.

#### Files
``workspace/SP-sources.zip``  
``workspace/SP-build-Commands``

#### System Configuration:
Source packager -> [Advanced]

* ``Ignore files (full path)`` - list of relative paths of files/folders to ignore.  
```
.git
path/to/ignored/file
path/to/ignored/folder/
```

* ``Ignore files (check this in every directory)`` - list of filenames to ignore.  
```
.svn
ignore_this
```

* ``Files with additional ignorelists`` - list of relative paths to files which contais paths to ignore (like ``Ignore files (full path)``).
This files are also ignored.  
```
.gitignore
path/to/ignorefile
```


#### Job Configuration:
Build Environment -> check Source packager