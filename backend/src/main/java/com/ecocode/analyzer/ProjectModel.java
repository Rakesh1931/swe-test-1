package com.ecocode.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectModel {
    private String projectPath;
    private final List<ClassInfo> classes = Collections.synchronizedList(new ArrayList<>());
    private final List<MethodCallInfo> allMethodCalls = Collections.synchronizedList(new ArrayList<>());

    // Additional detected AST structures for special rule checks
    private final List<StringConcatEvent> stringConcatsInLoop = Collections.synchronizedList(new ArrayList<>());
    private final List<ObjectCreationEvent> objectCreationsInLoop = Collections.synchronizedList(new ArrayList<>());
    private final List<ResourceHandlingEvent> unclosedResources = Collections.synchronizedList(new ArrayList<>());

    public ProjectModel() {}

    public ProjectModel(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getProjectPath() { return projectPath; }
    public void setProjectPath(String projectPath) { this.projectPath = projectPath; }

    public List<ClassInfo> getClasses() { return classes; }
    public synchronized void addClass(ClassInfo cls) { this.classes.add(cls); }

    public List<MethodCallInfo> getAllMethodCalls() { return allMethodCalls; }
    public synchronized void addMethodCall(MethodCallInfo call) { this.allMethodCalls.add(call); }

    public List<StringConcatEvent> getStringConcatsInLoop() { return stringConcatsInLoop; }
    public synchronized void addStringConcatEvent(StringConcatEvent event) { this.stringConcatsInLoop.add(event); }

    public List<ObjectCreationEvent> getObjectCreationsInLoop() { return objectCreationsInLoop; }
    public synchronized void addObjectCreationEvent(ObjectCreationEvent event) { this.objectCreationsInLoop.add(event); }

    public List<ResourceHandlingEvent> getUnclosedResources() { return unclosedResources; }
    public synchronized void addUnclosedResource(ResourceHandlingEvent event) { this.unclosedResources.add(event); }

    public static class StringConcatEvent {
        public String filePath;
        public String className;
        public String methodName;
        public int lineNumber;
        public String expression;

        public StringConcatEvent(String filePath, String className, String methodName, int lineNumber, String expression) {
            this.filePath = filePath;
            this.className = className;
            this.methodName = methodName;
            this.lineNumber = lineNumber;
            this.expression = expression;
        }
    }

    public static class ObjectCreationEvent {
        public String filePath;
        public String className;
        public String methodName;
        public int lineNumber;
        public String typeName;
        public List<String> arguments;

        public ObjectCreationEvent(String filePath, String className, String methodName, int lineNumber, String typeName, List<String> arguments) {
            this.filePath = filePath;
            this.className = className;
            this.methodName = methodName;
            this.lineNumber = lineNumber;
            this.typeName = typeName;
            this.arguments = arguments != null ? arguments : new ArrayList<>();
        }
    }

    public static class ResourceHandlingEvent {
        public String filePath;
        public String className;
        public String methodName;
        public int lineNumber;
        public String resourceType;
        public String variableName;

        public ResourceHandlingEvent(String filePath, String className, String methodName, int lineNumber, String resourceType, String variableName) {
            this.filePath = filePath;
            this.className = className;
            this.methodName = methodName;
            this.lineNumber = lineNumber;
            this.resourceType = resourceType;
            this.variableName = variableName;
        }
    }
}
