package gitlet;

import java.io.File;
import java.util.*;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Wangjishi
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;

    /** 时间戳 */
    private long timeStamp;

    /** 父节点的“指针”，为了避免序列化时额外开销，存储其sha1哈希值。*/
    private String parentHash1 = null;
    private String parentHash2 = null;

    /** 指向文件内容的“指针”，同理使用sha1哈希值，为记录文件变化，需同时存储名字和sha1。*/
    public TreeMap<String, String> files = null;

    public Commit(String message, String parentHash1, TreeMap<String, String> files) {
        this.message = message;
        timeStamp = new Date().getTime();
        this.parentHash1 = parentHash1;
        this.files = files;
    }

    public Commit(String message, String parentHash1, String parentHash2, TreeMap<String, String> files) {
        this.message = message;
        timeStamp = new Date().getTime();
        this.parentHash1 = parentHash1;
        this.parentHash2 = parentHash2;
        this.files = files;
    }

    /** 创建固定的默认最初提交 */
    public Commit() {
        this.message = "initial commit";
        timeStamp = 0L;
        this.files = new TreeMap<>();
        System.out.println(this);
    }

    public void printLog() {
        // TODO
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
