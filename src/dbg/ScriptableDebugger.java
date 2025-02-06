package dbg;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import dbg.command.CommandDispatcher;
import dbg.command.DebuggerContext;
import dbg.ui.CLIDebuggerUI;
import dbg.ui.DebuggerUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class ScriptableDebugger {

  private Class<?> debugClass;
  private VirtualMachine vm;
  private DebuggerUI ui;

  public ScriptableDebugger() {
    this.ui = new CLIDebuggerUI();
  }

  // Possibilité d'injecter une autre implémentation de UI
  public ScriptableDebugger(DebuggerUI ui) {
    this.ui = ui;
  }

  public VirtualMachine connectAndLaunchVM() throws IOException, IllegalConnectorArgumentsException, VMStartException {
    LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();
    Map<String, Connector.Argument> arguments = launchingConnector.defaultArguments();
    arguments.get("main").setValue(debugClass.getName());
    ui.showOutput("Lancement de la VM pour " + debugClass.getName());
    return launchingConnector.launch(arguments);
  }

  public void attachTo(Class<?> debuggeeClass) {
    this.debugClass = debuggeeClass;
    try {
      vm = connectAndLaunchVM();
      startOutputReader();
      enableClassPrepareRequest();
      startDebugger();
    } catch (VMStartException e) {
      e.printStackTrace();
      ui.showOutput(e.toString());
    } catch (VMDisconnectedException e) {
      ui.showOutput("Virtual Machine is disconnected: " + e);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void enableClassPrepareRequest() {
    EventRequestManager erm = vm.eventRequestManager();
    ClassPrepareRequest cpReq = erm.createClassPrepareRequest();
    cpReq.addClassFilter(debugClass.getName());
    cpReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
    cpReq.enable();
    ui.showOutput("ClassPrepareRequest activée pour " + debugClass.getName());
  }

  private void setBreakPoint(ReferenceType refType, int lineNumber) {
    try {
      List<Location> locations = refType.locationsOfLine(lineNumber);
      if (!locations.isEmpty()) {
        Location location = locations.get(0);
        EventRequestManager erm = vm.eventRequestManager();
        BreakpointRequest bpReq = erm.createBreakpointRequest(location);
        bpReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        bpReq.enable();
        ui.showOutput("Breakpoint installé dans " + refType.name() + " à la ligne " + lineNumber);
      } else {
        ui.showOutput("Aucune instruction trouvée à la ligne " + lineNumber + " dans " + refType.name());
      }
    } catch (AbsentInformationException e) {
      ui.showOutput("Informations de debug absentes pour " + refType.name());
    }
  }

  private void startOutputReader() {
    new Thread(() -> {
      try {
        InputStreamReader isr = new InputStreamReader(vm.process().getInputStream());
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
          ui.showOutput("\nTarget VM: " + line + "\n");
        }
      } catch (IOException e) {
        ui.showOutput("Erreur lors de la lecture de la sortie de la VM : " + e.getMessage());
      }
    }, "VM-OutputReader").start();
  }

  /**
   * Attend une commande de l'utilisateur dans le contexte donné et renvoie true
   * si la commande indique de reprendre (c'est-à-dire, si le résultat commence par "RESUME:").
   */
  private boolean waitForUserCommand(ThreadReference thread) {
    boolean resumeRequested = false;
    while (!resumeRequested) {
      StackFrame frame;
      try {
        frame = thread.frame(0);
      } catch (IncompatibleThreadStateException e) {
        ui.showOutput("Impossible de récupérer la frame courante. Le thread n'est peut-être plus suspendu.");
        return true;
      }
      DebuggerContext context = new DebuggerContext(vm, thread, frame);
      String command = ui.getCommand(context);
      if (command == null || command.trim().isEmpty()) {
        ui.showOutput("Veuillez taper une commande.");
        continue;
      }
      // Le dispatcher traite la commande saisie et renvoie un résultat
      CommandDispatcher dispatcher = new CommandDispatcher();
      Object result = dispatcher.dispatchCommand(context, command);
      if (result != null) {
        String resStr = result.toString();
        ui.showOutput(resStr);
        if (resStr.startsWith("RESUME:")) {
          resumeRequested = true;
        }
      }
    }
    return resumeRequested;
  }

  public void startDebugger() throws VMDisconnectedException, InterruptedException {
    EventQueue eventQueue = vm.eventQueue();
    boolean debugging = true;
    while (debugging) {
      EventSet eventSet = eventQueue.remove();
      for (Event event : eventSet) {
        ui.showOutput(">> " + event);
        if (event instanceof VMStartEvent) {
          ui.showOutput("La VM a démarré.");
          eventSet.resume();
        } else if (event instanceof ClassPrepareEvent cpEvent) {
          ReferenceType refType = cpEvent.referenceType();
          ui.showOutput("Classe préparée: " + refType.name());
          // Pour test
//          if (refType.name().equals(debugClass.getName())) {
//            // Par exemple, on installe un breakpoint à la ligne 11 (adaptable)
//            setBreakPoint(refType, 11);
//          }
//          eventSet.resume();
          if (waitForUserCommand(cpEvent.thread())) {
            eventSet.resume();
          }
        } else if (event instanceof BreakpointEvent bpEvent) {
          ui.showOutput("Breakpoint atteint à: " + bpEvent.location());

          Object targetObj = bpEvent.request().getProperty("breakOnCount");
          if (targetObj != null) {
            int targetCount = (Integer) targetObj;
            Object currentObj = bpEvent.request().getProperty("currentHitCount");
            int currentHit = (currentObj == null) ? 0 : (Integer) currentObj;
            currentHit++;
            bpEvent.request().putProperty("currentHitCount", currentHit);
            System.out.println("Hit count for this breakpoint: " + currentHit);
            // Si ce n'est pas un multiple du seuil, reprendre automatiquement
            if (currentHit % targetCount != 0) {
              System.out.println("Breakpoint reached but not on target count (" + targetCount + "). Resuming automatically.");
              eventSet.resume();
              continue;
            } else {
              System.out.println("Breakpoint reached on target count (" + targetCount + ").");
              bpEvent.request().putProperty("breakOnCount", null);
            }
          }

          // Vérifier si c'est un breakpoint one-shot (break-once)
          Object onceObj = bpEvent.request().getProperty("breakOnce");
          if (onceObj != null && (Boolean) onceObj) {
            bpEvent.request().disable();
            vm.eventRequestManager().deleteEventRequest(bpEvent.request());
            ui.showOutput("One-shot breakpoint removed after being hit.");
            if (waitForUserCommand(bpEvent.thread())) {
              eventSet.resume();
            }
            continue;
          }

          if (waitForUserCommand(bpEvent.thread())) {
            eventSet.resume();
          }
        } else if (event instanceof MethodEntryEvent meEvent) {
          Object targetMethodObj = meEvent.request().getProperty("targetMethod");
          if (targetMethodObj != null) {
            String targetMethod = targetMethodObj.toString();
            String currentMethod = meEvent.location().method().name();
            if (currentMethod.equals(targetMethod)) {
              System.out.println("MethodEntryEvent: Break-before-method-call reached for method: " + currentMethod);
              if (waitForUserCommand(meEvent.thread())) {
                eventSet.resume();
              }
            } else {
              // Ce n'est pas la méthode recherchée, reprendre immédiatement
              eventSet.resume();
            }
          } else {
            // Pas un événement lié à break-before-method-call, reprendre
            eventSet.resume();
          }
        } else if (event instanceof VMDisconnectEvent) {
          ui.showOutput("La VM est déconnectée. Fin du debug.");
          debugging = false;
          eventSet.resume();
        }
      }
    }
    ui.showOutput("End of program");
  }
}
