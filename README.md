# civassetextractor
Extract assets from Civilization I to *.png files.
You need Civilization I installed or downloaded. 

1. [Installation](#installation)
2. [Example](#example)

## Installation

**Java 1.8.0 or higher is required**,
Maven 3 or higher is recommended to use as a build tool

download/clone the git and use
```cmd
mvn install 
```
Usage
```cmd
java -jar civ-1.0-SNAPSHOT.jar <civilisationpath> {-out <outpath>}
```

## Example

The following is a simple example of usage.

```cmd
java -jar civ-1.0-SNAPSHOT.jar c:\civ
```
c:\civ is a path where your Civilization I *.pic files are. 
This will generate assets in your work directory.

```cmd
java -jar civ-1.0-SNAPSHOT.jar c:\civ -out c:\civassets
```
This will generate assets in your c:\civassets directory.
![thumbnails](https://raw.githubusercontent.com/sda553/civassetextractor/master/thumbnails.png)