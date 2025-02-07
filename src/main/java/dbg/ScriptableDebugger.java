package dbg;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import dbg.command.CommandDispatcher;
import dbg.command.DebuggerContext;
import dbg.event.*;
import dbg.state.ExecutionState;
import dbg.ui.DebuggerUI;
import dbg.ui.CLIDebuggerUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
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
    if (!ui.isBlocking()) {
      // Code pour UI non bloquante...
    }

    boolean resumeRequested = false;
    // Réutiliser le contexte existant ou en créer un nouveau si nécessaire
    DebuggerContext context = DebuggerSession.hasContext() ?
      DebuggerSession.getContext() :
      new DebuggerContext(vm, thread, null);

    while (!resumeRequested) {
      try {
        // Mettre à jour la frame dans le contexte existant
        StackFrame frame = thread.frame(0);
        context.setCurrentThread(thread);
        context.setCurrentFrame(frame);

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
      } catch (IncompatibleThreadStateException e) {
        ui.showOutput("Impossible de récupérer la frame courante.");
        return true;
      }
    }
    return resumeRequested;
  }


//  public void startDebugger() throws VMDisconnectedException, InterruptedException {
//    DebuggerSession.setContext(new DebuggerContext(vm, null, null));
//    EventQueue eventQueue = vm.eventQueue();
//    boolean debugging = true;
//    while (debugging) {
//      EventSet eventSet = eventQueue.remove();
//      DebuggerSession.setCurrentEventSet(eventSet);
//      for (Event event : eventSet) {
//        ui.showOutput(">> " + event);
//        DebuggerEventHandler handler = getHandlerForEvent(event);
//        if (handler != null) {
//          if (handler.handle(event, eventSet, this)) {
//            eventSet.resume();
//          }
//        } else {
//          eventSet.resume();
//        }
//        if (event instanceof VMDisconnectEvent) {
//          debugging = false;
//          break;
//        }
//      }
//    }
//    ui.showOutput("Fin du débogage.");
//  }
private void startDebugger() throws VMDisconnectedException, InterruptedException {
  DebuggerSession.setContext(new DebuggerContext(vm, null, null));
  EventQueue eventQueue = vm.eventQueue();
  boolean debugging = true;

  while (debugging) {
    try {
      EventSet eventSet = eventQueue.remove();

      for (Event event : eventSet) {
        ui.showOutput(">> " + event);

        if (DebuggerSession.isReplayInProgress()) {
          ui.showOutput("DEBUG: En mode replay");

          if (event instanceof ClassPrepareEvent) {
            ClassPrepareEvent cpe = (ClassPrepareEvent) event;
            ExecutionState targetState = DebuggerSession.getContext().getTargetState();
            ui.showOutput("DEBUG: ClassPrepareEvent reçu pour " + cpe.referenceType().name());

            if (targetState != null) {
              ui.showOutput("DEBUG: État cible trouvé, location: " +
                              targetState.getLocation().declaringType().name() + ":" +
                              targetState.getLocation().lineNumber());

              if (cpe.referenceType().name().equals(targetState.getLocation().declaringType().name())) {
                ui.showOutput("DEBUG: Configuration du breakpoint replay");
                setupReplayBreakpoint();
              }
            }
          }
          else if (event instanceof BreakpointEvent) {
            BreakpointEvent bpe = (BreakpointEvent) event;
            ui.showOutput("DEBUG: BreakpointEvent reçu à " + bpe.location());

            if (isTargetLocation(bpe.location())) {
              ui.showOutput("DEBUG: Point cible atteint!");
              DebuggerSession.resetReplay();

              // Récupérer l'historique jusqu'à l'état cible
              List<ExecutionState> history = DebuggerSession.getContext().getExecutionHistory();
              int targetIndex = DebuggerSession.getContext().getTargetState().getExecutionIndex();
              List<ExecutionState> truncatedHistory = history.subList(0, targetIndex );

              // Créer le nouveau contexte avec l'historique tronqué
              DebuggerContext context = new DebuggerContext(vm, bpe.thread(), bpe.thread().frame(0));
              context.setExecutionHistory(truncatedHistory);
              DebuggerSession.setContext(context);

              if (waitForUser(bpe.thread(), eventSet)) {
                eventSet.resume();
              }
              continue;
            }
          }
          eventSet.resume();
          continue;
        }

        // Gestion normale des événements
        DebuggerEventHandler handler = getHandlerForEvent(event);
        if (handler != null) {
          if (handler.handle(event, eventSet, this)) {
            eventSet.resume();
          }
        } else {
          eventSet.resume();
        }

        if (event instanceof VMDeathEvent) {
          ui.showOutput("DEBUG: VMDeathEvent reçu - Mode replay: " + DebuggerSession.isReplayInProgress());
          if (!DebuggerSession.isReplayInProgress()) {
            debugging = false;
            break;
          }
          ui.showOutput("DEBUG: Continuation après VMDeathEvent en mode replay");
        }
      }
    } catch (VMDisconnectedException e) {
      if (DebuggerSession.isReplayInProgress()) {
        ui.showOutput("DEBUG: VMDisconnectedException en mode replay - Redémarrage");
        try {
          vm = connectAndLaunchVM(); // Reconnecter directement
          eventQueue = vm.eventQueue(); // Réinitialiser la queue d'événements
          enableClassPrepareRequest();
          setupReplayBreakpoint();
          continue;
        } catch (Exception ex) {
          ui.showOutput("Erreur fatale pendant le redémarrage: " + ex.getMessage());
          debugging = false;
        }
      }
      debugging = false;
    } catch (IncompatibleThreadStateException e) {
      throw new RuntimeException(e);
    }
  }
}

  private void setupReplayBreakpoint() {
    try {
      ExecutionState targetState = DebuggerSession.getContext().getTargetState();
      if (targetState != null) {
        Location targetLocation = targetState.getLocation();
        // Nettoyer d'abord les anciens breakpoints
        EventRequestManager erm = vm.eventRequestManager();
        for (BreakpointRequest bp : erm.breakpointRequests()) {
          erm.deleteEventRequest(bp);
        }
        // Placer le nouveau breakpoint
        setBreakpoint(targetLocation.declaringType().name(),
                      targetLocation.lineNumber(),
                      true);
        ui.showOutput("Breakpoint replay configuré à " +
                        targetLocation.declaringType().name() + ":" +
                        targetLocation.lineNumber());
      }
    } catch (Exception e) {
      ui.showOutput("Erreur lors de la configuration du breakpoint de replay: " + e.getMessage());
    }
  }

  private boolean isTargetLocation(Location current) {
    ui.showOutput("DEBUG: Comparaison des locations - Current: " +
                    current.declaringType().name() + ":" + current.lineNumber());

    ExecutionState targetState = DebuggerSession.getContext().getTargetState();
    if (targetState == null) return false;

    Location targetLocation = targetState.getLocation();
    boolean isTarget = current.lineNumber() == targetLocation.lineNumber() &&
      current.declaringType().name().equals(targetLocation.declaringType().name());

    if (isTarget) {
      DebuggerSession.resetReplay();
      ui.showOutput("DEBUG: Cible atteinte!");
    }
    return isTarget;
  }

  private void restartDebugging() {
    try {
      DebuggerContext currentContext = DebuggerSession.getContext();
      ExecutionState targetState = currentContext.getTargetState();
      List<ExecutionState> savedHistory = currentContext.getExecutionHistory();

      ui.showOutput("DEBUG: Démarrage du redémarrage - État cible: " + targetState.getExecutionIndex());

      // Redémarrer la VM
      vm.dispose();
      vm = connectAndLaunchVM();
      ui.showOutput("DEBUG: Nouvelle VM connectée");

      // Nouveau contexte avec historique préservé
      DebuggerContext newContext = new DebuggerContext(vm, null, null);
      newContext.setExecutionHistory(savedHistory);
      newContext.setTargetState(targetState);
      newContext.setReplayMode(true);
      DebuggerSession.setContext(newContext);
      ui.showOutput("DEBUG: Nouveau contexte créé avec historique");

      // Configuration des requêtes
      enableClassPrepareRequest();
      ui.showOutput("DEBUG: ClassPrepareRequest activée");

      // Configuration explicite du mode replay
      DebuggerSession.startReplay(targetState);
      ui.showOutput("DEBUG: Mode replay activé");

      setupReplayBreakpoint();
      ui.showOutput("DEBUG: Breakpoint replay configuré");

    } catch (Exception e) {
      ui.showOutput("DEBUG: Erreur pendant le redémarrage: " + e.getMessage());
      e.printStackTrace();
      DebuggerSession.resetReplay();
    }
  }

  private void handleEvent(Event event, EventSet eventSet) {
    // Afficher l'événement pour le suivi du débogage
    ui.showOutput(">> " + event);

    // Récupérer le handler approprié pour cet événement
    DebuggerEventHandler handler = getHandlerForEvent(event);

    try {
      if (handler != null) {
        // Si le handler renvoie true, cela signifie qu'il faut reprendre l'exécution
        if (handler.handle(event, eventSet, this)) {
          eventSet.resume();
        }
      } else {
        // Si aucun handler n'est trouvé, on reprend l'exécution par défaut
        eventSet.resume();
      }

      // Si c'est un événement de déconnexion de la VM, on sort de la boucle de débogage
      if (event instanceof VMDisconnectEvent) {
        ui.showOutput("VM déconnectée");
        return;
      }
    } catch (Exception e) {
      ui.showOutput("Erreur lors du traitement de l'événement : " + e.getMessage());
      // En cas d'erreur, on essaie de reprendre l'exécution pour éviter un blocage
      eventSet.resume();
    }
  }


  /**
   * Place un point d'arrêt dans une classe à une ligne spécifique.
   * Cette méthode est particulièrement importante pour le time travelling,
   * car elle nous permet de nous arrêter à l'endroit exact où nous voulons revenir.
   *
   * @param className Le nom complet de la classe où placer le breakpoint
   * @param lineNumber Le numéro de ligne où placer le breakpoint
   * @return true si le breakpoint a été placé avec succès, false sinon
   */
