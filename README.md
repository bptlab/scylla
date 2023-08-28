# Scylla - An extensible BPMN process simulator [![Build Status](https://github.com/bptlab/scylla/actions/workflows/CI.yml/badge.svg)]([https://travis-ci.org/bptlab/scylla](https://github.com/bptlab/scylla/actions/workflows/CI.yml/))

Scylla is an extensible business process simulator.
Simulations are configured with a file for general information, such as resources, multiple BPMN model files, which allow multi-process simulation with shared resources, and simulation configuration files, which extend the BPMN files with simulation-specific information, e.g., inter-arrival time of instances, task duration.
Scylla simulates these inputs using discrete event simulation (DES) and produces information on the simulated process instances, such as an XES event log.

The engine can be controlled via a GUI, which also provides an interface for creating the configuration input files, or by command line.
Scylla stands out by offering well-defined entry points for extensions based on a plug-in structure, which allows to make it fulfil specific simulation requirements.

## Quick Start
Download the [latest release](https://github.com/bptlab/scylla/releases/latest) zipfile and unpack it.
It is important that the `lib` folder stays in the same directory as the `scylla.jar` file.

You can then start Scylla by executing the jarfile, e.g., by calling `java -jar scylla.jar` in the unpacked folder.

Note that a valid Java installation is needed to run Scylla, we recommend at least Java 11.


## Usage
Scylla has two main ways of operation, with a graphical user interface and via a command line interface.

### UI
When starting Scylla without any additional parameters, the Scylla GUI opens.
![grafik](https://github.com/bptlab/scylla/assets/28008098/c932693d-2324-42ab-9c7d-a50f32b0d823)

The UI is structured as follows:
- The left side shows the simulation inputs. There, configuration and process model files can be loaded and removed. Further new configuration files can be created or existing ones edited. This then opens the respective interfaces as new tabs.
- The right side shows the loaded plugins. It allows to select which plugins are active, and even to (de-)activate single classes.
- The bottom shows the simulation console output and provides the controls to start the simulation and inspect the last simulation outputs.

Note that to run a simulation, at the following inputs are needed: One global configuration file, at least one bpmn file, simulation configuration files for all processes of the bpmn files. For details on the simulation inputs, please refer to the [wiki](../../wiki).

### CLI/Headless Mode
Scylla can also be run without GUI. This is useful, e.g., when calling it from another program or when running the same Simulation multiple times. Configuration of the simulation then happens with the following program parameters (defined directly in the main class [Scylla.java](src/main/java/de/hpi/bpt/scylla/Scylla.java)):
- `--help` prints information about the command line usage of Scylla
- `--headless` activates the headless mode
- `--config=<path to file>` where `<path to file>` must lead to a global configuration file. This parameter must be present exactly once in headless mode
- `--bpmn=<path to file>` where `<path to file>` must lead to a bpmn process model. This parameter must be present at least once in headless mode, but might be multiple times
- `--sim=<path to file>` where `<path to file>` must lead to a simulation model configuration file. This parameter might be present multiple times, but there must be configurations for each simulated business process
- `--enable-bps-logging` enables logging of the executed process instances. This flag is optional, but recommended to activate
- `--enable-des-logging` enables logging of the descrete event simulation used. The flag is optional, recommended only to use for debugging
- `--output=<path to folder>` sets the output folder to `<path to folder>`. Optional, otherwise a default path will be used

### Calling from Code
Scylla can also be directly called from another Java application. For this, the application has to import Scylla as Maven dependency.
Then, either call the main class `Scylla`'s `main` method, or manually create a new `SimulationManager` and call `run.`


## Plugins
One distinctive feature of Scylla is its plugin system, which allows to easily add functionality for specialized or refined simulation behavior.

### Loading Plugins
To load additional plugins, put their respective jarfiles into the `plugins` subfolder of your Scylla folder. You can check whether a plugin has been loaded by starting the Scylla GUI and asserting that it appears in the plugins list.

### Plugin Development
To create a new plugin, create a new Maven project for your plugin. Add Scylla as a Maven dependency to that project. Potentially, you first need to install Scylla via Maven.
Then, create your plugin classes. These are all classes that extend one of the entrypoints (see wiki), which in turn implement the `IPluggable` interface. Note that all classes belonging to the same plugin should return the same value in their implementation of `getName`.
To load your plugin, run Maven package to generate a jarfile and put it into the `plugins` subfolder of your scylla folder.
More information on the plug-in structure and how plug-ins can be developed are given in the [wiki](../../wiki/Plugin-Concept).


## Related Projects
- [Scylla-Container](https://github.com/INSM-TUM/Scylla-Container) provides a simple http API and dockerization of Scylla
- [SimuBridge](https://github.com/INSM-TUM/SimuBridge) is an application that bridges between process mining and business process simulation. It uses Scylla as Simulator and provides a generic metamodel and GUI for the construction of business process simulation models, usable in Scylla. 

## Related Publications
- [Pufahl, L., Wong, T.Y., Weske, M. (2018). **Design of an Extensible BPMN Process Simulator**. In: Teniente, E., Weidlich, M. (eds) Business Process Management Workshops. BPM 2017. Lecture Notes in Business Information Processing, vol 308. Springer, Cham. https://doi.org/10.1007/978-3-319-74030-0_62](https://doi.org/10.1007/978-3-319-74030-0_62)
- [Pufahl, L., & Weske, M. (2017). **Extensible BPMN Process Simulator**. In: Proceedings of the
BPM Demo Track and BPM Dissertation Award](https://ceur-ws.org/Vol-1920/BPM_2017_paper_198.pdf)

## About
Scylla was initially developed in 2017 as part of a Master's Thesis by Tsun Yin Wong at the chair for Business Process Technologies (BPT) at the Hasso Plattner Institute Potsdam. Further development was then done at the BPT chair. Currently, the project is being developed further and maintained in cooperation by the HPI BPT chair and the chair for Information Systems at the Technical University of Munich.
