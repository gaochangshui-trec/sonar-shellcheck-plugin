# Sonarqube plugin for Sellcheck

This plugin will add the shell language to your Sonarqube installation, with the followinf benefits :

- Shell scripts (bash and others) are added to your project and counted as valid source files
- [The shellcheck tool](https://github.com/koalaman/shellcheck) will be run on your shell files, giving you meaningfull hindsight about your scripts quality

## Installation

Just copy the jar file to your `$SONAR_HOME/extensions/plugins`

## Configuration

- *Shell suffixes* : The suffixes (separated by commas) of the files that are to be considered shell scripts. Default value : `.sh`

## Behavior

This plugin will download a specific version of shellcheck, extract it and run it on the machine that runs the sonarqube scanner.

If this is unaceptable for any reason, you can run shellcheck yourself before starting sonar scanner. To do so, you must run shellcheck
from your project root directory and save a json-formatted repport to a file, as so :

```bash
shellcheck -f json [path/to/file1] [path/to/file2] ... [path/to/fileN] > shellcheck-report.json
```

Then, you must include the report in your `sonar-project.properties` file:

```
sonar.shellcheck.reportPath=shellcheck-repport.json
```

## Credits

All the magic is done by [the shellcheck tool](https://github.com/koalaman/shellcheck). This plugin only imports shellcheck repports into sonarqube.

Furthermore, all issues documentations presented in the sonarqube interface has been shamelessly "borrowed" from [shellcheck's wiki](https://github.com/koalaman/shellcheck/wiki).

## Caveats

Sonarqube will ignore any file that doesn't have an extension. So your shell scripts must have an extension to be correctly analysed.

