package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.api.ApiException;
import com.appdynamics.extensions.api.ControllerApiService;
import com.appdynamics.extensions.conf.controller.ControllerInfo;
import com.appdynamics.extensions.conf.controller.ControllerInfoValidator;
import com.appdynamics.extensions.dashboard.CustomDashboardGenerator;
import com.appdynamics.extensions.dashboard.CustomDashboardUploader;
import com.appdynamics.extensions.dashboard.CustomDashboardUtils;
import com.appdynamics.extensions.dashboard.DashboardConstants;
import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomDashboardModule {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardModule.class);
    private ControllerInfo controllerInfo;
    private String dashboardName;
    private boolean overwrite;
    private String dashboardTemplate;
    private Map config;

    // todo use atomic boolean and volatile
    private AtomicBoolean dashboardUploaded = new AtomicBoolean();

    public void initCustomDashboard(Map<String, ?> config, String metricPrefix, String monitorName,
                                    ControllerInfo controllerInfo) {
        logger.debug("Custom Dashboard Module controllerInfo : {}", controllerInfo);
        this.controllerInfo = controllerInfo;
        this.config = config;
        Map customDashboardConfig = (Map) config.get("customDashboard");
        if ((customDashboardConfig != null && !customDashboardConfig.isEmpty())) {
            if ((Boolean) customDashboardConfig.get(DashboardConstants.ENABLED)) {
                dashboardName = CustomDashboardUtils.getDashboardName(customDashboardConfig, monitorName);
                overwrite = CustomDashboardUtils.getOverwrite(customDashboardConfig);
                dashboardTemplate = getDashboardTemplate(metricPrefix, customDashboardConfig);
            } else {
                logger.info("customDashboard is not enabled in config.yml, not uploading dashboard.");
            }
        } else {
            logger.info("No customDashboard Info in config.yml, not uploading dashboard.");
        }
    }

    private String getDashboardTemplate(String metricPrefix, Map customDashboardConfig) {
        ControllerInfoValidator validator = new ControllerInfoValidator();
        if (validator.isValidatedAndResolved(controllerInfo)) {
            String dashboardMetricPrefix = CustomDashboardUtils.buildMetricPrefixForDashboard(metricPrefix);
            CustomDashboardGenerator dashboardGenerator = new CustomDashboardGenerator(customDashboardConfig, controllerInfo,
                    dashboardMetricPrefix, dashboardName);
            return dashboardGenerator.getDashboardTemplate();
        }
        return null;
    }

    private Boolean isValidDashboardTemplate(String dashboardTemplate) {
        if (!Strings.isNullOrEmpty(dashboardTemplate)) {
            logger.debug("Dashboard values resolved. Ready for uploader");
            return true;
        }
        logger.debug("Empty dashboard file, skipping dashboard upload");
        return false;
    }

    public void uploadDashboard() {
        if (!dashboardUploaded.get() && isValidDashboardTemplate(dashboardTemplate)) {
            long startTime = System.currentTimeMillis();
            ControllerApiService apiService = new ControllerApiService(controllerInfo);
            Map httpProperties = CustomDashboardUtils.getHttpProperties(controllerInfo, config);
            try (CloseableHttpClient client = Http4ClientBuilder.getBuilder(httpProperties).build()) {
                CustomDashboardUploader dashboardUploader = new CustomDashboardUploader();
                dashboardUploader.gatherDashboardDataToUpload(apiService, client, dashboardName, dashboardTemplate,
                        httpProperties, overwrite);
                dashboardUploaded.set(true);
                long endTime = System.currentTimeMillis();
                logger.debug("Time to complete customDashboardModule in :" + (endTime - startTime) + " ms");
            } catch (ApiException e) {
                logger.error("Unable to establish connection, not uploading dashboard.", e);
            } catch (IOException e) {
                logger.error("Error while uploading dashboard", e);
            }
        }
    }
}

