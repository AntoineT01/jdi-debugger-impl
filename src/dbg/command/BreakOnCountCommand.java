package dbg.command;

import com.sun.jdi.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

import java.util.List;

public class BreakOnCountCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    if(args.length < 3) return "Usage: break-on-count <ClassName> <lineNumber> <count>";
    String className = args[0];
    int line, count;
    try {
      line = Integer.parseInt(args[1]);
      count = Integer.parseInt(args[2]);
    } catch(NumberFormatException e) {
      return "Invalid line number or count.";
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
      // On utilise la propriété count pour déclencher le breakpoint après un certain nombre d'atteintes
      bpReq.addCountFilter(count);
      bpReq.enable();
      return "Breakpoint (break-on-count) set at " + className + ":" + line + " with count " + count;
    } catch (Exception e) {
      return "Error setting break-on-count: " + e.getMessage();
    }
  }
}
