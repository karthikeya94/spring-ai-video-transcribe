# AI Documentation Generator for Video Content

## Overview

This project is an AI-powered documentation generator designed to process video content (such as YouTube videos) and produce structured, high-quality documentation in Markdown format. It leverages a multi-agent workflow, orchestrated by Spring Boot, to analyze, generate, merge, and evaluate content using specialized agents.

---

## Features
- **YouTube Transcript Extraction:** Automatically fetches and processes video transcripts.
- **Multi-Agent Content Generation:** Uses domain-specialist agents for theory, code, visuals, and more.
- **Parallel and Chained Workflows:** Combines parallel agent execution with sequential merging and evaluation.
- **Iterative Improvement:** Evaluates and refines generated documentation.
- **Caching:** Avoids redundant processing for previously analyzed videos.
- **Progress Updates:** Real-time progress reporting via WebSocket.

---

## Architecture & Flow

### High-Level Flow

1. **User Request:**
   - User submits a video URL or transcript via the `/process` endpoint.
2. **Controller Layer:**
   - `MainController` validates and forwards the request to the service layer.
3. **Service Layer:**
   - `VideoProcessingService` handles transcript extraction, caching, and orchestration.
4. **Agent Orchestration:**
   - `OrchestrationService` coordinates the following agent workflows:
     - **RoutingWorkflow:** Domain specialist analysis.
     - **ParallelizationWorkflow:** Multi-agent content generation.
     - **ChainWorkflow:** Merges agent outputs.
     - **Evaluator:** Iterative improvement and scoring.
5. **Response:**
   - The final Markdown document and metadata are returned to the user.

### Layered Call Diagram

```
User
  |
  v
MainController (REST API)
  |
  v
VideoProcessingService
  |
  v
OrchestrationService
  |-----------------------------|
  |      |         |           |
Routing  Parallel  Chain     Evaluator
Workflow Workflow Workflow   Agent
```

---

## Agent Workflow Details

- **RoutingWorkflow:**
  - Analyzes the content and routes it to appropriate domain-specialist agents for deep analysis.
- **ParallelizationWorkflow:**
  - Executes multiple agents in parallel to generate different content sections (theory, code, visuals, etc.).
- **ChainWorkflow:**
  - Merges outputs from all agents into a single, cohesive document.
- **Evaluator:**
  - Iteratively improves and scores the generated document for quality.

---

## How to Run

1. **Prerequisites:**
   - Java 17+
   - Maven

2. **Build and Run:**
   ```sh
   mvn clean install
   mvn spring-boot:run
   ```

3. **Usage:**
   - Open your browser and navigate to `http://localhost:8080`.
   - Submit a YouTube URL or transcript for processing.
   - Download or view the generated Markdown documentation.

---

## Extending the System

- **Add New Agents:**
  - Implement new agent classes in `src/main/java/com/springai/video/agents/`.
  - Register and orchestrate them in `OrchestrationService`.
- **Customize Workflows:**
  - Modify or extend `RoutingWorkflow`, `ParallelizationWorkflow`, or `ChainWorkflow` for custom logic.
- **Change Output Format:**
  - Adjust the merging and evaluation logic to produce different documentation formats.

---

## File/Folder Structure

```
src/main/java/com/springai/video/
  ├── AiDocumentationGeneratorApplication.java  # Spring Boot entry point
  ├── controllers/
  │     └── MainController.java                # REST/WebSocket endpoints
  ├── services/
  │     ├── VideoProcessingService.java        # Main processing logic
  │     ├── OrchestrationService.java          # Agent orchestration
  │     └── YouTubeTranscriptService.java      # Transcript extraction
  ├── agents/
  │     ├── ChainWorkflow.java
  │     ├── ParallelizationWorkflow.java
  │     ├── RoutingWorkflow.java
  │     └── Evaluator.java
  ├── models/                                  # Data models (requests, results, etc.)
  ├── repo/                                    # Data repositories
  └── util/                                    # Utilities and prompts
```

---

## License

This project is for educational and research purposes.

