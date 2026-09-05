package com.ecocode.analyzer;

import java.util.List;
import java.util.Optional;

public class SymbolSolver {

    public void resolveSymbols(ProjectModel model) {
        List<ClassInfo> classes = model.getClasses();

        for (MethodCallInfo call : model.getAllMethodCalls()) {
            if (call.getResolvedTarget() != null) continue;

            String calleeName = call.getCalleeName();
            String scope = call.getScope();
            int argCount = call.getArguments().size();

            // Try to match scope with field variable types or class names
            Optional<ClassInfo> matchedClass = Optional.empty();

            if (scope != null) {
                // Find caller class
                Optional<ClassInfo> callerClass = classes.stream()
                        .filter(c -> c.getQualifiedName().equals(call.getCallerClassName()) || c.getSimpleName().equals(call.getCallerClassName()))
                        .findFirst();

                if (callerClass.isPresent()) {
                    String fieldType = callerClass.get().getFieldTypes().get(scope);
                    if (fieldType != null) {
                        matchedClass = classes.stream()
                                .filter(c -> c.getSimpleName().equals(fieldType) || c.getQualifiedName().equals(fieldType))
                                .findFirst();
                    }
                }

                if (matchedClass.isEmpty()) {
                    // Scope might directly match simple class name or variable name
                    matchedClass = classes.stream()
                            .filter(c -> c.getSimpleName().equalsIgnoreCase(scope) || scope.toLowerCase().contains(c.getSimpleName().toLowerCase()))
                            .findFirst();
                }
            }

            if (matchedClass.isPresent()) {
                ClassInfo cls = matchedClass.get();
                for (MethodInfo method : cls.getMethods()) {
                    if (method.getName().equals(calleeName) && method.getParameterTypes().size() == argCount) {
                        call.setResolvedTarget(cls.getQualifiedName() + "#" + calleeName);
                        break;
                    }
                }
            }

            // Fallback: match any class having this method name & arg count
            if (call.getResolvedTarget() == null) {
                for (ClassInfo cls : classes) {
                    for (MethodInfo method : cls.getMethods()) {
                        if (method.getName().equals(calleeName) && method.getParameterTypes().size() == argCount) {
                            call.setResolvedTarget(cls.getQualifiedName() + "#" + calleeName);
                            break;
                        }
                    }
                    if (call.getResolvedTarget() != null) break;
                }
            }

            // Default fallback target if unresolved
            if (call.getResolvedTarget() == null) {
                String targetClass = scope != null ? scope : "UnknownClass";
                call.setResolvedTarget(targetClass + "#" + calleeName);
            }
        }
    }
}
