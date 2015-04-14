package com.appdynamics.extensions.dashboard;

import com.singularity.ee.agent.resolver.AgentAccountInfo;
import com.singularity.ee.agent.resolver.AgentRegistrationInfo;
import com.singularity.ee.agent.resolver.AgentResolver;
import com.singularity.ee.agent.resolver.ControllerInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class AgentEnvironmentResolverTest {

    @Before
    public void before() {
    }

    @Test
    public void validateRequiredProperties() throws Exception {
        AgentEnvironmentResolver originalEnvResolver = new AgentEnvironmentResolver();
        AgentResolver agentResolver = Mockito.mock(AgentResolver.class);
        AgentEnvironmentResolver envResolver = setupMock(originalEnvResolver, agentResolver);

        envResolver.validateRequiredProperties(agentResolver);

        Assert.assertTrue(envResolver.isResolved());

        Assert.assertEquals("192.168.1.134", envResolver.getControllerHostName());
        Assert.assertEquals(8090, envResolver.getControllerPort());
        Assert.assertEquals(true, envResolver.isControllerUseSSL());

        Assert.assertEquals("account1",envResolver.getAccountName());
        Assert.assertEquals("key1",envResolver.getAccesskey());

        Assert.assertEquals("app1",envResolver.getApplicationName());
        Assert.assertEquals("tier1",envResolver.getTierName());
    }

    private AgentEnvironmentResolver setupMock(AgentEnvironmentResolver originalEnvResolver, AgentResolver agentResolver) {

        MockHelper.set("agentResolver", originalEnvResolver, agentResolver);
        AgentEnvironmentResolver envResolver = Mockito.spy(originalEnvResolver);


        ControllerInfo info = new ControllerInfo("192.168.1.134",8090,true);
        Mockito.doReturn(info).when(agentResolver).getControllerHostInfo();

        AgentAccountInfo accountInfo = new AgentAccountInfo("account1","key1");
        Mockito.doReturn(accountInfo).when(agentResolver).getAgentAccountInfo();

        AgentRegistrationInfo registrationInfo = new AgentRegistrationInfo("app1","tier1","node1",false);
        Mockito.doReturn(registrationInfo).when(agentResolver).getAgentRegistrationInfo();
        return envResolver;
    }

    @Test
    public void validateRequiredPropertiesWithConfigOverride() throws Exception {
        AgentEnvironmentResolver originalEnvResolver = new AgentEnvironmentResolver();
        Map<String, String> config = new HashMap<String, String>();
        config.put("applicationName","app2");
        config.put("tierName","tier2");
        MockHelper.set("dashboardConfig",originalEnvResolver,config);

        AgentResolver agentResolver = Mockito.mock(AgentResolver.class);
        AgentEnvironmentResolver envResolver = setupMock(originalEnvResolver,agentResolver);

        envResolver.validateRequiredProperties(agentResolver);

        Assert.assertTrue(envResolver.isResolved());

        Assert.assertEquals("app2",envResolver.getApplicationName());
        Assert.assertEquals("tier2",envResolver.getTierName());
    }

    @Test
    public void validateRequiredPropertiesWithConfigOverrideNull() throws Exception {
        AgentEnvironmentResolver originalEnvResolver = new AgentEnvironmentResolver();
        Map<String, String> config = new HashMap<String, String>();
        config.put("applicationName","app2");
        config.put("tierName","tier2");
        MockHelper.set("dashboardConfig",originalEnvResolver,config);

        AgentResolver agentResolver = Mockito.mock(AgentResolver.class);
        AgentEnvironmentResolver envResolver = setupMock(originalEnvResolver,agentResolver);

        AgentRegistrationInfo registrationInfo = new AgentRegistrationInfo(null,null,null,false);
        Mockito.doReturn(registrationInfo).when(agentResolver).getAgentRegistrationInfo();

        envResolver.validateRequiredProperties(agentResolver);

        Assert.assertTrue(envResolver.isResolved());

        Assert.assertEquals("app2",envResolver.getApplicationName());
        Assert.assertEquals("tier2",envResolver.getTierName());
    }
}