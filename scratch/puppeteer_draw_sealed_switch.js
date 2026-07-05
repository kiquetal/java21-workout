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
        richText: makeRichText('Java 21: Sealed Interface & Pattern Matching Switch'),
        color: 'black',
        fill: 'none',
        w: 900,
        h: 50,
        size: 'm',
        font: 'draw',
        align: 'start',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });

    // --- LEFT COLUMN: Sealed Hierarchy ---
    const leftX = 80;
    const hierarchyY = 130;
    
    editor.createShape({
      id: 'shape:sealed_title',
      type: 'geo',
      x: leftX,
      y: hierarchyY,
      props: {
        geo: 'rectangle',
        richText: makeRichText('sealed interface LendingResult\n(Permits only defined subtypes)'),
        color: 'violet',
        fill: 'semi',
        w: 380,
        h: 60,
        size: 's',
        font: 'draw',
        align: 'middle',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });

    const subtypes = [
      { name: 'Success(LendingDetail)', y: hierarchyY + 90, color: 'green' },
      { name: 'AlreadyLent(LendingDetail)', y: hierarchyY + 150, color: 'orange' },
      { name: 'MemberNotFound(MemberId)', y: hierarchyY + 210, color: 'orange' },
      { name: 'BookNotFound(BookItemId)', y: hierarchyY + 270, color: 'orange' },
      { name: 'MemberHasOverdueBooks(MemberId, ...)', y: hierarchyY + 330, color: 'orange' },
      { name: 'MaximumLimitReached(MemberId)', y: hierarchyY + 390, color: 'orange' }
    ];

    subtypes.forEach((sub, idx) => {
      editor.createShape({
        id: `shape:sub_${idx}`,
        type: 'geo',
        x: leftX + 40,
        y: sub.y,
        props: {
          geo: 'rectangle',
          richText: makeRichText(sub.name),
          color: sub.color,
          fill: 'semi',
          w: 300,
          h: 45,
          size: 's',
          font: 'draw',
          align: 'middle',
          verticalAlign: 'middle',
          dash: 'draw'
        }
      });

      // Branch arrow from title to subtype
      editor.createShape({
        type: 'arrow',
        x: 0,
        y: 0,
        props: {
          color: 'violet',
          fill: 'none',
          dash: 'draw',
          size: 's',
          start: { x: leftX + 20, y: hierarchyY + 60 },
          end: { x: leftX + 40, y: sub.y + 22 },
          arrowheadStart: 'none',
          arrowheadEnd: 'arrow',
          bend: 0
        }
      });
    });


    // --- MIDDLE COLUMN: Pattern Matching Switch Expression ---
    const midX = 520;
    const switchY = 220;

    editor.createShape({
      id: 'shape:switch_box',
      type: 'geo',
      x: midX,
      y: switchY,
      props: {
        geo: 'rectangle',
        richText: makeRichText('switch (result)\n(Type-Safe Switch)'),
        color: 'blue',
        fill: 'semi',
        w: 240,
        h: 220,
        size: 's',
        font: 'draw',
        align: 'middle',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });

    // Flow arrow from Sealed subtypes to Switch Box
    editor.createShape({
      type: 'arrow',
      x: 0,
      y: 0,
      props: {
        color: 'blue',
        fill: 'none',
        dash: 'draw',
        size: 'm',
        start: { x: leftX + 340, y: hierarchyY + 240 },
        end: { x: midX, y: switchY + 110 },
        arrowheadStart: 'none',
        arrowheadEnd: 'arrow',
        bend: 0
      }
    });


    // --- RIGHT COLUMN: HTTP Status Mappings (Record Pattern Destruction) ---
    const rightX = 820;
    const mapY = 130;

    const mappings = [
      { text: 'case Success(var detail)\n➡️ Response.ok() (200)', color: 'green', y: mapY },
      { text: 'case AlreadyLent(var detail)\n➡️ status(409) Conflict', color: 'red', y: mapY + 100 },
      { text: 'case MemberNotFound | BookNotFound\n➡️ status(404) Not Found', color: 'red', y: mapY + 200 },
      { text: 'case MemberHasOverdueBooks\n➡️ status(403) Forbidden', color: 'red', y: mapY + 300 },
      { text: 'case MaximumLimitReached\n➡️ status(403) Forbidden', color: 'red', y: mapY + 400 }
    ];

    mappings.forEach((m, idx) => {
      editor.createShape({
        id: `shape:map_${idx}`,
        type: 'geo',
        x: rightX,
        y: m.y,
        props: {
          geo: 'rectangle',
          richText: makeRichText(m.text),
          color: m.color,
          fill: 'semi',
          w: 320,
          h: 70,
          size: 's',
          font: 'draw',
          align: 'middle',
          verticalAlign: 'middle',
          dash: 'draw'
        }
      });

      // Arrow from switch box to HTTP response mapping
      editor.createShape({
        type: 'arrow',
        x: 0,
        y: 0,
        props: {
          color: 'blue',
          fill: 'none',
          dash: 'draw',
          size: 's',
          start: { x: midX + 240, y: switchY + 110 },
          end: { x: rightX, y: m.y + 35 },
          arrowheadStart: 'none',
          arrowheadEnd: 'arrow',
          bend: 0
        }
      });
    });


    // --- FOOTER NOTE: Highlight key benefits ---
    editor.createShape({
      type: 'geo',
      x: leftX,
      y: hierarchyY + 480,
      props: {
        geo: 'rectangle',
        richText: makeRichText('💡 COMPILER EXHAUSTIVENESS CHECK:\nIf you add a new LendingResult subtype, the switch expression will fail to compile until you explicitly map it.\nNo default case needed! No unhandled errors! Record fields are deconstructed directly (Record Patterns).'),
        color: 'black',
        fill: 'none',
        w: 1060,
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

  fs.writeFileSync('diagrams/sealed_switch_clean.tldr', tldrData);
  console.log('Successfully wrote diagrams/sealed_switch_clean.tldr via Puppeteer!');
  
  await browser.close();
  server.close();
}

run().catch(console.error);
