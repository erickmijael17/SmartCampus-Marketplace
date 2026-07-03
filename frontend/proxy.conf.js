const PROXY_CONFIG = {
  "/auth/**": {
    target: "http://localhost:18080",
    secure: false,
    changeOrigin: true,
    logLevel: "debug"
  },
  "/api/**": {
    target: "http://localhost:18080",
    secure: false,
    changeOrigin: true,
    logLevel: "debug"
  }
};

module.exports = PROXY_CONFIG;
