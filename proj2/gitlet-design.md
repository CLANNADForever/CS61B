# Gitlet Design Document

**Name**: Wangjishi

## Classes and Data Structures

### Class 1 : Main
用于接受命令行命令与参数，检测二者是否合法后，抛出错误或调用Repository的相应方法。

#### Methods
作为程序入口，只有main方法。

#### Fields
作为程序入口，本身无变量和字段。


### Class 2 : Utils
课程提供的工具类，只需阅读javadoc注释并按需使用。

#### Methods

1. string sha1(Object... vals) 或 sha1(List\<Object\> vals): 返回传入对象的sha1哈希值。
2. boolean restrictedDelete(String file) 或  restrictedDelete(File file): 删除非目录的文件，返回是否成功。只可用于删除含.gitlet目录下的文件。
3. byte[] readContents(File file): 按字节数组读取文件。
4. String readContentsAsString(File file): 按字符串读取文件。
5. void writeContents(File file, Object... contents): 传入若干字符串和字节数组，将其写入文件。若存在文件，则直接覆盖。
6. \<T extends Serializable\> T readObject(File file, Class\<T\> expectedClass): 将文件读取为对应类型的对象。
7. void writeObject(File file, Serializable obj): 将对象写入文件。
8. List\<String\> plainFilenamesIn(File dir 或 String dir): 按字典序返回传入目录下所有文件名构成的列表。
9. File join(String 或 File first, String... others)： 返回当前操作系统下的一个拼接的路径。
10. byte[] serialize(Serializable obj): 返回一个可序列化对象的序列化内容。
11. GitletException error(String msg, Object... args): 返回一个传入字段构成信息的异常。
12. void message(String msg, Object... args): 打印出传入的信息。

#### Fields
仅有用于实现功能的private变量。


### Class 3 : 

#### Fields

1. Field 1
2. Field 2


## Algorithms

## Persistence

