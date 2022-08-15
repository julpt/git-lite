package gitlet;

import java.io.File;
import java.util.Objects;
import java.util.TreeMap;

public class Staging {
    public static final File CWD = Paths.CWD;
    public static final File STAGE_DIR = Paths.STAGE_DIR;
    public static final File HEAD = Paths.HEAD;
    public static final File INDEX = Paths.INDEX;

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
        TreeMap<String, String> stagedFiles = Utils.readObject(INDEX, TreeMap.class);
        File destination = Utils.join(STAGE_DIR, addedFile.getSHA1());

        /* Check if a version of the file is already staged. If so, it will get deleted.
        The new version will be staged, unless it's reverting to the most recent commit. */
        String stagedSHA = stagedFiles.get(fileName);
        if (stagedSHA != null) {
            Utils.join(STAGE_DIR, stagedSHA).delete();
        }

        /* TODO: UNSTAGE FOR REMOVAL when you get to rm */
        /* Check if new version is the same as the one in the most recent commit.
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


    /** Checks if file to be added exists. Exits program if no file is found. */
    public static void checkFileExists(String fileName) {
        File source = Utils.join(CWD, fileName);
        if (!source.isFile()) {
            Utils.printAndExit("File does not exist.");
        }
    }

    /** Creates an empty index file to track staged files.
     * If one exists, it is reset.
     */
    public static void resetIndex() {
        if (!INDEX.isFile()) {
            Utils.createFile(INDEX);
        }
        Utils.writeObject(INDEX, new TreeMap<String, String>());
    }

}
