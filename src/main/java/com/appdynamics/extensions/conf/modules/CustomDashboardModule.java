package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.controller.ControllerHttpRequestException;
import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.controller.apiservices.ControllerAPIServiceFactory;
import com.appdynamics.extensions.controller.apiservices.CustomDashboardAPIService;
import com.appdynamics.extensions.dashboard.CustomDashboardUploader;
import com.appdynamics.extensions.dashboard.CustomDashboardUtils;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static com.appdynamics.extensions.dashboard.DashboardConstants.CUSTOM_DASHBOARD;

public class CustomDashboardModule {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardModule.class);
    private boolean initialized;
    private String dashboardName;
    private boolean overwrite;
    private String dashboardTemplate;
    private CustomDashboardUploader dashboardUploader;
    private long timeDelayInMilliSeconds;
    private volatile AtomicLong lastRecordedTime;
    private Map<String, ?> proxyMap;

    public void initCustomDashboard(Map<String, ?> config, String metricPrefix, String monitorName, ControllerInfo controllerInfo) {
        initialized = false;
        lastRecordedTime = new AtomicLong();
        CustomDashboardAPIService customDashboardAPIService = ControllerAPIServiceFactory.getCustomDashboardAPIService();
        if(controllerInfo == null || customDashboardAPIService == null) {
            logger.debug("ControllerInfo/ControllerClient is null.....Not initializing CustomDashBoardModule");
            return;
        }
        Map customDashboardConfig = (Map) config.get(CUSTOM_DASHBOARD);
        if (CustomDashboardUtils.isCustomDashboardEnabled(customDashboardConfig)) {
            dashboardName = CustomDashboardUtils.getDashboardName(customDashboardConfig, monitorName);
            String dashboardTemplate = CustomDashboardUtils.getDashboardTemplate(metricPrefix, customDashboardConfig, controllerInfo, dashboardName);
            if (CustomDashboardUtils.isValidDashboardTemplate(dashboardTemplate)) {
                this.dashboardTemplate = dashboardTemplate;
                proxyMap = (Map<String, ?>)config.get("proxy");
                overwrite = CustomDashboardUtils.getOverwrite(customDashboardConfig);
                timeDelayInMilliSeconds = CustomDashboardUtils.getTimeDelay(customDashboardConfig) * 1000;
                dashboardUploader = new CustomDashboardUploader(customDashboardAPIService);
                initialized = true;
            }
        } else {
            logger.info("Custom Dashboard is not enabled in config.yml.");
        }
    }

    public void uploadDashboard() {
        if (initialized) {
            long currentTime = System.currentTimeMillis();
            if (hasTimeElapsed(currentTime, lastRecordedTime.get(), timeDelayInMilliSeconds)) {
                try {
                    logger.debug("Attempting to upload dashboard: {}", dashboardName);
                    dashboardUploader.checkAndUpload(dashboardName, dashboardTemplate, proxyMap, overwrite);
                    lastRecordedTime.set(currentTime);
                    long endTime = System.currentTimeMillis();
                    logger.debug("Time to complete customDashboardModule  :" + (endTime - currentTime) + " ms");
                } catch (ControllerHttpRequestException e) {
                    logger.error("Error while checking and uploading dashboard", e);
                } catch (Exception e) {
                    logger.error("Unable to establish connection, not uploading dashboard.", e);
                }
            }
        } else {
            logger.debug("Auto upload of custom dashboard is disabled.");
        }
    }

    private boolean hasTimeElapsed(long curr, long prev, long threshold) {
        return (curr - prev > threshold);
    }
}