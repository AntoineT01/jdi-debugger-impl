package dbg.command;

import com.sun.jdi.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;

import java.util.List;

public class BreakpointsCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    StringBuilder sb = new StringBuilder();
    EventRequestManager erm = context.getVm().eventRequestManager();
    List<BreakpointRequest> bpRequests = erm.breakpointRequests();
    if(bpRequests.isEmpty()) return "No active breakpoints.";
    for(BreakpointRequest bp : bpRequests) {
      sb.append(bp.location().toString()).append("\n");
    }
    return sb.toString();
  }
}
