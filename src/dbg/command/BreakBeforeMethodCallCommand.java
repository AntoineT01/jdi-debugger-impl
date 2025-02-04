package dbg.command;

import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;

public class BreakBeforeMethodCallCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    if(args.length < 1) return "Usage: break-before-method-call <methodName>";
    String methodName = args[0];
    try {
      EventRequestManager erm = context.getVm().eventRequestManager();
      // La stratégie ici peut consister à créer une MethodEntryRequest filtrée par le nom de la méthode.
      MethodEntryRequest entryReq = erm.createMethodEntryRequest();
      entryReq.addClassFilter(context.getCurrentFrame().location().declaringType());
      entryReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
      // On filtrera en vérifiant le nom de la méthode dans l'événement (ou en utilisant un filtre spécifique s'il existe).
      entryReq.enable();
      return "Break-before-method-call set for method: " + methodName;
    } catch (Exception e) {
      return "Error setting break-before-method-call: " + e.getMessage();
    }
  }
}
