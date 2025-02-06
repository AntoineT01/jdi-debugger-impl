package dbg.command;

import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;

public class StepCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    try {
      // Capturer l'état avant le step
      context.captureCurrentState();

      EventRequestManager erm = context.getVm().eventRequestManager();
      for (StepRequest sr : erm.stepRequests()) {
        if (sr.thread().equals(context.getCurrentThread()))
          erm.deleteEventRequest(sr);
      }

      StepRequest stepReq = erm.createStepRequest(
        context.getCurrentThread(),
        StepRequest.STEP_MIN,
        StepRequest.STEP_INTO
      );
      stepReq.addCountFilter(1);
      stepReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
      stepReq.enable();

      return "RESUME: Step into command executed.";
    } catch (Exception e) {
      return "Error executing step: " + e.getMessage();
    }
  }
}