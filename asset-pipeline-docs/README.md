# Asset Pipeline Documentation

This subproject contains the AsciiDoc documentation for the Asset Pipeline library.

## Building Locally

To build the documentation locally:

```bash
./gradlew :asset-pipeline-docs:asciidoctor
```

The generated HTML documentation will be available in `asset-pipeline-docs/build/docs/`.

## Viewing the Documentation

After building, open the generated HTML file:

```bash
open asset-pipeline-docs/build/docs/index.html
```

Or on Linux:

```bash
xdg-open asset-pipeline-docs/build/docs/index.html
```

## Publishing to GitHub Pages

The documentation is automatically published to GitHub Pages when changes are pushed to the main branches (5.0.x, main, or master).

The GitHub Actions workflow (`.github/workflows/publish-docs.yml`) handles:
- Building the AsciiDoc documentation
- Publishing to GitHub Pages

### Setup GitHub Pages

To enable GitHub Pages for this repository:

1. Go to repository Settings → Pages
2. Under "Source", select "GitHub Actions"
3. The workflow will automatically deploy on the next push

## Documentation Structure

- `src/asciidoc/index.adoc` - Main documentation entry point
- `src/asciidoc/introduction.adoc` - Introduction to Asset Pipeline
- `src/asciidoc/concepts.adoc` - Core concepts
- `src/asciidoc/gradle.adoc` - Gradle integration
- `src/asciidoc/grails3.adoc` - Grails 3+ integration
- `src/asciidoc/grails2.adoc` - Grails 2 integration
- `src/asciidoc/micronaut.adoc` - Micronaut integration
- `src/asciidoc/ratpack.adoc` - Ratpack integration
- `src/asciidoc/spring-boot.adoc` - Spring Boot integration
- `src/asciidoc/modules.adoc` - Available modules
- `src/asciidoc/extending.adoc` - Extending Asset Pipeline

## AsciiDoc Resources

- [AsciiDoc Syntax Quick Reference](https://docs.asciidoctor.org/asciidoc/latest/syntax-quick-reference/)
- [Asciidoctor Gradle Plugin](https://asciidoctor.org/docs/asciidoctor-gradle-plugin/)
