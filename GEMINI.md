# GEMINI.md — EcoCode Agent Configuration
> **Version 1.0** — EcoCode AI Development Orchestrator
> Place this file at your project root. Antigravity reads it automatically on workspace open.

---

## AGENT IDENTITY

You are building **EcoCode** — a green-software analysis platform.
- **Project:** UCS503P-202627-EcoCode (Thapar Institute)
- **Repo:** https://github.com/BrownBoy90/UCS503P-202627-EcoCode
- **Team:** Gurnoor (Static Analysis), Rakesh (Runtime/Build), Manav (Integration/UI)
- **Language:** Java 21 (analysis target) + React + TypeScript (frontend)

EcoCode analyzes Java source code, finds resource inefficiencies (CPU/memory/DB/network),
profiles running apps in Docker, and compares software versions to prove improvements.
**No AI is used for detection — all rules are deterministic.**

---

## CRITICAL: READ BEFORE ANY IMPLEMENTATION

Before writing any code, you MUST understand the full architecture:

```
ECOCODE SYSTEM
│
├── Frontend      React 18 + TypeScript + Tailwind (port 3000)
├── Backend       Spring Boot 3 / Java 21 (port 8080)
├── Database      PostgreSQL 15
├── Analyzer      JavaParser 3.25 → AST → CallGraph → RuleEngine
├── Profiler      Docker sandbox → JFR → RuntimeMetrics
└── DevOps        Docker Compose + GitHub Actions CI
```

---

## TECHNOLOGY STACK (FIXED — DO NOT DEVIATE)

| Layer         | Technology                              |
|---------------|-----------------------------------------|
| Frontend      | React 18 + TypeScript + Vite            |
| Styling       | Tailwind CSS                            |
| Backend       | Spring Boot 3 (Java 21)                |
| Database      | PostgreSQL 15 + Flyway migrations       |
| Static Analysis | JavaParser 3.25                       |
| Profiling     | Docker stats + JFR (Java Flight Recorder)|
| Containers    | Docker + Docker Compose                 |
| Testing       | JUnit 5, Mockito, Testcontainers        |
| CI/CD         | GitHub Actions                          |
| Git ops       | JGit                                    |
| Build target  | Gradle                                  |
