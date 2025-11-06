package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.Serializable;
import java.util.Date;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Wangjishi
 */
public class Commit implements Serializable {
    /* List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`. */

    /** The message of this Commit. */
    private final String message;

    /** 时间戳 */
    private final long timeStamp;

    /** 父节点的“指针”，为了避免序列化时额外开销，存储其sha1哈希值。*/
    private final String parentHash1;
    private final String parentHash2;

    /** 指向文件内容的“指针”，同理使用sha1哈希值，为记录文件变化，需同时存储名字和sha1。*/
    private final TreeMap<String, String> files;

    // 一系列"getter"函数，用于维护私有字段不可变性和封装规范的同时，可在外部使用变量
    public String getMessage() {
        return message;
    }

    public String getParentHash1() {
        return parentHash1;
    }

    public String getParentHash2() {
        return parentHash2;
    }

    public TreeMap<String, String> getFiles() {
        return files;
    }

    public Commit(String msg, String hash1, TreeMap<String, String> files) {
        this.message = msg;
        timeStamp = new Date().getTime();
        this.parentHash1 = hash1;
        this.parentHash2 = null;
        this.files = files;
    }

    public Commit(String msg, String hash1, String hash2, TreeMap<String, String> files) {
        this.message = msg;
        timeStamp = new Date().getTime();
        this.parentHash1 = hash1;
        this.parentHash2 = hash2;
        this.files = files;
    }

    /** 创建固定的默认最初提交 */
    public Commit() {
        this.message = "initial commit";
        timeStamp = 0L;
        this.parentHash1 = null;
        this.parentHash2 = null;
        this.files = new TreeMap<>();
    }

    /** 按照指定格式输出日志。仅包含本提交的信息 */
    public void printLog(String commitHash) { // FIXME: merge
        Date date = new Date(timeStamp);
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        message("===");
        message("commit " + commitHash);
        message("Date: " + formatter.format(date));
        message(message);
        message("");
    }

    @Override
    public String toString() {
        StringBuilder content = new StringBuilder();
        content.append("message: ").append(message);
        content.append("\ntimeStamp: ").append(timeStamp);
        if (parentHash1 != null) {
            content.append("\nparentHash1: ").append(parentHash1);
        }
        if (parentHash2 != null) {
            content.append("\nparentHash2: ").append(parentHash2);
        }
        if (files != null) {
            content.append("\nfiles: ");
            for (Map.Entry<String, String> entry : files.entrySet()) {
                content.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
            }
        }
        return content.toString();
    }

    /** 检查传入的文件在本提交中是否存在（完全相同） */
    public boolean containSameFile(File f) {
        String hash = sha1(readContentsAsString(f));
        String fileName = f.getName();
        return files.containsKey(fileName) && files.get(fileName).equals(hash);
    }

}
