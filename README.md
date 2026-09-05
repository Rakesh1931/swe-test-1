# EcoCode — Green-Software Analysis Platform

EcoCode is a green-software analysis platform that parses Java source code into an AST, builds a call graph, applies deterministic rules (EC101–EC108) to detect resource inefficiencies, profiles running applications in Docker sandboxes, computes an EcoScore, and compares software versions to prove whether code changes actually improved efficiency.

## Table of Contents
- [Quick Start](#quick-start)
- [What Is EcoCode?](#what-is-ecocode)
- [Architecture](#architecture)
- [Static Analysis Rules (EC101–EC108)](#static-analysis-rules-ec101ec108)
- [Tech Stack](#tech-stack)
- [Team](#team)

## Quick Start
> Requires: Docker, Docker Compose

```bash
git clone https://github.com/BrownBoy90/UCS503P-202627-EcoCode.git
cd UCS503P-202627-EcoCode
docker-compose up --build

# Frontend: http://localhost:3000
# Backend API: http://localhost:8080/api
```

## What Is EcoCode?

EcoCode addresses software sustainability by identifying resource-wasting code patterns before deployment. By combining AST-based static code analysis with Docker-based runtime profiling, EcoCode delivers an actionable **EcoScore** and granular recommendations to reduce CPU, memory, database, and network overhead.

## Architecture

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

## Static Analysis Rules (EC101–EC108)

| Rule ID | Name | Severity | Description |
|---------|------|----------|-------------|
| **EC101** | Repeated Call In Loop | HIGH | Database queries or heavy calls inside loop iterations (N+1 query problem) |
| **EC102** | String Concat In Loop | MEDIUM | String concatenation (`+` / `+=`) inside loop; should use `StringBuilder` |
| **EC103** | Collection Lookup In Loop | MEDIUM | Linear lookup (`List.contains` / `indexOf`) inside loop; should use `Set` |
| **EC104** | Unbounded Collection | HIGH | Unpaginated database fetch (`findAll()`) returning unbounded rows |
| **EC105** | Repeated Allocation Hot Path | LOW | Object creation inside loop independent of loop iteration variables |
| **EC106** | Deep Call Chain | LOW | Reachability call stack depth exceeding 8 hops |
| **EC107** | Resource Handling | HIGH | Unclosed I/O streams or database resources (not using try-with-resources) |
| **EC108** | Duplicate Computation | MEDIUM | Identical method call computed multiple times in the same scope |

## Tech Stack

- **Frontend**: React 18, TypeScript, Vite, Tailwind CSS
- **Backend**: Spring Boot 3 (Java 21), Spring Data JPA, Flyway
- **Static Analysis**: JavaParser 3.25
- **Database**: PostgreSQL 15
- **Containers & DevOps**: Docker, Docker Compose, GitHub Actions

## Team

- **Gurnoor** — Static Analysis Engine & AST Parser
- **Rakesh** — Runtime Profiler & Docker Sandbox
- **Manav** — Frontend UI & Integration
