package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Wangjishi
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
            message("A Gitlet version-control system already exists in the current directory.");
        }
    }

    public static void addFile(String fileName) {
        File f = join(CWD, fileName);
        // 若文件不存在，打印错误信息
        if (!f.exists()) {
            message("File does not exist.");
            return;
        }
        String hash = sha1(readContentsAsString(f));

        // 若文件内容与当前提交中该文件内容完全相同，不添加到暂存区
        if (readCommit(headPointer).containSameFile(f)) {
            // 如果已在暂存区存在相同的文件，将其移除
            if (fileExistInDir(STAGING_DIR, f)) {
                join(STAGING_DIR, hash).delete(); // 从暂存区中移除
                remove(SNAPSHOT_DIR, "changed", fileName); // 从map中移除
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
        // 信息为空，应报错
        if (msg == null || msg.trim().isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }

        TreeMap<String, String> changedMap = readMap(SNAPSHOT_DIR, "changed");
        TreeMap<String, String> removedMap = readMap(SNAPSHOT_DIR, "removed");
        // 无任何修改和删除，应报错
        if (changedMap.isEmpty() && removedMap.isEmpty()) {
            message("No changes added to the commit.");
            return;
        }

        // 根据changedMap和removedMap，修改提交的files
        Commit parentCommit = readCommit(headPointer);
        Commit c = new Commit(msg, headPointer, parentCommit.files);
        for (String fileName : changedMap.keySet()) { // 此处fileName是"1.txt"，因而能修改" "1.txt": foobar "映射
            c.files.put(fileName, changedMap.get(fileName));
        }
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

    /** 取消暂存和跟踪一个文件，若文件未被用户删除，则将其删除 */
    public static void removeFile(String fileName) {
        TreeMap<String, String> changedMap = readMap(SNAPSHOT_DIR, "changed");
        TreeMap<String, String> commitMap = readCommit(headPointer).files;
        if (!changedMap.containsKey(fileName) && !commitMap.containsKey(fileName)) { // 既未被暂存，又未被跟踪
            message("No reason to remove the file.");
            return;
        }

        // 如果该文件的修改操作被暂存，取消暂存
        if (changedMap.containsKey(fileName)) {
            removeFile(STAGING_DIR, changedMap.get(fileName)); // 暂存区被删除的文件文件名应为sha1，由changedMap跟踪
            remove(SNAPSHOT_DIR, "changed", fileName); // remove内部会改变原本的文件
        }

        // 如果该文件被跟踪，则暂存删除
        if (commitMap.containsKey(fileName)) {
            put(SNAPSHOT_DIR, "removed", fileName, null); // 删除区值置为null，当集合使用
            // 如果用户未删除，则删除文件
            if (join(CWD, fileName).exists()) restrictedDelete(fileName);
        }
    }

    /** 从head提交开始反向打印提交信息，直到初始提交 */
    public static void printLog() {
        Commit c = readCommit(headPointer);
        String commitHash = headPointer;
        while (c != null) {
            c.printLog(commitHash);
            commitHash = c.parentHash1;
            c = readCommit(commitHash);
        }
    }

    /** 按任意顺序打印所有提交的日志信息 */
    public static void printGlobalLog() {
        List<String> commitHashes = plainFilenamesIn(COMMIT_DIR);
        assert commitHashes != null; // 始终有初始提交
        for (String commitHash : commitHashes) {
            Commit c = readCommit(commitHash);
            c.printLog(commitHash);
        }
    }

    /** 传入提交信息，打印对应提交的id */
    public static void findCommit(String msg) {
        List<String> commitHashes = plainFilenamesIn(COMMIT_DIR);
        assert commitHashes != null; // 始终有初始提交
        int isFound = 0;
        for (String commitHash : commitHashes) {
            Commit c = readCommit(commitHash);
            if (c.message.equals(msg)) {
                isFound = 1;
                message(commitHash);
            }
        }
        if (isFound == 0) {
            message("Found no commit with that message.");
        }
    }

    /** 打印当前文件和分支状态 */
    public static void printStatus() {
        message("=== Branches ===");
        for (String branchName : branches.keySet()) {
            if (branchName.equals(currentBranch)) message("*" + branchName); // 标记当前分支
            else message(branchName);
        }
        message("");

        message("=== Staged Files ===");
        for (String fileName : readMap(SNAPSHOT_DIR, "changed").keySet()) {
            message(fileName); // TreeMap默认为字典序
        }
        message("");

        message("=== Removed Files ===");
        for (String fileName : readMap(SNAPSHOT_DIR, "removed").keySet()) {
            message(fileName);
        }
        message("");

        message("=== Modifications Not Staged For Commit ===");
        TreeSet<String> unstagedFileNames = getUnstagedFile();
        for (String fileName : unstagedFileNames) message(fileName);
        message("");

        message("=== Untracked Files ===");
        TreeSet<String> untrackedFileNames = getUntrackedFile();
        for (String fileName : untrackedFileNames) message(fileName);
        message("");
    }


    /** 将指定文件从头提交中检出 */
    public static void checkoutFile(String fileName) {
        checkoutFileInCommit(headPointer, fileName); // 该命令为特殊情况，可直接调用普遍情况的逻辑
    }

    /** 将指定文件从指定提交中检出 */
    public static void checkoutFileInCommit(String commitHash, String fileName) {
        Commit c = readCommit(commitHash);
        if (c == null) { // 不存在此提交
            message("No commit with that id exists.");
            return;
        }
        if (!c.files.containsKey(fileName)) { // 提交中不存在此文件
            message("File does not exist in that commit.");
            return;
        }

        File fileInCommit = join(FILE_DIR, c.files.get(fileName));
        File fileInCWD = join(CWD, fileName);
        if (!fileInCWD.exists()) {
            try {
                fileInCWD.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        writeContents(fileInCWD, readContentsAsString(fileInCommit)); // 会覆盖而不是追加
    }

    /** 检出并切换到指定分支 */
    public static void checkoutBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            message("No such branch exists.");
        }
        if (branchName.equals(currentBranch)) {
            message("No need to checkout the current branch.");
        }

        // 当前工作区存在未被跟踪且将被覆盖的文件
        String commitHash = get(GITLET_DIR, "branches", branchName);
        Commit c = readCommit(commitHash);
    }

    /** 创建一个指向头提交的新分支（不会切换分支） */
    public static void createBranch(String branchName) {
        // 若分支名已经存在，报错并返回
        if (branches.containsKey(branchName)) { // branches会被反复读入，因此初始化时已经读好
            message("A branch with that name already exists.");
            return;
        }

        // 否则新建一个指向头提交的分支（不切换到新分支）
        put(GITLET_DIR, "branches", branchName, headPointer);
    }


    // 以下为私有方法，大部分由于需要复用或调整结构而设立

    // 仓库状态相关
    private static TreeSet<String> getUnstagedFile() {
        TreeMap<String, String> currentCommitFiles = readCommit(headPointer).files;
        TreeMap<String, String> changedFiles = readMap(SNAPSHOT_DIR, "changed");
        TreeMap<String, String> removedFiles = readMap(SNAPSHOT_DIR, "removed");
        TreeSet<String> unstagedFileNames = new TreeSet<>(); //

        for (String fileName : currentCommitFiles.keySet()) {
            // 当前提交跟踪，但工作目录删除，且删除操作未被暂存，属于“未暂存”
            if (!join(CWD, fileName).exists() && !removedFiles.containsKey(fileName)) {
                unstagedFileNames.add(fileName + " (deleted)");
                continue;
            }
            // 当前提交跟踪，但工作目录修改，属于“未暂存”
            String fileHash = sha1(readContentsAsString(join(CWD, fileName))); // FIXME
            if (!fileHash.equals(currentCommitFiles.get(fileName))) { // 映射的值为跟踪文件的sha1
                unstagedFileNames.add(fileName + " (modified)");
                continue;
            }
        }
        for (String fileName : changedFiles.keySet()) {
            // 已暂存更改，但工作目录删除，属于“未暂存”
            if (!join(CWD, fileName).exists()) {
                unstagedFileNames.add(fileName + " (deleted)");
                continue;
            }
            // 已暂存更改，但工作目录修改，属于“未暂存”
            String fileHash = sha1(readContentsAsString(join(CWD, fileName)));
            if (!fileHash.equals(changedFiles.get(fileName))) { // 映射的值为暂存文件的sha1
                unstagedFileNames.add(fileName + " (modified)");
                continue;
            }
        }
        return unstagedFileNames;
    }

    private static TreeSet<String> getUntrackedFile() {
        List<String> fileNamesInCWD = plainFilenamesIn(CWD);
        assert fileNamesInCWD != null;
        TreeMap<String, String> currentCommitFiles = readCommit(headPointer).files;
        TreeMap<String, String> changedFiles = readMap(SNAPSHOT_DIR, "changed");
        TreeSet<String> untrackedFileNames = new TreeSet<>();

        for (String fileName : fileNamesInCWD) {
            if (join(CWD, fileName).isFile()) { // 不处理多级目录
                // 工作目录中存在，但未被暂存也未被跟踪
                if (!currentCommitFiles.containsKey(fileName) && !changedFiles.containsKey(fileName)) {
                    untrackedFileNames.add(fileName);
                }
            }
        }
        return untrackedFileNames;
    }

    // 提交（Commit）相关
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

    /** 用sha1（ID）读取出一个提交，若不存在则返回null */
    private static Commit readCommit(String commitHash) {
        if (commitHash == null) {
            return null;
        }
        File f = join(COMMIT_DIR, commitHash);
        if (f.exists()) {
            return readObject(f, Commit.class);
        } else {
            return null;
        }
    }

    // 文件相关
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

    private static boolean fileEquals(File f1, File f2) {
        return readContentsAsString(f1).equals(readContentsAsString(f2));
    }

    // Map文件相关
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
        writeObject(f, new TreeMap<>());
    }

    // 字符串文件相关
    /** 改变字符串文件的值，并返回该值 */
    private static String changedStringFile(File dir, String fileName, String newValue) {
        File f = join(dir, fileName);
        writeContents(f, newValue);
        return newValue;
    }

    /** 读取并返回字符串文件的值 */
    private static String readStringFile(File dir, String fileName) {
        File f = join(dir, fileName);
        return readContentsAsString(f);
    }
}
