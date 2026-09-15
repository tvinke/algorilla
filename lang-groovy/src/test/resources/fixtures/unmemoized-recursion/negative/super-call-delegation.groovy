class Sub extends Base {
    String resolveKey(Map vars) {
        if (super.resolveKey(vars) != null) {
            return super.resolveKey(vars)
        }
        return "default"
    }
}
