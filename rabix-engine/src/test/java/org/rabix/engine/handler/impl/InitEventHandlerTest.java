package org.rabix.engine.handler.impl;

import com.google.inject.Injector;
import com.google.inject.Guice;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.ResourceHelper;
import org.rabix.engine.EngineModule;
import org.rabix.engine.event.impl.InitEvent;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.HandlerFactory;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.status.impl.NoOpEngineStatusCallback;
import org.rabix.engine.test.DummyConfigModule;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Test(groups = { "functional" })
public class InitEventHandlerTest {

  private Injector injector;

  @BeforeMethod
  public void setUp() {
    injector = Guice.createInjector(new DummyConfigModule(new HashMap<>()), new EngineModule());
    injector.getInstance(EventProcessor.class).start(
        Collections.emptyList(), new NoOpEngineStatusCallback()
    );

  }


  @Test
  public void testHandle() throws Exception {

    HandlerFactory hf = injector.getInstance(HandlerFactory.class);
    ContextRecordService crs = injector.getInstance(ContextRecordService.class);
    JobRecordService jrs = injector.getInstance(JobRecordService.class);

    String simpleApp = ResourceHelper.readResource("apps/simple.cwl.yml");
    String appUrl = URIHelper.createDataURI(simpleApp);
    Bindings b = BindingsFactory.create(appUrl);

    Map<String, Object> inputs = Collections.singletonMap("in", "bla");
    DAGNode node = b.translateToDAG(new Job(appUrl, inputs));

    InitEvent ie = new InitEvent(new HashMap<>(), "rootId", node, Collections.emptyMap());
    hf.get(ie.getType()).handle(ie);

    ContextRecord cr = crs.find("rootId");
    assertNotNull(cr);
    assertEquals(cr.getId(), "rootId");

    JobRecord jr = jrs.find("root", "rootId");
    assertNotNull(jr);

  }

}