//  private boolean setBreakpoint(String className, int lineNumber) {
//    try {
//      // Parcourir toutes les classes chargées dans la VM
//      for (ReferenceType refType : vm.allClasses()) {
//        // Trouver la classe qui correspond au nom donné
//        if (refType.name().equals(className)) {
//          // Récupérer toutes les locations possibles pour cette ligne
//          List<Location> locations = refType.locationsOfLine(lineNumber);
//
//          if (locations != null && !locations.isEmpty()) {
//            // Prendre la première location valide
//            Location location = locations.get(0);
//
//            // Créer et configurer la requête de breakpoint
//            EventRequestManager erm = vm.eventRequestManager();
//            BreakpointRequest bpReq = erm.createBreakpointRequest(location);
//
//            // Si nous sommes en mode replay, marquer ce breakpoint comme spécial
//            if (DebuggerSession.getContext().isInReplayMode()) {
//              bpReq.putProperty("replayBreakpoint", true);
//            }
//
//            // Activer le breakpoint
//            bpReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
//            bpReq.enable();
//
//            ui.showOutput("Breakpoint placé à " + className + ":" + lineNumber);
//            return true;
//          } else {
//            ui.showOutput("Impossible de trouver une location valide pour le breakpoint à la ligne " + lineNumber);
//          }
//        }
//      }
//      ui.showOutput("Classe " + className + " non trouvée pour placer le breakpoint");
//      return false;
//    } catch (AbsentInformationException e) {
//      ui.showOutput("Information de débogage manquante pour placer le breakpoint : " + e.getMessage());
//      return false;
//    } catch (Exception e) {
//      ui.showOutput("Erreur lors de la création du breakpoint : " + e.getMessage());
//      return false;
//    }
//  }


  private void setBreakpoint(String className, int lineNumber, boolean isReplayBreakpoint) {
    try {
      for (ReferenceType refType : vm.allClasses()) {
        if (refType.name().equals(className)) {
          List<Location> locations = refType.locationsOfLine(lineNumber);
          if (!locations.isEmpty()) {
            Location location = locations.get(0);
            EventRequestManager erm = vm.eventRequestManager();
            BreakpointRequest bpReq = erm.createBreakpointRequest(location);

            if (isReplayBreakpoint) {
              bpReq.putProperty("replayBreakpoint", true);
            }

            bpReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
            bpReq.enable();

            String type = isReplayBreakpoint ? "replay" : "normal";
            ui.showOutput("Breakpoint " + type + " placé à " + className + ":" + lineNumber);
            return;
          }
        }
      }
    } catch (Exception e) {
      ui.showOutput("Erreur lors de la création du breakpoint : " + e.getMessage());
    }
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
