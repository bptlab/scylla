# Scylla - An extensible BPMN process simulator [![Build Status](https://github.com/bptlab/scylla/actions/workflows/CI.yml/badge.svg)]([https://travis-ci.org/bptlab/scylla](https://github.com/bptlab/scylla/actions/workflows/CI.yml/))

Scylla is an extensible BPMN process simulator which relies on the building blocks of discrete event simulation.  The process simulator offers an UI where BPMN models can be extended with simulation-specific information (e.g., inter-arrival time of instances, task duration). Resources are centrally specified in an extra file, such that the simulator also allows multi-process simulation (a simulation with several process models using the same resources).

Scylla is implemented in the Java programming language and can be extended. It offers well-defined entry points for extensions based on a plug-in structure.

## Quick Start
Download the [latest release](https://github.com/bptlab/scylla/releases/latest) zipfile and unpack it.
It is important that the `lib` folder stays in the same directory as the `scylla.jar` file.

You can then start Scylla by executing the jarfile, e.g., by calling `java -jar scylla.jar` in the unpacked folder.

Note that a valid Java installation is needed to run Scylla, we recommend at least Java 11.

## Extensions on Scylla

Import the *scylla* Maven project in your Java IDE (e.g. Eclipse).
More information on the plug-in structure and how plug-ins can be developed are given in the [developer documentation](https://github.com/bptlab/scylla/wiki).
