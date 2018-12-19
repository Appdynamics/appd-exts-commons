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
java -cp the-monitoring-extension.jar com.appdynamics.extensions.crypto.Encryptor <encryptionKey> <clearTextPassword>
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

The SDK supports automatic upload of a pre-built dashboard to the controller. The dashboard template 
can be updated and more data can be added to increase the visibility of the metrics provided by the extension. 
For configuring auto upload of custom dashboards, the following configuration needs to be defined in the `config.yml`.

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


### Proxy

## Concurrent Fan Out



## Events Services

## Task Schedule

## Extension Logger

## Workbench



## 
 

# Getting Help

For help related AppDynamics Extensions, please open a ticket on [AppDynamics Support Portal](http://help.appdynamics.com).

