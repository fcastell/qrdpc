// Ensure JSON payloads with non-ASCII characters (e.g. accented text in string
// values) are encoded correctly. Reassigning stringToBytesFuncs['default'] alone is
// NOT enough: qr8BitByte reads the already-bound `qrcode.stringToBytes` reference
// (captured at library load time), so it must be reassigned directly.
qrcode.stringToBytes = qrcode.stringToBytesFuncs["UTF-8"];

const SCHEMA_VERSION = 1;
const STORAGE_KEY = "qrdpc-generator-last-config";

const restrictionsList = document.getElementById("restrictions-list");
const addRestrictionBtn = document.getElementById("add-restriction");
const rowTemplate = document.getElementById("restriction-row-template");
const packageNameInput = document.getElementById("package-name");
const generateBtn = document.getElementById("generate");
const errorsEl = document.getElementById("errors");
const outputEl = document.getElementById("output");
const qrContainer = document.getElementById("qr-container");
const byteCountEl = document.getElementById("byte-count");
const jsonPreviewEl = document.getElementById("json-preview-content");
const downloadPngBtn = document.getElementById("download-png");
const downloadSvgBtn = document.getElementById("download-svg");
const copyJsonBtn = document.getElementById("copy-json");

let rowCounter = 0;
let lastPayloadJson = null;
let lastQr = null;

function addRestrictionRow(initial) {
  const fragment = rowTemplate.content.cloneNode(true);
  const row = fragment.querySelector("[data-row]");
  row.dataset.rowId = String(rowCounter++);

  const keyInput = row.querySelector('[data-field="key"]');
  const typeSelect = row.querySelector('[data-field="type"]');
  const valueContainer = row.querySelector("[data-value-container]");
  const removeBtn = row.querySelector('[data-action="remove"]');

  if (initial) {
    keyInput.value = typeof initial.key === "string" ? initial.key : "";
    if (typeof initial.type === "string") {
      typeSelect.value = initial.type;
    }
  }

  renderValueControl(typeSelect.value, valueContainer);
  if (initial) {
    setRowValue(typeSelect.value, valueContainer, initial.value);
  }

  typeSelect.addEventListener("change", () => {
    renderValueControl(typeSelect.value, valueContainer);
  });

  removeBtn.addEventListener("click", () => {
    row.remove();
    saveState();
  });

  restrictionsList.appendChild(fragment);
}

function setRowValue(type, container, value) {
  const valueEl = container.querySelector('[data-field="value"]');
  if (!valueEl) return;
  if (type === "bool") {
    valueEl.checked = Boolean(value);
  } else {
    valueEl.value = typeof value === "string" ? value : "";
  }
}

function renderValueControl(type, container) {
  container.innerHTML = "";

  switch (type) {
    case "bool": {
      const wrapper = document.createElement("label");
      wrapper.className = "checkbox-field";
      const input = document.createElement("input");
      input.type = "checkbox";
      input.dataset.field = "value";
      const span = document.createElement("span");
      span.textContent = "Enabled";
      wrapper.appendChild(input);
      wrapper.appendChild(span);
      container.appendChild(wrapper);
      break;
    }
    case "integer": {
      const input = document.createElement("input");
      input.type = "number";
      input.step = "1";
      input.dataset.field = "value";
      input.placeholder = "42";
      container.appendChild(input);
      break;
    }
    case "multi-select": {
      const input = document.createElement("input");
      input.type = "text";
      input.dataset.field = "value";
      input.placeholder = "value-a, value-b, value-c";
      input.autocomplete = "off";
      container.appendChild(input);
      break;
    }
    case "bundle": {
      const textarea = document.createElement("textarea");
      textarea.dataset.field = "value";
      textarea.placeholder = '{"nestedKey": "nestedValue"}';
      container.appendChild(textarea);
      break;
    }
    case "bundle_array": {
      const textarea = document.createElement("textarea");
      textarea.dataset.field = "value";
      textarea.placeholder = '[{"host": "a"}, {"host": "b"}]';
      container.appendChild(textarea);
      break;
    }
    case "string":
    case "choice":
    default: {
      const input = document.createElement("input");
      input.type = "text";
      input.dataset.field = "value";
      input.placeholder = "value";
      input.autocomplete = "off";
      container.appendChild(input);
      break;
    }
  }
}

