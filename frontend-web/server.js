const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 5173;
const DIR = __dirname;

const mimeTypes = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon'
};

const server = http.createServer((req, res) => {
  let reqUrl = req.url.split('?')[0];
  let cleanUrl = reqUrl.replace(/^\/+/, '');
  if (!cleanUrl) cleanUrl = 'index.html';
  let filePath = path.resolve(DIR, cleanUrl);

  // Seguridad: evitar directory traversal
  if (!filePath.startsWith(DIR)) {
    filePath = path.resolve(DIR, 'index.html');
  }

  if (!fs.existsSync(filePath) || fs.statSync(filePath).isDirectory()) {
    filePath = path.resolve(DIR, 'index.html');
  }

  const ext = path.extname(filePath).toLowerCase();
  const contentType = mimeTypes[ext] || 'application/octet-stream';

  if (req.method === 'HEAD') {
    res.writeHead(200, {
      'Content-Type': contentType,
      'Access-Control-Allow-Origin': '*'
    });
    res.end();
    return;
  }

  fs.readFile(filePath, (err, content) => {
    if (err) {
      res.writeHead(500, { 'Content-Type': 'text/plain' });
      res.end('Error 500: ' + err.message);
    } else {
      res.writeHead(200, {
        'Content-Type': contentType,
        'Access-Control-Allow-Origin': '*'
      });
      res.end(content);
    }
  });
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`GestoPago Web server ready at http://localhost:${PORT}`);
});
