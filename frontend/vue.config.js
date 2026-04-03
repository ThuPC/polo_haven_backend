module.exports = {
  publicPath: process.env.BASE_URL || "/",
  devServer: {
    port: 5173, 
    allowedHosts: ['.ngrok-free.app'],
    client: {
      webSocketURL: 'auto://0.0.0.0:0/ws'
    },
    headers: {
      "Access-Control-Allow-Origin": "*",
    }
  },
};