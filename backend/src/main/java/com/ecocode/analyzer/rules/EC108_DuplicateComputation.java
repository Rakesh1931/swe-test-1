package com.ecocode.analyzer.rules;

import com.ecocode.analyzer.CallGraph;
import com.ecocode.analyzer.ClassInfo;
import com.ecocode.analyzer.MethodCallInfo;
import com.ecocode.analyzer.MethodInfo;
import com.ecocode.analyzer.ProjectModel;
import com.ecocode.model.StaticIssue;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EC108_DuplicateComputation implements EcoRule {

    private static final Set<String> IGNORED_EXACT_NAMES = Set.of(
            "add", "get", "append", "print", "println", "printf", "write", "format",
            "toString", "hashCode", "equals", "equalsIgnoreCase", "size", "length", "isEmpty", "getClass",
            "hasNext", "next", "iterator", "values", "keySet", "entrySet",
            "build", "builder", "of", "toBuilder", "valueOf", "name", "ordinal",
            "info", "debug", "warn", "error", "trace", "log",
            "close", "flush", "clear", "reset", "run", "start", "execute",
            "remove", "delete", "pop", "poll",
            "when", "given", "then", "fail"
    );

    @Override
    public String getRuleId() { return "EC108"; }

    @Override
    public String getName() { return "Duplicate Computation"; }

    @Override
    public String getDescription() {
        return "Executes identical method call with identical arguments multiple times on the same target in the same method scope.";
    }

    @Override
    public String getSeverity() { return "MEDIUM"; }

    @Override
    public List<StaticIssue> analyze(ProjectModel model, CallGraph callGraph) {
        List<StaticIssue> issues = new ArrayList<>();

        for (ClassInfo cls : model.getClasses()) {
            for (MethodInfo method : cls.getMethods()) {
                Map<String, List<MethodCallInfo>> callGroups = new HashMap<>();

                for (MethodCallInfo call : method.getCalls()) {
                    String callee = call.getCalleeName();
                    if (callee == null || isIgnoredMethod(callee)) {
                        continue;
                    }

                    // Scope must match so calls on different targets (e.g. userA.getName() vs userB.getName()) are not grouped
                    String scope = call.getScope() != null ? call.getScope().trim() : "this";
                    String argsStr = String.join(", ", call.getArguments());
                    String key = scope + "." + callee + "(" + argsStr + ")";

                    callGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(call);
                }

                for (Map.Entry<String, List<MethodCallInfo>> entry : callGroups.entrySet()) {
                    if (entry.getValue().size() >= 2) {
                        MethodCallInfo firstCall = entry.getValue().get(0);
                        StaticIssue issue = new StaticIssue();
                        issue.setRuleId(getRuleId());
                        issue.setSeverity(getSeverity());
                        issue.setConfidence("HIGH");
                        issue.setFileName(firstCall.getFilePath());
                        issue.setLineNumber(firstCall.getLineNumber());
                        issue.setClassName(cls.getQualifiedName());
                        issue.setMethodName(method.getName());
                        issue.setCallPath(cls.getQualifiedName() + "#" + method.getName() + " -> " + entry.getKey());
                        issue.setExplanation("Identical method call '" + entry.getKey() + "' executed " + entry.getValue().size() + " times inside method '" + method.getName() + "'.");
                        issue.setRecommendation("Store the return value of '" + entry.getKey() + "' in a local variable and reuse it.");
                        issue.setTradeOffs("Saves CPU cycles by eliminating duplicate execution without changing program semantics.");
                        issues.add(issue);
                    }
                }
            }
        }

        return issues;
    }

    private static boolean isIgnoredMethod(String callee) {
        if (IGNORED_EXACT_NAMES.contains(callee)) {
            return true;
        }
        // Standard getters (e.g. getId, getName, getCreatedAt)
        if (callee.startsWith("get") && callee.length() > 3 && Character.isUpperCase(callee.charAt(3))) {
            return true;
        }
        // Boolean status accessors (e.g. isEnabled, isValid, hasPermission)
        if (callee.startsWith("is") && callee.length() > 2 && Character.isUpperCase(callee.charAt(2))) {
            return true;
        }
        if (callee.startsWith("has") && callee.length() > 3 && Character.isUpperCase(callee.charAt(3))) {
            return true;
        }
        // Setters and mutators (e.g. setName, setStatus, removeEntry)
        if (callee.startsWith("set") && callee.length() > 3 && Character.isUpperCase(callee.charAt(3))) {
            return true;
        }
        if (callee.startsWith("remove") || callee.startsWith("delete")) {
            return true;
        }
        // Fluent builders (e.g. withHeader, withTimeout)
        if (callee.startsWith("with") && callee.length() > 4 && Character.isUpperCase(callee.charAt(4))) {
            return true;
        }
        // Testing / assertion frameworks (e.g. assertEquals, verify, checkArgument)
        if (callee.startsWith("assert") || callee.startsWith("verify") || callee.startsWith("check")) {
            return true;
        }
        return false;
    }
}
