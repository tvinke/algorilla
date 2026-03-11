// Color language tags based on their text content
function colorLanguageTags() {
  document.querySelectorAll(".md-tag").forEach(function (tag) {
    var text = tag.textContent.trim();
    var key = text.toLowerCase();
    if (["java", "kotlin", "groovy", "javascript"].indexOf(key) !== -1) {
      tag.setAttribute("data-lang", key);
    }
  });
}

// Run on page load and on navigation (Material uses instant loading)
document.addEventListener("DOMContentLoaded", colorLanguageTags);
document.addEventListener("DOMContentSwitch", colorLanguageTags);
if (typeof document$ !== "undefined") {
  document$.subscribe(colorLanguageTags);
}
