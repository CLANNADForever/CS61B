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

- **string sha1(Object... vals)** 或 **sha1(List\<Object\> vals)**: 返回传入对象的sha1哈希值。
- **boolean restrictedDelete(String file)** 或  **restrictedDelete(File file)**: 删除非目录的文件，返回是否成功。只可用于删除含.gitlet目录下的文件。
- **byte[] readContents(File file)**: 按字节数组读取文件。
- **String readContentsAsString(File file):** 按字符串读取文件。
- **void writeContents(File file, Object... contents)**: 传入若干字符串和字节数组，将其写入文件。若存在文件，则直接覆盖。
- **\<T extends Serializable\> T readObject(File file, Class\<T\> expectedClass)**: 将文件读取为对应类型的对象。
- **void writeObject(File file, Serializable obj)**: 将对象写入文件。
- **List\<String\> plainFilenamesIn(File dir 或 String dir)**: 按字典序返回传入目录下所有文件名构成的列表。
- **File join(String 或 File first, String... others)**: 返回当前操作系统下的一个拼接的路径。
- **byte[] serialize(Serializable obj)**: 返回一个可序列化对象的序列化内容。
- **GitletException error(String msg, Object... args)**: 返回一个传入字段构成信息的异常。
- **void message(String msg, Object... args)**: 打印出传入的信息。

#### Fields
仅有用于实现功能的private变量。


### Class 3 : Commit
代表一次提交，包含其自身时间戳和提交信息，同时包含文件名及其引用，和指向父提交的引用。
一个Commit对象本身会被序列化写入文件。

#### Fields

- **String message**: 提交信息。
- **long timeStamp**: 提交时的时间戳。
- **String parentHash1**: 父提交1的sha1
- **String parentHash2**: 父提交2的sha1
- **TreeMap<String, String> files**: 该提交内所有关联文件名:sha1的映射

#### Methods
- **Commit()**: 创建一个默认初始提交
- **Commit(...)**: 根据传入信息，创建一个提交。时间戳自动生成，无需传入。
- **toString**: 按固定方式，将Commit实例与字符串一一对应的方法，用于哈希。// TODO


### Class 4 : Repository
核心类。管理仓库状态，包含gitlet的所有使用方法（init，add等），以及private辅助方法。

#### Methods
- **initGitlet**: 初始化一个gitlet仓库，若已经存在，则抛出错误并返回。
- **addFile**: 将一个文件添加到暂存区。
  1. 若其与当前提交中文件版本相同，则不添加。(algo1)
  2. 在i的基础上，如果staging-area/changed已经存在该文件，将其从staging-area/changed中移除。
  3. 若该文件存在于staging-area/removed，将其从staging-area/removed移除。
- **commitWithMessage**: 传入提交信息，进行一次提交。
  1. 创建一个commit，其中父提交为当前提交，file复制自父提交，时间与信息不同。
  2. 读取snapshot/changed和/removed，分别修改和删除commit的file字段。
  3. 将修改的文件存入gitlet/file。
  4. 将head与currentBranch改为该提交的sha1。
  5. 清空staging-area。

#### Fields
- 若干路径变量
- **String headPointer**: 存储当前所在提交的sha1，永远指向当前所处的提交。
- **String 


## File Structure
📦.gitlet<br>
┣ 📂commits:存储所有提交<br>
┃ ┗ 📜1760c3ce6ced84ee5483c6e1f91d63d8b082fbaa（文件名为commit的sha1）<br>
┣ 📂files:存储所有历史版本文件，文件名为file的sha1<br>
┣ 📂snapshot:存储当前版本被改变或删除的文件名与sha1的映射<br>
┃ ┣ 📜changed:内容是一个map\<string, string\>，键是被修改的文件名，值是staging-area中的文件的sha1<br>
┃ ┗ 📜removed:内容是一个set\<string\>，存储所有被删除的文件名<br>
┗ 📂staging-area:存储当前add的文件，文件名为file的sha1


## Algorithms

## Persistence

