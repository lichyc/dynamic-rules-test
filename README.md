# Dynamic Rules Test

## Scenario1: Evaluation of new rules in context of existing rules
- A set of released rules, deployed in kJar.
- You like to elvaluate on how new rule(s) impacts the result on a certain fact.
- You like to do this in inside an application, so not in a development environment.
- The regular rules processing by other use cases should not get impacted. 

This example shows how to achieve this using the BRMS/Drools-Kie-API.

## Scenario2: Working with a rules repository outside a kJar
- A set of rules stored somewhere, like in database or any other repository.
- On how to get  an instance of `com.redhat.gss.brms.dynamic.rules.db.KieBaseEntity` is up to you. To simplify even any JPA annotations have been avoided. 
- We do this without using a `KieScanner`, to avoid a specific implementation.

## Project Structure
The project consists out of three modules:
- datamodel: As a fact is needed, this project contains a simple Pojo to be used as business model.
- static-rules: A simple kJar, providing some (static) rules.
- runtime: the project which contains the pieces from interest.

## What to pay attention to?
- `com.redhat.gss.brms.dynamic.rules.TestDriver` contains the impl.
- `com.redhat.gss.brms.dynamic.rules.TestIfWorksAsExpected` the releated test.
- check the log carefully. You'll see that it's always the same class which get used for the "static rules". So this implmentation avoids overhead by rebuilding them again during building the kBase including the dynamic injected rules.


## How to build
It's a Maven project, so arrest the usual suspects.
**Note:** The dependencies reference BRMS (`<version.org.kie>6.3.0.Final-redhat-5</version.org.kie>`). If you like to go for Drools (so community) please adjust property in pareńt pom.

