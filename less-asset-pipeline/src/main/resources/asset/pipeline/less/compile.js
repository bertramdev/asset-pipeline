var compile = function(fileText, paths) {
    var me = this;
    globalPaths = paths;

    var parser = new(less.Parser);

    var result;
    less.render(fileText,{}, function(e, output) {
        var lessResults = {};
        if(output) {
            lessResults.success = true
            lessResults.css = output.css
        } else {
            throw(e);
        }
        Packages.asset.pipeline.less.LessProcessor.setResults(lessResults)
    })
    return result;
};
