class ReflectionInvoke {
    void setFields(List<Object> items) throws Exception {
        for (Object item : items) {
            Method setter = item.getClass().getMethod("setId", Long.class);
            setter.invoke(item, null); // reflection, not IO
        }
    }
}
