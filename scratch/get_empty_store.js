import { tldrawToImage } from '/mydata/.nvm/versions/node/v22.14.0/lib/node_modules/@kitschpatrol/tldraw-cli/dist/lib/index.js';
import puppeteer from 'puppeteer';
import fs from 'fs';
import { serve } from '@hono/node-server';
import { Hono } from 'hono';
import { serveStatic } from '@hono/node-server/serve-static';
import getPort from 'get-port';
import path from 'path';
import { fileURLToPath } from 'url';

async function run() {
  const e = path.dirname(fileURLToPath(import.meta.url));
  const tldrawDist = path.join('/mydata/.nvm/versions/node/v22.14.0/lib/node_modules/@kitschpatrol/tldraw-cli/dist/tldraw');

  const app = new Hono();
  const port = await getPort();
  
  app.get('/tldr-data', c => c.text('', 200));
  app.use('/*', serveStatic({ root: tldrawDist }));
  
  const server = serve({ fetch: app.fetch, port });
  
  // Wait for server
  await new Promise(resolve => setTimeout(resolve, 500));
  
  const browser = await puppeteer.launch({ headless: true });
  const page = await browser.newPage();
  await page.goto(`http://localhost:${port}`);
  
  // Wait for tldraw to load
  await page.waitForFunction(() => window.editor !== undefined);
  
  // Get snapshot/schema of the blank editor
  const snapshot = await page.evaluate(() => {
    return JSON.stringify(window.editor.store.serialize(), null, 2);
  });
  
  fs.writeFileSync('diagrams/clean_blank.json', snapshot);
  console.log('Got clean blank tldraw store!');
  
  await browser.close();
  server.close();
}

run().catch(console.error);
