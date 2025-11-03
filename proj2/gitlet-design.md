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

- **String sha1(Object... vals)** 或 **sha1(List\<Object\> vals)**: 返回传入对象的sha1哈希值。


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
- **toString**: 按固定方式，将Commit实例与字符串一一对应的方法，用于哈希。**TODO**
- **printLog**: 按指定格式打印出自己的日志。 **TODO**


### Class 4 : Repository
核心类。管理仓库状态，包含gitlet的所有使用方法（init，add等），以及private辅助方法。

#### Methods
- **initGitlet()**: 初始化一个gitlet仓库，若已经存在，则抛出错误并返回。


- **addFile(fileName)**: 将一个文件添加到暂存区。
  1. 若其与当前提交中文件版本相同，则不添加。(algo1)
  2. 在i的基础上，如果staging-area/changed已经存在该文件，将其从staging-area/changed中移除。
  3. 若该文件存在于staging-area/removed，将其从staging-area/removed移除。


- **commitWithMessage(message)**: 传入提交信息，进行一次提交。
  1. 创建一个commit，其中父提交为当前提交，file复制自父提交，时间与信息不同。
  2. 读取snapshot/changed和/removed，分别修改和删除commit的file字段。
  3. 将修改的文件存入gitlet/file。
  4. 将head与currentBranch改为该提交的sha1。
  5. 清空staging-area。


- **removeFile(fileName)**: 移除或取消暂存一个文件。
  1. 在暂存区中搜索该文件，若找到，则从staging-area和snapshot中删除。
  2. 在head指向的file中搜索该文件，若找到，将其加入snapshot/removed。
  3. 若以上均未找到，打印错误信息"*No reason to remove the file.*"。
  4. 在当前工作目录中搜索该文件，若找到，将其删除.


- **printLog()**: 从当前的head提交开始，沿着提交树反向显示每个提交的信息，直到初始提交。
  1. 调用head提交的printLog。
  2. 从head开始不断沿着parentHash1找到上一提交，并调用其printLog，直至初始提交（parentHash为null）。


- **global-log()**: 按任意顺序打印出所有提交的信息。
  1. 调用plainFilenamesIn，得到commits文件夹下所有文件名构成的列表。
  2. 遍历该列表，用文件名获得文件，读入commit并调用其printLog。


- **find(commitMessage)**: 打印出所有具有给定提交信息的提交的id，每行一个。如果有多个这样的提交，将id打印在不同的行上。
  1. 调用plainFilenamesIn，得到commit文件夹下所有文件名构成的列表。
  2. 遍历该列表，用文件名获得文件，读入commit。若其提交信息与指定信息相同，打印其id。


- **printStatus()**: 按格式打印分支与文件信息。
  1. **Branches**: 按字典序（TreeMap默认）遍历branches，与head比较，若相等则在开头加*并打印，否则直接打印。
  2. **Staged Files**: 按字典序遍历snapshot/changed，打印所有文件名。
  3. **Removed Files**: 按字典序遍历snapshot/removed，打印所有文件名。
  4. **Modifications Not Staged For Commit**: optional, 待定
  5. **Untracked Files**: optional, 待定


- **checkoutFile(fileName)**: 
  1. 若文件不存在，中止并打印错误信息"File does not exist in that commit."。
  2. 将文件在头提交中存在的版本取出并放入工作目录，如果工作目录中已有该文件的版本，则覆盖它。


- **checkoutFileInCommit(commitId, fileName)**: **TODO**: 尝试用6位数字进行搜索
  1. 若ID不存在，打印错误信息"No commit with that id exists"，若提交中无文件，打印错误信息"File does not exist in that commit."。
  2. 将给定ID提交中存在的文件版本取出并放入工作目录，如果工作目录中已有该文件的版本，则覆盖它。


- **checkoutBranch(branchName)**: 将工作目录文件状态切换到给定的分支上，并将head指向该分支最新节点。
  1. 若分支不存在，打印"No such branch exists."，如果该分支是当前分支，则打印"No need to checkout the current branch."。
  2. 如果当前工作目录中有**未被跟踪**，且在对应提交中存在的文件，
  3. 将提交中的所有文件复制到当前目录，若已存在，则覆盖。


- **createBranch(branchName)**: 创建一个具有给定名称的新分支，并将其指向当前的HEAD提交。若已经存在，打印错误消息"A branch with that name already exists."。


- **removeBranch(branchName)**: 删除具有给定名称的分支。仅删除branches中的映射，而不影响任何实际提交。若删除不存在和当前所在的分支。


- **reset(commitId)**: 检出给定提交所跟踪的所有文件。移除在该提交中不存在的已跟踪文件。同时将当前分支的HEAD移动到该提交节点。


- **merge(branchName)**: 将给定分支的文件合并到当前分支。
  1. 从给定分支与当前分支分别向前遍历，用一个集合找到最新分裂点。
  2. 若分裂点与给定分支头结点相同，打印"Given branch is an ancestor of the current branch."并退出。
  3. 若分裂点与当前所在分支头结点(head)相同，checkout给定分支，打印消息"Current branch fast-forwarded."并退出。
  4. 将
    
#### Fields
- 若干路径变量
- **String headPointer**: 存储当前所在提交的sha1，永远指向当前所处的提交。**从文件中读取和写入！！！持久化**
- **String currentBranch**: 当前所在分支名。**从文件中读取和写入！！！持久化**
- **TreeMap\<String, String\> branches**: **有序地**存储所有分支名与所指提交sha1的映射。**从文件中读取和写入！！！持久化**


## File Structure
📦.gitlet<br>
┣ 📜headPointer:存储头指针指向的提交的sha1字符串<br>
┣ 📜currentBranch:存储当前所在分支的名称字符串<br>
┣ 📜branches:存储所有分支名与提交的TreeMap<br>
┣ 📂commits:存储所有提交<br>
┃ ┗ 📜6ce94a8e54d7a942b9ecf8ca60abcb66d547cc37（文件名为commit的sha1）<br>
┣ 📂files:存储所有历史版本文件，文件名为file的sha1<br>
┣ 📂snapshot:存储当前版本被改变或删除的文件名与sha1的映射<br>
┃ ┣ 📜changed:内容是一个TreeMap\<String, String\>，键是被修改的文件名，值是staging-area中的文件的sha1<br>
┃ ┗ 📜removed:内容是一个TreeMap\<String, String\>，存储所有被删除的文件名，值为null<br>
┗ 📂staging-area:存储当前add的文件，文件名为file的sha1


## Algorithms

## Persistence

