// String methods with plain string arguments -- should NOT be flagged in JS.
// In JavaScript, split/replace with string args do NOT compile regex.
// This fixture tests that the rule correctly skips these cases.

function splitWithStrings(lines) {
    for (const line of lines) {
        // Plain string arguments -- should NOT be flagged in JS
        const parts = line.split('/');
        const cleaned = line.replace('foo', 'bar');
        const segments = line.split('?v=');
    }
}

function replaceWithStrings(items) {
    items.forEach(item => {
        // Plain string arguments -- no regex compilation in JS
        const result = item.replace('-', '');
        const tokens = item.split('.');
    });
}
