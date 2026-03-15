/**
 * Geometry utility functions called multiple times in the same function.
 * pointFrom(), vectorCross(), etc. are constant-time operations — too cheap
 * to flag even when called with same-looking arguments.
 * Should NOT trigger redundant-expensive-call.
 */

function pointFrom(x, y) {
  return { x, y };
}

function vectorCross(a, b) {
  return a.x * b.y - a.y * b.x;
}

function headingIsHorizontal(heading) {
  return heading === 0 || heading === 180;
}

function processLine(startX, startY, endX, endY) {
  const start = pointFrom(startX, startY);
  const end = pointFrom(endX, endY);
  const cross1 = vectorCross(start, end);
  const cross2 = vectorCross(end, start);
  const h1 = headingIsHorizontal(0);
  const h2 = headingIsHorizontal(90);
  return { start, end, cross1, cross2, h1, h2 };
}
