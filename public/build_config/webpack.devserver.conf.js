//
// Imports
//

const path = require('path');
const webpack = require('webpack');

//
// Config
//

const port = 2268;
const addr = 'campaign-central-assets.local.dev-gutools.co.uk';
const host = 'https://' + addr;

//
// Exports
//

module.exports = {
  mode: 'development',
  devServer: {
    port: port,
    addr: addr,
    host: host,
    disableHostCheck: true  // see https://github.com/webpack/webpack-dev-server/issues/1604
  },
  entry: {
    app: [
      'webpack-dev-server/client?' + host,
      'webpack/hot/dev-server',
      path.join(__dirname, '..', 'app.js')
    ]
  },
  output: {
    path: path.join(__dirname, '..'),
    publicPath: host + '/assets/build/',
    filename: 'app.js'
  },
  resolveLoader: {
    modules: ['node_modules']
  },
  module: {
    rules: [
      {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        loaders: ['babel-loader?presets[]=es2015&presets[]=react&plugins[]=transform-object-assign&plugins[]=transform-class-properties']
      },
      {
        test: require.resolve('react'),
        loader: 'expose-loader?React'
      },
      {
        test: /\.scss$/,
        loaders: ['style-loader', 'css-loader', 'sass-loader']
      },
      {
        test: /\.css$/,
        loaders: ['style-loader', 'css-loader']
      },
      {
        test: /\.woff(2)?(\?v=[0-9].[0-9].[0-9])?$/,
        loader: "url-loader?mimetype=application/font-woff"
      },
      {
        test: /\.(ttf|eot|svg|gif)(\?v=[0-9].[0-9].[0-9])?$/,
        loader: "file-loader?name=[name].[ext]"
      }
    ]
  },
  plugins: [
    new webpack.HotModuleReplacementPlugin()
  ],
  resolve: {
    // Allows require('file') instead of require('file.js|x')
    extensions: ['.js', '.jsx', '.json']
  }
};
