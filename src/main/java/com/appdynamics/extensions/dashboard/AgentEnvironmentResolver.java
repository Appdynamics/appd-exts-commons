package com.appdynamics.extensions.dashboard;

import com.appdynamics.TaskInputArgs;
import com.appdynamics.extensions.NumberUtils;
import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.StringUtils;
import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.singularity.ee.agent.configuration.identity.AgentResolverUtil;
import com.singularity.ee.agent.resolver.AgentAccountInfo;
import com.singularity.ee.agent.resolver.AgentRegistrationInfo;
import com.singularity.ee.agent.resolver.AgentResolver;
import com.singularity.ee.agent.resolver.ControllerInfo;
import com.singularity.ee.agent.systemagent.Agent;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.util.log4j.Log4JLogger;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Created by abey.tom on 4/10/15.
 */
public class AgentEnvironmentResolver {
    public static final org.slf4j.Logger logger = LoggerFactory.getLogger(AgentEnvironmentResolver.class);
    private AgentResolver agentResolver;
    private boolean resolved;
    private Map dashboardConfig;
    private boolean resolveTier;
    private String applicationName;
    private String tierName;
    private String userName;
    private String password;

    AgentEnvironmentResolver() {

    }

    public AgentEnvironmentResolver(Map dashboardConfig, boolean resolveTier) {
        this.dashboardConfig = dashboardConfig;
        this.resolveTier = resolveTier;
        try {
            AgentResolverUtil.AgentResolverState resolver = Agent.createDefaultAgentResolver("Machine Agent",
                    new Log4JLogger(Logger.getLogger("CustomDashboardTaskResolver")));
            AgentResolver agentResolver = resolver.getAgentResolver();
            agentResolver.execute(5000, 1000);
            validateRequiredProperties(agentResolver);
            lookupCredentials(dashboardConfig);
            this.agentResolver = agentResolver;
        } catch (InterruptedException e) {
            logger.error("Error while resolving the Agent Properties", e);
        } catch (IllegalArgumentException e) {
            logger.error("Agent Resolution is not valid", e);
        } catch (Throwable e) {
            //Might throw no class def found error in unsupported Machine Agents.
            logger.error("Unknown exception while resolving the agent", e);
        }
    }

    protected void lookupCredentials(Map dashboardConfig) {
        String username = (String) dashboardConfig.get(TaskInputArgs.USER);
        if (StringUtils.hasText(username)) {
            this.userName = username;
        } else {
            logger.error("The username was not read from the config.yml");
            resolved = false;
        }
        String password = Http4ClientBuilder.getPassword(dashboardConfig, dashboardConfig);
        if (StringUtils.hasText(password)) {
            this.password = password;
        } else {
            logger.error("The password was not read from the config.yml");
            resolved = false;
        }
    }

    protected void validateRequiredProperties(AgentResolver agentResolver) {
        ControllerInfo controllerInfo = agentResolver.getControllerHostInfo();
        assertNotNull(controllerInfo, "Controller Info is NULL");
        assertNotNull(controllerInfo.getControllerHostName(), "");

        AgentAccountInfo agentAccountInfo = agentResolver.getAgentAccountInfo();
        assertNotNull(agentAccountInfo, "AgentAccountInfo is NULL");

        AgentRegistrationInfo agentRegistrationInfo = agentResolver.getAgentRegistrationInfo();
        assertNotNull(agentRegistrationInfo, "AgentRegistrationInfo is NULL");
        if (StringUtils.hasText(agentRegistrationInfo.getApplicationName())) {
            this.applicationName = agentRegistrationInfo.getApplicationName();
        }
        if (StringUtils.hasText(agentRegistrationInfo.getApplicationComponentName())) {
            this.tierName = agentRegistrationInfo.getApplicationComponentName();
        }
        logger.debug("App [{}] and Tier [{}] after resolving from registration ", applicationName, tierName);

        updateDashboardConfig(dashboardConfig);
        logger.debug("App [{}] and Tier [{}] after resolving from dashboard config", applicationName, tierName);

        //resolveFromLogsAndRestAPI();

        if (StringUtils.hasText(applicationName)) {
            if (resolveTier) {
                if (StringUtils.hasText(tierName)) {
                    resolved = true;
                }
            } else {
                resolved = true;
            }
        }
        logger.info("Final App [{}], Tier [{}], resolved [{}] after resolving from all sources"
                , applicationName, tierName, resolved);
        if (!resolved) {
            logger.error("Cannot resolve the Application Name & Tier Name. Please add them to the config.yml");
        }
    }

