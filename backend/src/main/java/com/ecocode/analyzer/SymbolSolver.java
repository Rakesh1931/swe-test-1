package com.ecocode.analyzer;

import java.util.*;

public class SymbolSolver {

    public void resolveSymbols(ProjectModel model) {
        List<ClassInfo> classes = model.getClasses();

        // Build fast O(1) lookup indexes
        Map<String, ClassInfo> qualifiedMap = new HashMap<>(classes.size() * 2);
        Map<String, List<ClassInfo>> simpleMap = new HashMap<>(classes.size() * 2);
        Map<String, List<String>> methodSignatureMap = new HashMap<>(); // key: "methodName:argCount" -> list of "qualifiedClass#methodName"

        for (ClassInfo cls : classes) {
            qualifiedMap.put(cls.getQualifiedName(), cls);
            simpleMap.computeIfAbsent(cls.getSimpleName(), k -> new ArrayList<>(2)).add(cls);

            for (MethodInfo m : cls.getMethods()) {
                String sig = m.getName() + ":" + m.getParameterTypes().size();
                methodSignatureMap.computeIfAbsent(sig, k -> new ArrayList<>(2)).add(cls.getQualifiedName() + "#" + m.getName());
            }
        }

        for (MethodCallInfo call : model.getAllMethodCalls()) {
            if (call.getResolvedTarget() != null) continue;

            String calleeName = call.getCalleeName();
            String scope = call.getScope();
            int argCount = call.getArguments().size();
            String sig = calleeName + ":" + argCount;

            ClassInfo matchedClass = null;

            if (scope != null) {
                // Find caller class
                ClassInfo callerClass = qualifiedMap.get(call.getCallerClassName());
                if (callerClass == null) {
                    List<ClassInfo> callers = simpleMap.get(call.getCallerClassName());
                    if (callers != null && !callers.isEmpty()) {
                        callerClass = callers.get(0);
                    }
                }

                if (callerClass != null) {
                    String fieldType = callerClass.getFieldTypes().get(scope);
                    if (fieldType != null) {
                        matchedClass = qualifiedMap.get(fieldType);
                        if (matchedClass == null) {
                            List<ClassInfo> matches = simpleMap.get(fieldType);
                            if (matches != null && !matches.isEmpty()) {
                                matchedClass = matches.get(0);
                            }
                        }
                    }
                }

                if (matchedClass == null) {
                    List<ClassInfo> matches = simpleMap.get(scope);
                    if (matches != null && !matches.isEmpty()) {
                        matchedClass = matches.get(0);
                    }
                }
            }

            if (matchedClass != null) {
                for (MethodInfo method : matchedClass.getMethods()) {
                    if (method.getName().equals(calleeName) && method.getParameterTypes().size() == argCount) {
                        call.setResolvedTarget(matchedClass.getQualifiedName() + "#" + calleeName);
                        break;
                    }
                }
            }

            // Fallback: fast indexed lookup by signature
            if (call.getResolvedTarget() == null) {
                List<String> targets = methodSignatureMap.get(sig);
                if (targets != null && !targets.isEmpty()) {
                    call.setResolvedTarget(targets.get(0));
                }
            }
        }
    }
}
