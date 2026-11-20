package com.ecocode.service;

import com.ecocode.analyzer.*;
import com.ecocode.analyzer.rules.RuleEngine;
import com.ecocode.model.StaticIssue;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaticAnalyzerService {

    private final RuleEngine ruleEngine;

    public static class AnalysisResult {
        private final ProjectModel model;
        private final List<StaticIssue> issues;

        public AnalysisResult(ProjectModel model, List<StaticIssue> issues) {
            this.model = model;
            this.issues = issues;
        }

        public ProjectModel getModel() { return model; }
        public List<StaticIssue> getIssues() { return issues; }
    }

    public StaticAnalyzerService(RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    public AnalysisResult analyze(String projectPath) {
        JavaProjectParser parser = new JavaProjectParser();
        ProjectModel model = parser.parseProject(projectPath);

        SymbolSolver solver = new SymbolSolver();
        solver.resolveSymbols(model);

        CallGraph callGraph = CallGraph.buildFrom(model);

        List<StaticIssue> issues = ruleEngine.run(model, callGraph);
        return new AnalysisResult(model, issues);
    }

    public List<StaticIssue> analyzeProject(String projectPath) {
        return analyze(projectPath).getIssues();
    }
}
