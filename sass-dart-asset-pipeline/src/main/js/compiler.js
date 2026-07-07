const sass = require('sass');

// Cache for import contents resolved during canonicalize, consumed during load
const importCache = {};

// Build modern API options
// https://sass-lang.com/documentation/js-api/functions/compilestring/
const modernOptions = {
    url: new URL('asset-pipeline:///stdin'),
    importers: [{
        canonicalize(url, context) {
            let importUrl = url;
            let prev = 'stdin';

            // The modern sass API resolves relative URLs (e.g. '../sub') against the
            // containingUrl before calling canonicalize, producing an absolute URL like
            // 'asset-pipeline:///sub'. Extract the path and treat as an absolute import.
            if (url.startsWith('asset-pipeline:///')) {
                importUrl = '/' + url.substring('asset-pipeline:///'.length);
            } else if (context.containingUrl) {
                prev = context.containingUrl.pathname;
                if (prev.startsWith('/')) prev = prev.substring(1);
            }

            const resolved = importer.resolveImport(importUrl, prev);
            if (resolved && resolved.contents !== undefined && resolved.contents !== null) {
                const canonicalUrl = new URL('asset-pipeline:///' + (resolved.path || importUrl));
                importCache[canonicalUrl.href] = resolved.contents;
                return canonicalUrl;
            }
            return null;
        },
        load(canonicalUrl) {
            const contents = importCache[canonicalUrl.href];
            delete importCache[canonicalUrl.href];
            const path = canonicalUrl.pathname;
            const syntax = path.endsWith('.sass') ? 'indented' : 'scss';
            return { contents: contents || '', syntax };
        }
    }]
};

// Forward supported options from compileOptions to the modern API
['style', 'sourceMap', 'sourceMapIncludeSources', 'charset', 'quietDeps', 'verbose',
 'silenceDeprecations', 'fatalDeprecations', 'futureDeprecations'].forEach(function(key) {
    if (compileOptions[key] !== undefined && compileOptions[key] !== null) {
        modernOptions[key] = compileOptions[key];
    }
});

// Compile and return the rendered CSS
const result = sass.compileString(compileOptions.data, modernOptions);
global.css = result.css;
