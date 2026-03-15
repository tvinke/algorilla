class StTokInLoop {
    void parseTokens(String input) {
        java.util.StringTokenizer stTok = new java.util.StringTokenizer(input, "&");
        while (stTok.hasMoreTokens()) {
            String token = stTok.nextToken();
            System.out.println(token);
        }
    }
}
