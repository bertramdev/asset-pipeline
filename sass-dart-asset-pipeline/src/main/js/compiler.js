const sass = require('sass');

// Call back for resolving imports via the Java asset pipeline resolvers
compileOptions.importer = [
    function(url, prev) {
        return importer.resolveImport(url, prev, compileOptions.assetFilePath);
    }
];

// Compile and return the rendered CSS
result = sass.renderSync(compileOptions);
css = result.css;
