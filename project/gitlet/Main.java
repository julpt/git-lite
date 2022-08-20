package gitlet;



/** Driver class for Gitlet, a subset of the Git version-control system. */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                Repository.setup();
                break;
            case "add":
                checkOperands(args, 2);
                Repository.addFile(args[1]);
                break;
            case "commit":
                checkOperands(args, 2);
                Repository.commit(args[1]);
                break;
            case "rm":
                checkOperands(args, 2);
                Repository.removeFile(args[1]);
                break;
            case "log":
                checkOperands(args, 1);
                Repository.log();
                break;
            case "global-log":
                checkOperands(args, 1);
                Repository.logAll();
                break;
            case "find":
                checkOperands(args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                checkOperands(args, 1);
                Repository.status();
                break;
            case "checkout":
                if (args.length == 2) {
                    // Usage: java gitlet.Main checkout [branch name]
                    Repository.checkoutBranch(args[1]);
                } else if (args.length == 3 && args[1].equals("--")) {
                    // Usage: java gitlet.Main checkout -- [file name]
                    Repository.checkoutFile(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    // Usage: java gitlet.Main checkout [commit id] -- [file name]
                    Repository.checkoutFromCommit(args[1], args[3]);
                } else {
                    Utils.printAndExit("Incorrect operands.");
                }
                break;
            case "branch":
                checkOperands(args, 2);
                Repository.addBranch(args[1]);
                break;
            case "rm-branch":
                checkOperands(args, 2);
                Repository.removeBranch(args[1]);
                break;
            case "reset":
                checkOperands(args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                checkOperands(args, 2);
                Repository.merge(args[1]);
                break;
            case "add-remote":
                // Usage: java gitlet.Main add-remote [remote name] [name of remote directory]/.gitlet
                checkOperands(args, 3);
                Repository.addRemote(args[1], args[2]);
                break;
            case "rm-remote":
                // Usage: java gitlet.Main rm-remote [remote name]
                checkOperands(args, 2);
                Repository.removeRemote(args[1]);
                break;
            case "push":
                // Usage: java gitlet.Main push [remote name] [remote branch name]
                checkOperands(args, 3);
                Repository.push(args[1], args[2]);
                break;
            case "fetch":
                // Usage: java gitlet.Main fetch [remote name] [remote branch name]
                checkOperands(args, 3);
                Repository.fetch(args[1], args[2]);
                break;
            case "pull":
                // Usage: java gitlet.Main pull [remote name] [remote branch name]
                checkOperands(args, 3);
                Repository.pull(args[1], args[2]);
                break;
            default:
                Utils.printAndExit("No command with that name exists.");
        }
    }

    /** Checks if the number of arguments is correct. Prints an error message
     * if the wrong number of arguments was provided. */
    private static void checkOperands(String[] operands, int correctLength) {
        if (operands.length != correctLength) {
            Utils.printAndExit("Incorrect operands.");
        }
    }

}
