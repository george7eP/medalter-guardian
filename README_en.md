# 🛠️ Equipment Maintenance and Early Warning System

<p align="center">
  <a href="./README.md">中文</a> | <b>English</b>
</p>

---

## 📖 Project Introduction
This project is a digital management platform custom-built for modern enterprise equipment operation and maintenance (O&M) scenarios. Addressing common pain points in traditional industrial equipment O&M—such as cumbersome paper records, inefficient offline scheduling, opaque equipment statuses, and high rates of sudden breakdowns—the platform leverages a mainstream decoupled front-end and back-end technology stack to achieve core capabilities like digital equipment ledger management, automated maintenance workflows, intelligent fault early warnings, and comprehensive operational log traceability.

By replacing traditional manual O&M models with standardized, process-driven, and intelligent management, the system effectively enhances equipment utilization, reduces the risk of unexpected failures, and implements precise, regulated control over the entire O&M lifecycle.

---

## 🎯 Requirements Analysis

### 1. Business Pain Points
* **Opaque Status**: Equipment status information is not transparent, making it impossible to grasp operation and maintenance conditions in real time.
* **Low Scheduling Efficiency**: Manual scheduling is inefficient, causing maintenance tasks to be easily missed and posing risks of overdue servicing.
* **Poor Retrievability & Traceability**: Traditional paper records are difficult to retain, resulting in a heavy manual auditing workload and substandard traceability.

### 2. Core Functional Objectives
* **Fine-grained Authorization**: Build a three-tier subdivision management and control system (User, Role, Permission) based on the RBAC permission model to achieve fine-grained access control.
* **Full-business Closed-loop Management**: Cover the entire lifecycle including equipment warehousing, ledger maintenance, maintenance plan generation, execution logging, and early warning processing, forming a complete data closed loop.
* **Full-process Security Auditing**: Automatically record system core operational logs leveraging AOP (Aspect-Oriented Programming) technology, ensuring all O&M actions are searchable and traceable.

### 3. Non-functional Requirements
* **Data Security**: User passwords are encrypted and stored using BCrypt one-way hashing to eliminate the risk of plaintext leaks.
* **API Security**: Implement stateless identity authentication based on JWT to guarantee the legitimacy of API access.
* **System Performance**: Ensure high availability and low latency of APIs to handle high-frequency daily O&M operations in an enterprise environment.

---

## 💻 Environment & Tech Stack

### 1. Backend Tech Stack
* **Core Language**: Java 17
* **Framework**: Spring Boot 3.2
* **Database**: MySQL 8.0
* **Cache (Optional)**: Redis
* **Build Tool**: Maven

### 2. Frontend Tech Stack
* **Runtime**: Node.js 18.x
* **Core Framework**: Vue 3.4 (Composition API)
* **Build Tool**: Vite 5.0
* **State Management**: Pinia
* **HTTP Client**: Axios
* **UI Library**: Element Plus

### 3. Tools & OS
* **Backend Development**: IntelliJ IDEA
* **Frontend Development**: VS Code
* **Target OS**: Windows 11 / macOS

---

## 🛠️ Project Design

### 1. Overall Design Concept
This project adopts a decoupled front-end and back-end architecture, with high security, high maintainability, and low coupling as core design principles to build a standardized equipment O&M management platform.
* **Technical Architecture**: The backend delivers standardized RESTful APIs built on Spring Boot 3, while the frontend provides a lightweight, highly interactive user interface via Vue 3 + Element Plus. The layers communicate exclusively through data interfaces, achieving complete decoupling.
* **Authorization Design**: Based on the RBAC role-permission model, users, roles, and permissions are associated within the database to achieve dual fine-grained access control over both frontend routing and backend APIs.
* **Security & Defense**: Integrate Spring Security + JWT for stateless login authentication; utilize BCrypt to encrypt and store user passwords; apply AOP aspect programming to automatically capture and audit global operation logs, ensuring end-to-end traceability.
* **Development Strategy**: Adopt modular development by breaking down system businesses into independent functional modules, reducing code coupling while improving scalability and maintainability.

### 2. Core Modules

#### 🔐 User & Permission Module
The fundamental security line of the system. It supports user login, logout, password modification, role assignment, and permission directive control. Through fine-grained permission configuration, it differentiates the operational privileges of various O&M personnel to ensure secure access.

#### 📋 Equipment Information Management Module
The foundational data module of the system, offering full CRUD management for the equipment ledger. It supports precise searches based on criteria such as equipment name and running status, permanently retaining comprehensive baseline profiles for all assets.

#### 🔧 Maintenance Management Module
A core business module divided into two sub-features: maintenance scheduling and result entry. It enables O&M personnel to formulate periodic maintenance plans and log execution results, realizing closed-loop workflow management from planning to execution and eliminating the issue of overdue maintenance.

#### ⚠️ Early-Warning Information Module
The core safety assurance module. It supports the dynamic configuration of fault warning rules and monitors abnormal equipment statuses in real time. It offers operations such as confirmation, handling, and dismissal for triggered warnings, allowing prompt troubleshooting of hidden dangers and reducing the risk of sudden downtime.

#### 📊 System Audit Module
Leveraging AOP technology, this module automatically collects system operation data to accurately log details such as operator, IP address, operation time, request latency, and action content. It automatically generates O&M logs to meet corporate auditing and troubleshooting requirements.

---

## 📁 Architecture Layers
* **Top Layer**: Equipment Maintenance and Early Warning System (Overall Business Platform)
* **Core Security Layer**: JWT Authentication, Permission Interceptors, AOP Logging
* **Data Persistence Layer**: MySQL + MyBatis-Plus Data Persistence
* **Business Functional Layer**: Equipment Ledger System, Maintenance Workflow, Early Warning Monitoring Center, System Management

---

## ✅ Project Highlights
* Decoupled frontend and backend architecture ensures separated codebases, facilitating smooth iterative maintenance and secondary development.
* Fine-grained RBAC permission control perfectly accommodates O&M management scenarios with diverse roles and positions.
* Dual security protection combining stateless JWT authentication and BCrypt password encryption.
* Automated log auditing powered by AOP requires no manual entry, achieving effortless full-process traceability.
* Bidirectional management integrating equipment maintenance and fault early warning to build a smart O&M ecosystem.