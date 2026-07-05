const fs = require('fs');

const tldr = {
  "tldrawFileFormatVersion": 1,
  "schema": {
    "schemaVersion": 2,
    "sequences": {
      "com.tldraw.store": 4,
      "com.tldraw.asset": 1,
      "com.tldraw.camera": 1,
      "com.tldraw.document": 2,
      "com.tldraw.instance": 22,
      "com.tldraw.instance_page_state": 5,
      "com.tldraw.page": 1,
      "com.tldraw.instance_presence": 5,
      "com.tldraw.pointer": 1,
      "com.tldraw.shape": 4,
      "com.tldraw.user": 1,
      "com.tldraw.asset.image": 2,
      "com.tldraw.asset.video": 2,
      "com.tldraw.asset.bookmark": 0,
      "com.tldraw.shape.group": 0,
      "com.tldraw.shape.text": 1,
      "com.tldraw.shape.bookmark": 0,
      "com.tldraw.shape.draw": 1,
      "com.tldraw.shape.geo": 3,
      "com.tldraw.shape.note": 4,
      "com.tldraw.shape.line": 0,
      "com.tldraw.shape.frame": 0,
      "com.tldraw.shape.arrow": 1,
      "com.tldraw.shape.highlight": 0,
      "com.tldraw.shape.embed": 4,
      "com.tldraw.shape.image": 2,
      "com.tldraw.shape.video": 1,
      "com.tldraw.binding.arrow": 0
    }
  },
  "records": [
    {
      "gridSize": 10,
      "name": "Railway Lending Flow",
      "meta": {},
      "id": "document:document",
      "typeName": "document"
    },
    {
      "id": "page:page",
      "name": "Page 1",
      "index": "a1",
      "meta": {},
      "typeName": "page"
    }
  ]
};

// Title
tldr.records.push({
  "x": 100,
  "y": 50,
  "id": "shape:title",
  "type": "text",
  "parentId": "page:page",
  "index": "a0",
  "props": {
    "text": "Railway Oriented Programming - Bookstore Lending Flow",
    "size": "l",
    "color": "black",
    "font": "sans"
  },
  "typeName": "shape"
});

const steps = [
  { id: "step1", name: "1. findMember", color: "blue", geo: "rectangle", x: 100, y: 150 },
  { id: "step2", name: "2. checkOverdue", color: "blue", geo: "rectangle", x: 380, y: 150 },
  { id: "step3", name: "3. checkLimit", color: "blue", geo: "rectangle", x: 660, y: 150 },
  { id: "step4", name: "4. findBook", color: "blue", geo: "rectangle", x: 940, y: 150 },
  { id: "step5", name: "5. checkIfAlreadyLent", color: "blue", geo: "rectangle", x: 1220, y: 150 },
  { id: "step6", name: "Success", color: "green", geo: "rectangle", x: 1500, y: 150 }
];

const errors = [
  { id: "err1", name: "MemberNotFound", color: "red", geo: "ellipse", x: 100, y: 300 },
  { id: "err2", name: "MemberHasOverdueBooks", color: "red", geo: "ellipse", x: 380, y: 300 },
  { id: "err3", name: "MaximumLimitReached", color: "red", geo: "ellipse", x: 660, y: 300 },
  { id: "err4", name: "BookNotFound", color: "red", geo: "ellipse", x: 940, y: 300 },
  { id: "err5", name: "AlreadyLent", color: "red", geo: "ellipse", x: 1220, y: 300 }
];

// Add step shapes
steps.forEach((s, idx) => {
  tldr.records.push({
    "x": s.x,
    "y": s.y,
    "id": "shape:" + s.id,
    "type": "geo",
    "parentId": "page:page",
    "index": "a1" + idx,
    "props": {
      "geo": s.geo,
      "text": s.name,
      "color": s.color,
      "fill": "semi",
      "w": 200,
      "h": 80,
      "size": "s",
      "font": "sans"
    },
    "typeName": "shape"
  });
});

// Add error shapes
errors.forEach((e, idx) => {
  tldr.records.push({
    "x": e.x,
    "y": e.y,
    "id": "shape:" + e.id,
    "type": "geo",
    "parentId": "page:page",
    "index": "a2" + idx,
    "props": {
      "geo": e.geo,
      "text": e.name,
      "color": e.color,
      "fill": "semi",
      "w": 200,
      "h": 80,
      "size": "s",
      "font": "sans"
    },
    "typeName": "shape"
  });
});

// Add horizontal arrows connecting steps
for (let i = 0; i < steps.length - 1; i++) {
  tldr.records.push({
    "id": `shape:arrow_step_${i}`,
    "type": "arrow",
    "parentId": "page:page",
    "index": `a3${i}`,
    "x": 0,
    "y": 0,
    "props": {
      "start": { "x": steps[i].x + 200, "y": steps[i].y + 40 },
      "end": { "x": steps[i+1].x, "y": steps[i+1].y + 40 },
      "color": "black",
      "arrowheadEnd": "arrow"
    },
    "typeName": "shape"
  });
}

// Add vertical arrows connecting steps to errors
for (let i = 0; i < errors.length; i++) {
  tldr.records.push({
    "id": `shape:arrow_err_${i}`,
    "type": "arrow",
    "parentId": "page:page",
    "index": `a4${i}`,
    "x": 0,
    "y": 0,
    "props": {
      "start": { "x": steps[i].x + 100, "y": steps[i].y + 80 },
      "end": { "x": errors[i].x + 100, "y": errors[i].y },
      "color": "grey",
      "dash": "dashed",
      "arrowheadEnd": "arrow"
    },
    "typeName": "shape"
  });
}

fs.writeFileSync('diagrams/railway_lending_v5.tldr', JSON.stringify(tldr, null, 2));
console.log('Successfully wrote diagrams/railway_lending_v5.tldr');
