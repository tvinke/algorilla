function mergeArrays(arrays) {
    let result = [];
    for (const arr of arrays) {
        result = result.concat(arr);
    }
    return result;
}
