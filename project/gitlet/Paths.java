package gitlet;

import java.io.File;

/** Keeps track of most used file paths. */
public final class Paths {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");

    /** Directory inside .gitlet for storing commits. */
    public static final File COMM_DIR = Utils.join(GITLET_DIR, "commits");

    /** Directory inside .gitlet for storing blobs. */
    public static final File BLOB_DIR = Utils.join(GITLET_DIR, "blobs");

    /** Directory inside .gitlet for storing pointers to branches. */
    public static final File HEAD_DIR = Utils.join(GITLET_DIR, "heads");

    /** Staging directory. */
    public static final File STAGE_DIR = Utils.join(GITLET_DIR, "staging");

    /** File that tracks the current HEAD branch. */
    public static final File HEAD = Utils.join(GITLET_DIR, "head");

    /** File that tracks items added to the staging area. Maps file names to Blob SHA1s. */
    public static final File INDEX = Utils.join(GITLET_DIR, "index");
}