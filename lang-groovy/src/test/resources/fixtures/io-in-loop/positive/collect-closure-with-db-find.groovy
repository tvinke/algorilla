class CollectWithDbFind {
    def loadAllAnimals(List<String> ids) {
        ids.each(id -> {
            couchDbRepository.find(Animal, id);
        });
    }
}
