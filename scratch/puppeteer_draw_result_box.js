import puppeteer from 'puppeteer';
import fs from 'fs';
import { serve } from '@hono/node-server';
import { Hono } from 'hono';
import { serveStatic } from '@hono/node-server/serve-static';
import getPort from 'get-port';
import path from 'path';

async function run() {
  const tldrawDist = path.join('/mydata/.nvm/versions/node/v22.14.0/lib/node_modules/@kitschpatrol/tldraw-cli/dist/tldraw');

  const app = new Hono();
  const port = await getPort();
  
  app.get('/tldr-data', c => c.text('', 200));
  app.use('/*', serveStatic({ root: tldrawDist }));
  
  const server = serve({ fetch: app.fetch, port });
  
  // Wait for server
  await new Promise(resolve => setTimeout(resolve, 500));
  
  const browser = await puppeteer.launch({ headless: true });
  const context = await browser.createBrowserContext();
  const page = await context.newPage();
  
  await page.goto(`http://localhost:${port}`);
  await page.evaluate(() => {
    localStorage.clear();
    sessionStorage.clear();
    if (window.indexedDB && window.indexedDB.databases) {
      window.indexedDB.databases().then(dbs => {
        dbs.forEach(db => window.indexedDB.deleteDatabase(db.name));
      });
    }
  });
  
  await page.reload();
  await page.waitForFunction(() => window.editor !== undefined);
  
  await page.evaluate(() => {
    const editor = window.editor;
    
    const shapes = editor.getCurrentPageShapes();
    if (shapes.length > 0) {
      editor.deleteShapes(shapes.map(s => s.id));
    }

    function makeRichText(text) {
      return {
        type: "doc",
        content: [
          {
            type: "paragraph",
            content: [
              {
                type: "text",
                text: text
              }
            ]
          }
        ]
      };
    }
    
    // Main Title
    editor.createShape({
      type: 'geo',
      x: 80,
      y: 40,
      props: {
        geo: 'rectangle',
        richText: makeRichText('The Monadic Container: Result<T, E> in Java 21'),
        color: 'black',
        fill: 'none',
        w: 750,
        h: 50,
        size: 'm',
        font: 'draw',
        align: 'start',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });

    // --- ROOT CONTAINER: Result<T, E> ---
    const rootX = 320;
    const rootY = 140;

    editor.createShape({
      id: 'shape:root_box',
      type: 'geo',
      x: rootX,
      y: rootY,
      props: {
        geo: 'rectangle',
        richText: makeRichText('sealed interface Result<T, E>'),
        color: 'violet',
        fill: 'semi',
        w: 320,
        h: 80,
        size: 'm',
        font: 'draw',
        align: 'middle',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });

    // --- LEFT BRANCH: Ok<T, E> ---
    const okX = 100;
    const okY = 320;

    editor.createShape({
      id: 'shape:ok_box',
      type: 'geo',
      x: okX,
      y: okY,
      props: {
        geo: 'rectangle',
        richText: makeRichText('record Ok<T, E>(T value)\n\n🟢 Success Track\nCarries the successful result'),
        color: 'green',
        fill: 'semi',
        w: 280,
        h: 120,
        size: 's',
        font: 'draw',
        align: 'middle',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });

    // Arrow from Root to Ok
    editor.createShape({
      type: 'arrow',
      x: 0,
      y: 0,
      props: {
        color: 'green',
        fill: 'none',
        dash: 'draw',
        size: 'm',
        start: { x: rootX + 80, y: rootY + 80 },
        end: { x: okX + 140, y: okY },
        arrowheadStart: 'none',
        arrowheadEnd: 'arrow',
        bend: 0
      }
    });

    // --- RIGHT BRANCH: Err<T, E> ---
    const errX = 580;
    const errY = 320;

    editor.createShape({
      id: 'shape:err_box',
      type: 'geo',
      x: errX,
      y: errY,
      props: {
        geo: 'rectangle',
        richText: makeRichText('record Err<T, E>(E error)\n\n🔴 Failure Track\nCarries rich domain error payload'),
        color: 'red',
        fill: 'semi',
        w: 280,
        h: 120,
        size: 's',
        font: 'draw',
        align: 'middle',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });

    // Arrow from Root to Err
    editor.createShape({
      type: 'arrow',
      x: 0,
      y: 0,
      props: {
        color: 'red',
        fill: 'none',
        dash: 'draw',
        size: 'm',
        start: { x: rootX + 240, y: rootY + 80 },
        end: { x: errX + 140, y: errY },
        arrowheadStart: 'none',
        arrowheadEnd: 'arrow',
        bend: 0
      }
    });

    // --- BOTTOM FOOTNOTE: Compile-time check ---
    editor.createShape({
      type: 'geo',
      x: 100,
      y: 490,
      props: {
        geo: 'rectangle',
        richText: makeRichText('💡 Java 21 Sealed Rules:\n- Only Ok and Err can implement Result.\n- Allows exhaustive pattern-matching without throwing exceptions.\n- Promotes functional railway design patterns.'),
        color: 'black',
        fill: 'none',
        w: 760,
        h: 80,
        size: 's',
        font: 'draw',
        align: 'start',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });

  });

  const tldrData = await page.evaluate(() => {
    return JSON.stringify({
      tldrawFileFormatVersion: 1,
      schema: window.editor.store.schema ? window.editor.store.schema.serialize() : { schemaVersion: 2, sequences: {} },
      records: Object.values(window.editor.store.serialize())
    }, null, 2);
  });

  fs.writeFileSync('diagrams/result_box_clean.tldr', tldrData);
  console.log('Successfully wrote diagrams/result_box_clean.tldr via Puppeteer!');
  
  await browser.close();
  server.close();
}

run().catch(console.error);
