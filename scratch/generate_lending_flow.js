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
    },
    {
      "id": "shape:step1",
      "type": "geo",
      "parentId": "page:page",
      "index": "a1",
      "x": 100,
      "y": 100,
      "rotation": 0,
      "isLocked": false,
      "opacity": 1,
      "meta": {},
      "props": {
        "geo": "rectangle",
        "w": 200,
        "h": 80,
        "size": "m",
        "color": "blue",
        "text": "findMember",
        "font": "draw",
        "align": "middle",
        "verticalAlign": "middle",
        "growY": 0,
        "url": "",
        "dash": "solid",
        "fill": "none"
      },
      "typeName": "shape"
    }
  ]
};

fs.writeFileSync('diagrams/railway_lending_clean.tldr', JSON.stringify(tldr, null, 2));
console.log('Successfully wrote diagrams/railway_lending_clean.tldr');
