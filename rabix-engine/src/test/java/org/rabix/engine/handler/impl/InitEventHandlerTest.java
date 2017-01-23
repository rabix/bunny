package org.rabix.engine.handler.impl;

import com.google.inject.Injector;
import com.google.inject.Guice;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.config.ConfigModule;
import org.rabix.engine.EngineModule;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.event.impl.InitEvent;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.processor.handler.impl.InitEventHandler;
import org.rabix.engine.test.TestApp;
import org.rabix.engine.test.TestPort;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import java.util.HashMap;

@Test(groups = { "functional" })
public class InitEventHandlerTest {

  private Injector injector;

  @BeforeMethod
  public void setUp() {
    injector = Guice.createInjector(new ConfigModule(null, null), new EngineModule());
  }



  @Test()
  public void testHandle() {

    InitEventHandler h = injector.getInstance(InitEventHandler.class);
    TestApp app = new TestApp();
    app.inputs.add(TestPort.simplePort("in"));
    app.outputs.add(TestPort.simplePort("out"));
    InitEvent ie = new InitEvent(new HashMap(), "rootId", app.toDagNode("node", null), new HashMap<>());
    try {
      h.handle(ie);
    } catch (EventHandlerException e) {
      e.printStackTrace();
    }
  }

}
