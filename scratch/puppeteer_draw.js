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
    
    // Title with 'draw' font
    editor.createShape({
      type: 'geo',
      x: 80,
      y: 40,
      props: {
        geo: 'rectangle',
        richText: makeRichText('Lending Pipeline Chain, Results & HTTP Pattern Matching'),
        color: 'black',
        fill: 'none',
        w: 850,
        h: 50,
        size: 'm',
        font: 'draw',
        align: 'start',
        verticalAlign: 'middle',
        dash: 'draw'
      }
    });

    const chain = [
      { id: 'step1', name: '1. findMember(Command)', x: 80, y: 120, result: 'Result<Member, MemberNotFound>', resColor: 'orange' },
      { id: 'step2', name: '2. checkOverdue(Member)', x: 80, y: 240, result: 'Result<Member, MemberHasOverdueBooks>', resColor: 'orange' },
      { id: 'step3', name: '3. checkMaximumLentNumber(Member)', x: 80, y: 360, result: 'Result<Member, MaximumLimitReached>', resColor: 'orange' },
      { id: 'step4', name: '4. findBookItemAndMember(Command, Member)', x: 80, y: 480, result: 'Result<Pair, BookNotFound>', resColor: 'orange' },
      { id: 'step5', name: '5. checkIfAlreadyLent(Pair)', x: 80, y: 600, result: 'Result<Pair, AlreadyLent>', resColor: 'orange' },
      { id: 'step6', name: '6. persistAndReturnResult(BookLending)', x: 80, y: 720, result: 'LendingResult.Success', resColor: 'green' }
    ];

    // Create Chain steps and Result boxes
    chain.forEach(item => {
      // Step box
      editor.createShape({
        id: 'shape:' + item.id,
        type: 'geo',
        x: item.x,
        y: item.y,
        props: {
          geo: 'rectangle',
          richText: makeRichText(item.name),
          color: 'blue',
          fill: 'semi',
          w: 380,
          h: 70,
          size: 's',
          font: 'draw',
          align: 'middle',
          verticalAlign: 'middle',
          dash: 'draw'
        }
      });

      // Result box
      editor.createShape({
        id: 'shape:res_' + item.id,
        type: 'geo',
        x: item.x + 480,
        y: item.y,
        props: {
          geo: 'rectangle',
          richText: makeRichText(item.result),
          color: item.resColor,
          fill: 'semi',
          w: 360,
          h: 70,
          size: 's',
          font: 'draw',
          align: 'middle',
          verticalAlign: 'middle',
          dash: 'draw'
        }
      });

      // Arrow from Step to Result
      editor.createShape({
        type: 'arrow',
        x: 0,
        y: 0,
        props: {
          color: 'grey',
          fill: 'none',
          dash: 'draw',
          size: 's',
          start: { x: item.x + 380, y: item.y + 35 },
          end: { x: item.x + 480, y: item.y + 35 },
          arrowheadStart: 'none',
          arrowheadEnd: 'arrow',
          bend: 0
        }
      });
    });

    // Create vertical flow arrows down the chain
    for (let i = 0; i < chain.length - 1; i++) {
      editor.createShape({
        type: 'arrow',
        x: 0,
        y: 0,
        props: {
          color: 'blue',
          fill: 'none',
          dash: 'draw',
          size: 's',
          start: { x: chain[i].x + 190, y: chain[i].y + 70 },
          end: { x: chain[i+1].x + 190, y: chain[i+1].y },
          arrowheadStart: 'none',
          arrowheadEnd: 'arrow',
          bend: 0
        }
      });
    }

  });

  const tldrData = await page.evaluate(() => {
    return JSON.stringify({
      tldrawFileFormatVersion: 1,
      schema: window.editor.store.schema ? window.editor.store.schema.serialize() : { schemaVersion: 2, sequences: {} },
      records: Object.values(window.editor.store.serialize())
    }, null, 2);
  });

  fs.writeFileSync('diagrams/railway_lending_clean.tldr', tldrData);
  console.log('Successfully wrote diagrams/railway_lending_clean.tldr via Puppeteer!');
  
  await browser.close();
  server.close();
}

run().catch(console.error);
