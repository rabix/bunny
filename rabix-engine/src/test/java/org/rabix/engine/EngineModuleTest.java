package org.rabix.engine;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.mockito.ArgumentCaptor;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.config.ConfigModule;
import org.rabix.common.helper.ResourceHelper;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.event.impl.InitEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.store.model.ContextRecord;
import org.rabix.engine.store.model.JobRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.HandlerFactory;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.status.EngineStatusCallback;
import org.rabix.engine.test.DummyConfigModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

@Test(groups = { "functional" })
public class EngineModuleTest {

  private Injector injector;
  private HandlerFactory hf;
  private EventProcessor ep;

  private DAGNodeDB nodeDb;

  private ContextRecordService crs;
  private JobRecordService jrs;
  private LinkRecordService lrs;
  private VariableRecordService vrs;

  @BeforeMethod(enabled=false)
  public void setUp() {
    ConfigModule configModule = new ConfigModule(null, null);
    injector = Guice.createInjector(new DummyConfigModule(), new EngineModule(configModule));
    ep = injector.getInstance(EventProcessor.class);

    hf = injector.getInstance(HandlerFactory.class);

    nodeDb = injector.getInstance(DAGNodeDB.class);

    crs = injector.getInstance(ContextRecordService.class);
    jrs = injector.getInstance(JobRecordService.class);
    lrs = injector.getInstance(LinkRecordService.class);
    vrs = injector.getInstance(VariableRecordService.class);
  }

  @Test(enabled=false)
  public void testNull() throws Exception {
    EngineStatusCallback esc = mock(EngineStatusCallback.class);

    ep.start();
    String simpleApp = ResourceHelper.readResource("apps/null.cwl.yml");
    String appUrl = URIHelper.createDataURI(simpleApp);
    Bindings b = BindingsFactory.create(appUrl);

    Map<String, Object> inputs = Collections.emptyMap();
    DAGNode node = b.translateToDAG(new Job(appUrl, inputs));

    // INIT
    UUID nullJobId = UUID.randomUUID();
    InitEvent ie = new InitEvent(UUID.randomUUID(), new HashMap<>(), nullJobId, Collections.emptyMap(), null, null);
    hf.get(ie.getType()).handle(ie);

    ContextRecord cr = crs.find(nullJobId);
    assertNotNull(cr);
    assertEquals(cr.getId(), nullJobId);

    JobRecord jr = jrs.find("root", nullJobId);
    assertNotNull(jr);

    ArgumentCaptor<Job> job = ArgumentCaptor.forClass(Job.class);

    verify(esc).onJobReady(job.capture());
    assertTrue(job.getValue().getInputs().isEmpty());
    assertEquals(job.getValue().getStatus(), Job.JobStatus.READY);
    assertTrue(job.getValue().isRoot());
    assertNull(job.getValue().getOutputs());

    // JOB COMPLETED
    JobStatusEvent jse = new JobStatusEvent("root", nullJobId, JobRecord.JobState.COMPLETED,
        Collections.emptyMap(), UUID.randomUUID(), "node");
    hf.get(jse.getType()).handle(jse);

    job = ArgumentCaptor.forClass(Job.class);

    verify(esc, times(1)).onJobRootCompleted(job.capture());

    // verify(esc, times(1)).onJobCompleted(job.capture());
    assertTrue(job.getValue().getOutputs().isEmpty());

    verifyNoMoreInteractions(esc);
  }

  @Test(enabled=false)
  public void testNoInputs() throws Exception {

  }

  @Test
  public void testNoOutputs() throws Exception {

  }

  @Test(enabled=false)
  public void testSimple() throws Exception {
    ep.start();

    String simpleApp = ResourceHelper.readResource("apps/simple.cwl.yml");
    String appUrl = URIHelper.createDataURI(simpleApp);
    Bindings b = BindingsFactory.create(appUrl);

    Map<String, Object> inputs = Collections.singletonMap("in", "bla");
    DAGNode node = b.translateToDAG(new Job(appUrl, inputs));

    UUID rootJobId = UUID.randomUUID();
    InitEvent ie = new InitEvent(UUID.randomUUID(), new HashMap<>(), rootJobId, Collections.emptyMap(), null, "node");
    hf.get(ie.getType()).handle(ie);

    ContextRecord cr = crs.find(rootJobId);
    assertNotNull(cr);
    assertEquals(cr.getId(), rootJobId);

    JobRecord jr = jrs.find("root", rootJobId);
    assertNotNull(jr);

  }

}
