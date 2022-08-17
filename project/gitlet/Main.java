package gitlet;



/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
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
            // TODO: FILL THE REST IN
        }
    }

    /** Exits with message "Incorrect operands.".
     * To be used if the number or order of operands if wrong. */
    private static void wrongOperands() {
        Utils.printAndExit("Incorrect operands.");
    }

}
