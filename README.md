# Scylla - An extensible BPMN process simulator

Scylla is an extensible BPMN process simulator which relies on the building blocks of discrete event simulation.  The process simulator offers an UI where BPMN models can be extended with simulation-specific information (e.g., inter-arrival time of instances, task duration). Resources are centrally specified in an extra file, such that the simulator also allows multi-process simulation (a simulation with several process models using the same resources).

Scylla is implemented in the Java programming language and can be extended. It offers well-defined entry points for extensions based on a plug-in structure.

## Runnable JAR
The most recent JAR was built on January 18, 2017.

Runnable JAR: [scylla_jar](https://github.com/bptlab/scylla/files/1663367/Scylla.zip)

Scylla needs a JRE 1.8 installation to run.

## Prerequisites

- Apache Maven 3+
- Java 8

## Extensions on Scylla

Import the *scylla* Maven project in your Java IDE (e.g. Eclipse).
More information on the plug-in structure and how plug-ins can be developed are given in the [developer documentation](https://github.com/bptlab/scylla/wiki).
