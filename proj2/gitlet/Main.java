package gitlet;
import java.util.HashMap;
import java.util.Set;

import static gitlet.Repository.CWD;
import static gitlet.Utils.*;
/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Wangjishi
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // 如果无命令，打印信息并退出
        if (args == null || args.length == 0) {
            message("Please enter a command");
            return;
        }

        String command = args[0];
        int argumentSize = args.length - 1;

        // 建立所有命令与参数数量对应的映射
        HashMap<String, Set<Integer>> allCommand = new HashMap<>();
        allCommand.put("init", Set.of(0));
        allCommand.put("add", Set.of(1));
        allCommand.put("commit", Set.of(1));
        allCommand.put("rm", Set.of(1));
        allCommand.put("log", Set.of(0));
        allCommand.put("global-log", Set.of(0));
        allCommand.put("find", Set.of(1));
        allCommand.put("status", Set.of(0));
        allCommand.put("checkout", Set.of(1, 2, 3)); // 多种含义的命令
        allCommand.put("branch", Set.of(1));
        allCommand.put("rm-branch", Set.of(1));
        allCommand.put("reset", Set.of(1));
        allCommand.put("merge", Set.of(1));

        // 命令不存在
        if (!allCommand.containsKey(command)) {
            message("No command with that name exists.");
            return;
        }

        // 参数数量错误
        if (!allCommand.get(command).contains(argumentSize)) {
            message("Incorrect operands.");
            return;
        }

        // 在未初始化时使用非init命令
        if (!join(CWD, ".gitlet").exists() && !command.equals("init")) {
            message("Not in an initialized Gitlet directory.");
            return;
        }

        switch(command) {
            case "init":
                Repository.initGitlet();
                break;
            case "add":
                Repository.addFile(args[1]);
                break;
            case "commit":
                Repository.commitWithMessage(args[1]);
                break;
            case "rm":
                Repository.removeFile(args[1]);
                break;
            case "log":
                Repository.printLog();
                break;
            case "global-log":
                Repository.printGlobalLog();
                break;
            case "find":
                Repository.findCommit(args[1]);
                break;
            case "status":
                Repository.printStatus();
                break;
            case "checkout":
                if (argumentSize == 1) { // checkout [branch name]
                    Repository.checkoutBranch(args[1]);
                } else if (argumentSize == 2) { // checkout -- [file name]
                    if (args[1].equals("--")) Repository.checkoutFile(args[2]);
                    else message("Incorrect operands.");
                } else { // checkout [commit id] -- [file name]
                    if (args[2].equals("--")) Repository.checkoutFileInCommit(args[1], args[3]);
                    else message("Incorrect operands.");
                }
                break;
            case "branch":
                Repository.createBranch(args[1]);
                break;
            case "rm-branch":
                Repository.removeBranch(args[1]);
                break;
            case "reset":
                Repository.reset(args[1]);
                break;
            case "merge":
                break;
        }
    }

}
