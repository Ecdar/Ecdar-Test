# Ecdar-test
The test framework for automatic test case generation for Ecdar engines.

Requires a configuration file `configuration.json` with information for each engine such as: 
```json
[
   {
     "name": "Reveaal",
     "executablePath": "path/to/Reveaal.exe",
     "parameterExpression" : "-p={ip}:{port}",
     "ip": "127.0.0.1",
     "port" : 7000,
     "processes" : 8,
     "enabled" : true
   }
 ]
```
