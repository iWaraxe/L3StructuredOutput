# Spring AI Structured Output Course 🎓

A comprehensive course on mastering structured output with Spring AI, from fundamentals to production-ready implementations.

## 📚 Course Overview

This course teaches you how to leverage Spring AI's structured output capabilities to convert Large Language Model (LLM) responses into strongly-typed Java objects, enabling reliable AI-powered applications.

### Why Structured Output?

Traditional LLM interactions return unstructured text, making it challenging to:
- Parse responses reliably
- Integrate AI into existing systems
- Validate output quality
- Handle errors gracefully

Spring AI's structured output solves these challenges by providing a robust framework for converting AI responses into type-safe Java objects.

## 🗂️ Repository Structure

### Learning Path

The course is organized into 16 progressive sections, each in its own branch:

| Section | Branch | Topic | Learning Guide |
|---------|--------|-------|----------------|
| s1 | `01-foundation` | Foundation & Setup | [Foundation Guide](docs/s1-foundation.md) |
| s2 | `02-prompt-templates` | Prompt Templates & Basic Output | [Templates Guide](docs/s2-prompt-templates.md) |
| s3 | `03-structured-fundamentals` | Structured Output Fundamentals | [Fundamentals Guide](docs/s3-fundamentals.md) |
| s4 | `04-converter-factory` | Converter Factory Pattern | [Converters Guide](docs/s4-converters.md) |
| s5 | `05-advanced-bean-converter` | Advanced Bean Converter | [Advanced Bean Guide](docs/s5-advanced-bean.md) |
| s6 | `06-map-list-converters` | Map & List Converters | [Collections Guide](docs/s6-collections.md) |
| s7 | `07-chatclient-vs-chatmodel` | ChatClient vs ChatModel | [API Comparison Guide](docs/s7-api-comparison.md) |
| s8 | `08-openai-json-modes` | OpenAI JSON Modes | [JSON Modes Guide](docs/s8-json-modes.md) |
| s9 | `10-property-ordering-annotations` | Property Ordering & Annotations | [Annotations Guide](docs/s9-annotations.md) |
| s10 | `11-multi-model-support` | Multi-Model Support | [Multi-Model Guide](docs/s10-multi-model.md) |
| s11 | `12-validation-error-recovery` | Validation & Error Recovery | [Validation Guide](docs/s11-validation.md) |
| s12 | `13-custom-format-conversion` | Custom Format & Conversion | [Custom Formats Guide](docs/s12-custom-formats.md) |
| s13 | `14-advanced-generics` | Advanced Generic Types | [Generics Guide](docs/s13-generics.md) |
| s14 | `15-performance-optimization` | Performance & Optimization | [Performance Guide](docs/s14-performance.md) |
| s15 | `16-testing-strategies` | Testing Strategies | [Testing Guide](docs/s15-testing.md) |
| s16 | `17-real-world-best-practices` | Real-world Use Cases | [Best Practices Guide](docs/s16-real-world.md) |

## 🚀 Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.9+
- OpenAI API key (or other AI provider credentials)
- Basic understanding of Spring Boot

### Quick Start
```bash
# Clone the repository
git clone https://github.com/iWaraxe/L3StructuredOutput.git
cd L3StructuredOutput

# Switch to a specific section
git checkout 02-prompt-templates

# Configure your API key
export OPENAI_API_KEY=your-api-key

# Run the application
./mvnw spring-boot:run
```

### Testing Endpoints
Each section includes REST endpoints for testing. After starting the application:
```bash
# View available demos
curl http://localhost:8080/api/s2/demos

# Run specific examples
curl -X POST http://localhost:8080/api/s2/weather \
  -H "Content-Type: application/json" \
  -d '{"city": "Seattle"}'
```

## 📖 Learning Approach

### Recommended Path
1. **Start with Fundamentals** (s1-s3): Understand core concepts
2. **Explore Converters** (s4-s6): Master different converter types
3. **API Deep Dive** (s7-s8): Learn API choices and JSON modes
4. **Advanced Topics** (s9-s13): Annotations, validation, generics
5. **Production Ready** (s14-s16): Performance, testing, real-world use

### Key Learning Outcomes
- ✅ Convert LLM responses to Java objects reliably
- ✅ Choose the right converter for your use case
- ✅ Implement production-grade error handling
- ✅ Optimize performance for scale
- ✅ Test AI-powered applications effectively
- ✅ Deploy to production with confidence

## 🔑 Core Concepts

### StructuredOutputConverter
The foundation interface that combines:
- `Converter<String, T>` - Spring's conversion mechanism
- `FormatProvider` - Instructions for the AI model
- Type safety and validation

### Converter Types
1. **BeanOutputConverter** - For POJOs and records
2. **MapOutputConverter** - For flexible key-value structures
3. **ListOutputConverter** - For simple lists
4. **Custom Converters** - For specialized needs

## 📊 Architecture Overview

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   User Request  │────▶│  Spring AI API   │────▶│   AI Provider   │
└─────────────────┘     └──────────────────┘     └─────────────────┘
                               │                           │
                               ▼                           ▼
                        ┌──────────────────┐      ┌─────────────────┐
                        │ Format Provider  │      │  LLM Response   │
                        └──────────────────┘      └─────────────────┘
                               │                           │
                               └───────────┬───────────────┘
                                          │
                                          ▼
                                ┌──────────────────┐
                                │ Output Converter │
                                └──────────────────┘
                                          │
                                          ▼
                                ┌──────────────────┐
                                │ Java Object (T)  │
                                └──────────────────┘
```

## 📚 Additional Resources

### Documentation
- [Spring AI Reference](https://docs.spring.io/spring-ai/reference/)
- [OpenAI Structured Outputs](https://platform.openai.com/docs/guides/structured-outputs)
- [JSON Schema Specification](https://json-schema.org/)

### Community
- [Spring AI GitHub](https://github.com/spring-projects/spring-ai)
- [Spring Community Forums](https://community.spring.io/)

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring AI team for the excellent framework
- OpenAI for structured output capabilities
- All contributors and learners

---

**Happy Learning!** 🚀

For questions or support, please open an issue or contact the course maintainers.