# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 📚 AI Assistant Documentation

**IMPORTANT**: Before working on any task, read the relevant documentation in the `.claude/` directory:

- **Start here**: `.claude/index.md` - Quick reference and navigation guide
- **Task guides**: `.claude/tasks/` - Step-by-step instructions for common tasks
- **Code patterns**: `.claude/patterns/` - Reusable code patterns and best practices
- **Key files**: `.claude/key-files.md` - Critical file locations
- **Codebase map**: `.claude/codebase-map.md` - Navigate the codebase structure
- **Common pitfalls**: `.claude/gotchas.md` - Avoid common mistakes
- **Tech stack**: `.claude/tech-stack.md` - Technology stack details

The `.claude/` directory contains task-oriented documentation optimized for AI assistants to quickly understand and work with this codebase.

## Project Overview

Barda is a low-code platform fork based on Openblocks, designed for building internal tools and applications. It's a Chinese-localized version with performance optimizations and additional features.

## Architecture

The project consists of three main parts:

### Frontend (client/)
- **Monorepo** using Yarn workspaces
- **Main app**: `client/packages/barda/` - React 18 app with Vite, Redux, styled-components
- **Core library**: `client/packages/barda-core/` - Component building library
- **Design system**: `client/packages/barda-design/` - Icons and design tokens
- **Remote components**: `client/packages/barda-comps/` - Remotely loaded components (Chart, Calendar, ImageEditor)
- **SDK**: `client/packages/barda-sdk/` - SDK for embedding apps
- **CLI tools**: `client/packages/barda-cli/` - Scaffolding for custom components

### Java Backend (server/api-service/)
- **Spring Boot 3.3.1** with Java 17
- **Reactive stack** using Project Reactor
- **MongoDB** for data persistence
- **Redis** for caching
- **Module structure**:
  - `barda-server` - Main application entry point
  - `barda-domain` - Domain models and business logic
  - `barda-infra` - Infrastructure layer
  - `barda-sdk` - Backend SDK
  - `barda-plugins` - Database connectors (MySQL, PostgreSQL, MongoDB, Redis, etc.)

### Node Service (server/node-service/)
- TypeScript-based data source plugin system
- Handles additional data sources not covered by Java plugins

## Common Commands

### Frontend Development
```bash
cd client
yarn                           # Install dependencies
yarn start                     # Start dev server (http://localhost:8000)
yarn build                     # Production build
yarn test                      # Run tests
yarn build:core                # Build barda-core library
yarn build:sdk                 # Build barda-sdk
yarn start:comps               # Start component development (http://localhost:9000)
```

### Backend Development (Java)
```bash
cd server/api-service
mvn clean package -DskipTests  # Build without tests

# Run with IntelliJ IDEA or command line:
java -Dpf4j.mode=development -Dpf4j.pluginsDir=barda-plugins -Dspring.profiles.active=barda -jar barda-server/target/barda-server-1.0.1-SNAPSHOT.jar
```

### Node Service
```bash
cd server/node-service
yarn && yarn dev               # Development
yarn build && yarn start       # Production
```

### Docker Development (WSL2/Linux)
```bash
cd develop
sudo ./dev.sh -fr              # Remote frontend debug (no Docker needed)
sudo ./dev.sh -f               # Local frontend with Docker backend
sudo ./dev.sh -dc              # Component development
sudo ./dev.sh -b               # Full stack development
sudo ./dev.sh -p               # Build Docker image
sudo ./dev.sh -c               # Clean containers and cache
```

## Key File Locations

### Adding Native Components
```
client/packages/barda-design/src/icons/           # Icon SVG files
client/packages/barda-design/src/icons/index.ts   # Icon exports
client/packages/barda/src/comps/uiCompRegistry.ts # Component type registration
client/packages/barda/src/comps/index.tsx         # Component registration
client/packages/barda/src/comps/comps/            # Component source files
client/packages/barda/src/pages/editor/editorConstants.tsx  # Sidebar icons
client/packages/barda/src/comps/controls/styleControlConstants.tsx  # Component styles
client/packages/barda/src/i18n/locales/en.ts      # English translations
client/packages/barda/src/i18n/locales/zh.ts      # Chinese translations
```

### Backend Entry Points
```
server/api-service/barda-server/src/main/java/com/barda/api/ServerApplication.java  # Main class
server/api-service/barda-plugins/                  # Database plugin implementations
```

### Architecture Docs (see `docs/developer/architecture/` for details)
```
# Preload JS — 预加载机制、沙箱执行引擎、CSS注入
docs/developer/architecture/preload-js.md

# JS Library — 库管理（上传/删除/存储）、运行时注入
docs/developer/architecture/js-library.md

# Code Editor — CodeMirror编辑器类型、JS自动补全、上下文数据采集
docs/developer/architecture/code-editor.md
```

## Development Notes

- Project uses Aliyun Maven mirrors for faster dependency downloads in China
- Frontend uses Chinese npm mirrors configured in yarn
- Backend requires MongoDB and Redis services running
- Default ports: Frontend 8000, Components 9000, Backend API 8080, Docs 30000
- RSA key pairs in `server/api-service/` are used for login encryption

## i18n

- `en.ts` is the default language file - missing keys in `zh.ts` fall back to English
- Use `trans("uiComp.componentName")` pattern for component text
- Component names must start with uppercase letter when exported
