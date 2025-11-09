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
    /* List all instance variables of the Repository class here with a useful
     *  comment above them describing what that variable represents and how that
     *  variable is used. We've provided two examples for you. */

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
    private static String headPointer = GITLET_DIR.exists()
            ? readContentsAsString(join(GITLET_DIR, "headPointer")) : null;
    private static String currentBranch = GITLET_DIR.exists()
            ? readContentsAsString(join(GITLET_DIR, "currentBranch")) : null;
    private static TreeMap<String, String> branches = GITLET_DIR.exists()
            ? readObject(join(GITLET_DIR, "branches"), TreeMap.class) : null;

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
            // 如果文件暂存删除（存在于snapshot/removed），将其暂存删除的操作删除。
            if (readMap(SNAPSHOT_DIR, "removed").containsKey(fileName)) {
                remove(SNAPSHOT_DIR, "removed", fileName);
                return;
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
            removeFileInDir(STAGING_DIR, get(SNAPSHOT_DIR, "changed", fileName));
        }

        // 将文件添加到暂存区
        writeFile(STAGING_DIR, f);

        // 将文件添加（修改）操作加入snapshot/changed
        put(SNAPSHOT_DIR, "changed", fileName, hash);
    }

    /** 传入提交信息，进行一次提交 */
    public static void commitWithMessage(String msg) {
        commitWithMessage(msg, null); // 此处为了照顾merge提交逻辑，重构了代码结构
    }

    /** 取消暂存和跟踪一个文件，若文件未被用户删除，则将其删除 */
    public static void removeFile(String fileName) {
        TreeMap<String, String> changedMap = readMap(SNAPSHOT_DIR, "changed");
        TreeMap<String, String> commitMap = readCommit(headPointer).getFiles();
        if (!changedMap.containsKey(fileName) && !commitMap.containsKey(fileName)) { // 既未被暂存，又未被跟踪
            message("No reason to remove the file.");
            return;
        }

        // 如果该文件的修改操作被暂存，取消暂存
        if (changedMap.containsKey(fileName)) {
            removeFileInDir(STAGING_DIR, changedMap.get(fileName)); // 暂存区文件名应为sha1，由changedMap跟踪
            remove(SNAPSHOT_DIR, "changed", fileName); // remove内部会改变原本的文件
        }

        // 如果该文件被跟踪，则暂存删除
        if (commitMap.containsKey(fileName)) {
            put(SNAPSHOT_DIR, "removed", fileName, null); // 删除区值置为null，当集合使用
            // 如果用户未删除，则删除文件
            if (join(CWD, fileName).exists()) {
                restrictedDelete(fileName);
            }
        }
    }

    /** 从head提交开始反向打印提交信息，直到初始提交 */
    public static void printLog() {
        Commit c = readCommit(headPointer);
        String commitHash = headPointer;
        while (c != null) {
            c.printLog(commitHash);
            commitHash = c.getParentHash1();
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
            if (c.getMessage().equals(msg)) {
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
            if (branchName.equals(currentBranch)) {
                message("*" + branchName); // 标记当前分支
            } else {
                message(branchName);
            }
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
        for (String fileName : unstagedFileNames) {
            message(fileName);
        }
        message("");

        message("=== Untracked Files ===");
        TreeSet<String> untrackedFileNames = getUntrackedFile();
        for (String fileName : untrackedFileNames) {
            message(fileName);
        }
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
        if (!c.getFiles().containsKey(fileName)) { // 提交中不存在此文件
            message("File does not exist in that commit.");
            return;
        }

        File fileInCommit = join(FILE_DIR, c.getFiles().get(fileName));
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
            return;
        }
        if (branchName.equals(currentBranch)) {
            message("No need to checkout the current branch.");
            return;
        }
        // 当前工作区存在未被跟踪且将被覆盖的文件
        String commitHash = branches.get(branchName);
        if (isUntrackedOverwritten(commitHash)) {
            message("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            return;
        }
        // 检出给定分支所指提交
        checkoutCommit(commitHash);
        // 切换分支并清空暂存区
        currentBranch = changedStringFile(GITLET_DIR, "currentBranch", branchName); // 赋值只是含义更为直观
        headPointer = changedStringFile(GITLET_DIR, "headPointer", commitHash);
        clearMap(SNAPSHOT_DIR, "changed");
        clearMap(SNAPSHOT_DIR, "removed");
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

    /** 删除给定的分支（仅删除分支的指针） */
    public static void removeBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            message("A branch with that name does not exist.");
            return;
        }
        if (branchName.equals(currentBranch)) {
            message("Cannot remove the current branch.");
            return;
        }
        remove(GITLET_DIR, "branches", branchName);
    }

    /** 重置仓库状态至指定提交 */
    public static void reset(String commitHash) {
        if (!join(COMMIT_DIR, commitHash).exists()) {
            message("No commit with that id exists.");
            return;
        }
        if (isUntrackedOverwritten(commitHash)) {
            message("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            return;
        }
        // 检出指定提交
        checkoutCommit(commitHash);
        // 移动所在分支和头指针至给定提交，并清空暂存区
        put(GITLET_DIR, "branches", currentBranch, commitHash);
        headPointer = changedStringFile(GITLET_DIR, "headPointer", commitHash);
        clearMap(SNAPSHOT_DIR, "changed");
        clearMap(SNAPSHOT_DIR, "removed");
    }

    /** 将给定分支的文件合并至当前分支 */
    public static void merge(String branchName) {
        // 错误情况
        if (!readMap(SNAPSHOT_DIR, "changed").isEmpty()
                || !readMap(SNAPSHOT_DIR, "removed").isEmpty()) {
            message("You have uncommitted changes.");
            return;
        } else if (!readMap(GITLET_DIR, "branches").containsKey(branchName)) {
            message("A branch with that name does not exist.");
            return;
        } else if (branchName.equals(currentBranch)) {
            message("Cannot merge a branch with itself.");
            return;
        }
        String branchHeadCommitHash = branches.get(branchName);
        Commit branchHeadCommit = readCommit(branchHeadCommitHash);
        Commit headCommit = readCommit(headPointer);
        if (isUntrackedOverwritten(branchHeadCommitHash)) {
            message("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            return;
        }
        // 1. 找到两分支的最新共同祖先（分裂点）
        String splitPointHash = findSplitPoint(headPointer, branchHeadCommitHash);
        assert splitPointHash != null; // 任意两提交一定有最新共同祖先
        Commit splitPointCommit = readCommit(splitPointHash);
        TreeMap<String, String> currentFiles = headCommit.getFiles();
        TreeMap<String, String> branchFiles = branchHeadCommit.getFiles();
        TreeMap<String, String> splitFiles = splitPointCommit.getFiles();

        // 2. 判断分裂点提交是否为特殊情况
        if (splitPointHash.equals(branchHeadCommitHash)) { // 分裂点为给定分支
            message("Given branch is an ancestor of the current branch.");
            return;
        }
        if (splitPointHash.equals(headPointer)) { // 分裂点为当前分支
            checkoutBranch(branchName);
            message("Current branch fast-forwarded.");
            return;
        }

        // 3. 处理无冲突的文件
        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(currentFiles.keySet());
        allFiles.addAll(branchFiles.keySet());
        int conflictFlag = 0;
        for (String fileName : allFiles) { // 遍历所有可能存在的文件
            int curToSplit = isSameFile(currentFiles, splitFiles, fileName);
            int branchToSplit = isSameFile(branchFiles, splitFiles, fileName);
            int curToBranch = isSameFile(currentFiles, branchFiles, fileName);
            if (curToSplit == 1) { // 当前分支相比分岔点未修改
                if (branchToSplit == 0) { // 给定分支相比分叉点已修改
                    checkoutFileInCommit(branchHeadCommitHash, fileName); // 检出分支的文件并暂存
                    put(SNAPSHOT_DIR, "changed", fileName, branchFiles.get(fileName));
                } else if (branchToSplit == -1) { // 给定分支此文件被删除
                    removeFile(fileName); // 取消暂存该文件
                }
            }
            if (branchToSplit == -2 && curToSplit == -3) { // 文件在分支中新增，在当前分支和分岔点不存在
                checkoutFileInCommit(branchHeadCommitHash, fileName); // 检出分支文件并暂存
                put(SNAPSHOT_DIR, "changed", fileName, branchFiles.get(fileName));
            }
            // 4. 处理冲突文件
            if (curToSplit == 0 && branchToSplit == 0 && curToBranch == 0 // 相比分岔点，均修改但修改内容不同
                    || curToSplit == -1 && branchToSplit == 0 // 当前分支删除，另一分支修改
                    || curToSplit == 0 && branchToSplit == -1 // 当前分支修改，另一分支删除
                    || curToSplit == -2 && branchToSplit == -2 && curToBranch == 1) { // 均新建，内容不同
                conflictFlag = 1;
                handleConflict(fileName, currentFiles.get(fileName), branchFiles.get(fileName)); // 修改并暂存文件
            }
        }

        // 5. 完成提交
        String mergeMessage = "Merged " + branchName + " into " + currentBranch + ".";
        commitWithMessage(mergeMessage, branchHeadCommitHash); // 传入hash2
        if (conflictFlag == 1) {
            message("Encountered a merge conflict.");
        }
    }

    // 以下为私有方法，大部分由于需要复用或调整结构而设立

    // 仓库状态相关，涉及较复杂的逻辑
    /** 进行一次提交 */
    public static void commitWithMessage(String msg, String parentHash2) {
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
        // 提交的属性在初始化后应不可变，所以应对map操作完成后，再用map初始化commit
        Commit parentCommit = readCommit(headPointer);
        TreeMap<String, String> filesInNewCommit = parentCommit.getFiles();

        for (String fileName : changedMap.keySet()) { // fileName是"1.txt"，因而能修改"1.txt": "foobar"映射
            filesInNewCommit.put(fileName, changedMap.get(fileName));
        }
        for (String fileName : removedMap.keySet()) { // 同理为fileName
            filesInNewCommit.remove(fileName);
        }
        Commit c;
        if (parentHash2 == null) {
            c = new Commit(msg, headPointer, filesInNewCommit);
        } else { // merge提交，有第二个父提交
            c = new Commit(msg, headPointer, parentHash2, filesInNewCommit);
        }


        // 将修改的文件从暂存区复制至文件区并删除自身，若已经存在，则不复制
        // 这里需要的是sha1作为名字去staging中寻址，并写入files文件夹，所以用values
        for (String fileHash : changedMap.values()) {
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
        put(GITLET_DIR, "branches", currentBranch, headPointer); // 将当前分支向前推进
        clearMap(SNAPSHOT_DIR, "changed");
        clearMap(SNAPSHOT_DIR, "removed");
    }

    /** 对一个给定提交的检出 */
    private static void checkoutCommit(String commitHash) {
        Set<String> commitFileNames = readCommit(commitHash).getFiles().keySet();
        // 删除当前分支中存在，但给定分支没有的文件
        Commit cHead = readCommit(headPointer);
        for (String fileName : cHead.getFiles().keySet()) {
            if (!commitFileNames.contains(fileName)) {
                restrictedDelete(fileName);
            }
        }
        // 检出指定分支的所有文件
        for (String fileName : commitFileNames) {
            checkoutFileInCommit(commitHash, fileName);
        }
    }

    /** 检查对给定提交检出是否会覆盖未跟踪文件 */
    private static boolean isUntrackedOverwritten(String commitHash) {
        Set<String> commitFileNames = readCommit(commitHash).getFiles().keySet();
        TreeSet<String> untrackedFileNames = getUntrackedFile();
        for (String fileName : commitFileNames) {
            if (untrackedFileNames.contains(fileName)) { // 未被跟踪且将被检出覆盖
                return true;
            }
        }
        return false;
    }

    /** 获得未暂存文件列表 */
    private static TreeSet<String> getUntrackedFile() {
        List<String> fileNamesInCWD = plainFilenamesIn(CWD);
        assert fileNamesInCWD != null;
        TreeMap<String, String> currentCommitFiles = readCommit(headPointer).getFiles();
        TreeMap<String, String> changedFiles = readMap(SNAPSHOT_DIR, "changed");
        TreeSet<String> untrackedFileNames = new TreeSet<>();

        for (String fileName : fileNamesInCWD) {
            if (join(CWD, fileName).isFile()) { // 不处理多级目录
                // 工作目录中存在，但未被暂存也未被跟踪
                if (!currentCommitFiles.containsKey(fileName)
                        && !changedFiles.containsKey(fileName)) {
                    untrackedFileNames.add(fileName);
                }
            }
        }
        return untrackedFileNames;
    }

    /** 获取未暂存文件集合，主要用于优化代码结构，解耦逻辑 */
    private static TreeSet<String> getUnstagedFile() {
        TreeMap<String, String> currentCommitFiles = readCommit(headPointer).getFiles();
        TreeMap<String, String> changedFiles = readMap(SNAPSHOT_DIR, "changed");
        TreeMap<String, String> removedFiles = readMap(SNAPSHOT_DIR, "removed");
        TreeSet<String> unstagedFileNames = new TreeSet<>();

        for (String fileName : currentCommitFiles.keySet()) {
            // 当前提交跟踪，但工作目录删除，且删除操作未被暂存，属于“未暂存”
            if (!join(CWD, fileName).exists()) {
                if (!removedFiles.containsKey(fileName)) {
                    unstagedFileNames.add(fileName + " (deleted)");
                }
                continue; // 即使未被添加，也不应进到下一部分，否则会去读取不存在的文件
            }
            // 当前提交跟踪，但工作目录修改，属于“未暂存”
            String fileHash = sha1(readContentsAsString(join(CWD, fileName)));
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

    /**
     * 寻找两个提交的最新共同祖先（分裂点），主要用于优化代码结构，解耦逻辑
     * @return 分裂点sha1值
     */
    private static String findSplitPoint(String commitHash1, String commitHash2) {
        // BFS, 先从当前分支（泛化为commit1，不影响结果）出发，存储所有当前分支可达点
        Queue<String> q = new LinkedList<>();
        Set<String> isVisited = new HashSet<>();
        q.offer(commitHash1);
        while (!q.isEmpty()) {
            String commitHash = q.poll();
            isVisited.add(commitHash);
            Commit c = readCommit(commitHash);
            if (c.getParentHash1() != null) {
                q.offer(c.getParentHash1());
            }
            if (c.getParentHash2() != null) {
                q.offer(c.getParentHash2());
            }
        }
        // 再从给定分支（泛化为commit2，不影响结果）出发，扫描至第一个可达点，即为最新共同祖先
        q.offer(commitHash2);
        while (!q.isEmpty()) {
            String commitHash = q.poll();
            if (isVisited.contains(commitHash)) {
                return commitHash; // 扫描到第一个就直接返回，否则层数更深，不符合“最新”的定义
            }
            Commit c = readCommit(commitHash);
            if (c.getParentHash1() != null) {
                q.offer(c.getParentHash1());
            }
            if (c.getParentHash2() != null) {
                q.offer(c.getParentHash2());
            }
        }
        return null;
    }

    /** 处理冲突文件，包括得到文件内容，(创建并)写入文件，并将其暂存
     * @param fileName 文件名
     * @param fileHash1 头提交（当前分支）文件内容
     * @param fileHash2 合并分支文件内容
     */
    private static void handleConflict(String fileName, String fileHash1, String fileHash2) {
        String newFileString = mergeConflictFile(fileHash1, fileHash2);
        File f = join(CWD, fileName);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        writeContents(f, newFileString); // 修改当前工作目录的文件内容
        addFile(fileName); // 将其暂存
    }

    /** 返回一个合并的冲突文件内容 */
    private static String mergeConflictFile(String fileHash1, String fileHash2) {
        String fileString1;
        String fileString2;
        if (fileHash1 == null) {
            fileString1 = "";
        } else {
            fileString1 = readContentsAsString(join(FILE_DIR, fileHash1));
        }
        if (fileHash2 == null) {
            fileString2 = "";
        } else {
            fileString2 = readContentsAsString(join(FILE_DIR, fileHash2));
        }

        StringBuilder newFileString = new StringBuilder();
        newFileString.append("<<<<<<< HEAD\n");
        newFileString.append(fileString1);
        newFileString.append("=======\n");
        newFileString.append(fileString2);
        newFileString.append(">>>>>>>\n");
        return newFileString.toString();
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
    private static void removeFileInDir(File dir, String fileName) {
        File f = join(dir, fileName);
        f.delete();
    }

    /** 传入一个文件夹和文件，检查在该路径下是否存在内容完全相同的文件。使用sha1与文件名对比 */
    private static boolean fileExistInDir(File dir, File file) {
        String hash = sha1(readContentsAsString(file));
        File f = join(dir, hash);
        return f.exists();
    }

//    private static boolean fileExistInDir(File dir, String fileName) {
//        File f = join(dir, fileName);
//        return f.exists();
//    }
//
//    private static boolean fileEquals(File f1, File f2) {
//        return readContentsAsString(f1).equals(readContentsAsString(f2));
//    }

    // Map(文件)相关
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

    /** 比较一个键在两个map中的值是否相等 *
     * @return 根据不同结果返回不同的整数。1代表相等，0代表不相等，-1代表map1中不存在，-2代表map2中不存在，-3代表都不存在
     */
    private static int isSameFile(TreeMap<String, String> map1, TreeMap<String, String> map2, String key) {
        if (!map1.containsKey(key) && !map2.containsKey(key)) {
            return -3;
        } else if (!map1.containsKey(key)) {
            return -1;
        } else if (!map2.containsKey(key)) {
            return -2;
        } else if (map1.get(key).equals(map2.get(key))) {
            return 1;
        } else {
            return 0;
        }
    }

    // 字符串文件相关
    /** 改变字符串文件的值，并返回该值 */
    private static String changedStringFile(File dir, String fileName, String newValue) {
        File f = join(dir, fileName);
        writeContents(f, newValue);
        return newValue;
    }

//    /** 读取并返回字符串文件的值 */
//    private static String readStringFile(File dir, String fileName) {
//        File f = join(dir, fileName);
//        return readContentsAsString(f);
//    }
}
