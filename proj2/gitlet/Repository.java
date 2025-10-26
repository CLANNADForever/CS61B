package gitlet;

import java.io.File;
import java.io.IOException;

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
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** commit文件夹下存储每个commit序列化后的内容 */
    public static final File COMMIT_DIR = join(GITLET_DIR, "commit");

    /* TODO: fill in the rest of this class. */
    /** 在当前目录创建一个新的 Gitlet。*/
    public static void initGitlet() {
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
            COMMIT_DIR.mkdir();
            Commit initialCommit = new Commit();
            writeCommit(initialCommit);
        } else {
            throw error("A Gitlet version-control system already exists in the current directory.");
        }
    }

    private static void writeCommit(Commit commit) {
        File sentinel = join(CWD,"gitlet","sentinel");
        writeObject(sentinel, commit);
        String hash = sha1(readContentsAsString(sentinel));
        File d = join(COMMIT_DIR, hash);
        try {
            d.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writeObject(d, commit);
    }
}
