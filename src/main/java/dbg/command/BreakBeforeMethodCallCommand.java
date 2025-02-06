package dbg.command;

import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;

public class BreakBeforeMethodCallCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    if (args.length != 1) return "Usage: break-before-method-call <methodName>";
    String targetMethod = args[0];
    try {
      EventRequestManager erm = context.getVm().eventRequestManager();
      // Créer une demande d'entrée de méthode pour la classe concernée.
      MethodEntryRequest entryReq = erm.createMethodEntryRequest();
      // On filtre par la classe du frame courant (vous pouvez affiner selon vos besoins)
      entryReq.addClassFilter(context.getCurrentFrame().location().declaringType());
      entryReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
      // Stocker le nom de la méthode recherchée dans une propriété
      entryReq.putProperty("targetMethod", targetMethod);
      entryReq.enable();
      return "Break-before-method-call set for method: " + targetMethod;
    } catch (Exception e) {
      return "Error setting break-before-method-call: " + e.getMessage();
    }
  }
}
