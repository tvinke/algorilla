function cleanupSessions(sessions: Map<string, object>, expired: string[]): void {
  for (const key of expired) {
    sessions.delete(key);
  }
}
