package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.TreeMap;

public class Staging {

    /** Current working directory. */
    public static final File CWD = Paths.CWD;

    /** Directory of files staged for addition. */
    public static final File STAGE_DIR = Paths.STAGE_DIR;

    /** File that tracks the current HEAD branch. */
    public static final File HEAD = Paths.HEAD;

    /** File that tracks items added to the staging area. Maps file names to Blob SHA1s. */
    public static final File INDEX = Paths.INDEX;

    /** File that tracks items staged for removal. */
     public static final File REMOVED = Paths.REMOVED;


    /** Adds a copy of the file as it currently exists to the staging area.
     * If file is already staged, it is overwritten.
     *
     * If the current working version of the file is identical to the version
     * in the current commit, it won't be staged, and will be removed from the
     * staging area if it is already there.
     *
     * The file will no longer be staged for removal, if it was at the time of the command.
     */
    public static void stageFile(String fileName) {
        Blob addedFile = new Blob(fileName);
        TreeMap<String, String> stagedFiles = getStagedIndex();
        File destination = Utils.join(STAGE_DIR, addedFile.getSHA1());

        /* If file was staged for removal, it will be unstaged. */
        ArrayList<String> removedFiles = getRemoved();
        if (removedFiles.contains(fileName)) {
            removedFiles.remove(fileName);
            Utils.writeObject(REMOVED, removedFiles);
        }

        /* Check if a version of the file is already staged. If so, it will get deleted.
        The new version will be staged, unless it is reverting to the current commit. */
        String stagedSHA = stagedFiles.get(fileName);
        if (stagedSHA != null) {
            Utils.join(STAGE_DIR, stagedSHA).delete();
        }

        /* Check if new version is the same as the one in the current commit.
        In that case, this version of the file isn't saved and its reference in
        the index is removed. */
        String currentSHA = Branch.getHeadCommit().getFileSHA(fileName);
        if (Objects.equals(currentSHA, addedFile.getSHA1())) {
            stagedFiles.remove(fileName);
            Utils.writeObject(INDEX, stagedFiles);
        } else {
            /* Add file to Staging area. */
            Utils.createFile(destination);
            Utils.writeObject(destination, addedFile);

            /* Add file name and SHA1 to index, then save index. */
            stagedFiles.put(fileName, addedFile.getSHA1());
            Utils.writeObject(INDEX, stagedFiles);
        }
    }

    /** Unstages the file if it is currently staged for addition.
     * Returns true if the file was unstaged, false otherwise. */
    private static boolean unstage(String fileName) {
        TreeMap<String, String> stagedFiles = getStagedIndex();
        if (stagedFiles.containsKey(fileName)) {
            stagedFiles.remove(fileName);
            Utils.writeObject(INDEX, stagedFiles);
            return true;
        }
        return false;
    }

    /** If the file is tracked in the current commit, stages it for removal and
     * removes it from the working directory.
     * Returns true if the file was staged for removal, false otherwise.
     */
    private static boolean stageForRemoval(String fileName) {
        String fileInCurrentCommit = Branch.getHeadCommit().getFileSHA(fileName);
        if (fileInCurrentCommit != null) {
            ArrayList<String> removed = getRemoved();
            if (!removed.contains(fileName)) {
                removed.add(fileName);
                Utils.writeObject(REMOVED, removed);
                File fileToDelete = Utils.join(CWD, fileName);
                Utils.restrictedDelete(fileToDelete);
                return true;
            }
        }
        return false;
    }

    /** Unstage the file if it is currently staged for addition.
     * If the file is tracked in the current commit, stage it for removal and remove the file
     * from the working directory if the user has not already done so.<br><br>
     *
     * If the file is neither staged nor tracked by the head commit, prints an error message.
     * */
    public static void removeFile(String fileName) {
        if (!unstage(fileName) && !stageForRemoval(fileName)) {
            Utils.printAndExit("No reason to remove the file.");
        }
    }

    /** Checks if file to be added exists. Exits program if no file is found. */
    public static void checkFileExists(String fileName) {
        File source = Utils.join(CWD, fileName);
        if (!source.isFile()) {
            Utils.printAndExit("File does not exist.");
        }
    }

    /** Clears the Staging directory.
     * Creates an empty {index} file to track staged files.
     * Also creates an empty {removed} file to track files staged for removal.
     * If either file exists, it is reset.
     */
    public static void resetStaging() {
        clearStaging();
        if (!INDEX.isFile()) {
            Utils.createFile(INDEX);
        }
        Utils.writeObject(INDEX, new TreeMap<String, String>());
        if (!REMOVED.isFile()) {
            Utils.createFile(REMOVED);
        }
        Utils.writeObject(REMOVED, new ArrayList<String>());
    }


    /** Deletes all files in the Staging directory. Used after a commit. */
    private static void clearStaging() {
        File[] stagedContents = STAGE_DIR.listFiles();
        if (stagedContents != null) {
            for (File stagedFile: stagedContents) {
                stagedFile.delete();
            }
        }
    }

    /** Checks if any files have been staged. Exits with error message if there are
     * no staged files. Used before making a commit. */
    public static void checkStaged (){
        if (getStagedIndex().size() == 0 && getRemoved().size() == 0) {
            Utils.printAndExit("No changes added to the commit.");
        }
    }

    /** Returns a map of the staged file names and their respective SHA1s. */
    @SuppressWarnings("unchecked")
    public static TreeMap<String, String> getStagedIndex() {
        return Utils.readObject(INDEX, TreeMap.class);
    }

    /** Returns a list of files staged to be removed. */
    @SuppressWarnings("unchecked")
    public static ArrayList<String> getRemoved() {
        return Utils.readObject(REMOVED, ArrayList.class);
    }

}
