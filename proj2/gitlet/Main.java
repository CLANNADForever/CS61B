package gitlet;
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
        if (args == null) {
            message("Please enter a command");
            return;
        }

        String firstArg = args[0];
        int argumentSize = args.length - 1;
        switch(firstArg) {
            case "init":
                if (validateNumArgs(argumentSize, 0)) {
                    Repository.initGitlet();
                }
                break;
            case "add":
                if (validateNumArgs(argumentSize, 1)) {
                    Repository.addFile(args[1]);
                }
                break;
            case "commit":
                if (validateNumArgs(argumentSize, 1)) {
                    Repository.commitWithMessage(args[1]);
                }
                break;
            case "rm":
                if (validateNumArgs(argumentSize, 1)) {
                    Repository.removeFile(args[1]);
                }
                break;
            case "log":
                break;
            case "global-log":
                break;
            case "find":
                break;
            case "status":
                break;
            case "checkout":
                break;
            case "branch":
                break;
            case "rm-branch":
                break;
            case "reset":
                break;
            case "merge":
                break;
            default:
                message("No command with that name exists.");
                return;
        }
    }

}
