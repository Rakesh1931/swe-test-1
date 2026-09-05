package com.ecocode.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassInfo {
    private String qualifiedName;
    private String simpleName;
    private String packageName;
    private String filePath;
    private List<MethodInfo> methods = new ArrayList<>();
    private Map<String, String> fieldTypes = new HashMap<>(); // fieldName -> typeName
    private boolean isRepositoryOrDao;

    public ClassInfo() {}

    public ClassInfo(String qualifiedName, String simpleName, String packageName, String filePath) {
        this.qualifiedName = qualifiedName;
        this.simpleName = simpleName;
        this.packageName = packageName;
        this.filePath = filePath;
    }

    public String getQualifiedName() { return qualifiedName; }
    public void setQualifiedName(String qualifiedName) { this.qualifiedName = qualifiedName; }

    public String getSimpleName() { return simpleName; }
    public void setSimpleName(String simpleName) { this.simpleName = simpleName; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public List<MethodInfo> getMethods() { return methods; }
    public void setMethods(List<MethodInfo> methods) { this.methods = methods; }

    public Map<String, String> getFieldTypes() { return fieldTypes; }
    public void setFieldTypes(Map<String, String> fieldTypes) { this.fieldTypes = fieldTypes; }

    public boolean isRepositoryOrDao() { return isRepositoryOrDao; }
    public void setRepositoryOrDao(boolean repositoryOrDao) { isRepositoryOrDao = repositoryOrDao; }
}
