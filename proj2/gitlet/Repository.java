package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

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

    /** commit文件夹存储每个commit序列化后的内容 */
    public static final File COMMIT_DIR = join(GITLET_DIR, "commits");

    /** staging文件夹存储被add且未被commit的暂存文件 */
    public static final File STAGING_DIR = join(GITLET_DIR, "staging-area");

    /** files文件夹存储所有被提交了的文件，即实际追踪的历史版本文件 */
    public static final File FILE_DIR = join(GITLET_DIR, "files");

    /** snapshot文件夹存储当前文件中与所在提交中不同的（修改或删除）文件名映射，使提交时能够只改变这些文件。 */
    public static final File SNAPSHOT_DIR = join(GITLET_DIR, "snapshot");
    public static final File CHANGED_DIR = join(SNAPSHOT_DIR, "changed");
    public static final File REMOVED_DIR = join(SNAPSHOT_DIR, "removed");

    public String headPointer;
    public String currentBranch;
    public TreeMap<String, String> branches;

    /** init命令：在当前目录创建一个新的 Gitlet。*/
    public static void initGitlet() {
        // 若存在gitlet，抛出错误，否则新建.gitlet及其所有附属文件夹，并提交一个初始提交。
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
            COMMIT_DIR.mkdir();
            STAGING_DIR.mkdir();
            FILE_DIR.mkdir();
            SNAPSHOT_DIR.mkdir();
            CHANGED_DIR.mkdir();
            REMOVED_DIR.mkdir();
            Commit initialCommit = new Commit(); // 无参数为默认初始提交
            writeCommit(initialCommit);
        } else {
            throw error("A Gitlet version-control system already exists in the current directory.");
        }
    }

    public static void addFile(String fileName) {
        File f = join(CWD, fileName);
        // 若文件不存在，抛出错误
        if (!f.exists()) {
            throw error("File does not exist.");
        }

        // 若文件内容与当前提交中该文件内容完全相同，不添加到暂存区
        if (false) {
            return;
        }

        // 将文件添加到暂存区

    }

    /** 向提交文件夹写入一个提交，写入时文件名为sha1序列，内容为提交序列化后的结果 */
    private static void writeCommit(Commit commit) {
        // FIXME: 完成commit的toString
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

    /** 向指定文件夹写入一个文件，写入时文件名为sha1序列，内容为文件序列化后的结果。 */
    private static void writeFile(File dir, File file) {
        String fileString = readContentsAsString(file);
        String hash = sha1(fileString);
        File newFile = join(STAGING_DIR, hash);
    }

    /** 传入一个文件夹和文件，检查在该路径下是否存在内容完全相同的文件。使用sha1与文件名对比 */
    private static boolean fileExist(File dir, File file) {

        return false;
    }
}
