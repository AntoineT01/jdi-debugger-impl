package dbg.command;

import com.sun.jdi.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

import java.util.List;

public class BreakCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    if (args.length < 2) return "Usage: break <ClassName> <lineNumber>";
    String inputName = args[0];
    // Retirer l'extension .java s'il y en a une, en ignorant la casse.
    if (inputName.toLowerCase().endsWith(".java")) {
      inputName = inputName.substring(0, inputName.length() - 5);
    }
    int line;
    try {
      line = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      return "Invalid line number.";
    }

    try {
      // Recherche dans toutes les classes chargées (allClasses()) de la VM
      // On compare le nom simple (après le dernier '.') en ignorant la casse.
      ReferenceType refType = null;
      for (ReferenceType rt : context.getVm().allClasses()) {
        String fullName = rt.name(); // Exemple : "dbg.JDISimpleDebuggee"
        String simpleName = fullName.substring(fullName.lastIndexOf('.') + 1);
        if (simpleName.equalsIgnoreCase(inputName)) {
          refType = rt;
          break;
        }
      }

      if (refType == null) {
        return "Class " + inputName + " not found.";
      }

      List<Location> locations = refType.locationsOfLine(line);
      if (locations.isEmpty()) {
        return "No executable code at line " + line + " in " + refType.name();
      }
      Location location = locations.get(0);
      EventRequestManager erm = context.getVm().eventRequestManager();
      BreakpointRequest bpReq = erm.createBreakpointRequest(location);
      bpReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
      bpReq.enable();
      return "Breakpoint set at " + refType.name() + ":" + line;
    } catch (Exception e) {
      return "Error setting breakpoint: " + e.getMessage();
    }
  }
}
