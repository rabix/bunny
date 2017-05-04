package org.rabix.engine.stub.plugins;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.ResourceRequirement;
import org.rabix.common.helper.JSONHelper;
import org.rabix.engine.model.ContextRecord;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.mechanism.TransportPlugin;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.impl.rabbitmq.TransportPluginRabbitMQ;
import org.rabix.transport.mechanism.impl.rabbitmq.TransportQueueRabbitMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SlurmPlugin {
    private final static Logger logger = LoggerFactory.getLogger(SlurmPlugin.class);
    protected BackendRabbitMQ backend;
    protected TransportPluginRabbitMQ transportPlugin;

    protected TransportQueueRabbitMQ sendToBackendQueue;
    protected TransportQueueRabbitMQ sendToBackendControlQueue;
    protected TransportQueueRabbitMQ receiveFromBackendQueue;
    protected TransportQueueRabbitMQ receiveFromBackendHeartbeatQueue;
    public HashMap<String, Job.JobStatus> slurmBunnyJobStates = createSlurmBunnyJobStatesMap();

    public SlurmPlugin(BackendRabbitMQ backendRabbitMQ, Configuration configuration) throws TransportPluginException {
        logger.info("Slurm plugin initialized");
        this.transportPlugin = new TransportPluginRabbitMQ(configuration);

        BackendRabbitMQ.BackendConfiguration backendConfiguration = backendRabbitMQ.getBackendConfiguration();
        this.sendToBackendQueue = new TransportQueueRabbitMQ(backendConfiguration.getExchange(), backendConfiguration.getExchangeType(), backendConfiguration.getReceiveRoutingKey());
        this.sendToBackendControlQueue = new TransportQueueRabbitMQ(backendConfiguration.getExchange(), backendConfiguration.getExchangeType(), backendConfiguration.getReceiveControlRoutingKey());

        BackendRabbitMQ.EngineConfiguration engineConfiguration = backendRabbitMQ.getEngineConfiguration();
        this.receiveFromBackendQueue = new TransportQueueRabbitMQ(engineConfiguration.getExchange(), engineConfiguration.getExchangeType(), engineConfiguration.getReceiveRoutingKey());
        this.receiveFromBackendHeartbeatQueue = new TransportQueueRabbitMQ(engineConfiguration.getExchange(), engineConfiguration.getExchangeType(), engineConfiguration.getHeartbeatRoutingKey());

        initialize();
        start();
        logger.info("Slurm plugin started successfully");
    }

    private void initialize() {
        try {
            transportPlugin.initializeExchange(backend.getBackendConfiguration().getExchange(), backend.getBackendConfiguration().getExchangeType());
            transportPlugin.initializeExchange(backend.getEngineConfiguration().getExchange(), backend.getEngineConfiguration().getExchangeType());
        } catch (TransportPluginException e) {
            // do nothing
        }
    }

    private void start() {
        transportPlugin.startReceiver(sendToBackendQueue, Job.class, new TransportPlugin.ReceiveCallback<Job>() {
            @Override
            public void handleReceive(Job job) throws TransportPluginException {
                String jobId = sendSlurmJob(job);
                Thread squeueChecker = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Runtime rt = Runtime.getRuntime();
                        String command = "squeue -h -t all -j" + jobId;
                        String[] s;
                        while (true) {
                            try {
                                Thread.sleep(1000);
                                Process proc = rt.exec(command);
                                BufferedReader stdInput = new BufferedReader(new
                                        InputStreamReader(proc.getInputStream()));
                                // example line:
                                //      14     debug job-tran  vagrant   F       0:00      1 (NonZeroExitCode)
                                // Explanation:
                                //   JOBID  PARTITION  NAME   USER      ST       TIME  NODES NODELIST(REASON)
                                s = stdInput.readLine().split(" ");
                                String jobState = s[4];
                                if (isFinished(jobState)){
                                    send(Job.cloneWithStatus(job, slurmBunnyJobStates.get(jobState)));
                                }
                            }catch(IOException e) {
                                logger.error("Could not open job file");
                                e.printStackTrace(System.err);
                                System.exit(10);
                            }catch (InterruptedException e) {
                                logger.error("Failed to wait for squeue to respond", e);
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });
            }
        }, new TransportPlugin.ErrorCallback() {
            @Override
            public void handleError(Exception error) {
                logger.error("Failed to receive message.", error);
            }
        });

    }

    public void send(Job job) {
        transportPlugin.send(receiveFromBackendQueue, job);
    }


    private String sendSlurmJob(Job job) {
        String output = "";
        String jobId = "";
        try {
            Bindings bindings = BindingsFactory.create(job);

            String bunnyJobPath = "job.json";
            String slurmJobText = "#!/bin/sh\n";

            ResourceRequirement resourceRequirements = bindings.getResourceRequirement(job);
            String slurmDirective = getSlurmResourceRequirements(resourceRequirements);
            slurmJobText += slurmDirective;

            String slurmCommand = "srun /usr/share/rabix-slurm-command-line/rabix -j " + bunnyJobPath;
            slurmJobText += slurmCommand;

            // this file must be transferred to execution node
            File bunnyJobFile = new File(bunnyJobPath);
            FileUtils.writeStringToFile(bunnyJobFile, JSONHelper.writeObject(job));

            String slurmJobPath = "slurm-job.sh";
            File slurmJob = new File(slurmJobPath);
            FileUtils.writeStringToFile(slurmJob, slurmJobText);

            // TODO: explore ProcessBuilder
            Runtime rt = Runtime.getRuntime();
            String command = "sbatch " + slurmJobPath;
            Process proc = rt.exec(command);
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));
            String s;
            int i = 0;
            while ((s = stdInput.readLine()) != null) {
                // first line of output:
                // slurmctld: _slurm_rpc_submit_batch_job JobId=12 usec=184
                if (i == 0) {
                    String pattern = "JobId=\\d*?";
                    Pattern r = Pattern.compile(pattern);
                    Matcher m = r.matcher(s);
                    if (m.find()) {
                        jobId = m.group(0);
                    }
                    output += s;
                    i++;
                }
            }
            } catch(IOException e){
                logger.error("Could not open job file");
                e.printStackTrace(System.err);
                System.exit(10);
            }catch(BindingException e){
                logger.error("Failed to use Bindings", e);
                e.printStackTrace(System.err);
                System.exit(11);
            }
        return jobId;
    }


    private static String getSlurmResourceRequirements(ResourceRequirement requirements){
        final String batchDirective = "#SBATCH";
        String directive = "";
        Long cpuMin = requirements.getCpuMin();
        Long memMin = requirements.getMemMinMB();
        if (cpuMin != null){
            directive += batchDirective + " --ntasks-per-node=" + Long.toString(cpuMin) + "\n";
        }
        if (memMin != null){
            directive += batchDirective + " --mem=" + Long.toString(memMin) + "\n";
        }
        return directive;
    }

    private boolean isFinished(String jobStatus) {
        switch (jobStatus) {
            case "F": // failed
            case "ST":
            case "SE":
            case "S":
            case "TO":
            case "CD":
            case "BF":
            case "NF":
                return true;
            default:
                return false;
        }
    }

    private HashMap<String, Job.JobStatus> createSlurmBunnyJobStatesMap(){
        HashMap<String, Job.JobStatus> m = new HashMap<String, Job.JobStatus>();
        m.put("F", Job.JobStatus.FAILED);
        m.put("BF", Job.JobStatus.FAILED);
        m.put("NF", Job.JobStatus.FAILED);
        m.put("SE", Job.JobStatus.FAILED);
        m.put("PR", Job.JobStatus.ABORTED);
        m.put("ST", Job.JobStatus.ABORTED);
        m.put("S", Job.JobStatus.ABORTED);
        m.put("TO", Job.JobStatus.ABORTED);
        m.put("CD", Job.JobStatus.COMPLETED);
        return m;
    }
}

