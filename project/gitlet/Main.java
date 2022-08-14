package gitlet;

import static gitlet.Utils.printAndExit;

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
                // TODO: handle the `init` command
                Repository.makeRepo();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                checkInitialized();
                if (args.length != 2) {
                    wrongOperands();
                }
                String fileName = args[1];
                Repository.addBranch(fileName);
                break;
            // TODO: FILL THE REST IN
        }
    }

    /** Exits with message "Incorrect operands.".
     * To be used if the number or order of operands if wrong. */
    private static void wrongOperands() {
        printAndExit("Incorrect operands.");
    }

    /** Checks if current working directory is in an initialized Gitlet directory.
     * If it's not, exits with a message. */
    private static void checkInitialized() {
        if (!Repository.isInitialized()) {
            printAndExit("Not in an initialized Gitlet directory.");
        }
    }
}
