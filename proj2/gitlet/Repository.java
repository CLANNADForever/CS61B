package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import static gitlet.Utils.*;

/** Represents a gitlet repository.
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
    public static final File CHANGED = join(SNAPSHOT_DIR, "changed");
    public static final File REMOVED = join(SNAPSHOT_DIR, "removed");

    /** headPointer,currentBranch,branches分别持久化头结点位置，当前分支名字，所有分支位置 */
    public static String headPointer = GITLET_DIR.exists() ? readContentsAsString(join(GITLET_DIR, "headPointer")) : null;
    public static String currentBranch = GITLET_DIR.exists() ? readContentsAsString(join(GITLET_DIR, "currentBranch")) : null;
    public static TreeMap<String, String> branches = GITLET_DIR.exists() ? readObject(join(GITLET_DIR, "branches"), TreeMap.class) : null;

    /** init命令：在当前目录创建一个新的 Gitlet。*/
    public static void initGitlet() {
        // 若存在gitlet，抛出错误，否则新建.gitlet及其所有附属文件夹，并提交一个初始提交。
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
            COMMIT_DIR.mkdir();
            STAGING_DIR.mkdir();
            FILE_DIR.mkdir();
            SNAPSHOT_DIR.mkdir();
            try {
                CHANGED.createNewFile();
                REMOVED.createNewFile();
                join(GITLET_DIR, "headPointer").createNewFile();
                join(GITLET_DIR, "currentBranch").createNewFile();
                join(GITLET_DIR, "branches").createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Commit initialCommit = new Commit(); // 无参数为默认初始提交
            writeCommit(initialCommit);

            // 初始化changed和removed文件
            TreeMap<String, String> map = new TreeMap<>();
            writeObject(CHANGED, map);
            writeObject(REMOVED, map);
            // 初始化头结点和分支
            String hash = sha1(initialCommit.toString());
            writeObject(join(GITLET_DIR, "branches"), map);
            writeContents(join(GITLET_DIR, "headPointer"), hash);
            writeContents(join(GITLET_DIR, "currentBranch"), "master"); // 默认一开始为master
            put(GITLET_DIR, "branches", "master", hash);
        } else {
            throw error("A Gitlet version-control system already exists in the current directory.");
        }
    }

    public static void addFile(String fileName) {
        File f = join(CWD, fileName);
        String hash = sha1(readContentsAsString(f));
        // 若文件不存在，抛出错误
        if (!f.exists()) {
            throw error("File does not exist.");
        }

        // 若文件内容与当前提交中该文件内容完全相同，不添加到暂存区
        if (readCommit(headPointer).containSameFile(f)) {
            // 如果已在暂存区存在相同的文件，将其移除
            if (fileExistInDir(STAGING_DIR, f)) {
                // 从暂存区中移除
                File dupFile = join(STAGING_DIR, hash);
                assert dupFile.exists();
                dupFile.delete();

                // 从snapshot/changed中移除
                remove(SNAPSHOT_DIR, "changed", fileName);
            }
            return;
        }

        // 如果文件暂存删除（存在于snapshot/removed），将其暂存删除的操作删除。
        if (readMap(SNAPSHOT_DIR, "removed").containsKey(fileName)) {
            remove(SNAPSHOT_DIR, "removed", fileName);
            return;
        }

        // 如果文件已经在暂存区，覆盖先前的版本
        if (readMap(SNAPSHOT_DIR, "changed").containsKey(fileName)) {
            removeFile(STAGING_DIR, get(SNAPSHOT_DIR, "changed", fileName));
        }

        // 将文件添加到暂存区
        writeFile(STAGING_DIR, f);

        // 将文件添加（修改）操作加入snapshot/changed
        put(SNAPSHOT_DIR, "changed", fileName, hash);
    }

    /** 传入提交信息，进行一次提交 */
    public static void commitWithMessage(String msg) {
        Commit parentCommit = readCommit(headPointer);
        Commit c = new Commit(msg, headPointer, parentCommit.files);
        TreeMap<String, String> changedMap = readMap(SNAPSHOT_DIR, "changed");
        TreeMap<String, String> removedMap = readMap(SNAPSHOT_DIR, "removed");

        // 根据changedMap，将修改的文件在commit的files中改为修改后的sha1值
        for (String fileName : changedMap.keySet()) { // 此处fileName是"1.txt"，因而能修改" "1.txt": foobar "映射
            c.files.put(fileName, changedMap.get(fileName));
        }

        // 根据removedMap，将删除的文件从commit的files中移除
        for (String fileName : removedMap.keySet()) { // 同理为fileName
            c.files.remove(fileName);
        }

        // 将修改的文件从暂存区复制至文件区并删除自身，若已经存在，则不复制
        for (String fileHash : changedMap.values()) { // 这里需要的是sha1作为名字去staging中寻址，并写入files文件夹，所以用values
            File stagingFile = join(STAGING_DIR, fileHash);
            File newFile = join(FILE_DIR, fileHash);
            if (!newFile.exists()) {
                writeFile(FILE_DIR, stagingFile);
            }
            // 删除暂存区的文件
            stagingFile.delete();
        }

        // 更新状态
        writeCommit(c);
        headPointer = changedStringFile(GITLET_DIR, "headPointer", sha1(c.toString())); // 更新头指针sha1
        put(GITLET_DIR, "branches", currentBranch, headPointer);
        clearMap(SNAPSHOT_DIR, "changed");
        clearMap(SNAPSHOT_DIR, "removed");
    }

    /** 向提交文件夹写入一个提交，写入时文件名为sha1序列，内容为提交序列化后的结果 */
    private static void writeCommit(Commit commit) {
        String hash = sha1(commit.toString());
        File d = join(COMMIT_DIR, hash);
        try {
            d.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writeObject(d, commit);
    }

    /** 向指定文件夹写入一个文件，写入时文件名为sha1序列，内容为文件内容 */
    private static void writeFile(File dir, File file) {
        String fileString = readContentsAsString(file);
        String hash = sha1(fileString);
        File newFile = join(dir, hash);
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writeContents(newFile, fileString);
    }

    /** 删除指定文件夹中的指定文件 */
    private static void removeFile(File dir, String fileName) {
        File f = join(dir, fileName);
        f.delete();
    }

    /** 传入一个文件夹和文件，检查在该路径下是否存在内容完全相同的文件。使用sha1与文件名对比 */
    private static boolean fileExistInDir(File dir, File file) {
        String hash = sha1(readContentsAsString(file));
        File f = join(dir, hash);
        return f.exists();
    }

    private static boolean fileExistInDir(File dir, String fileName) {
        File f = join(dir, fileName);
        return f.exists();
    }

    /** 用sha1读取出一个提交 */
    private static Commit readCommit(String commitHash) {
        File f = join(COMMIT_DIR, commitHash);
        assert f.exists();
        Commit c = readObject(f, Commit.class);
        assert c != null;
        return c;
    }

    // 以下为三个map文件的辅助方法

    /** 从指定文件中读取并返回map */
    private static TreeMap<String, String> readMap(File dir, String fileName) {
        File f = join(dir, fileName);
        return readObject(f, TreeMap.class);
    }

    /** 从指定map文件中读取一个键的值 */
    private static String get(File dir, String fileName, String key) {
        File f = join(dir, fileName);
        TreeMap<String, String> map = readObject(f, TreeMap.class);
        return map.get(key);
    }

//    /** 从指定map文件返回是否存在某个键 */
//    private static boolean containKey(File dir, String fileName, String key) {
//        File f = join(dir, fileName);
//        TreeMap<String, String> map = readObject(f, TreeMap.class);
//        return map.containsKey(key);
//    }

    /** 向内容为Map的文件中写入一个键值对，若存在键，则覆盖其值 */
    private static void put(File dir, String fileName, String key, String value) {
        File f = join(dir, fileName);
        TreeMap<String, String> map = readObject(f, TreeMap.class);

        map.put(key, value);
        writeObject(f, map);
    }

    /** 向内容为Map的文件移除键值对 */
    private static void remove(File dir, String fileName, String key) {
        File f = join(dir, fileName);
        TreeMap<String, String> map = readObject(f, TreeMap.class);
        assert map.containsKey(key);

        map.remove(key);
        writeObject(f, map);
    }

    /** 清空一个文件中map所有的键值对 */
    private static void clearMap(File dir, String fileName) {
        File f = join(dir, fileName);

        // 只需创建空map并写入
        TreeMap<String, String> map = new TreeMap<>();
        writeObject(f, map);
    }

    // 以下为字符串文件相关辅助方法

    /** 改变字符串文件的值，并返回该值 */
    private static String changedStringFile(File dir, String fileName, String newValue) {
        File f = join(dir, fileName);
        writeContents(f, newValue);
        return newValue;
    }
}
