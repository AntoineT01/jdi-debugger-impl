package dbg;

import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;

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
        // On passe le nom complet de la classe debuggee à la VM via l’argument "main"
        arguments.get("main").setValue(debugClass.getName());
        System.out.println("Lancement de la VM pour " + debugClass.getName());
      return launchingConnector.launch(arguments);
    }

    /**
     * Méthode d'attachement au debuggee.
     * Après connexion à la VM, on active la requête de préparation de classe
     * puis on démarre la boucle de gestion des événements.
     */
    public void attachTo(Class debuggeeClass) {
        this.debugClass = debuggeeClass;
        try {
            vm = connectAndLaunchVM();
            // Activer la requête pour intercepter la préparation de la classe cible (étape 1.4)
            enableClassPrepareRequest();
            // Démarrer la boucle d’événements (étapes 1.2, 1.3, 1.5, 1.6)
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
     * Active une ClassPrepareRequest afin d'être notifié dès que la classe debuggee est chargée.
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
     * Installe un breakpoint sur la classe cible à une ligne donnée.
     * Ici, nous choisissons par exemple la ligne 6 de la méthode main.
     */
    private void setBreakPoint(ReferenceType refType, int lineNumber) {
        try {
            List<Location> locations = refType.locationsOfLine(lineNumber);
            if (!locations.isEmpty()) {
                Location location = locations.get(0); // On prend la première location disponible
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
     * Active une StepRequest sur le thread donné pour avancer d'une instruction.
     * On supprime d'abord les requêtes de step précédentes sur ce thread.
     */
    private void enableStepRequest(ThreadReference thread) {
        EventRequestManager erm = vm.eventRequestManager();
        // Supprimer d'éventuelles StepRequest existantes pour ce thread
        for (StepRequest sr : erm.stepRequests()) {
            if (sr.thread().equals(thread)) {
                erm.deleteEventRequest(sr);
            }
        }
        // Créer et activer une nouvelle StepRequest
        StepRequest stepReq = erm.createStepRequest(thread, StepRequest.STEP_MIN, StepRequest.STEP_OVER);
        stepReq.addCountFilter(1); // Un seul pas à la fois
        stepReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        stepReq.enable();
        System.out.println("StepRequest activée pour le thread " + thread.name());
    }

    private void waitForUserCommand(ThreadReference thread) {
        System.out.println("Commande : tapez 'step' pour avancer d'une instruction, sinon tapez autre chose pour continuer.");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String cmd = reader.readLine();
            if ("step".equalsIgnoreCase(cmd.trim())) {
                enableStepRequest(thread);
                System.out.println("Pas-à-pas activé. Exécution de l'instruction suivante...");
            } else {
                System.out.println("Reprise de l'exécution jusqu'au prochain point d'arrêt.");
                // Ici, aucune StepRequest n'est configurée ; la VM reprendra son cours jusqu'au prochain événement.
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de la lecture de la commande utilisateur.");
            e.printStackTrace();
        }
    }

    /**
     * Boucle principale de gestion des événements JDI.
     * Gère les événements suivants :
     * - VMStartEvent : notification de démarrage de la VM.
     * - ClassPrepareEvent : la classe debuggee est chargée, on y installe un breakpoint.
     * - BreakpointEvent : arrêt sur breakpoint, on active le step.
     * - StepEvent : affichage de la location actuelle et réactivation du step.
     * - VMDisconnectEvent : fin de la session.
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
                } else if (event instanceof ClassPrepareEvent cpEvent) {
                  ReferenceType refType = cpEvent.referenceType();
                    System.out.println("Classe préparée: " + refType.name());
                    // Si c'est la classe que nous voulons déboguer, installer le breakpoint (par exemple à la ligne 6)
                    if (refType.name().equals(debugClass.getName())) {
                        setBreakPoint(refType, 6);
                    }
                } else if (event instanceof BreakpointEvent bpEvent) {
                  System.out.println("Breakpoint atteint à: " + bpEvent.location());
                    // Pour l'étape 7, on attend une commande utilisateur ici...
                    waitForUserCommand(bpEvent.thread());
                } else if (event instanceof StepEvent stepEvent) {
                  System.out.println("StepEvent à: " + stepEvent.location());
                    // Demander une commande utilisateur
                    waitForUserCommand(stepEvent.thread());
                } else if (event instanceof VMDisconnectEvent) {
                    debugging = false;
                }
            }
            // Reprise de l'exécution de la VM après traitement des événements
            eventSet.resume();
        }

        // Une fois la session de debugging terminée, afficher les sorties du debuggee
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
