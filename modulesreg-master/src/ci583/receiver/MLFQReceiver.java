package ci583.receiver;
/**
 * The Multi-Level Feedback Queue Receiver. This receiver manages two lists of processes, which are YOUNG
 * and OLD. To schedule the next process it does the following:
 * + If the the list of YOUNG processes is not empty, take the next process, allow it to run then
 *   put it at the end of the list of OLD processes (unless the state of
 *   process is TERMINATED).
 * + If the list of YOUNG processes is empty, take the next process from the list of OLD processes, allow it
 *   to run then put it at the end of the list of YOUNG processes (unless the state of
 *   process is TERMINATED).
 *
 *   @author Jim Burton
 */
import java.util.ArrayList;
import java.util.List;

public class MLFQReceiver extends ModRegReceiver {

    private List<ModuleRegister> YOUNG;
    private List<ModuleRegister> OLD;

    /**
     * Constructs a multi-level feedback queue receiver. The constructor needs to call the constructor of the
     * superclass then initialise the two lists for young and old processes.
     *
     * @param quantum
     */
    public MLFQReceiver(long quantum) {
        super(quantum);
        this.OLD = new ArrayList<>();
        this.YOUNG = new ArrayList<>();
    }

    /**
     * Adds a new process to the list of young processes.
     *
     * @param m
     */
    @Override
    public void enqueue(ModuleRegister m) {
        YOUNG.add(m);
    }

    /**
     * Schedule the module registration processes. This method needs to:
     * + create an empty list which will hold the completed processes. This will be the
     * return value of the method.
     * + while one of the queues is not empty:
     * - if the list of YOUNG processes is not empty, take the next process and get its State.
     * - if the state is NEW, start the process then sleep for QUANTUM milliseconds
     * then put the process at the back of the list of OLD processes.
     * - if the state is TERMINATED, add it to the results list.
     * - if the state is anything else then interrupt the process to wake it up then
     * sleep for QUANTUM milliseconds, then put the process at the back of the queue.
     * <p>
     * - if the list of YOUNG processes is empty, do the same except take the process from the
     * list of OLD processes and, after it does its 'work' put it at the end of the list of
     * YOUNG processes.
     * + when both lists are empty, return the list of completed processes.
     *
     * @return
     */


    /*
     * THIS METHOD WILL FAIL ON MAC OS.
     * THE TESTS HAVE BEEN SUCCESSFUL ON MULTIPLE WINDOWS DEVICES.
     *
     * THERE IS ALSO AN ISSUE WHERE AFTER RUNNING ON WINDOWS MULTIPLE TIMES, IT WILL RANDOMLY FAIL
     * AFTER THIS RANDOM FAILED TEST, IT WILL CONTINUE TO PASS LIKE NORMAL.
     */

    @Override
    public List<ModuleRegister> startRegistration() {
        ArrayList<ModuleRegister> results = new ArrayList<>();

        while (!YOUNG.isEmpty() || !OLD.isEmpty()) {
            if (!YOUNG.isEmpty()) {
                ModuleRegister process = YOUNG.remove(0);
                ModuleRegister.State state = process.getState();
                if (state == ModuleRegister.State.NEW) {
                    process.start();
                    try {
                        Thread.sleep(QUANTUM);
                    } catch (InterruptedException ignored) {
                    }
                    OLD.add(process);
                } else if (state == ModuleRegister.State.TERMINATED) {
                    results.add(process);
                } else {
                    process.interrupt();
                    try {
                        Thread.sleep(QUANTUM);
                    } catch (InterruptedException ignored) {
                    }
                    OLD.add(process);
                }
            }
            if (!OLD.isEmpty()){
                ModuleRegister process = OLD.remove(0);
                ModuleRegister.State state = process.getState();
                if (state == ModuleRegister.State.NEW) {
                    process.start();
                    try {
                        Thread.sleep(QUANTUM);
                    } catch (InterruptedException ignored) {
                    }
                    YOUNG.add(process);
                } else if (state == ModuleRegister.State.TERMINATED) {
                    results.add(process);
                } else {
                    process.interrupt();
                    try {
                        Thread.sleep(QUANTUM);
                    } catch (InterruptedException ignored) {
                    }
                    YOUNG.add(process);
                }
            }
        }
        return results;
    }

}

