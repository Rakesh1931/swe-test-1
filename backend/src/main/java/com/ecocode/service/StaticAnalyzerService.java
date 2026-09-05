package com.ecocode.service;

import com.ecocode.analyzer.*;
import com.ecocode.analyzer.rules.RuleEngine;
import com.ecocode.model.StaticIssue;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaticAnalyzerService {

    private final RuleEngine ruleEngine;

    public StaticAnalyzerService(RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    public List<StaticIssue> analyzeProject(String projectPath) {
        JavaProjectParser parser = new JavaProjectParser();
        ProjectModel model = parser.parseProject(projectPath);

        SymbolSolver solver = new SymbolSolver();
        solver.resolveSymbols(model);

        CallGraph callGraph = CallGraph.buildFrom(model);

        return ruleEngine.run(model, callGraph);
    }
}
