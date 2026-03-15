class GdkFindWithClosure {
    def findLargeAnimal(List<Animal> animals) {
        def large = animals.find { it.weight > 100 }
        return large
    }
}
