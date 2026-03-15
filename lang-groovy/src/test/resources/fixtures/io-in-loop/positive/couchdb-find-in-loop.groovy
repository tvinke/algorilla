class CouchDbFindInLoop {
    def syncAnimals(List<String> animalIds) {
        for (String id : animalIds) {
            def animal = couchDbRepository.find(Animal, id)
            process(animal)
        }
    }
}
