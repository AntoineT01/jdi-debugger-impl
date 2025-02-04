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
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import dbg.command.CommandDispatcher;
import dbg.command.DebuggerContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

public class ScriptableDebugger {

  private Class debugClass;      // Classe cible à déboguer
  private VirtualMachine vm;     // Machine virtuelle du debuggee

  /**
   * Connexion et lancement de la VM en passant en argument le nom de la classe debuggee.
   */
  public VirtualMachine connectAndLaunchVM() throws IOException, IllegalConnectorArgumentsException, VMStartException {
    LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();
    Map<String, Connector.Argument> arguments = launchingConnector.defaultArguments();
    arguments.get("main").setValue(debugClass.getName());
    System.out.println("Lancement de la VM pour " + debugClass.getName());
    return launchingConnector.launch(arguments);
  }

  /**
   * Méthode d'attachement au debuggee.
   */
  public void attachTo(Class debuggeeClass) {
    this.debugClass = debuggeeClass;
    try {
      vm = connectAndLaunchVM();
      enableClassPrepareRequest();
      startDebugger();
    } catch (VMStartException e) {
      e.printStackTrace();
      System.out.println(e);
    } catch (VMDisconnectedException e) {
      System.out.println("Virtual Machine is disconnected: " + e);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Active une ClassPrepareRequest pour la classe debuggee.
   */
  private void enableClassPrepareRequest() {
    EventRequestManager erm = vm.eventRequestManager();
    ClassPrepareRequest cpReq = erm.createClassPrepareRequest();
    cpReq.addClassFilter(debugClass.getName());
    cpReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
    cpReq.enable();
    System.out.println("ClassPrepareRequest activée pour " + debugClass.getName());
  }

  /**
   * Installe un breakpoint sur la classe cible à la ligne donnée.
   */
  private void setBreakPoint(ReferenceType refType, int lineNumber) {
    try {
      List<Location> locations = refType.locationsOfLine(lineNumber);
      if (!locations.isEmpty()) {
        Location location = locations.get(0);
        EventRequestManager erm = vm.eventRequestManager();
        BreakpointRequest bpReq = erm.createBreakpointRequest(location);
        bpReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        bpReq.enable();
        System.out.println("Breakpoint installé dans " + refType.name() + " à la ligne " + lineNumber);
      } else {
        System.out.println("Aucune instruction trouvée à la ligne " + lineNumber + " dans " + refType.name());
      }
    } catch (AbsentInformationException e) {
      System.out.println("Informations de debug absentes pour " + refType.name());
    }
  }

  /**
   * Attend une commande utilisateur et retourne true si la commande indique de reprendre l'exécution.
   * Cette méthode boucle jusqu'à obtenir une commande dont le résultat commence par "RESUME:".
   */
  private boolean waitForUserCommand(ThreadReference thread) {
    boolean resumeRequested = false;
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    while (!resumeRequested) {
      StackFrame frame;
      try {
        frame = thread.frame(0);
      } catch (IncompatibleThreadStateException itse) {
        System.out.println("Impossible de récupérer la frame courante. Le thread n'est peut-être plus suspendu.");
        return true;
      }
      DebuggerContext context = new DebuggerContext(vm, thread, frame);
      CommandDispatcher dispatcher = new CommandDispatcher();
      Object result = dispatcher.dispatchCommand(context);
      if (result != null) {
        String resStr = result.toString();
        System.out.println(resStr);
        if (resStr.startsWith("RESUME:")) {
          resumeRequested = true;
        }
      }
    }
    return resumeRequested;
  }

  /**
   * Boucle principale de gestion des événements JDI.
   * Le debugger ne reprend l'exécution que lorsque l'utilisateur le lui demande.
   */
  public void startDebugger() throws VMDisconnectedException, InterruptedException {
    EventQueue eventQueue = vm.eventQueue();
    boolean debugging = true;
    while (debugging) {
      EventSet eventSet = eventQueue.remove();
      // On traite chaque événement de l'EventSet
      for (Event event : eventSet) {
        System.out.println(">> " + event);
        if (event instanceof VMStartEvent) {
          System.out.println("La VM a démarré.");
          // Événement non interactif : on resume immédiatement.
          eventSet.resume();
        } else if (event instanceof ClassPrepareEvent) {
          ClassPrepareEvent cpEvent = (ClassPrepareEvent) event;
          ReferenceType refType = cpEvent.referenceType();
          System.out.println("Classe préparée: " + refType.name());
          if (refType.name().equals(debugClass.getName())) {
            setBreakPoint(refType, 6);
          }
          // Événement non interactif : on resume immédiatement.
          eventSet.resume();
        } else if (event instanceof BreakpointEvent) {
          BreakpointEvent bpEvent = (BreakpointEvent) event;
          System.out.println("Breakpoint atteint à: " + bpEvent.location());
          // Événement interactif : on attend une commande de reprise.
          if (waitForUserCommand(bpEvent.thread())) {
            eventSet.resume();
          }
        } else if (event instanceof StepEvent) {
          StepEvent stepEvent = (StepEvent) event;
          System.out.println("StepEvent à: " + stepEvent.location());
          // Événement interactif : on attend une commande de reprise.
          if (waitForUserCommand(stepEvent.thread())) {
            eventSet.resume();
          }
        } else if (event instanceof VMDisconnectEvent) {
          System.out.println("La VM est déconnectée. Fin du debug.");
          debugging = false;
          eventSet.resume();
        }
      }
    }

    // Transfert du flux de sortie du debuggee à la fin du debug.
    System.out.println("End of program");
    try {
      InputStreamReader reader = new InputStreamReader(vm.process().getInputStream());
      OutputStreamWriter writer = new OutputStreamWriter(System.out);
      reader.transferTo(writer);
      writer.flush();
    } catch (IOException e) {
      System.out.println("Target VM input stream reading error.");
    }
  }
}
