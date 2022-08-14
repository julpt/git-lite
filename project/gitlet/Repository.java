package gitlet;

import java.io.File;
import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** Directory inside .gitlet for storing commits. */
    public static final File COMM_DIR = join(GITLET_DIR, "commits");
    /** Directory inside .gitlet for storing blobs. */
    public static final File BLOB_DIR = join(GITLET_DIR, "blobs");
    /** Directory inside .gitlet for storing pointers to branches. */
    public static final File HEAD_DIR = join(GITLET_DIR, "heads");

    public static void makeRepo() {
        boolean created = GITLET_DIR.mkdir();
        if (!created) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        } else {
            COMM_DIR.mkdirs();
            BLOB_DIR.mkdirs();
            HEAD_DIR.mkdirs();
            Commit init = new Commit("initial commit", null, null, true);
            addCommit(init);
        }
    }

    public static void addCommit(Commit comm) {
        File destination = Utils.join(COMM_DIR, comm.getPrefix());
        destination.mkdir();
        File commFile = Utils.join(destination, comm.getSHA1());
        try {
            commFile.createNewFile();
        } catch (Exception e) {
            System.out.printf("Caught exception %s when creating file.%n", e.toString());
            System.exit(0);
        }
        Utils.writeObject(commFile, comm);
    }

    /* TODO: fill in the rest of this class. */
}
