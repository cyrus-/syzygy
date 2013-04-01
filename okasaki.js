// Constants
var ELEMENT_NODE = 1;
var TEXT_NODE = 3;
var baseURL = "index.html";

var body = document.body;
var editor = document.getElementById('editor');

function newEditor(position, lines) {
	var url = makeURL(position, lines);
	return window.open(url);
}

function makeURL(position, lines) {
	return [baseURL, makeHash(position, lines)].join("#");
}

function makeHash(position, lines) {
	return [position[0], "&",
		    position[1], "&",
		    encodeLines(lines)].join("");
}

function parseHash(hash) {
	var position = hash.split("&");
	var contents = decodeLines(position.pop());
	return [position, contents];
}

function encodeLines(lines) {
	return encodeURIComponent(lines.join("\n")).replace(/%C2%A0/g, "%20");
}

function decodeLines(hashed) {
	return decodeURIComponent(hashed).split("\n");
}

function getLines() {
	var children = editor.childNodes;
	var lines = [];
	for (var i=0; i < children.length; i++) {
		var child = children[i];
		if (child.nodeType == ELEMENT_NODE) {
			if (children[i].firstChild.tagName == "BR") {
				// turn empty lines into empty strings
				lines.push("");
			} else {
				lines.push(children[i].innerText);
			}
		}
	}
	return lines;
}

function populate(parent, lines) {
	parent.innerHTML = "";
	for (var i=0; i < lines.length; ++i) {
		var line = lines[i].replace(/ /g, "&nbsp;");
		var lineElem = document.createElement("div");
		if (line == "") {
			lineElem.innerHTML = "<br>";
		} else {
			lineElem.innerHTML = line;
		}
		parent.appendChild(lineElem);
	}
}

function getLineOf(e) {
	var children = editor.childNodes;
	var line = 0;
	for (var i=0; i < children.length; i++) {
		if (children[i].nodeType == ELEMENT_NODE) {
			if (children[i] == e) return line;
			line++
		}
	}

	throw "Node not found."
}

function getLineElem(n) {
	var children = editor.childNodes;
	var line = 0;
	for (var i=0; i < children.length; i++) {
		var child = children[i];
		if (child.nodeType == ELEMENT_NODE) {
			if (line++ == n) {
				return child;
			}
		}
	}

	throw "Line not found: " + n + "."
}

function getPosition() {
	var sel = window.getSelection();
	var col = sel.focusOffset;
	var parDiv = sel.focusNode;
	if (parDiv.nodeType == TEXT_NODE) {
		// If it is a text node, get its parent.
		parDiv = parDiv.parentNode;
	}
	var line = getLineOf(parDiv);
	return [line, col];
}

function makeRange(lineElem, col) {
	var range = document.createRange();
	var child = lineElem.firstChild;
	range.setStart(child, col);
	//range.setEnd(child, col);
	console.log(range);
	return range;
}

function setPosition(position) {
	var line = position[0];
	var lineElem = getLineElem(line);
	var col = position[1];

	var sel = window.getSelection();
	sel.removeAllRanges();
	sel.addRange(makeRange(lineElem, col));
}

var hash = window.location.hash.substring(1);
var positionState = [0, 0];
if (hash != "") {
	parsedHash = parseHash(hash);
	populate(editor, parsedHash[1]);
	console.log(parsedHash[1]);
	positionState = parsedHash[0];
	setPosition(positionState);
}

var editorState = editor.cloneNode(true);

editor.addEventListener('input', function(e) {
	var position = getPosition();
	var lines = getLines();

	console.log(position);
	console.log(lines);
	
	// open a new window
	var w = newEditor(position, lines);

	// restore initial state of editor
	editor.innerHTML = editorState.innerHTML;
	setPosition(positionState);
}, true);
