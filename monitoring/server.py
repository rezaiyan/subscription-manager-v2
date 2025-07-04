#!/usr/bin/env python3
"""
Simple HTTP server for the Subscription Manager monitoring dashboard
"""

import http.server
import socketserver
import os
import sys
from pathlib import Path

# Configuration
PORT = 8082
DIRECTORY = Path(__file__).parent

class CustomHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(DIRECTORY), **kwargs)
    
    def end_headers(self):
        # Add CORS headers for cross-origin requests
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        super().end_headers()
    
    def do_GET(self):
        # Serve index.html for root path
        if self.path == '/':
            self.path = '/index.html'
        return super().do_GET()

def main():
    os.chdir(DIRECTORY)
    
    print(f"🚀 Starting monitoring dashboard server...")
    print(f"📁 Serving from: {DIRECTORY}")
    print(f"🌐 URL: http://localhost:{PORT}")
    print(f"📊 Dashboard: http://localhost:{PORT}/index.html")
    print(f"🔍 Health Checker: http://localhost:8083/health")
    print(f"⏹️  Press Ctrl+C to stop")
    print("-" * 50)
    
    try:
        with socketserver.TCPServer(("", PORT), CustomHTTPRequestHandler) as httpd:
            print(f"✅ Server started successfully on port {PORT}")
            httpd.serve_forever()
    except KeyboardInterrupt:
        print(f"\n⏹️  Server stopped by user")
    except OSError as e:
        if e.errno == 48:  # Address already in use
            print(f"❌ Port {PORT} is already in use. Please stop the existing server or use a different port.")
            print(f"💡 You can kill the process using: lsof -ti:{PORT} | xargs kill")
        else:
            print(f"❌ Error starting server: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main() 