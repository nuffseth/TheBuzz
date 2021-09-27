const path = require("path");

const HtmlWebpackPlugin = require('html-webpack-plugin');
//const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
    entry: path.resolve(__dirname, "../", 'app.tsx'),
    target: "web",
    output: {
        path: path.resolve(__dirname, '../', process.env['TARGETFOLDER'], process.env['WEBFOLDERNAME']),
        filename: 'app.js'
    },

    plugins: [
        new HtmlWebpackPlugin({ template: path.resolve(__dirname, "..", "index.html") }),
       // new CopyWebpackPlugin({ patterns: [{ from: path.resolve(__dirname, "..", "public/"), to: "public/" }] })
    ],

    module: {
        rules: [
            { test: /\.(ts|tsx)$/, loader: 'ts-loader', exclude: /node_modules/ },
            { test: /\.css$/, use: ["style-loader", "css-loader"] }
        ]
    },
    resolve: { extensions: [".ts", ".tsx", ".json", ".js"] },
};