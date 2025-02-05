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
import java.util.List;
import java.util.Map;

public class ScriptableDebugger {

  private Class<?> debugClass;
  private VirtualMachine vm;

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
  public void attachTo(Class<?> debuggeeClass) {
    this.debugClass = debuggeeClass;
    try {
      vm = connectAndLaunchVM();
      startOutputReader();
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
        Location location = locations.getFirst();
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
    new InputStreamReader(System.in);
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
      for (Event event : eventSet) {
        System.out.println(">> " + event);
        if (event instanceof VMStartEvent) {
          System.out.println("La VM a démarré.");
          eventSet.resume();
        } else if (event instanceof ClassPrepareEvent cpEvent) {
          ReferenceType refType = cpEvent.referenceType();
          System.out.println("Classe préparée: " + refType.name());
          if (refType.name().equals(debugClass.getName())) {
            setBreakPoint(refType, 11);
          }
          eventSet.resume();
        } else if (event instanceof BreakpointEvent bpEvent) {
          System.out.println("Breakpoint atteint à: " + bpEvent.location());
          // Vérifier si ce breakpoint est un break-on-count
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
          // Si ce n'est pas un break-on-count ou que c'est le bon hit, attendre la commande de reprise.
          if (waitForUserCommand(bpEvent.thread())) {
            eventSet.resume();
          }
        } else if (event instanceof StepEvent stepEvent) {
          System.out.println("StepEvent à: " + stepEvent.location());
          if (waitForUserCommand(stepEvent.thread())) {
            eventSet.resume();
          }
        } else if (event instanceof MethodEntryEvent meEvent) {
          // Vérifier si ce MethodEntryEvent correspond au break-before-method-call
          Object targetMethodObj = meEvent.request().getProperty("targetMethod");
          if (targetMethodObj != null) {
            String targetMethod = targetMethodObj.toString();
            String currentMethod = meEvent.location().method().name();
            if (currentMethod.equals(targetMethod)) {
              System.out.println("MethodEntryEvent: Break-before-method-call reached for method: " + currentMethod);
              // Suspendre et attendre une commande de reprise
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
          System.out.println("La VM est déconnectée. Fin du debug.");
          debugging = false;
          eventSet.resume();
        }
      }
    }

    System.out.println("End of program");
  }

  private void startOutputReader() {
    new Thread(() -> {
      try {
        InputStreamReader isr = new InputStreamReader(vm.process().getInputStream());
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
          System.out.println("\nTarget VM: " + line + "\n");
        }
      } catch (IOException e) {
        System.out.println("Erreur lors de la lecture de la sortie de la VM : " + e.getMessage());
      }
    }, "VM-OutputReader").start();
  }
}
