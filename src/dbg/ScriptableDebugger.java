package dbg;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import dbg.graphic.controller.DebuggerController;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScriptableDebugger {
  private Class debugClass;
  private VirtualMachine vm;
  private DebuggerController controller;
  private final AtomicBoolean suspended = new AtomicBoolean(false);

  public ScriptableDebugger() {}

  public void setController(DebuggerController controller) {
    this.controller = controller;
  }

  public VirtualMachine getVM() {
    return vm;
  }

  public boolean isSuspended() {
    return suspended.get();
  }

  public VirtualMachine connectAndLaunchVM() throws IOException, IllegalConnectorArgumentsException, VMStartException {
    LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();
    Map<String, Connector.Argument> arguments = launchingConnector.defaultArguments();
    arguments.get("main").setValue(debugClass.getName());
    System.out.println("Launching VM for " + debugClass.getName());
    return launchingConnector.launch(arguments);
  }

  public void attachTo(Class debuggeeClass) {
    this.debugClass = debuggeeClass;
    try {
      vm = connectAndLaunchVM();
      enableClassPrepareRequest();
      startDebugger();
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
        System.out.println("Breakpoint set at line " + lineNumber);
      }
    } catch (AbsentInformationException e) {
      System.out.println("No line number information available");
    }
  }

  public synchronized void resume() {
    if (suspended.get() && vm != null) {
      suspended.set(false);
      vm.resume();
    }
  }

  public void startDebugger() {
    EventQueue eventQueue = vm.eventQueue();
    boolean connected = true;

    while (connected) {
      try {
        EventSet eventSet = eventQueue.remove();
        boolean shouldResume = true;

        for (Event event : eventSet) {
          if (event instanceof ClassPrepareEvent) {
            ClassPrepareEvent evt = (ClassPrepareEvent) event;
            if (evt.referenceType().name().equals(debugClass.getName())) {
              setBreakPoint(evt.referenceType(), 6);
            }
          }
          else if (event instanceof BreakpointEvent || event instanceof StepEvent) {
            shouldResume = false;
            suspended.set(true);
          }
          else if (event instanceof VMDisconnectEvent) {
            connected = false;
          }

          if (controller != null) {
            controller.handleEvent(event);
          }
        }

        if (shouldResume && !suspended.get()) {
          eventSet.resume();
        }
      } catch (InterruptedException | VMDisconnectedException e) {
        connected = false;
      }
    }

    try {
      if (vm != null && vm.process() != null) {
        InputStreamReader reader = new InputStreamReader(vm.process().getInputStream());
        OutputStreamWriter writer = new OutputStreamWriter(System.out);
        reader.transferTo(writer);
        writer.flush();
      }
    } catch (IOException e) {
      System.out.println("Error reading VM output");
    }
  }
}