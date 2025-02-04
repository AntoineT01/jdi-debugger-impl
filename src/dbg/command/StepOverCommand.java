package dbg.command;

import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;

public class StepOverCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    try {
      EventRequestManager erm = context.getVm().eventRequestManager();
      for (StepRequest sr : erm.stepRequests()) {
        if (sr.thread().equals(context.getCurrentThread()))
          erm.deleteEventRequest(sr);
      }
      StepRequest stepReq = erm.createStepRequest(context.getCurrentThread(),
        StepRequest.STEP_MIN, StepRequest.STEP_OVER);
      stepReq.addCountFilter(1);
      stepReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
      stepReq.enable();
      return "Step over command executed.";
    } catch (Exception e) {
      return "Error executing step-over: " + e.getMessage();
    }
  }
}
