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
                // TODO: handle the `add [filename]` command for removing
                if (args.length != 2) {
                    wrongOperands();
                }
                String addedFile = args[1];
                Repository.addFile(addedFile);
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
                String rmFile = args[1];
                Repository.removeFile(rmFile);
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
            // TODO: FILL THE REST IN
        }
    }

    /** Exits with message "Incorrect operands.".
     * To be used if the number or order of operands if wrong. */
    private static void wrongOperands() {
        Utils.printAndExit("Incorrect operands.");
    }

}
