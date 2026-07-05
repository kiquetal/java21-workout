const fs = require('fs');

const store = JSON.parse(fs.readFileSync('diagrams/clean_blank.json', 'utf8'));

// Title
store["shape:title"] = {
  "id": "shape:title",
  "typeName": "shape",
  "parentId": "page:page",
  "index": "a0",
  "x": 80,
  "y": 40,
  "rotation": 0,
  "isLocked": false,
  "opacity": 1,
  "meta": {},
  "type": "text",
  "props": {
    "color": "black",
    "size": "l",
    "font": "draw",
    "text": "Railway Oriented Programming (ROP) - Bookstore Lending Flow",
    "align": "start",
    "verticalAlign": "middle",
    "growY": 0,
    "url": "",
    "w": 800
  }
};

const steps = [
  { id: "step1", name: "1. findMember", color: "blue", geo: "rectangle", x: 80, y: 150 },
  { id: "step2", name: "2. checkOverdue", color: "blue", geo: "rectangle", x: 360, y: 150 },
  { id: "step3", name: "3. checkLimit", color: "blue", geo: "rectangle", x: 640, y: 150 },
  { id: "step4", name: "4. findBook", color: "blue", geo: "rectangle", x: 920, y: 150 },
  { id: "step5", name: "5. checkIfAlreadyLent", color: "blue", geo: "rectangle", x: 1200, y: 150 },
  { id: "step6", name: "Success Track\n(Ok / Success)", color: "green", geo: "rectangle", x: 1480, y: 150 }
];

const errors = [
  { id: "err1", name: "MemberNotFound", color: "red", geo: "ellipse", x: 80, y: 320 },
  { id: "err2", name: "MemberHasOverdueBooks", color: "red", geo: "ellipse", x: 360, y: 320 },
  { id: "err3", name: "BookItemNotAvailable", color: "red", geo: "ellipse", x: 640, y: 320 },
  { id: "err4", name: "BookNotFound", color: "red", geo: "ellipse", x: 920, y: 320 },
  { id: "err5", name: "AlreadyLent", color: "red", geo: "ellipse", x: 1200, y: 320 }
];

// Add step shapes
steps.forEach((s, idx) => {
  store["shape:" + s.id] = {
    "id": "shape:" + s.id,
    "typeName": "shape",
    "parentId": "page:page",
    "index": "a1" + idx,
    "x": s.x,
    "y": s.y,
    "rotation": 0,
    "isLocked": false,
    "opacity": 1,
    "meta": {},
    "type": "geo",
    "props": {
      "geo": s.geo,
      "text": s.name,
      "color": s.color,
      "fill": "semi",
      "w": 220,
      "h": 90,
      "size": "s",
      "font": "draw",
      "align": "middle",
      "verticalAlign": "middle",
      "growY": 0,
      "url": "",
      "dash": "solid"
    }
  };
});

// Add error shapes
errors.forEach((e, idx) => {
  store["shape:" + e.id] = {
    "id": "shape:" + e.id,
    "typeName": "shape",
    "parentId": "page:page",
    "index": "a2" + idx,
    "x": e.x,
    "y": e.y,
    "rotation": 0,
    "isLocked": false,
    "opacity": 1,
    "meta": {},
    "type": "geo",
    "props": {
      "geo": e.geo,
      "text": e.name,
      "color": e.color,
      "fill": "semi",
      "w": 220,
      "h": 90,
      "size": "s",
      "font": "draw",
      "align": "middle",
      "verticalAlign": "middle",
      "growY": 0,
      "url": "",
      "dash": "solid"
    }
  };
});

// Add horizontal arrows (Success Track)
for (let i = 0; i < steps.length - 1; i++) {
  const id = `arrow_step_${i}`;
  store["shape:" + id] = {
    "id": "shape:" + id,
    "typeName": "shape",
    "parentId": "page:page",
    "index": "a3" + i,
    "x": 0,
    "y": 0,
    "rotation": 0,
    "isLocked": false,
    "opacity": 1,
    "meta": {},
    "type": "arrow",
    "props": {
      "color": "green",
      "fill": "none",
      "dash": "solid",
      "size": "s",
      "start": { "x": steps[i].x + 220, "y": steps[i].y + 45 },
      "end": { "x": steps[i+1].x, "y": steps[i+1].y + 45 },
      "arrowheadStart": "none",
      "arrowheadEnd": "arrow",
      "bend": 0
    }
  };
}

// Add vertical arrows (Failure Track)
for (let i = 0; i < errors.length; i++) {
  const id = `arrow_err_${i}`;
  store["shape:" + id] = {
    "id": "shape:" + id,
    "typeName": "shape",
    "parentId": "page:page",
    "index": "a4" + i,
    "x": 0,
    "y": 0,
    "rotation": 0,
    "isLocked": false,
    "opacity": 1,
    "meta": {},
    "type": "arrow",
    "props": {
      "color": "red",
      "fill": "none",
      "dash": "dashed",
      "size": "s",
      "start": { "x": steps[i].x + 110, "y": steps[i].y + 90 },
      "end": { "x": errors[i].x + 110, "y": errors[i].y },
      "arrowheadStart": "none",
      "arrowheadEnd": "arrow",
      "bend": 0
    }
  };
}

const tldrFile = {
  "tldrawFileFormatVersion": 1,
  "schema": {
    "schemaVersion": 2,
    "sequences": {
      "com.tldraw.store": 5,
      "com.tldraw.asset": 1,
      "com.tldraw.camera": 1,
      "com.tldraw.document": 2,
      "com.tldraw.instance": 26,
      "com.tldraw.instance_page_state": 5,
      "com.tldraw.page": 1,
      "com.tldraw.instance_presence": 6,
      "com.tldraw.pointer": 1,
      "com.tldraw.shape": 4,
      "com.tldraw.user": 1,
      "com.tldraw.asset.image": 6,
      "com.tldraw.asset.video": 5,
      "com.tldraw.asset.bookmark": 2,
      "com.tldraw.shape.group": 0,
      "com.tldraw.shape.text": 4,
      "com.tldraw.shape.bookmark": 2,
      "com.tldraw.shape.draw": 5,
      "com.tldraw.shape.geo": 11,
      "com.tldraw.shape.note": 13,
      "com.tldraw.shape.line": 5,
      "com.tldraw.shape.frame": 1,
      "com.tldraw.shape.arrow": 8,
      "com.tldraw.shape.highlight": 4,
      "com.tldraw.shape.embed": 4,
      "com.tldraw.shape.image": 5,
      "com.tldraw.shape.video": 4,
      "com.tldraw.binding.arrow": 1
    }
  },
  "records": Object.values(store)
};

fs.writeFileSync('diagrams/railway_lending_clean.tldr', JSON.stringify(tldrFile, null, 2));
console.log('Successfully wrote diagrams/railway_lending_clean.tldr!');
