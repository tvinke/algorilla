Add a source file with an O(1) lookup (e.g. set.contains()) inside a loop — should NOT trigger.
The nested-lookup rule must distinguish between O(n) lookups on lists and O(1) lookups on sets/maps.
