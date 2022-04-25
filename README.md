# Ecdar-test
The test framework for automatic test case generation for Ecdar engines.

Requires a configuration file `configuration.json` with information for each engine executable: 
```json
[
    {
        "name": "Reveaal",
        "version": "Main",
        "executablePath": "path/to/Reveaal.exe",
        "parameterExpression" : "-p={ip}:{port}",
        "ip": "127.0.0.1",
        "port" : 7000,
        "processes" : 8,
        "enabled" : true,
        "verbose": true
    },
    {
        "name": "J-Ecdar",
        "version": "UCDD",
        "executablePath": "path/to/j-Ecdar.bat",
        "parameterExpression" : "-p={ip}:{port}",
        "ip": "127.0.0.1",
        "port" : 8000,
        "processes" : 8,
        "enabled" : false
    },
    {
        "name": "External",
        "version": "v1.0",
        "ip": "127.0.0.1",
        "port" : 9000,
        "processes" : 1,
        "enabled" : false
    }
 ]
```
If an `executablePath` or `parameterExpression` is omitted, the engine is expected to be hosted externally. An example of this is the `External` engine in the above configuration. Engines can optionally be marked `verbose` to print failed queries while the tests are run from [Run Tests for Engine](#run-tests-for-engine)

##Run Tests for Engine
Run all tests on enabled engines from `main()` in [Main.kt](src/main/kotlin/Main.kt). Test results are stored in `results/ENGINE_NAME/ENGINE_VERSION/RUN_NUMBER`. Run numbering is used so new results on same engine and version do not override previous results.
```
Found 5730 tests

Running 5730 tests on engine "Reveaal"
| 100% [5730/5730]
4372/5730 tests succeeded (76%) in 78 seconds
```
##Print Results From Latest Run
Pretty print results of the latest run from `main()` in [Results.kt](src/main/kotlin/Results.kt).
This further provides the arguments for the engine to rerun any failed query.
```
Printing results for file "results\Reveaal\Main\0.json"
4372/5730 tests succeeded
1358 tests failed
0 failed due to exceptions

Expected SATISFIED, but was UNSATISFIED in consistency: (((A || G) // G) && ((A || Q) // Q) && (Q // (A || Q))); refinement: A <= (((A || G) // G) && ((A || Q) // Q) && (Q // (A || Q)))
Rerun with arguments: "consistency: (((A || G) // G) && ((A || Q) // Q) && (Q // (A || Q))); refinement: A <= (((A || G) // G) && ((A || Q) // Q) && (Q // (A || Q)))" -i samples/json/AG 

Expected SATISFIED, but was UNSATISFIED in consistency: ((G // (A || G)) && (G // (A || Q))); refinement: A <= ((G // (A || G)) && (G // (A || Q)))
Rerun with arguments: "consistency: ((G // (A || G)) && (G // (A || Q))); refinement: A <= ((G // (A || G)) && (G // (A || Q)))" -i samples/json/AG 
...
```
##Generate Interactive Plots Comparing Engines and Versions 
Line and density plots can be generated from `main()` in [Plotting.kt](src/main/kotlin/Plotting.kt). 
Plots can be generated comparing all engines or all versions of a specific engine. 
Plots can optionally have log scale axes, as the ones seen below.
###Line Plots
![Line Plot](https://i.imgur.com/dsKycFL.png "Line Plot")
###Density Plots
![Density Plot](https://i.imgur.com/PAl3BdX.png "Density Plot")
