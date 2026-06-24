import type { NextConfig } from "next";

const backendUrl = process.env.NODE_ENV === 'production' 
  ? 'http://sre-api-gateway:8080' 
  : 'http://localhost:8080';

const nextConfig: NextConfig = {
  output: "standalone",
  async rewrites() {
    return [
      {
        source: '/api/v1/:path*',
        destination: `${backendUrl}/api/v1/:path*`,
      },
    ];
  },
};

export default nextConfig;
