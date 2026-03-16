// Implicit regex compilation inside loops -- all should be flagged.
// This fixture tests that regex literal arguments (/pattern/) are correctly
// detected as regex compilation triggers in JavaScript.

function processLines(lines) {
    for (const line of lines) {
        // Regex literal -- should be flagged
        const parts = line.replace(/\s+/g, ' ');
        const matched = line.match(/^(\d+)\s+(.*)$/);
    }
}

function splitWithRegex(items) {
    items.forEach(item => {
        // Regex literal -- should be flagged
        const tokens = item.split(/[,;|]/);
    });
}
