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
        richText: makeRichText('Railway Monadic Operations: map vs flatMap'),
        color: 'black',
        fill: 'none',
        w: 800,
        h: 50,
        size: 'm',
        font: 'draw',
        align: 'start',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });

    // --- Section 1: MAP ---
    const mapY = 120;
    editor.createShape({
      type: 'geo',
      x: 80,
      y: mapY,
      props: {
        geo: 'rectangle',
        richText: makeRichText('MAP operator: Shares the identical decision tree switch on Ok/Err.\nThe operator wraps the mapper\'s raw output U in Ok(U).'),
        color: 'black',
        fill: 'none',
        w: 850,
        h: 50,
        size: 's',
        font: 'draw',
        align: 'start',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });

    // Success line (green)
    editor.createShape({
      type: 'arrow',
      x: 0,
      y: 0,
      props: {
        color: 'green',
        fill: 'none',
        dash: 'draw',
        size: 's',
        start: { x: 80, y: mapY + 110 },
        end: { x: 930, y: mapY + 110 },
        arrowheadStart: 'none',
        arrowheadEnd: 'arrow',
        bend: 0
      }
    });
    // Labels on success track
    editor.createShape({
      type: 'geo',
      x: 120,
      y: mapY + 80,
      props: {
        geo: 'rectangle',
        richText: makeRichText('Ok(T)'),
        color: 'green',
        fill: 'semi',
        w: 120,
        h: 50,
        size: 's',
        font: 'draw',
        align: 'middle',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });
    // Map function block on success track
    editor.createShape({
      type: 'geo',
      x: 400,
      y: mapY + 80,
      props: {
        geo: 'rectangle',
        richText: makeRichText('map(T ➡️ U)'),
        color: 'blue',
        fill: 'semi',
        w: 180,
        h: 50,
        size: 's',
        font: 'draw',
        align: 'middle',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });
    // Output label on success track
    editor.createShape({
      type: 'geo',
      x: 750,
      y: mapY + 80,
      props: {
        geo: 'rectangle',
        richText: makeRichText('Ok(U)'),
        color: 'green',
        fill: 'semi',
        w: 120,
        h: 50,
        size: 's',
        font: 'draw',
        align: 'middle',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });

    // Failure line (red)
    editor.createShape({
      type: 'arrow',
      x: 0,
      y: 0,
      props: {
        color: 'red',
        fill: 'none',
        dash: 'dashed',
        size: 's',
        start: { x: 80, y: mapY + 190 },
        end: { x: 930, y: mapY + 190 },
        arrowheadStart: 'none',
        arrowheadEnd: 'arrow',
        bend: 0
      }
    });
    editor.createShape({
      type: 'geo',
      x: 120,
      y: mapY + 160,
      props: {
        geo: 'ellipse',
        richText: makeRichText('Err(E)'),
        color: 'red',
        fill: 'semi',
        w: 120,
        h: 50,
        size: 's',
        font: 'draw',
        align: 'middle',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });
    // Failure bypass label
    editor.createShape({
      type: 'geo',
      x: 400,
      y: mapY + 160,
      props: {
        geo: 'rectangle',
        richText: makeRichText('Bypasses function'),
        color: 'grey',
        fill: 'none',
        w: 180,
        h: 50,
        size: 's',
        font: 'draw',
        align: 'middle',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });
    editor.createShape({
      type: 'geo',
      x: 750,
      y: mapY + 160,
      props: {
        geo: 'ellipse',
        richText: makeRichText('Err(E)'),
        color: 'red',
        fill: 'semi',
        w: 120,
        h: 50,
        size: 's',
        font: 'draw',
        align: 'middle',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });


    // --- Section 2: FLATMAP ---
    const flatY = 400;
    editor.createShape({
      type: 'geo',
      x: 80,
      y: flatY,
      props: {
        geo: 'rectangle',
        richText: makeRichText('FLATMAP operator: Shares the identical decision tree switch on Ok/Err.\nThe mapper function itself returns a Result<U, E> directly to avoid double-wrapping.'),
        color: 'black',
        fill: 'none',
        w: 850,
        h: 50,
        size: 's',
        font: 'draw',
        align: 'start',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });

    // Success Track (green)
    editor.createShape({
      type: 'arrow',
      x: 0,
      y: 0,
      props: {
        color: 'green',
        fill: 'none',
        dash: 'draw',
        size: 's',
        start: { x: 80, y: flatY + 110 },
        end: { x: 930, y: flatY + 110 },
        arrowheadStart: 'none',
        arrowheadEnd: 'arrow',
        bend: 0
      }
    });
    editor.createShape({
      type: 'geo',
      x: 120,
      y: flatY + 80,
      props: {
        geo: 'rectangle',
        richText: makeRichText('Ok(T)'),
        color: 'green',
        fill: 'semi',
        w: 120,
        h: 50,
        size: 's',
        font: 'draw',
        align: 'middle',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });
    // flatMap block
    editor.createShape({
      type: 'geo',
      x: 400,
      y: flatY + 80,
      props: {
        geo: 'rectangle',
        richText: makeRichText('flatMap(T ➡️ Result<U, E>)'),
        color: 'blue',
        fill: 'semi',
        w: 240,
        h: 50,
        size: 's',
        font: 'draw',
        align: 'middle',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });
    editor.createShape({
      type: 'geo',
      x: 750,
      y: flatY + 80,
      props: {
        geo: 'rectangle',
        richText: makeRichText('Ok(U)'),
        color: 'green',
        fill: 'semi',
        w: 120,
        h: 50,
        size: 's',
        font: 'draw',
        align: 'middle',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });

    // Failure Track (red)
    editor.createShape({
      type: 'arrow',
      x: 0,
      y: 0,
      props: {
        color: 'red',
        fill: 'none',
        dash: 'dashed',
        size: 's',
        start: { x: 80, y: flatY + 190 },
        end: { x: 930, y: flatY + 190 },
        arrowheadStart: 'none',
        arrowheadEnd: 'arrow',
        bend: 0
      }
    });
    editor.createShape({
      type: 'geo',
      x: 120,
      y: flatY + 160,
      props: {
        geo: 'ellipse',
        richText: makeRichText('Err(E)'),
        color: 'red',
        fill: 'semi',
        w: 120,
        h: 50,
        size: 's',
        font: 'draw',
        align: 'middle',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });
    // Add the bypasses function label on flatMap's red track
    editor.createShape({
      type: 'geo',
      x: 400,
      y: flatY + 160,
      props: {
        geo: 'rectangle',
        richText: makeRichText('Bypasses function'),
        color: 'grey',
        fill: 'none',
        w: 180,
        h: 50,
        size: 's',
        font: 'draw',
        align: 'middle',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });
    editor.createShape({
      type: 'geo',
      x: 750,
      y: flatY + 160,
      props: {
        geo: 'ellipse',
        richText: makeRichText('Err(E)'),
        color: 'red',
        fill: 'semi',
        w: 120,
        h: 50,
        size: 's',
        font: 'draw',
        align: 'middle',
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

  fs.writeFileSync('diagrams/map_flatmap_clean.tldr', tldrData);
  console.log('Successfully wrote diagrams/map_flatmap_clean.tldr via Puppeteer!');
  
  await browser.close();
  server.close();
}

run().catch(console.error);
