# Scylla - An extensible BPMN process simulator [![Build Status](https://travis-ci.org/bptlab/scylla.svg?branch=dev_ui)](https://travis-ci.org/bptlab/scylla)

Scylla is an extensible BPMN process simulator which relies on the building blocks of discrete event simulation.  The process simulator offers an UI where BPMN models can be extended with simulation-specific information (e.g., inter-arrival time of instances, task duration). Resources are centrally specified in an extra file, such that the simulator also allows multi-process simulation (a simulation with several process models using the same resources).

Scylla is implemented in the Java programming language and can be extended. It offers well-defined entry points for extensions based on a plug-in structure.

## Runnable JAR
The most recent JAR was built on June 21, 2018.

The runnable JAR can be found inside the [Scylla_zip](https://github.com/bptlab/scylla/files/2123548/Scylla.zip).
To run scylla, simply exctract all the files inside and run the jar file.
It is important that the Scylla_lib folder stays in the same directory as the .jar file.

Scylla needs a JRE 1.8 installation to run.

## Prerequisites

- Apache Maven 3+
- Java 8

## Extensions on Scylla

Import the *scylla* Maven project in your Java IDE (e.g. Eclipse).
More information on the plug-in structure and how plug-ins can be developed are given in the [developer documentation](https://github.com/bptlab/scylla/wiki).
