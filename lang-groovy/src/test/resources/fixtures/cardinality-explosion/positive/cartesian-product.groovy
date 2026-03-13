class PairingService {
    List buildPairs(List users, List roles) {
        List result = new ArrayList();
        users.each(user -> {
            roles.each(role -> {
                result.add(user.name + ":" + role.name);
            });
        });
        return result;
    }
}
