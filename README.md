Avail Artifact
--------------------------------------------------------------------------------
[![Maven Central](https://img.shields.io/badge/maven--central-v2.0.0.alpha19-0f824e)](https://search.maven.org/artifact/org.availlang/avail-artifact)

***TODO Update this README; IT IS OUT OF DATE AND MUST BE UPDATED BEFORE 
TOOL RELEASE***

A library used for packaging Avail artifacts for distribution as libraries or
use as full running applications.

## Avail Artifact Types
At the time of writing this there are two types of Avail Artifacts:
 1. `LIBRARY` - Contains only Avail Roots that can be shared as libraries in 
 2. `APPLICATION` - Contains all the components needed to run an Avail application.

## Avail Artifact Manifest
The Avail Artifact Manifest file describes the contents of an Avail Artifact.
This file is a JSON file: 
    `avail-artifact-contents/avail-artifact-manifest.json`

## Avail Application Configuration
The Avail Application Configuration file describes how an Avail packaged 
application should be composed and loaded into an Avail runtime. It is only
present when the Avail Artifact Type is `APPLICATION`.
This file is a JSON file: 
    `avail-artifact-contents/avail-application-configuration.json`
