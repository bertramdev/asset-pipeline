module.exports = {
    mode: "production",
    target: "node",
    entry: {
        index: "./src/main/js/compiler.js"
    },
    output: {
        path: __dirname + "/src/main/resources/js",
        publicPath: '',
        filename: "compiler.js"
    },
    optimization: {
        minimize: true
    },
    node: {
        global: false,
        __filename: true,
        __dirname: true,
    },
    module: {
        rules: [
            {
                test: /.node$/,
                loader: 'node-loader',
            }
        ]
    }
};
