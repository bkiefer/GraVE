# Graphical VOnDA Editor

This is a stripped down and massively reworked fork of the [Visual Scene
Maker](https://github.com/SceneMaker/VisualSceneMaker) github repository. It
only keeps the project's graphical editor with modified functionality to use it
together with an automaton compiler that produces
[VOnDA](https://github.com/bkiefer/vonda) source code. The goal is to provide
simple graphical modelling facilities for dialogue management similar to Visual Scene Maker and
[DialogOS](https://github.com/dialogos-project/dialogos) based on hierarchical state machines, which can be blended seamlessly with VOnDA's rule based approch.

The editor and compiler are still work in progress. The compiler is a result of
the WS17/18 software project at the Universität des Saarlandes, implemented by
(in alphabetical order) Simon Ahrendt, Max Depenbrock and Jana Jungbluth, which was formerly in the [scenemaker2vonda](https://github.com/bkiefer/scenemaker2vonda) project. Now, the compiler is a submodule of this repository, and uses the VOnDA compiler to create Java code in a two-step process.

The compiler is mostly functional, but integration with the rest of the VOnDA runtime system still needs to be improved, and best practices have to be developed.

The project includes two working projects which are used for testing the compile and runtime functionality, `introduction` and `interrupt`. These can be used as starting points to develop new functionality.
<!-- Furthermore, there is a project in active development [gcs-dialog](https://github.com/bkiefer/gcs_dialog) using this module. -->
