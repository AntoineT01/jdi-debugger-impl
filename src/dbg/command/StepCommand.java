package dbg.command;

import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;

public class StepCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    try {
      EventRequestManager erm = context.getVm().eventRequestManager();
      // Supprimer d'éventuelles StepRequest existantes pour ce thread
      for (StepRequest sr : erm.stepRequests()) {
        if (sr.thread().equals(context.getCurrentThread()))
          erm.deleteEventRequest(sr);
      }
      // Créer un step "step into"
      StepRequest stepReq = erm.createStepRequest(context.getCurrentThread(),
        StepRequest.STEP_MIN, StepRequest.STEP_INTO);
      stepReq.addCountFilter(1); // un seul pas
      stepReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
      stepReq.enable();
      // Préfixe "RESUME:" indique qu'après exécution, on souhaite reprendre
      return "RESUME: Step into command executed.";
    } catch (Exception e) {
      return "Error executing step: " + e.getMessage();
    }
  }
}