    protected void updateDashboardConfig(Map dashboardConfig) {
        if (dashboardConfig != null) {
            this.dashboardConfig = dashboardConfig;
            String applicationName = (String) dashboardConfig.get("applicationName");
            if (StringUtils.hasText(applicationName)) {
                this.applicationName = applicationName;
            }
            String tierName = (String) dashboardConfig.get("tierName");
            if (StringUtils.hasText(tierName)) {
                this.tierName = tierName;
            }
        }
    }

    private void resolveFromLogsAndRestAPI() {
        if (applicationName == null && (resolveTier && tierName == null)) {
            String machineId = getMachineIdFromLogs();
            if (NumberUtils.isNumber(machineId)) {

                logger.debug("App [{}] and Tier [{}] after resolving from all rest", applicationName, tierName);
            } else {
                logger.info("Couldn't get a machine ID from the given String {}", machineId);
            }
        }
    }

    private String getMachineIdFromLogs() {
        File dir = PathResolver.resolveDirectory(AManagedMonitor.class);
        File logDir;
        if (dir != null && (logDir = new File(dir, "logs")).exists()) {
            File[] logs = logDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.contains("machine-agent.log");
                }
            });
            if (logs != null && logs.length > 0) {
                List<File> files = Arrays.asList(logs);
                Collections.sort(files, new Comparator<File>() {
                    public int compare(File o1, File o2) {
                        return new Long(o2.lastModified()).compareTo(o1.lastModified());
                    }
                });
                return readMachineId(files);
            } else {
                logger.error("Cannot find the machine-agent.log in the directory {}", logDir.getAbsolutePath());
            }
        } else {
            logger.error("Cannot find the directory [logs] in the Machine Agent base directory "
                    + dir != null ? dir.getAbsolutePath() : null);
        }
        return null;
    }

    private String readMachineId(List<File> files) {
        String searchStr = "Registered Machine Agent with machine ID";
        String machineId = null;
        for (File file : files) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String temp;
                while ((temp = reader.readLine()) != null) {
                    int startIndex;
                    if ((startIndex = temp.indexOf(searchStr)) != -1) {
                        machineId = temp.substring(startIndex + searchStr.length() + 2, temp.length() - 1);
                    }
                }
            } catch (IOException e) {
                logger.error("Error while reading the log file " + file.getAbsolutePath(), e);
            }
            if (machineId != null) {
                return machineId;
            }
        }
        return null;
    }

    private void assertNotNull(Object o, String message) {
        if (o == null) {
            throw new IllegalArgumentException(message);
        }

    }


    public String getTierName() {
        return tierName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public boolean isResolved() {
        return resolved;
    }

    public String getControllerHostName() {
        if (resolved) {
            return agentResolver.getControllerHostInfo().getControllerHostName();
        }
        return null;
    }

    public int getControllerPort() {
        if (resolved) {
            return agentResolver.getControllerHostInfo().getControllerPort();
        }
        return isControllerUseSSL() ? 443 : 80;
    }

    public boolean isControllerUseSSL() {
        if (resolved) {
            return agentResolver.getControllerHostInfo().isSslEnabled();
        }
        return false;
    }

    public String getAccountName() {
        if (resolved) {
            return agentResolver.getAgentAccountInfo().getAccountName();
        }
        return null;
    }

    public String getUsername() {
        if (resolved) {
            return userName;
        }
        return null;
    }

    public String getPassword() {
        if (resolved) {
            return password;
        }
        return null;
    }

}
