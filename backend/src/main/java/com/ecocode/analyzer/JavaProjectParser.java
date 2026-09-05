package com.ecocode.analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.Type;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaProjectParser {

    private static final Set<String> RESOURCE_TYPES = Set.of(
            "InputStream", "FileInputStream", "BufferedInputStream",
            "OutputStream", "FileOutputStream", "BufferedOutputStream",
            "Reader", "BufferedReader", "FileReader", "Writer", "BufferedWriter", "FileWriter",
            "Connection", "Statement", "PreparedStatement", "ResultSet"
    );

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JavaProjectParser.class);

    public ProjectModel parseProject(String projectPathStr) {
        Path root = Paths.get(projectPathStr);
        if (!Files.exists(root)) {
            Path parentRelative = Paths.get("..", projectPathStr);
            if (Files.exists(parentRelative)) {
                root = parentRelative;
            } else {
                throw new IllegalArgumentException("Project root not found: " + projectPathStr);
            }
        }
        return parse(root);
    }

    public ProjectModel parse(Path projectRoot) {
        log.info("JavaProjectParser scanning: {}", projectRoot.toAbsolutePath());

        if (!Files.exists(projectRoot)) {
            throw new IllegalArgumentException("Project root not found: " + projectRoot);
        }

        List<Path> javaFiles;
        try (Stream<Path> paths = Files.walk(projectRoot)) {
            javaFiles = paths.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error scanning project root: " + projectRoot, e);
        }

        log.info("Found {} Java files in {}", javaFiles.size(), projectRoot);

        if (javaFiles.isEmpty()) {
            throw new IllegalArgumentException("No .java files found in: " + projectRoot);
        }

        ProjectModel model = new ProjectModel(projectRoot.toString());
        for (Path file : javaFiles) {
            parseFile(file.toFile(), model);
        }
        return model;
    }

    public void parseFile(File file, ProjectModel model) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(file);
            String packageName = cu.getPackageDeclaration()
                    .map(pd -> pd.getNameAsString())
                    .orElse("");

            for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                String simpleName = classDecl.getNameAsString();
                String qualifiedName = packageName.isEmpty() ? simpleName : packageName + "." + simpleName;

                ClassInfo classInfo = new ClassInfo(qualifiedName, simpleName, packageName, file.getAbsolutePath());

                // Check if class itself is Repository/Dao or annotated
                boolean isRepo = simpleName.contains("Repository") || simpleName.contains("Dao") ||
                        simpleName.contains("JdbcTemplate") || simpleName.contains("EntityManager") ||
                        classDecl.isAnnotationPresent("Repository");
                classInfo.setRepositoryOrDao(isRepo);

                // Collect fields
                for (FieldDeclaration field : classDecl.getFields()) {
                    for (VariableDeclarator var : field.getVariables()) {
                        classInfo.getFieldTypes().put(var.getNameAsString(), var.getTypeAsString());
                    }
                }

                // Process Methods
                for (MethodDeclaration methodDecl : classDecl.getMethods()) {
                    String methodName = methodDecl.getNameAsString();
                    String returnType = methodDecl.getTypeAsString();
                    List<String> paramTypes = methodDecl.getParameters().stream()
                            .map(p -> p.getTypeAsString())
                            .collect(Collectors.toList());
                    List<String> paramNames = methodDecl.getParameters().stream()
                            .map(p -> p.getNameAsString())
                            .collect(Collectors.toList());

                    int startLine = methodDecl.getBegin().map(p -> p.line).orElse(0);
                    int endLine = methodDecl.getEnd().map(p -> p.line).orElse(0);
                    boolean isPublic = methodDecl.isPublic();

                    MethodInfo methodInfo = new MethodInfo(methodName, returnType, paramTypes, paramNames, startLine, endLine, isPublic);

                    // Parse Method Calls
                    for (MethodCallExpr callExpr : methodDecl.findAll(MethodCallExpr.class)) {
                        String calleeName = callExpr.getNameAsString();
                        String scope = callExpr.getScope().map(Expression::toString).orElse(null);
                        List<String> args = callExpr.getArguments().stream()
                                .map(Expression::toString)
                                .collect(Collectors.toList());
                        int line = callExpr.getBegin().map(p -> p.line).orElse(0);

                        boolean inLoop = isInsideLoop(callExpr);

                        MethodCallInfo callInfo = new MethodCallInfo(
                                calleeName, scope, args, line, inLoop,
                                qualifiedName, methodName, file.getAbsolutePath()
                        );

                        methodInfo.getCalls().add(callInfo);
                        model.getAllMethodCalls().add(callInfo);
                    }

                    // Parse String Concatenations in Loop (EC102)
                    methodDecl.findAll(BinaryExpr.class).forEach(binaryExpr -> {
                        if (binaryExpr.getOperator() == BinaryExpr.Operator.PLUS && isInsideLoop(binaryExpr)) {
                            if (isStringExpression(binaryExpr)) {
                                int line = binaryExpr.getBegin().map(p -> p.line).orElse(0);
                                model.getStringConcatsInLoop().add(
                                        new ProjectModel.StringConcatEvent(file.getAbsolutePath(), qualifiedName, methodName, line, binaryExpr.toString())
                                );
                            }
                        }
                    });

                    methodDecl.findAll(AssignExpr.class).forEach(assignExpr -> {
                        if ((assignExpr.getOperator() == AssignExpr.Operator.PLUS || assignExpr.getOperator() == AssignExpr.Operator.ASSIGN)
                                && isInsideLoop(assignExpr)) {
                            if (assignExpr.toString().contains("+") && isStringExpression(assignExpr.getValue())) {
                                int line = assignExpr.getBegin().map(p -> p.line).orElse(0);
                                model.getStringConcatsInLoop().add(
                                        new ProjectModel.StringConcatEvent(file.getAbsolutePath(), qualifiedName, methodName, line, assignExpr.toString())
                                );
                            }
                        }
                    });

                    // Parse Object Creation in Loop (EC105)
                    methodDecl.findAll(ObjectCreationExpr.class).forEach(objExpr -> {
                        if (isInsideLoop(objExpr)) {
                            int line = objExpr.getBegin().map(p -> p.line).orElse(0);
                            String typeName = objExpr.getTypeAsString();
                            List<String> args = objExpr.getArguments().stream().map(Expression::toString).collect(Collectors.toList());
                            model.getObjectCreationsInLoop().add(
                                    new ProjectModel.ObjectCreationEvent(file.getAbsolutePath(), qualifiedName, methodName, line, typeName, args)
                            );
                        }
                    });

                    // Parse Unclosed Resources (EC107)
                    methodDecl.findAll(VariableDeclarator.class).forEach(varDecl -> {
                        String typeStr = varDecl.getTypeAsString();
                        if (isResourceType(typeStr)) {
                            if (!isInsideTryWithResources(varDecl)) {
                                int line = varDecl.getBegin().map(p -> p.line).orElse(0);
                                model.getUnclosedResources().add(
                                        new ProjectModel.ResourceHandlingEvent(
                                                file.getAbsolutePath(), qualifiedName, methodName, line, typeStr, varDecl.getNameAsString()
                                        )
                                );
                            }
                        }
                    });

                    classInfo.getMethods().add(methodInfo);
                }

                model.getClasses().add(classInfo);
            }
        } catch (Exception e) {
            System.err.println("Skipping file " + file.getAbsolutePath() + " due to parse warning/error: " + e.getMessage());
        }
    }

    private boolean isInsideLoop(Node node) {
        return node.findAncestor(ForStmt.class).isPresent() ||
                node.findAncestor(ForEachStmt.class).isPresent() ||
                node.findAncestor(WhileStmt.class).isPresent() ||
                node.findAncestor(DoStmt.class).isPresent();
    }

    private boolean isStringExpression(Expression expr) {
        String s = expr.toString();
        return s.contains("\"") || s.contains("String") || s.contains(".toString()") || s.contains("report") || s.contains("name") || s.contains("text");
    }

    private boolean isResourceType(String typeStr) {
        return RESOURCE_TYPES.stream().anyMatch(typeStr::contains);
    }

    private boolean isInsideTryWithResources(Node node) {
        return node.findAncestor(TryStmt.class)
                .map(tryStmt -> !tryStmt.getResources().isEmpty() && tryStmt.getResources().stream().anyMatch(r -> r.toString().contains(node.toString())))
                .orElse(false);
    }
}
