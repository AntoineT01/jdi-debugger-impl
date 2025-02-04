package dbg.command;

import com.sun.jdi.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

import java.util.List;

public class BreakCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    if(args.length < 2) return "Usage: break <ClassName> <lineNumber>";
    String className = args[0];
    int line;
    try {
      line = Integer.parseInt(args[1]);
    } catch(NumberFormatException e) {
      return "Invalid line number.";
    }
    try {
      List<ReferenceType> classes = context.getVm().classesByName(className);
      if(classes.isEmpty()) return "Class " + className + " not found.";
      ReferenceType refType = classes.get(0);
      List<Location> locations = refType.locationsOfLine(line);
      if(locations.isEmpty()) return "No executable code at line " + line + " in " + className;
      Location location = locations.get(0);
      EventRequestManager erm = context.getVm().eventRequestManager();
      BreakpointRequest bpReq = erm.createBreakpointRequest(location);
      bpReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
      bpReq.enable();
      return "Breakpoint set at " + className + ":" + line;
    } catch (Exception e) {
      return "Error setting breakpoint: " + e.getMessage();
    }
  }
}
