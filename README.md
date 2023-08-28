# Scylla - An extensible BPMN process simulator [![Build Status](https://github.com/bptlab/scylla/actions/workflows/CI.yml/badge.svg)]([https://travis-ci.org/bptlab/scylla](https://github.com/bptlab/scylla/actions/workflows/CI.yml/))

Scylla is an extensible BPMN process simulator which relies on the building blocks of discrete event simulation.  The process simulator offers an UI where BPMN models can be extended with simulation-specific information (e.g., inter-arrival time of instances, task duration). Resources are centrally specified in an extra file, such that the simulator also allows multi-process simulation (a simulation with several process models using the same resources).

Scylla is implemented in the Java programming language and can be extended. It offers well-defined entry points for extensions based on a plug-in structure.

## Quick Start
Download the [latest release](https://github.com/bptlab/scylla/releases/latest) zipfile and unpack it.
It is important that the `lib` folder stays in the same directory as the `scylla.jar` file.

You can then start Scylla by executing the jarfile, e.g., by calling `java -jar scylla.jar` in the unpacked folder.

Note that a valid Java installation is needed to run Scylla, we recommend at least Java 11.

## Usage

...



### CLI/Headless Mode
Scylla can also be run without UI. This is useful, e.g., when calling it from another program or when running the same Simulation multiple times. Configuration of the simulation then happens with the following program parameters (defined directly in the main class [Scylla.java](src/main/java/de/hpi/bpt/scylla/Scylla.java)):
- `--help` prints information about the command line usage of Scylla
- `--headless` activates the headless mode
- `--config=<path to file>` where `<path to file>` must lead to a global configuration file. This parameter must be present exactly once in headless mode
- `--bpmn=<path to file>` where `<path to file>` must lead to a bpmn process model. This parameter must be present at least once in headless mode, but might be multiple times
- `--sim=<path to file>` where `<path to file>` must lead to a simulation model configuration file. This parameter might be present multiple times, but there must be configurations for each simulated business process
- `--enable-bps-logging` enables logging of the executed process instances. This flag is optional, but recommended to activate
- `--enable-des-logging` enables logging of the descrete event simulation used. The flag is optional, recommended only to use for debugging
- `--output=<path to folder>` sets the output folder to `<path to folder>`. Optional, otherwise a default path will be used

### Loading Plugins
To load additional plugins, put their respective jarfiles into the `plugins` folder of your Scylla folder. You can check whether a plugin has been loaded by starting the Scylla GUI and asserting that it appears in the plugins list.


## Extensions on Scylla

Import the *scylla* Maven project in your Java IDE (e.g. Eclipse).
More information on the plug-in structure and how plug-ins can be developed are given in the [developer documentation](https://github.com/bptlab/scylla/wiki).

## Related Projects
- [INSM-TUM/Scylla-Container](https://github.com/INSM-TUM/Scylla-Container) provides a simple http API and dockerization of Scylla
- [SimuBridge](https://github.com/INSM-TUM/SimuBridge) is an application that bridges between process mining and business process simulation. It uses Scylla as Simulator and provides a generic metamodel and GUI for the construction of business process simulation models, usable in Scylla. 
