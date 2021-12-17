package uk.co.adamchaplin.countrycounter

class CountryDao(var continentName: String, var countryName: String) : Comparable<CountryDao> {

    override fun compareTo(other: CountryDao): Int {
        if(this.countryName > other.countryName) return 1
        if(this.countryName < other.countryName) return -1
        return 0
    }

}