function parseRowValue(type, valueEl) {
  switch (type) {
    case "bool":
      return { ok: true, value: valueEl.checked };
    case "string":
    case "choice":
      return { ok: true, value: valueEl.value };
    case "integer": {
      const raw = valueEl.value.trim();
      if (raw === "" || !/^-?\d+$/.test(raw)) {
        return { ok: false, error: "must be an integer" };
      }
      return { ok: true, value: parseInt(raw, 10) };
    }
    case "multi-select": {
      const items = valueEl.value
        .split(",")
        .map((s) => s.trim())
        .filter((s) => s.length > 0);
      if (items.length === 0) {
        return { ok: false, error: "must have at least one comma-separated value" };
      }
      return { ok: true, value: items };
    }
    case "bundle": {
      let parsed;
      try {
        parsed = JSON.parse(valueEl.value);
      } catch (e) {
        return { ok: false, error: "must be valid JSON" };
      }
      if (typeof parsed !== "object" || parsed === null || Array.isArray(parsed)) {
        return { ok: false, error: "must be a JSON object" };
      }
      return { ok: true, value: parsed };
    }
    case "bundle_array": {
      let parsed;
      try {
        parsed = JSON.parse(valueEl.value);
      } catch (e) {
        return { ok: false, error: "must be valid JSON" };
      }
      if (!Array.isArray(parsed)) {
        return { ok: false, error: "must be a JSON array" };
      }
      const allObjects = parsed.every(
        (item) => typeof item === "object" && item !== null && !Array.isArray(item),
      );
      if (!allObjects) {
        return { ok: false, error: "every item must be a JSON object" };
      }
      return { ok: true, value: parsed };
    }
    default:
      return { ok: false, error: "unknown type" };
  }
}

function collectPayload() {
  const errors = [];
  const packageName = packageNameInput.value.trim();
  if (packageName === "") {
    errors.push("Package name is required.");
  }

  const restrictions = [];
  const seenKeys = new Set();
  const rows = restrictionsList.querySelectorAll("[data-row]");

  rows.forEach((row, index) => {
    const keyEl = row.querySelector('[data-field="key"]');
    const typeEl = row.querySelector('[data-field="type"]');
    const valueEl = row.querySelector('[data-field="value"]');
    const rowErrorEl = row.querySelector("[data-row-error]");
    rowErrorEl.hidden = true;
    rowErrorEl.textContent = "";

    const key = keyEl.value.trim();
    const type = typeEl.value;

    if (key === "") {
      const msg = `Restriction #${index + 1}: key is required.`;
      rowErrorEl.textContent = "Key is required.";
      rowErrorEl.hidden = false;
      errors.push(msg);
      return;
    }
    if (seenKeys.has(key)) {
      const msg = `Restriction #${index + 1}: duplicate key "${key}".`;
      rowErrorEl.textContent = "Duplicate key.";
      rowErrorEl.hidden = false;
      errors.push(msg);
      return;
    }
    seenKeys.add(key);

    const result = parseRowValue(type, valueEl);
    if (!result.ok) {
      const msg = `Restriction "${key}": ${result.error}.`;
      rowErrorEl.textContent = result.error.charAt(0).toUpperCase() + result.error.slice(1) + ".";
      rowErrorEl.hidden = false;
      errors.push(msg);
      return;
    }

    restrictions.push({ key, type, value: result.value });
  });

  if (errors.length > 0) {
    return { ok: false, errors };
  }

  return {
    ok: true,
    payload: {
      schemaVersion: SCHEMA_VERSION,
      packageName,
      restrictions,
    },
  };
}

function showErrors(errors) {
  errorsEl.innerHTML =
    "<strong>Fix the following before generating a QR code:</strong><ul>" +
    errors.map((e) => `<li>${escapeHtml(e)}</li>`).join("") +
    "</ul>";
  errorsEl.hidden = false;
}

function clearErrors() {
  errorsEl.hidden = true;
  errorsEl.innerHTML = "";
}

function escapeHtml(str) {
  const div = document.createElement("div");
  div.textContent = str;
  return div.innerHTML;
}

