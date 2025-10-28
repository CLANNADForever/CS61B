package gitlet;

// TODO: any imports you need here

import java.util.*;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable { // TODO: does it need to implement Serializable?
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
    private TreeMap<String, String> files = null;

    /* TODO: fill in the rest of this class. */
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
//        System.out.println(this);
    }
    // TODO： finish this
//    @Override
//    public String toString() {
//        String fileString;
//        for (String filename : files) {
//
//        }
//    }

}
