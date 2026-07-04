# Tldraw Diagrams — Steering

## Purpose
Use tldraw to create visual diagrams (architecture, flows, domain models) and export them as PNG files.

## Tools Available
- **tldraw MCP** (`@talhaorak/tldraw-mcp`) — create and manipulate `.tldr` canvas files (for browsing/editing only)
- **Puppeteer + tldraw-cli** — the CORRECT pipeline for PNG generation

## Important: Export Pipeline
The tldraw MCP creates `.tldr` files with schema v2 records. `@kitschpatrol/tldraw-cli` (v5+) expects a newer schema and **fails with "invalidRecords"** — do NOT use MCP-created files directly with tldraw-cli.

**Correct pipeline:**
```
Node.js script (Puppeteer + bundled tldraw editor)
  → creates shapes via editor API (window.editor)
  → exports store snapshot as .tldr file
  → tldraw-cli renders to PNG
```

**Do NOT use:**
```
tldraw MCP → .tldr → tldraw-cli export  ← schema incompatibility, will fail
tldraw_to_png.py (cairosvg)             ← ugly output, basic SVG rendering
```

## File Locations
- Renderer scripts: `/home/kiquetal/.tldraw/renderer/`
- Output PNGs: project's `diagrams/` folder

## Key Paths
```
Tldraw editor (bundled): /home/kiquetal/.npm/_npx/88d8eb54362880fe/node_modules/@kitschpatrol/tldraw-cli/dist/tldraw
Puppeteer:               /home/kiquetal/.npm/_npx/88d8eb54362880fe/node_modules/puppeteer/lib/esm/puppeteer/puppeteer.js
Hono server:             /home/kiquetal/.npm/_npx/88d8eb54362880fe/node_modules/@hono/node-server/dist/index.mjs
Hono serveStatic:        /home/kiquetal/.npm/_npx/88d8eb54362880fe/node_modules/@hono/node-server/dist/serve-static.mjs
Hono:                    /home/kiquetal/.npm/_npx/88d8eb54362880fe/node_modules/hono/dist/index.js
```

## Shape Creation Rules (tldraw 4.5)

### CRITICAL: Use `richText` NOT `text`
Both `text` and `geo` shapes use `richText` (ProseMirror doc format):

```js
function rt(text) {
  const lines = text.split('\n');
  return {
    type: 'doc',
    content: lines.map(line => ({
      type: 'paragraph',
      content: line ? [{ type: 'text', text: line }] : undefined
    }))
  };
}
```

### Shape Examples

**Text shape:**
```js
editor.createShape({ type: 'text', x: 100, y: 50, props: { richText: rt('Title'), size: 'xl', color: 'blue' } });
```

**Geo (rectangle with label):**
```js
editor.createShape({ type: 'geo', x: 100, y: 100, props: { w: 300, h: 80, geo: 'rectangle', color: 'blue', fill: 'solid', richText: rt('Service Layer') } });
```

**Arrow:**
```js
editor.createShape({ type: 'arrow', x: 0, y: 0, props: { start: { x: 100, y: 100 }, end: { x: 300, y: 100 }, color: 'black' } });
```

**Note (sticky):**
```js
editor.createShape({ type: 'note', x: 400, y: 100, props: { color: 'yellow', richText: rt('Remember this') } });
```

### Available Properties
- **Geo types:** rectangle, diamond, ellipse, arrow-right, arrow-left, cloud, star, hexagon, octagon, trapezoid, rhombus, oval
- **Colors:** black, red, orange, yellow, green, blue, violet, light-blue, light-green, light-red, light-violet, grey
- **Fill:** solid, semi, none, pattern
- **Sizes (text):** s, m, l, xl

## Template Script

```js
import puppeteer from '/home/kiquetal/.npm/_npx/88d8eb54362880fe/node_modules/puppeteer/lib/esm/puppeteer/puppeteer.js';
import { serve } from '/home/kiquetal/.npm/_npx/88d8eb54362880fe/node_modules/@hono/node-server/dist/index.mjs';
import { serveStatic } from '/home/kiquetal/.npm/_npx/88d8eb54362880fe/node_modules/@hono/node-server/dist/serve-static.mjs';
import { Hono } from '/home/kiquetal/.npm/_npx/88d8eb54362880fe/node_modules/hono/dist/index.js';
import { writeFileSync } from 'fs';
import { execSync } from 'child_process';

const app = new Hono();
const tldrawPath = '/home/kiquetal/.npm/_npx/88d8eb54362880fe/node_modules/@kitschpatrol/tldraw-cli/dist/tldraw';
app.get('/tldr-data', (c) => c.text('', 404));
app.use('/*', serveStatic({ root: tldrawPath }));
const server = serve({ fetch: app.fetch, port: 19878 });

const browser = await puppeteer.launch({ headless: true, args: ['--no-sandbox'] });
const page = await browser.newPage();
await page.goto('http://localhost:19878', { waitUntil: 'networkidle0', timeout: 30000 });
await page.waitForFunction('!!window.editor', { timeout: 15000 });

const tldrData = await page.evaluate(() => {
  const editor = window.editor;
  if (!editor) return null;

  function rt(text) {
    const lines = text.split('\n');
    return { type: 'doc', content: lines.map(line => ({
      type: 'paragraph', content: line ? [{ type: 'text', text: line }] : undefined
    }))};
  }

  // === CREATE SHAPES HERE ===
  editor.createShape({ type: 'geo', x: 50, y: 50, props: { w: 200, h: 100, geo: 'rectangle', color: 'blue', fill: 'solid', richText: rt('Hello World') } });

  // Export
  const snapshot = editor.store.getStoreSnapshot();
  const schema = editor.store.schema.serialize();
  return JSON.stringify({ tldrawFileFormatVersion: 1, schema, records: Object.values(snapshot.store) });
});

if (tldrData) {
  writeFileSync('/tmp/diagram.tldr', tldrData);
  execSync('npx @kitschpatrol/tldraw-cli export /tmp/diagram.tldr --format png --output ./diagrams/ --name my-diagram --scale 2', { stdio: 'inherit' });
}

await browser.close();
server.close();
```

## Workflow Summary

1. Create a `.mjs` script in `/home/kiquetal/.tldraw/renderer/`
2. Use the template above, add shapes via `editor.createShape()`
3. Export snapshot → write `.tldr` → run `tldraw-cli export --format png`
4. Copy resulting PNG to project's `diagrams/` folder
5. Show the PNG to the user

## When to Use
- Architecture diagrams for the project
- Flow diagrams (Either pipeline, request lifecycle)
- Domain model visualization
- Any visual explanation the user requests
- Prefer tldraw over mermaid when the user asks for diagrams (unless mermaid is explicitly requested)