function utf8ByteLength(str) {
  return new TextEncoder().encode(str).length;
}

function saveState() {
  const rows = [];
  restrictionsList.querySelectorAll("[data-row]").forEach((row) => {
    const keyEl = row.querySelector('[data-field="key"]');
    const typeEl = row.querySelector('[data-field="type"]');
    const valueEl = row.querySelector('[data-field="value"]');
    const type = typeEl.value;
    const value = type === "bool" ? valueEl.checked : valueEl ? valueEl.value : "";
    rows.push({ key: keyEl.value, type, value });
  });

  const state = { packageName: packageNameInput.value, rows };
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch (e) {
    // localStorage may be unavailable (private browsing quota, disabled storage, etc.);
    // persistence is a convenience, not a requirement, so fail silently.
  }
}

function loadState() {
  let raw = null;
  try {
    raw = localStorage.getItem(STORAGE_KEY);
  } catch (e) {
    raw = null;
  }

  let state = null;
  if (raw) {
    try {
      state = JSON.parse(raw);
    } catch (e) {
      state = null;
    }
  }

  if (!state || typeof state !== "object" || !Array.isArray(state.rows) || state.rows.length === 0) {
    addRestrictionRow();
    return;
  }

  packageNameInput.value = typeof state.packageName === "string" ? state.packageName : "";
  state.rows.forEach((row) => addRestrictionRow(row));
}

function generate() {
  clearErrors();
  outputEl.hidden = true;

  const result = collectPayload();
  if (!result.ok) {
    showErrors(result.errors);
    return;
  }

  const json = JSON.stringify(result.payload);
  // A leading UTF-8 BOM is not part of the JSON payload itself, but QR byte-mode
  // segments carry no charset marker, and several real-world scanners (verified with
  // zbar) fall back to a non-UTF-8 default charset for non-ASCII content without it,
  // corrupting accented/non-Latin characters on decode. Readers that don't look for a
  // BOM simply see JSON with a harmless leading whitespace-equivalent character.
  const qrData = "﻿" + json;
  const byteLength = utf8ByteLength(qrData);

  let qr;
  try {
    qr = qrcode(0, "M");
    qr.addData(qrData);
    qr.make();
  } catch (e) {
    showErrors([
      `Payload is too large to fit in a single QR code (${byteLength} bytes). ` +
        "Remove some restrictions and try again.",
    ]);
    return;
  }

  lastPayloadJson = json;
  lastQr = qr;

  qrContainer.innerHTML = qr.createSvgTag({ cellSize: 6, margin: 16, scalable: true });
  byteCountEl.textContent = `${byteLength} bytes encoded`;
  jsonPreviewEl.textContent = JSON.stringify(result.payload, null, 2);
  outputEl.hidden = false;
}

function downloadPng() {
  if (!lastQr) return;
  const dataUrl = lastQr.createDataURL(6, 16);
  const a = document.createElement("a");
  a.href = dataUrl;
  a.download = "qrdpc-config.png";
  document.body.appendChild(a);
  a.click();
  a.remove();
}

function downloadSvg() {
  if (!lastQr) return;
  const svg = lastQr.createSvgTag({ cellSize: 6, margin: 16 });
  const blob = new Blob([svg], { type: "image/svg+xml" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = "qrdpc-config.svg";
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

async function copyJson() {
  if (!lastPayloadJson) return;
  try {
    await navigator.clipboard.writeText(lastPayloadJson);
    const original = copyJsonBtn.textContent;
    copyJsonBtn.textContent = "Copied!";
    setTimeout(() => {
      copyJsonBtn.textContent = original;
    }, 1500);
  } catch (e) {
    showErrors(["Could not copy to clipboard in this browser."]);
  }
}

addRestrictionBtn.addEventListener("click", () => {
  addRestrictionRow();
  saveState();
});
generateBtn.addEventListener("click", generate);
downloadPngBtn.addEventListener("click", downloadPng);
downloadSvgBtn.addEventListener("click", downloadSvg);
copyJsonBtn.addEventListener("click", copyJson);

packageNameInput.addEventListener("input", saveState);
restrictionsList.addEventListener("input", saveState);
restrictionsList.addEventListener("change", saveState);

loadState();
