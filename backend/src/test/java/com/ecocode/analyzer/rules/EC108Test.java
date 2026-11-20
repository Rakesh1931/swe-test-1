package com.ecocode.analyzer.rules;

import com.ecocode.analyzer.CallGraph;
import com.ecocode.analyzer.ClassInfo;
import com.ecocode.analyzer.MethodCallInfo;
import com.ecocode.analyzer.MethodInfo;
import com.ecocode.analyzer.ProjectModel;
import com.ecocode.model.StaticIssue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EC108Test {

    private EC108_DuplicateComputation rule;

    @BeforeEach
    void setUp() {
        rule = new EC108_DuplicateComputation();
    }

    @Test
    void givenDifferentScopes_whenSameCalleeCalled_thenNoIssueDetected() {
        ProjectModel model = new ProjectModel("/test");
        ClassInfo cls = new ClassInfo("com.example.Service", "Service", "com.example", "/test/Service.java");
        MethodInfo method = new MethodInfo("process", "void", List.of(), List.of(), 10, 30, true);

        // userA.getId() and userB.getId() -> different scopes
        MethodCallInfo call1 = new MethodCallInfo("getId", "userA", List.of(), 12, false, "com.example.Service", "process", "/test/Service.java");
        MethodCallInfo call2 = new MethodCallInfo("getId", "userB", List.of(), 15, false, "com.example.Service", "process", "/test/Service.java");

        method.getCalls().add(call1);
        method.getCalls().add(call2);
        cls.getMethods().add(method);
        model.addClass(cls);

        List<StaticIssue> issues = rule.analyze(model, new CallGraph());
        assertTrue(issues.isEmpty(), "Calls on different scopes must not be flagged as duplicate computation");
    }

    @Test
    void givenTrivialGettersOrLoggers_whenCalledMultipleTimes_thenNoIssueDetected() {
        ProjectModel model = new ProjectModel("/test");
        ClassInfo cls = new ClassInfo("com.example.Service", "Service", "com.example", "/test/Service.java");
        MethodInfo method = new MethodInfo("process", "void", List.of(), List.of(), 10, 30, true);

        // log.info("done") called twice or user.getName() called twice
        MethodCallInfo call1 = new MethodCallInfo("info", "log", List.of("\"done\""), 12, false, "com.example.Service", "process", "/test/Service.java");
        MethodCallInfo call2 = new MethodCallInfo("info", "log", List.of("\"done\""), 15, false, "com.example.Service", "process", "/test/Service.java");
        MethodCallInfo call3 = new MethodCallInfo("getName", "user", List.of(), 16, false, "com.example.Service", "process", "/test/Service.java");
        MethodCallInfo call4 = new MethodCallInfo("getName", "user", List.of(), 18, false, "com.example.Service", "process", "/test/Service.java");

        method.getCalls().addAll(List.of(call1, call2, call3, call4));
        cls.getMethods().add(method);
        model.addClass(cls);

        List<StaticIssue> issues = rule.analyze(model, new CallGraph());
        assertTrue(issues.isEmpty(), "Trivial getters and logging must be excluded from EC108");
    }

    @Test
    void givenExpensiveMethod_whenCalledTwiceWithSameArgsOnSameScope_thenIssueDetected() {
        ProjectModel model = new ProjectModel("/test");
        ClassInfo cls = new ClassInfo("com.example.Service", "Service", "com.example", "/test/Service.java");
        MethodInfo method = new MethodInfo("process", "void", List.of(), List.of(), 10, 30, true);

        // parser.calculateComplexHash(data, salt) called twice on same target
        MethodCallInfo call1 = new MethodCallInfo("calculateComplexHash", "parser", List.of("data", "salt"), 12, false, "com.example.Service", "process", "/test/Service.java");
        MethodCallInfo call2 = new MethodCallInfo("calculateComplexHash", "parser", List.of("data", "salt"), 18, false, "com.example.Service", "process", "/test/Service.java");

        method.getCalls().add(call1);
        method.getCalls().add(call2);
        cls.getMethods().add(method);
        model.addClass(cls);

        List<StaticIssue> issues = rule.analyze(model, new CallGraph());
        assertEquals(1, issues.size(), "Genuine duplicate computation should be flagged");
        assertEquals("EC108", issues.get(0).getRuleId());
        assertTrue(issues.get(0).getExplanation().contains("calculateComplexHash"));
    }
}
