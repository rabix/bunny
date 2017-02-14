package org.rabix.engine;

import com.google.inject.Injector;
import com.google.inject.Guice;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.ResourceHelper;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.db.ReadyJobGroupsDB;
import org.rabix.engine.event.impl.InitEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.HandlerFactory;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.status.EngineStatusCallback;
import org.rabix.engine.status.impl.NoOpEngineStatusCallback;
import org.rabix.engine.test.DummyConfigModule;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Test(groups = { "functional" })
public class EngineModuleTest {

  private Injector injector;
  private HandlerFactory hf;
  private EventProcessor ep;

  private DAGNodeDB nodeDb;
  private ReadyJobGroupsDB readyDB;

  private ContextRecordService crs;
  private JobRecordService jrs;
  private LinkRecordService lrs;
  private VariableRecordService vrs;

  @BeforeMethod
  public void setUp() {
    injector = Guice.createInjector(
        new DummyConfigModule(), new EngineModule()
    );
    ep = injector.getInstance(EventProcessor.class);

    hf = injector.getInstance(HandlerFactory.class);

    nodeDb = injector.getInstance(DAGNodeDB.class);
    readyDB = injector.getInstance(ReadyJobGroupsDB.class);

    crs = injector.getInstance(ContextRecordService.class);
    jrs = injector.getInstance(JobRecordService.class);
    lrs = injector.getInstance(LinkRecordService.class);
    vrs = injector.getInstance(VariableRecordService.class);
  }

  @Test
  public void testNull() throws Exception {

    EngineStatusCallback esc = mock(EngineStatusCallback.class);

    ep.start(
        Collections.emptyList(), esc
    );
    String simpleApp = ResourceHelper.readResource("apps/null.cwl.yml");
    String appUrl = URIHelper.createDataURI(simpleApp);
    Bindings b = BindingsFactory.create(appUrl);

    Map<String, Object> inputs = Collections.emptyMap();
    DAGNode node = b.translateToDAG(new Job(appUrl, inputs));

    // INIT

    InitEvent ie = new InitEvent(new HashMap<>(), "null-job-id", node, Collections.emptyMap());
    hf.get(ie.getType()).handle(ie);

    ContextRecord cr = crs.find("null-job-id");
    assertNotNull(cr);
    assertEquals(cr.getId(), "null-job-id");

    JobRecord jr = jrs.find("root", "null-job-id");
    assertNotNull(jr);

    ArgumentCaptor<Job> job = ArgumentCaptor.forClass(Job.class);

    verify(esc).onJobReady(job.capture());
    assertTrue(job.getValue().getInputs().isEmpty());
    assertEquals(job.getValue().getStatus(), Job.JobStatus.READY);
    assertTrue(job.getValue().isRoot());
    assertNull(job.getValue().getOutputs());

    // JOB COMPLETED

    JobStatusEvent jse = new JobStatusEvent("root", "null-job-id", JobRecordService.JobState.COMPLETED, Collections.emptyMap(), "eventGroupId");
    hf.get(jse.getType()).handle(jse);

    job = ArgumentCaptor.forClass(Job.class);

    verify(esc, times(1)).onJobRootCompleted(job.capture());

//    verify(esc, times(1)).onJobCompleted(job.capture());

    assertTrue(job.getValue().getOutputs().isEmpty());

    verifyNoMoreInteractions(esc);
  }

  @Test
  public void testNoInputs() throws Exception {

  }

  @Test
  public void testNoOutputs() throws Exception {

  }

  @Test
  public void testSimple() throws Exception {

    ep.start(
        Collections.emptyList(), new NoOpEngineStatusCallback()
    );

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
