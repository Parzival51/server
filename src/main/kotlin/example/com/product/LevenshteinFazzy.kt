package example.com.product

import java.util.*

fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
    val lhsLength = lhs.length
    val rhsLength = rhs.length

    var cost = Array(lhsLength + 1) { it }
    var newCost = Array(lhsLength + 1) { 0 }

    for (i in 1..rhsLength) {
        newCost[0] = i

        for (j in 1..lhsLength) {
            val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1

            val costReplace = cost[j - 1] + match
            val costInsert = cost[j] + 1
            val costDelete = newCost[j - 1] + 1

            newCost[j] = minOf(costInsert, costDelete, costReplace)
        }

        val swap = cost
        cost = newCost
        newCost = swap
    }

    return cost[lhsLength]
}

fun fuzzySearch(
    query: String,
    products: List<Product>,
    categories: List<Category>,
    threshold: Int = 2
): Pair<List<Product>, List<Category>> {
    val lowerCaseQuery = query.lowercase(Locale.getDefault())

    val matchingProducts = products.filter {
        levenshtein(it.name.lowercase(Locale.getDefault()), lowerCaseQuery) <= threshold ||
                levenshtein(it.category.lowercase(Locale.getDefault()), lowerCaseQuery) <= threshold
    }

    val matchingCategories = categories.filter {
        levenshtein(it.name.lowercase(Locale.getDefault()), lowerCaseQuery) <= threshold
    }

    return Pair(matchingProducts, matchingCategories)
}

