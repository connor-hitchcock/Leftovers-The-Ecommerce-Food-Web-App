const sonarqubeScanner =  require('sonarqube-scanner');
sonarqubeScanner(
  {
    serverUrl:  'https://csse-s302g5.canterbury.ac.nz/sonarqube/',
    token: "235ea0a8a4bb8e5891a61d73452ec9da1eea49ef",
    options : {
      'sonar.projectKey': 'team-500-client',
      'sonar.projectName': 'Team 500 - Client',
      "sonar.sourceEncoding": "UTF-8",
      'sonar.sources': 'src',
      'sonar.tests': 'tests',
      'sonar.inclusions': '**',
      'sonar.test.inclusions': 'tests/unit/**/*.spec.ts',
      'sonar.typescript.lcov.reportPaths': 'coverage/lcov.info',
      'sonar.javascript.lcov.reportPaths': 'coverage/lcov.info',
      'sonar.testExecutionReportPaths': 'coverage/test-reporter.xml'
    }
  }, () => {});
