package uk.co.adamchaplin.countrycounter

enum class Continent(val value: String) {
    AFRICA("Africa"), ANTARCTICA("Antarctica"), ASIA("Asia"), EUROPE("Europe"), NORTH_AMERICA("North America"), OCEANIA("Oceania"), SOUTH_AMERICA("South America"), ALL_COUNTRIES("Countries"), ALL_CONTINENTS("Continents"), NONE("None");
    companion object {
        val basicContinents = listOf( AFRICA, ANTARCTICA, ASIA, EUROPE, NORTH_AMERICA, OCEANIA, SOUTH_AMERICA )
    }
}