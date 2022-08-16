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
        String fileName;
        String message;
        switch(firstArg) {
            case "init":
                Repository.setup();
                break;
            case "add":
                // TODO: handle the `add [filename]` command for removing
                if (args.length != 2) {
                    wrongOperands();
                }
                fileName = args[1];
                Repository.addFile(fileName);
                break;
            case "commit":
                if (args.length != 2) {
                    wrongOperands();
                }
                message = args[1];
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
            // TODO: FILL THE REST IN
        }
    }

    /** Exits with message "Incorrect operands.".
     * To be used if the number or order of operands if wrong. */
    private static void wrongOperands() {
        Utils.printAndExit("Incorrect operands.");
    }

}
