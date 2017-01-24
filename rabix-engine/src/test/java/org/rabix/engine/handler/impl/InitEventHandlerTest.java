package org.rabix.engine.handler.impl;

import com.google.inject.Injector;
import com.google.inject.Guice;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.config.ConfigModule;
import org.rabix.engine.EngineModule;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.event.impl.InitEvent;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.processor.handler.HandlerFactory;
import org.rabix.engine.processor.handler.impl.InitEventHandler;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.test.DummyConfigModule;
import org.rabix.engine.test.TestApp;
import org.rabix.engine.test.TestPort;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import java.util.HashMap;

@Test(groups = { "functional" })
public class InitEventHandlerTest {

  private Injector injector;

  @BeforeMethod
  public void setUp() {
    injector = Guice.createInjector(new DummyConfigModule(new HashMap<>()), new EngineModule());
  }


  @Test
  public void testHandle() {

    // Must be first to avoid creating dependency cycle
    EventProcessor ep = injector.getInstance(EventProcessor.class);

    HandlerFactory hf = injector.getInstance(HandlerFactory.class);
    ContextRecordService crs = injector.getInstance(ContextRecordService.class);
    JobRecordService jrs = injector.getInstance(JobRecordService.class);
    TestApp app = new TestApp();
    app.inputs.add(TestPort.simplePort("in"));
    app.outputs.add(TestPort.simplePort("out"));
    InitEvent ie = new InitEvent(new HashMap<>(), "rootId", app.toDagNode("node", null), new HashMap<>());
    try {
      hf.get(ie.getType()).handle(ie);
    } catch (EventHandlerException e) {
      e.printStackTrace();
    }

    ContextRecord cr = crs.find("rootId");
    assertNotNull(cr);
    assertEquals(cr.getId(), "rootId");

    JobRecord jr = jrs.find("root", "rootId");
    assertNotNull(jr);

  }

}
