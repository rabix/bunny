package org.rabix.backend.slurm.client;

import com.google.inject.Inject;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.rabix.backend.slurm.helpers.CWLJobInputsWriter;
import org.rabix.backend.slurm.model.SlurmJob;
import org.rabix.backend.slurm.model.SlurmState;
import org.rabix.backend.slurm.service.SlurmJobService;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.ResourceRequirement;
import org.rabix.common.helper.EncodingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SlurmClient {
    private final static Logger logger = LoggerFactory.getLogger(SlurmClient.class);
    private final String rabixWorkerCLI;
    private String rabixWorkerCLIConfigDir = null;
    @Inject
    private SlurmJobService slurmJobService;

    @Inject
    public SlurmClient(final Configuration configuration) {
        this.rabixWorkerCLI = Paths.get(configuration.getString("rabix.slurm.rabix-worker-cli")).toString();
        if (configuration.containsKey("rabix.slurm.rabix-worker-cli-config-dir")) {
            this.rabixWorkerCLIConfigDir = Paths.get(configuration.getString("rabix.slurm.rabix-worker-cli-config-dir")).toString();
        }
    }

    public void getState() {
        return;
    }

    public SlurmJob getJob(String slurmJobId) throws SlurmClientException {
        SlurmJob defaultSlurmJob = new SlurmJob(SlurmState.Unknown);
        try {
            String command = "squeue -h -t all -j " + slurmJobId;
            String result = "15     debug slurm-jo  vagrant  CD       0:00      1 server";
            // mock command
            command = "echo " + result;
            String[] s;

            File commandFile = new File("command.sh");
            FileUtils.writeStringToFile(commandFile, command);
            String[] commands = {"bash", "command.sh"};
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            Thread.sleep(1000);
            logger.debug("Sending command: \n" + command);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));
            // example output line:
            //      14     debug job-tran  vagrant   F       0:00      1 (NonZeroExitCode)
            // Explanation:
            //   JOBID  PARTITION  NAME   USER      ST       TIME  NODES NODELIST(REASON)
            String output = stdInput.readLine().trim();
            logger.debug("Pinging slurm queue: \n" + output);
            s = output.split("\\s+");
            String jobStatus = s[4];
            SlurmState jobState = SlurmJob.convertToJobState(jobStatus);
            return new SlurmJob(jobState);

        } catch (IOException e) {
            logger.error("Could not open job file");
            throw new SlurmClientException("Failed to get ServiceInfo entity", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return defaultSlurmJob;
    }


    public String runJob(Job job, File workingDir) {
        String output = "";
        String jobId = "";
        try {
            Bindings bindings = BindingsFactory.create(job);
            String slurmCommand = "sbatch -Q -J bunny_job_" + job.getName().replace(".", "_");
//          Special command
//            slurmCommand = "chmod -R 777 " + workingDir + ";" + slurmCommand;
            ResourceRequirement resourceRequirements = bindings.getResourceRequirement(job);
            String resourceDirectives = getSlurmResourceRequirements(resourceRequirements);
            slurmCommand += resourceDirectives;
            logger.debug("Sending slurm job");

            String cwlJob = EncodingHelper.decodeBase64(job.getApp());
            cwlJob = preprocessCWLJob(cwlJob);
            File cwlJobFile = new File(workingDir, "job.json");
            FileUtils.writeStringToFile(cwlJobFile, cwlJob);
            File inputsFile = CWLJobInputsWriter.createInputsFile(job, workingDir);

            String command = rabixWorkerCLI;
            if (rabixWorkerCLIConfigDir != null)
                command += " " + rabixWorkerCLIConfigDir;
            command += " " + cwlJobFile.getAbsolutePath() + " " + inputsFile.getAbsolutePath();
            slurmCommand += " --wrap=\"" + command + "\"";
            // Mock command
            slurmCommand = command + ";\necho Submitted batch job 16";
            String s;
            logger.debug("Submitting command: " + slurmCommand);
            File commandFile = new File(workingDir,"command.sh");
            FileUtils.writeStringToFile(commandFile, slurmCommand);
            String[] commands = {"bash", commandFile.getAbsolutePath()};
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));
            logger.debug("input stream obtained");
            while ((s = stdInput.readLine()) != null) {
                logger.debug("String: " + s);

                if (s.startsWith("Submitted")) {
                    // Example output (in case of success): "Submitted batch job 16"
                    // TODO: handle errors
                    String pattern = "job\\s*\\d*";
                    Pattern r = Pattern.compile(pattern);
                    Matcher m = r.matcher(s);
                    if (m.find()) {
                        jobId = m.group(0).split("\\s")[1];
                        logger.debug("Submitted job " + jobId);
                    } else {
                        logger.debug("Submission went unsuccessfully");
                    }
                    output += s;
                }
            }
        } catch (IOException e) {
            logger.error("Could not open job file");
            e.printStackTrace(System.err);
            System.exit(10);
        } catch (BindingException e) {
            logger.error("Failed to use Bindings", e);
            e.printStackTrace(System.err);
            System.exit(11);
        }
        return jobId;
    }

    private static String getSlurmResourceRequirements(ResourceRequirement requirements) {
        String directive = "";
        if (requirements != null) {
            Long cpuMin = requirements.getCpuMin();
            Long memMin = requirements.getMemMinMB();
            if (cpuMin != null) {
//                Uncomment on production
//                directive += " --ntasks-per-node=" + Long.toString(cpuMin);
            }
            if (memMin != null) {
//                Uncomment on production
//                directive += " --mem=" + Long.toString(memMin);
            }
        }
        return directive;
    }

    public static void runCommand(String command){
        try {
            File commandFile = new File("command.sh");
            FileUtils.writeStringToFile(commandFile, command);
            String[] commands = {"bash", "command.sh"};
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);
            Process p = pb.start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * method for replacing a regexp pattern in a string
     */
    public static String regexpReplacer(String source, String pattern, String replacer) {
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(source);
        if (m.find()) {
            source = source.replace(m.group(0), replacer);
        }
        return source;
    }

    /**
     * replaces garbage symbols at the beginning of base64 decoded app
     *
     * @param jsonSource decoded app
     * @return input string with stripped garbage symbols
     */
    private static String stripLeadingJSONCharacters(String jsonSource) {
        String pattern = "^.+(?=\\{)";
        return regexpReplacer(jsonSource, pattern, "");
    }

    private static String preprocessCWLJob(String cwlJob){
        // invoked bunny-cli doesn't finish separate scatter jobs if they have inputs with scatter = true
        cwlJob = regexpReplacer(cwlJob, ",\\s+\"scatter\" : true\\n", "");
        cwlJob = stripLeadingJSONCharacters(cwlJob);
        return cwlJob;
    }
}
