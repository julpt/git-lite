# git-lite 
A version-control system that mimics some of the basic features of the popular system Git.  
Personal implementation of CS61B Project 2, [Gitlet](https://sp21.datastructur.es/materials/proj/proj2/proj2). 

The Main class mostly calls helper methods in the Repository class. The Repository methods further use the Blob, Commit, Branch and Staging classes, as well as Utils (some utility methods for file-system-related tasks) and Paths (for file paths).

# Implemented commands:
## init  
    Usage: java gitlet.Main init
Creates a new Gitlet version-control system in the current directory. Automatically starts with one commit which contains no files.  

## add  
    Usage: java gitlet.Main add [file name]  
Adds a copy of the file as it currently exists to the staging area.  
Staging an already-staged file overwrites the previous entry in the staging area with the new contents.  

If the current working version of the file is identical to the version in the current commit, it will not be staged to be added, and is removed from the staging area if it is already there (as can happen when a file is changed, added, and then changed back to its original version). 

The file will no longer be staged for removal, if it was at the time of the command.  
## commit  
    Usage: java gitlet.Main commit [message]  
Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time, creating a new commit. The commit is said to be tracking the saved files.   

By default a commit has the same file contents as its parent. Files staged for addition and removal are the updates to the commit. Of course, the date (and likely the mesage) will also different from the parent.  

## rm  
    Usage: java gitlet.Main rm [file name]  
Unstage the file if it is currently staged for addition. If the file is tracked in the current commit, stage it for removal and remove the file from the working directory if the user has not already done so (will not remove it unless it is tracked in the current commit).  

## log  
    Usage: java gitlet.Main log  
Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit, following the first parent commit links, ignoring any second parents found in merge commits. (In regular Git, this is what you get with git log --first-parent).  

## global-log  
    Usage: java gitlet.Main global-log
Like log, except displays information about all commits ever made. The order of the commits does not matter.  

## find  
    Usage: java gitlet.Main find [commit message]
Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits, it prints the ids out on separate lines. The commit message is a single operand; to indicate a multiword message, put the operand in quotation marks.  

## status  
    Usage: java gitlet.Main status
Displays what branches currently exist, and marks the current branch with a * . Also displays what files have been staged for addition or removal.

## checkout
    Usages:
    1. java gitlet.Main checkout -- [file name]  
    2. java gitlet.Main checkout [commit id] -- [file name]  
    3. java gitlet.Main checkout [branch name]  
1. Takes the version of the file as it exists in the head commit and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.  
2. Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.  
3. Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist. Also, at the end of this command, the given branch will now be considered the current branch (HEAD). Any files that are tracked in the current branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the checked-out branch is the current branch.  

## branch
    Usage: java gitlet.Main branch [branch name]  
Creates a new branch with the given name, and points it at the current head commit. A branch is nothing more than a name for a reference (a SHA-1 identifier) to a commit node. This command does NOT immediately switch to the newly created branch (just as in real Git).  

## rm-branch
    Usage: java gitlet.Main rm-branch [branch name]  
Deletes the branch with the given name. This only means to delete the pointer associated with the branch; it does not mean to delete all commits that were created under the branch.  

## reset  
    Usage: java gitlet.Main reset [commit id]  
Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. Also moves the current branch’s head to that commit node.  
## merge
    Usage: java gitlet.Main merge [branch name]  
Merges files from the given branch into the current branch. Details about merging are in the link above.

## Extra credit:
- The status command also prints *Modifications Not Staged For Commit* and *Untracked Files*
- Also implemented *remote* commands: 
  1. add-remote
  2. rm-remote
  3. push
  4. fetch
  5. pull
