class DecodeNextToken {
    void parseParams(java.util.StringTokenizer stTok) {
        String first = java.net.URLDecoder.decode(stTok.nextToken(), "UTF-8");
        String second = java.net.URLDecoder.decode(stTok.nextToken(), "UTF-8");
    }
}
