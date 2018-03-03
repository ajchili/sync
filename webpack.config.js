var path = require('path');
var webpack = require('webpack');

var config = {
    context: path.resolve(__dirname + '/src'),
    entry: {
      app: ['webpack/hot/dev-server', './app.js']
    },
    output: {
        path: path.resolve(__dirname, 'build'),
        filename: 'app.bundle.js',
        publicPath: 'http://localhost:8080/build/'
    },
    devServer: {
        contentBase: './public',
        publicPath: 'http://localhost:8080/build/'
    },
    module: {
        loaders: [
            {
                test: /\.js$/,
                loader: 'babel-loader',
                exclude: /node_modules/,
                query: {
                    presets: ['es2015', 'react']
                },
                include: __dirname
            }
        ]
    },
    stats: {
        colors: true
    },
    devtool: 'source-map',
    plugins: [
      new webpack.HotModuleReplacementPlugin(),
      new webpack.IgnorePlugin(new RegExp("^(fs|ipc)$"))
    ]
};

module.exports = config;
