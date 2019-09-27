package io.monetizr.monetizrsdk.dto

class HierarchyVariant {
    val id: String
    val title: String
    val level: Int
    val price: Price
    val descendants: HashSet<HierarchyVariant>

    constructor(id: String, title: String, price: Price, level: Int, descendants: HashSet<HierarchyVariant>) {
        this.id = id
        this.title = title
        this.level = level
        this.price = price
        this.descendants = descendants
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this.javaClass != other.javaClass)
            return false

        val ve = other as HierarchyVariant? ?: return false
        return (id == ve.id)
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + id.hashCode()
        return result
    }


    companion object {
        private fun buildItems(level: Int, id: String?, rootId: String?, variants: ArrayList<Variant>): ArrayList<HierarchyVariant> {
            val result = ArrayList<HierarchyVariant>()

            for (variant in variants) {
                val title = variant.title
                val split = title.split("/").map { it.trim() }
                if (split.size < (level + 1)) continue
                val item = split[level]
                if (item.isEmpty()) continue


                if (level == 0) {
                    val name = variant.selectedOptions[0].name
                    val obj = HierarchyVariant(item, name, variant.priceV2, level, HashSet())
                    result.add(obj)
                } else if (level == 1) {
                    val name = variant.selectedOptions[1].name
                    val before = split[level - 1]
                    if (before == id) {
                        val obj = HierarchyVariant(item, name, variant.priceV2, level, HashSet())
                        result.add(obj)
                    }
                } else if (level == 2) {
                    val name = variant.selectedOptions[2].name
                    val before = split[level - 1]
                    val root = split[level - 2]

                    if (before == id && root == rootId) {
                        val obj = HierarchyVariant(item, name, variant.priceV2, level, HashSet())
                        result.add(obj)
                    }
                }
            }
            return result
        }

        fun buildStructure(variants: ArrayList<Variant>): HashSet<HierarchyVariant> {
            var result: HashSet<HierarchyVariant> = HashSet()
            result.addAll(buildItems(0, null, null, variants))

            for (item in result) {
                item.descendants.addAll(buildItems(1, item.id, null, variants))
            }

            for (item in result) {
                for (subItem in item.descendants) {
                    subItem.descendants.addAll(buildItems(2, subItem.id, item.id, variants))
                }
            }
            return result
        }
    }
}
