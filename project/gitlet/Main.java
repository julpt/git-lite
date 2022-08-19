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
                if (args.length != 2) {
                    wrongOperands();
                }
                String fileName = args[1];
                Repository.addFile(fileName);
                break;
            case "commit":
                if (args.length != 2) {
                    wrongOperands();
                }
                String message = args[1];
                Repository.commit(message);
                break;
            case "rm":
                if (args.length != 2) {
                    wrongOperands();
                }
                fileName = args[1];
                Repository.removeFile(fileName);
                break;
            case "log":
                if (args.length != 1) {
                    wrongOperands();
                }
                Repository.log();
                break;
            case "global-log":
                if (args.length != 1) {
                    wrongOperands();
                }
                Repository.logAll();
                break;
            case "find":
                if (args.length != 2) {
                    wrongOperands();
                }
                message = args[1];
                Repository.find(message);
                break;
            case "status":
                if (args.length != 1) {
                    wrongOperands();
                }
                Repository.status();
                break;
            case "checkout":
                if (args.length == 2) {
                    // Usage: java gitlet.Main checkout [branch name]
                    String branch = args[1];
                    Repository.checkoutBranch(branch);
                } else if (args.length == 3 && args[1].equals("--")) {
                    // Usage: java gitlet.Main checkout -- [file name]
                    fileName = args[2];
                    Repository.checkoutFile(fileName);
                } else if (args.length == 4 && args[2].equals("--")) {
                    // Usage: java gitlet.Main checkout [commit id] -- [file name]
                    String commitID = args[1];
                    fileName = args[3];
                    Repository.checkoutFromCommit(commitID, fileName);
                } else {
                    wrongOperands();
                }
                break;
            case "branch":
                if (args.length != 2) {
                    wrongOperands();
                }
                String branchName = args[1];
                Repository.addBranch(branchName);
                break;
            case "rm-branch":
                if (args.length != 2) {
                    wrongOperands();
                }
                branchName = args[1];
                Repository.removeBranch(branchName);
                break;
            case "reset":
                if (args.length != 2) {
                    wrongOperands();
                }
                String commitID = args[1];
                Repository.reset(commitID);
                break;
            case "merge":
                if (args.length != 2) {
                    wrongOperands();
                }
                branchName = args[1];
                Repository.merge(branchName);
                break;
            default:
                Utils.printAndExit("No command with that name exists.");
        }
    }

    /** Exits with message "Incorrect operands.".
     * Used if the number or order of operands is wrong. */
    private static void wrongOperands() {
        Utils.printAndExit("Incorrect operands.");
    }

}
