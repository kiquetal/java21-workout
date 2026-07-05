import puppeteer from 'puppeteer';
import path from 'path';
import { serve } from '@hono/node-server';
import { Hono } from 'hono';
import { serveStatic } from '@hono/node-server/serve-static';
import getPort from 'get-port';

async function run() {
  const tldrawDist = path.join('/mydata/.nvm/versions/node/v22.14.0/lib/node_modules/@kitschpatrol/tldraw-cli/dist/tldraw');
  const app = new Hono();
  const port = await getPort();
  app.use('/*', serveStatic({ root: tldrawDist }));
  const server = serve({ fetch: app.fetch, port });
  
  await new Promise(resolve => setTimeout(resolve, 500));
  const browser = await puppeteer.launch({ headless: true });
  const page = await browser.newPage();
  
  await page.goto(`http://localhost:${port}`);
  await page.waitForFunction(() => window.editor !== undefined);
  
  const validationTest = await page.evaluate(() => {
    try {
      const editor = window.editor;
      
      // Let's see what happens if we pass different fonts to tldraw
      // and catch validation errors to see what fonts are allowed.
      const results = {};
      const fontsToTest = ['draw', 'sans', 'serif', 'mono', 'painter', 'script', 'comic'];
      
      for (const font of fontsToTest) {
        try {
          const s = editor.createShape({
            type: 'geo',
            props: { font }
          });
          results[font] = { success: true };
          editor.deleteShapes([s.id]);
        } catch (err) {
          results[font] = { success: false, error: err.message };
        }
      }
      return results;
    } catch (err) {
      return { error: err.message };
    }
  });
  
  console.log('VALIDATION RESULTS:', JSON.stringify(validationTest, null, 2));
  await browser.close();
  server.close();
}
run().catch(console.error);
