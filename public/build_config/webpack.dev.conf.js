var path = require('path');
var ExtractTextPlugin = require('extract-text-webpack-plugin');

module.exports = {
  devtool: 'source-map',

  module: {
    rules: [
      {
        test:    /\.js$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: [["env", {
              "targets": {
                "browsers": ["last 2 Chrome versions"]
              }
            }], 'react'],
            plugins: ['transform-object-assign','transform-class-properties']
          }
        }
      },
      {
        test: /\.scss$/,
        use: ExtractTextPlugin.extract({
          fallback: 'style-loader',
          use: 'css-loader?sourceMap!sass-loader?sourceMap'
        })
      },
      {
        test: /\.css$/,
        use: ExtractTextPlugin.extract({
          fallback: 'style-loader',
          use: 'css-loader?sourceMap'
        })
      },
      {
        test: /\.woff(2)?(\?v=[0-9].[0-9].[0-9])?$/,
        use: 'url-loader?mimetype=application/font-woff'
      },
      {
        test: /\.(ttf|eot|svg|gif)(\?v=[0-9].[0-9].[0-9])?$/,
        use: 'file-loader?name=[name].[ext]'
      }
    ]
  },
  resolveLoader: {
    modules: [
      "node_modules"
    ]
  },

  resolve: {
    extensions: ['.js', '.jsx', '.json', '.scss'],
    modules: [
      path.join('..', "node_modules")
    ]
  },

  plugins: [
    new ExtractTextPlugin('main.css')
  ]
};
