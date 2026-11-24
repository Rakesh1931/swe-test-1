package com.ecocode.analyzer.rules;

import com.ecocode.analyzer.CallGraph;
import com.ecocode.analyzer.MethodCallInfo;
import com.ecocode.analyzer.ProjectModel;
import com.ecocode.model.StaticIssue;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EC101_RepeatedCallInLoop implements EcoRule {

    @Override
    public String getRuleId() { return "EC101"; }

    @Override
    public String getName() { return "Repeated Database Call in Loop"; }

    @Override
    public String getDescription() {
        return "Executes database queries or remote calls inside a loop body (N+1 query problem), causing high I/O and latency.";
    }

    @Override
    public String getSeverity() { return "HIGH"; }

    @Override
    public List<StaticIssue> analyze(ProjectModel model, CallGraph callGraph) {
        List<StaticIssue> issues = new ArrayList<>();

        for (MethodCallInfo call : model.getAllMethodCalls()) {
            if (!call.isInLoop()) continue;

            boolean isDbCall = isDatabaseMethod(call, callGraph);
            if (isDbCall) {
                StaticIssue issue = new StaticIssue();
                issue.setRuleId(getRuleId());
                issue.setSeverity(getSeverity());
                issue.setConfidence("HIGH");
                issue.setFileName(call.getFilePath());
                issue.setLineNumber(call.getLineNumber());
                issue.setClassName(call.getCallerClassName());
                issue.setMethodName(call.getCallerMethodName());
                String prefix = call.getScope() != null ? call.getScope() + "." : "";
                issue.setCallPath(call.getCallerFullMethodId() + " -> " + prefix + call.getCalleeName());
                issue.setExplanation("Database query '" + call.getCalleeName() + "' called inside a loop at line " + call.getLineNumber() + ".");
                issue.setRecommendation("Refactor to batch fetch data before entering the loop (e.g. repo.findAllById(ids)).");
                issue.setTradeOffs("Requires storing fetched entities in memory, but reduces network/DB roundtrips from N to 1.");
                issues.add(issue);
            }
        }

        return issues;
    }

    private static final Set<String> NON_DB_METHODS = Set.of(
            "tostring", "equals", "hashcode", "getclass", "clone", "notify", "wait",
            "size", "isempty", "contains", "add", "addall", "remove", "clear", "iterator", "stream",
            "print", "println", "printf", "write", "newline", "flush", "close",
            "next", "nextline", "nextint", "nextdouble", "hasnext", "hasnextline",
            "append", "charat", "length", "substring", "compareto", "compare", "sort"
    );

    private static final List<String> DB_CLASS_PATTERNS = List.of(
            "repository", "repo", "dao", "jdbctemplate", "entitymanager",
            "crudrepository", "jparepository", "mongorepository", "redistemplate",
            "datasource", "connection", "preparedstatement", "statement", "resultset", "session"
    );

    private static final List<String> EXPLICIT_DB_METHODS = List.of(
            "executequery", "executeupdate", "queryforlist", "queryforobject", "queryforrowset",
            "preparestatement", "createnativequery", "createquery", "findbyid", "findallbyid",
            "saveandflush"
    );

    private static final List<String> GENERIC_PERSISTENCE_METHODS = List.of(
            "findby", "find", "findall", "save", "saveall", "delete", "deletebyid",
            "query", "fetch", "fetchall", "select", "insert", "update", "count", "exists"
    );

    private boolean isDatabaseMethod(MethodCallInfo call, CallGraph callGraph) {
        String callee = call.getCalleeName();
        if (callee == null) return false;

        String lowerCallee = callee.toLowerCase();

        // 1. Immediate negative check: Common object methods, getters, setters, I/O, collections
        if (NON_DB_METHODS.contains(lowerCallee)) {
            return false;
        }

        // Standard getters/setters on POJOs/entities: e.g. getId, setName, getAge
        if ((lowerCallee.startsWith("get") || lowerCallee.startsWith("set"))
                && !lowerCallee.startsWith("getconnection")
                && !lowerCallee.startsWith("getresult")
                && !lowerCallee.startsWith("getjdbctemplate")) {
            String scope = call.getScope();
            if (scope == null || DB_CLASS_PATTERNS.stream().noneMatch(p -> scope.toLowerCase().contains(p))) {
                return false;
            }
        }

        String scope = call.getScope();
        String target = call.getResolvedTarget();

        // 2. Check if the scope is explicitly a Database class/interface
        boolean scopeMatchesDbClass = DB_CLASS_PATTERNS.stream()
                .anyMatch(p -> scope != null && scope.toLowerCase().contains(p));

        // 3. Check if callee is an unmistakable SQL/DB method
        boolean isExplicitDbMethod = EXPLICIT_DB_METHODS.stream()
                .anyMatch(lowerCallee::equalsIgnoreCase);

        if (isExplicitDbMethod) {
            return true;
        }

        // 4. If scope is a DB repository/DAO, generic methods like save/delete/update/findBy qualify
        if (scopeMatchesDbClass) {
            boolean isGenericPersistence = GENERIC_PERSISTENCE_METHODS.stream()
                    .anyMatch(lowerCallee::startsWith);
            if (isGenericPersistence) {
                return true;
            }
        }

        // 5. If target is resolved, check what the CALLEE reaches (NOT the caller!)
        if (target != null && callGraph != null) {
            String[] targetParts = target.split("#");
            String targetCls = targetParts.length > 0 ? targetParts[0].toLowerCase() : "";
            String targetMth = targetParts.length > 1 ? targetParts[1].toLowerCase() : "";

            // Check the callee target itself
            boolean targetIsDbClass = DB_CLASS_PATTERNS.stream().anyMatch(targetCls::contains);
            if (targetIsDbClass && (EXPLICIT_DB_METHODS.contains(targetMth) || GENERIC_PERSISTENCE_METHODS.stream().anyMatch(targetMth::startsWith))) {
                return true;
            }

            // Check methods reachable starting FROM the callee target
            Set<String> reachableFromCallee = callGraph.reachableFrom(target);
            for (String node : reachableFromCallee) {
                String[] parts = node.split("#");
                String cls = parts.length > 0 ? parts[0].toLowerCase() : "";
                String mth = parts.length > 1 ? parts[1].toLowerCase() : "";

                boolean nodeClassMatches = DB_CLASS_PATTERNS.stream().anyMatch(cls::contains);
                boolean nodeMethodMatches = EXPLICIT_DB_METHODS.contains(mth) ||
                        (nodeClassMatches && GENERIC_PERSISTENCE_METHODS.stream().anyMatch(mth::startsWith));

                if (nodeClassMatches && nodeMethodMatches) {
                    return true;
                }
            }
        }

        return false;
    }
}
