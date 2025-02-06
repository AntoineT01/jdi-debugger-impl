package dbg;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import dbg.command.CommandDispatcher;
import dbg.command.DebuggerContext;
import dbg.event.*;
import dbg.ui.DebuggerUI;
import dbg.ui.CLIDebuggerUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ScriptableDebugger {
  private DebuggerContext debuggerContext;


  private Class<?> debugClass;
  private VirtualMachine vm;
  private final DebuggerUI ui;
  private final Map<Class<? extends Event>, DebuggerEventHandler> eventHandlers;

  // Constructeur avec UI personnalisée
  public ScriptableDebugger(DebuggerUI ui) {
    this.ui = ui;
    this.eventHandlers = new HashMap<>();
    registerEventHandlers();
  }

  // Constructeur par défaut utilisant le mode CLI
  public ScriptableDebugger() {
    this(new CLIDebuggerUI());
  }

  private void registerEventHandlers() {
    eventHandlers.put(VMStartEvent.class, new VMStartEventHandler(ui));
    eventHandlers.put(ClassPrepareEvent.class, new ClassPrepareEventHandler(ui));
    eventHandlers.put(BreakpointEvent.class, new BreakpointEventHandler(ui));
    eventHandlers.put(MethodEntryEvent.class, new MethodEntryEventHandler(ui));
    eventHandlers.put(VMDisconnectEvent.class, new VMDisconnectEventHandler(ui));
    eventHandlers.put(StepEvent.class, new StepEventHandler(ui));
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
    } catch (Exception e) {
      ui.showOutput("Erreur lors de l'attachement : " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void enableClassPrepareRequest() {
    EventRequestManager erm = vm.eventRequestManager();
    ClassPrepareRequest cpReq = erm.createClassPrepareRequest();
    erm.createMethodEntryRequest().addClassFilter("dbg.JDISimpleDebuggee");
    cpReq.addClassFilter(debugClass.getName());
    cpReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
    cpReq.enable();
    ui.showOutput("ClassPrepareRequest activée pour " + debugClass.getName());
  }

  private void startOutputReader() {
    new Thread(() -> {
      try (BufferedReader br = new BufferedReader(new InputStreamReader(vm.process().getInputStream()))) {
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
   * Attend une commande de l'utilisateur.
   * En mode CLI (ui.isBlocking()==true), cette méthode lit en boucle jusqu'à obtenir la commande "continue" (peu importe la casse)
   * ou une commande via le CommandDispatcher qui renvoie une chaîne commençant par "RESUME:".
   * En mode GUI, elle retourne immédiatement, car l'action se fait via des boutons.
   */
  /**
   * Attend une commande de l'utilisateur.
   * En mode CLI (ui.isBlocking()==true), cette méthode lit en boucle jusqu'à obtenir une commande
   * via le CommandDispatcher qui renvoie une chaîne commençant par "RESUME:" ou, explicitement, la commande "continue".
   */
  public boolean waitForUser(ThreadReference thread, EventSet eventSet) {
    // Pour les UI non bloquantes (ex. GUI), reprendre immédiatement.
    if (!ui.isBlocking()) {
      StackFrame frame;
      try {
        frame = thread.frame(0);
      } catch (IncompatibleThreadStateException e) {
        ui.showOutput("Impossible de récupérer la frame courante. Le thread n'est peut-être plus suspendu.");
        return true;
      }
      DebuggerSession.setContext(new DebuggerContext(vm, thread, frame));
      return false;
    }
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
    DebuggerSession.setContext(new DebuggerContext(vm, null, null));
    EventQueue eventQueue = vm.eventQueue();
    boolean debugging = true;
    while (debugging) {
      EventSet eventSet = eventQueue.remove();
      DebuggerSession.setCurrentEventSet(eventSet);
      for (Event event : eventSet) {
        ui.showOutput(">> " + event);
        DebuggerEventHandler handler = getHandlerForEvent(event);
        if (handler != null) {
          if (handler.handle(event, eventSet, this)) {
            eventSet.resume();
          }
        } else {
          eventSet.resume();
        }
        if (event instanceof VMDisconnectEvent) {
          debugging = false;
          break;
        }
      }
    }
    ui.showOutput("Fin du débogage.");
  }

  private DebuggerEventHandler getHandlerForEvent(Event event) {
    DebuggerEventHandler handler = eventHandlers.get(event.getClass());
    if (handler == null) {
      for (Map.Entry<Class<? extends Event>, DebuggerEventHandler> entry : eventHandlers.entrySet()) {
        if (entry.getKey().isInstance(event)) {
          handler = entry.getValue();
          break;
        }
      }
    }
    return handler;
  }

  public VirtualMachine getVm() {
    return vm;
  }
}
