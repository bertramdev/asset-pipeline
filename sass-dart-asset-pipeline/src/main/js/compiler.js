const sass = require('sass');

// Call back for resolving imports via the Java asset pipeline resolvers
compileOptions.importer = [importer.resolveImport];

// Compile and return the rendered CSS
const result = sass.renderSync(compileOptions);
css = result.css;
