Java SDK for AppDynamics Monitoring Extensions
================================================

AppDynamics platform can be extended to support custom metrics by using [AppDynamics Monitoring Extensions](https://docs.appdynamics.com/display/LATEST/Extensions+and+Custom+Metrics). 
This Java SDK abstracts out common extension features and makes extension development very straightforward.

# Release Notes
All the changes to the SDK are tracked in the [CHANGELOG.md](CHANGELOG.md).

# Getting Started

## Extension Prerequisites
Before developing a new extension, please check if an AppDynamics supported extension already exists on the [exchange](https://www.appdynamics.com/community/exchange/).
AppDynamics monitoring extensions extend the AppDynamics Server Infrastructure Agent (SIM) or Standalone Machine Agent functionality to report additional metrics to the AppDynamics Controller. 
Please go over the [Extension Prerequisites Guide](https://community.appdynamics.com/t5/Knowledge-Base/Extensions-Prerequisites-Guide/ta-p/35213) for more details on the supported configurations. 

## Importing the Dependency

### Maven
```
<dependency>
  <groupId>com.appdynamics</groupId>
  <artifactId>appd-exts-commons</artifactId>
  <version>2.2.0</version>
</dependency>
```


## Usage

### monitor.xml
Every monitoring extension should ship with a `monitor.xml`. The Machine Agent loads the 
extension jar during its start up and schedules the extension to run based on `execution-frequency-in-seconds` defined in the `monitor.xml`. 
A `monitor.xml` should also point to a `config.yml`. Below is a sample `monitor.xml` 

```
<monitor>
    <name>ExtensionStarter</name>
    <type>managed</type>
    <description>Starter template to gather data from an http source</description>
    <monitor-configuration>
    </monitor-configuration>
    <monitor-run-task>
        <execution-style>periodic</execution-style>
        <execution-frequency-in-seconds>60</execution-frequency-in-seconds>
        <name>ExtensionStarter Monitor Run Task</name>
        <display-name>ExtensionStarter Task</display-name>
        <description>ExtensionStarter Task</description>
        <type>java</type>
        <execution-timeout-in-secs>120</execution-timeout-in-secs>
        <task-arguments>
            <argument name="config-file" is-required="true" default-value="monitors/ExtensionStarterMonitor/config.yml" />
        </task-arguments>
        <java-task>
            <classpath>extensions-starter.jar</classpath>
            <impl-class>com.appdynamics.extensions.extensionstarter.ExtStarterMonitor</impl-class>
        </java-task>
    </monitor-run-task>
</monitor>
```

### config.yml
Every monitoring extension should ship with a `config.yml`. This file should define the configuration of the extension artifact.
In addition to configuration, the SDK reads the `config.yml` to load and initialize all the different features mentioned [below](#Features).
  

### metrics.xml
This is an optional file to define the metrics and their properties for a given extension artifact. More about [Metric Types and Metric Transformers](#Metric and Metric Types) later.

A new extension should be developed by extending the ABaseMonitor class. For eg. 

```
public class SampleMonitor extends ABaseMonitor {
 
  protected abstract String getDefaultMetricPrefix(){
      return "Custom Metrics|Sample Monitor"
  }
 
  public abstract String getMonitorName(){
      return "Sample Monitor"
  }
 
  protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider){
      //...logic to add the core logic for the SampleMonitor
  }
 
  protected abstract List<Map<>> getServers(){
      //configuration for multiple servers to be processed in parallel
 
  }
 
}
```
For more details on how to use this Java SDK to build an AppDynamics extension, please check [Extension Starter project](https://github.com/Appdynamics/extension-starter)

# Features

## Encrypting Clear Text Passwords
The SDK provides a mechanism to encrypt clear text passwords that need to be defined in `config.yml`. 
To encrypt a password using an `encryptionKey`, run the following command using the extension's jar

```
java -cp <monitoring-extension.jar> com.appdynamics.extensions.crypto.Encryptor <encryptionKey> <clearTextPassword>
```  
The `encryptedPassword` produced using the above command along with the `encryptionKey` needs to be defined in the `config.yml`.

```
encryptedPassword: 
encryptionKey: 
```

The extension developer should use the following api to decrypt an encrypted password by adding the `encryptionKey` and `encryptedPassword` to the Map.

```
CryptoUtil.getPassword(Map)
```

For more details on password encrpytion, please check [How do I use Password Encryption with Extensions](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-use-Password-Encryption-with-Extensions/ta-p/29397)

## Metric Types and Metric Transformers

## Controller Info
The SDK automatically pulls the Controller information and builds a ControllerInfo object which is made available to the extension developer via
```
ABaseMonitor.getContextConfiguration().getContext().getControllerInfo()
```

The controller information can also be provided in the `config.yml`

```
controllerInfo:
    controllerHost: ""
    controllerPort: ""
    account: ""
    username: ""
    password: ""
    encryptedPassword: ""
    encryptionKey: ""
    controllerSslEnabled: ""
    enableOrchestration: ""
    uniqueHostId: ""
    accountAccessKey: ""
    machinePath: ""
    simEnabled: ""
    applicationName: ""
    tierName: ""
    nodeName: ""
```

The ControllerInfo object is built by loading the properties from the following sources in the following order
1. controller-info.xml
2. System Properties
3. config.yml

For more details on ControllerInfo, check [How do I auto upload dashboards from Extensions]().

## Auto Upload Custom Dashboards

The SDK supports automatic upload of a pre-built dashboard to the controller. The dashboard template should be provided to the extension through the `customDashboard` section
in the `config.yml`. If a dashboard is already created through this feature, any update 
can be updated and more data can be added to increase the visibility of the metrics provided by the extension. 
For enabling auto upload of custom dashboards, the following configuration needs to be defined in the `config.yml`.

```
customDashboard:
    enabled: true
    dashboardName: "Custom Dashboard"
    pathToSIMDashboard: "monitors/<ExtensionName>/simDashboard.json"
    pathToNormalDashboard: "monitors/<ExtensionName>/normalDashboard.json"
    periodToCheckDashboardPresenceInSeconds: 300

```

For more details on this feature, check [Uploading Dashboards automatically with AppDynamics Extensions]().

## Derived Metrics

Derived metrics are the metrics that can be created using existing metrics by applying a math formula.
Derived metrics can also be used to create rolled-up metrics at any level in the metric tree.
The SDK automatically starts emitting out the derived metrics defined in the `config.yml`. Following configuration needs to be defined in the `config.yml`

```
derivedMetrics:
   - derivedMetricPath: "{x}|Queue|{y}|Cache ratio"
     formula: “{x}|Queue|{y}|Cache hits / ({x}|Queue|{y}|Cache hits + {x}|Queue|{y}|Cache misses)”
   - derivedMetricPath: “RolledUp|Total ops"
     formula: “{x}|Total ops”
     aggregationType: “SUM"
     timeRollUpType: “SUM"
     clusterRollUpType: “COLLECTIVE”
  - derivedMetricPath: “{x}|Queue|Server Total ops”
    formula: “{x}|Queue|{y}|RAM ops + {x}|Queue|{y}|hdd ops"
``` 

For more information on derived metrics, check [Derived Metrics Calculation](https://community.appdynamics.com/t5/Knowledge-Base/What-are-the-Derived-Metrics-Calculator-and-Cluster-Metrics/ta-p/29403).

## Configuring HTTP Client

The SDK automatically configures a HTTP Client based on the `servers` section in the `config.yml`.

```
servers:
  - uri: ""
    username: ""
    password: ""
    encryptedPassword: ""

    # Uri is preferred instead of host-port-useSsl combo.
  - host: "" # Avoid this, use uri instead
    port: "" # Avoid this, use uri instead
    useSsl: false # Avoid this, use uri instead.
    username: ""
    password: ""
    encryptedPassword: ""
```    

This HTTP Client is available to be used by the extension developer via

```
ABaseMonitor.getContextConfiguration().getContext().getHttpClient()
```

Different settings related to the HTTP Client like SSL Certificates, Authentication, Proxy can be configured in the following manner in the `config.yml`.
    
```
connection:
  socketTimeout: 3000 # Read Timeout
  connectTimeout: 1000
  sslProtocols: ["TLSV1.2"] # Defaults to "default"
  sslCertCheckEnabled: true
  sslVerifyHostname: true

  # This need not be exposed to user. Should be used on a need basis
  sslCipherSuites: [] # Defaults to "default"

  sslTrustStorePath: "" # If not set, defaulted to machine-agent/conf/extensions-cacerts.jks. The prop "-Dappdynamics.extensions.truststore.path=/path/cacerts" takes precedence if set
  sslTrustStorePassword: ""
  sslTrustStoreEncryptedPassword: ""

  sslKeyStorePath: "" # If not set, defaulted to machine-agent/conf/extensions-clientcerts.jks. The prop "-Dappdynamics.extensions.keystore.path=/path/clientcerts" takes precedence if set
  sslKeyStorePassword: ""
  sslKeyStoreEncryptedPassword: ""

  enableCookies: false #Defaults to false
  enablePreemptiveAuth: true

proxy:
  uri: ""
  username: ""
  password: ""
  encryptedPassword: ""
```


## Concurrent Fan Out



## Events Services

One of the limitations of the AppDynamics Metric Browser is its inability to support 
anything other than numerical values.This can be overcome by using the Events Service Client in the SDK.
The Events Service client uses the AppDynamics Analytics Events API to define the structure of a 'custom event' 
and capture the event as it occurs in an application.

The SDK requires certain fields to be set in the config.yml to initiate a connection with the Analytics Events API. These fields are configured as follows: 

```
eventsServiceParameters:
  host:		#Events Service Host
  port: 	#Events Service Port
  globalAccountName:	#Found in Controller -> Settings -> License -> Account
  eventsApiKey:		#Generated from Controller -> Analytics -> Configuration -> API Keys
  useSsl: 	#true/false
```

The Events Service client can be used to execute standard CRUD operations. The extension developer can obtain the client as follows

```
ABaseMonitor.getContextConfiguration().getContext().getEventsServiceDataManager()
``` 

For more details about Events Service client, check [Events Service Client]().
   
## Task Schedule

## Extension Logger

## Workbench

Currently, the AppDynamics platform does not allow a user to delete a metric once it has been registered. 
The SDK provides the workbench feature in order to assist fine tuning of the extension before the metrics are registered in the controller. 

To start the Workbench mode, run the following command using the extension jar

```
java -jar <monitoring-extension.jar>
```

Workbench by default uses port 9090. Another port can be configured using
```
java -jar <monitoring-extension.jar> <host> <port>
```

Navigate to `http://localhost:9090/` to see the ![Workbench screen](workbench.png) 


For more details on workbench, check [How to use the Extensions WorkBench](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130).


# Getting Help

For any help related AppDynamics Extensions, please open a ticket on [AppDynamics Support Portal](http://help.appdynamics.com).

