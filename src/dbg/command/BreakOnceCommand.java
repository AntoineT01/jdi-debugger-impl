package dbg.command;

import com.sun.jdi.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

import java.util.List;

public class BreakOnceCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    if(args.length < 2) return "Usage: break-once <ClassName> <lineNumber>";
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
      ReferenceType refType = classes.getFirst();
      List<Location> locations = refType.locationsOfLine(line);
      if(locations.isEmpty()) return "No executable code at line " + line + " in " + className;
      Location location = locations.getFirst();
      EventRequestManager erm = context.getVm().eventRequestManager();
      BreakpointRequest bpReq = erm.createBreakpointRequest(location);
      bpReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
      // Ajout d'un listener ou vérification lors de l'événement pour désactiver ce breakpoint.
      // Ici, nous comptabilisons l'atteinte et le désactivons immédiatement après.
      bpReq.putProperty("breakOnce", true);
      bpReq.enable();
      return "One-shot breakpoint set at " + className + ":" + line;
    } catch (Exception e) {
      return "Error setting one-shot breakpoint: " + e.getMessage();
    }
  }
}
