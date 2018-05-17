## Overview
This folder contains the source code for an implementation of a genetic algorithm with a GUI. Specifically, the algorithm performs a non-deterministic search for a prescribed fitness of a protein structure.

## To Compile
From this directory: `javac *.java`

## To Run 
From the directory containing the compiled code: `java Searcher`

## Instructions
Provide the GUI with an amino acid sequence abstracted to a sequence of 'h's and 'p's for hydrophobic and hydrophilic, respectively and a target fitness. The target fitness represents the number of adjacencies between noncovalent hydrophobic amino acids. 

## Sample Inputs
Here are some amino acid sequences and target fitnesses that should terminate in a reasonable amount of time. 
1. 
```
Acid Sequence = pphpphhpppphhpppphhpppphh
Target Fitness = -7
```
1. 
```
Acid Sequence = hphpphhphpphphhpphph
Target Fitness = -7
```
1.
```
Acid Sequence = hhhpphphphpphphphpph
Target Fitness = -8

```