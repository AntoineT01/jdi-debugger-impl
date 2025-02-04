package dbg.command;

import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;

public class StepCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    // Active un stepping "STEP_INTO" sur le thread courant.
    try {
      EventRequestManager erm = context.getVm().eventRequestManager();
      // Supprimer d'éventuelles requêtes de step existantes
      for (StepRequest sr : erm.stepRequests()) {
        if (sr.thread().equals(context.getCurrentThread()))
          erm.deleteEventRequest(sr);
      }
      StepRequest stepReq = erm.createStepRequest(context.getCurrentThread(),
        StepRequest.STEP_MIN, StepRequest.STEP_INTO);
      stepReq.addCountFilter(1); // un seul pas
      stepReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
      stepReq.enable();
      return "Step into command executed.";
    } catch (Exception e) {
      return "Error executing step: " + e.getMessage();
    }
  }
}
