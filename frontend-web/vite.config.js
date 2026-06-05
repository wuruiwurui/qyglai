import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
export default defineConfig({
    plugins: [vue()],
    server: {
        proxy: {
            "/api": "http://localhost:8080",
            "/ai-api": {
                target: "http://localhost:8000",
                changeOrigin: true,
                rewrite: function (path) { return path.replace(/^\/ai-api/, ""); }
            }
        }
    }
});
