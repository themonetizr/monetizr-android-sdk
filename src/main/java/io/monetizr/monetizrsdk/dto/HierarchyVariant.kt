package io.monetizr.monetizrsdk.dto

class HierarchyVariant {
    val id: String
    val name: String
    val level: Int
    val price: Price
    val parent: HierarchyVariant?
    val parents: HashSet<HierarchyVariant>?
    val childs: HashSet<HierarchyVariant>

    constructor(id: String, name: String, price: Price, level: Int, parent: HierarchyVariant?, parents: HashSet<HierarchyVariant>?, childs: HashSet<HierarchyVariant>) {
        this.id = id
        this.name = name
        this.level = level
        this.price = price
        this.childs = childs
        this.parent = parent
        this.parents = parents
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
        private fun buildChilds(level: Int, parent: HierarchyVariant?, parents: HashSet<HierarchyVariant>?, variants: ArrayList<Variant>): ArrayList<HierarchyVariant> {
            val result = ArrayList<HierarchyVariant>()

            for (variant in variants) {
                val string = variant.title
                val parts = string.split("/").map { it.trim() }
                if (parts.size < (level + 1)) continue
                val name = variant.selectedOptions[level].name

                if (level == 0) {
                    val idLevel0 = parts[level]
                    val obj = HierarchyVariant(idLevel0, name, variant.priceV2, level, null, null, HashSet())
                    result.add(obj)
                } else if (level == 1) {
                    val idLevel0 = parts[level-1]
                    val idLevel1 = parts[level]

                    if (idLevel0 == parent!!.id) {
                        val obj = HierarchyVariant(idLevel1, name, variant.priceV2, level, parent, parents, HashSet())
                        result.add(obj)
                    }
                } else if (level == 2) {
                    val idLevel0 = parts[level - 2]
                    val idLevel1 = parts[level - 1]
                    val idLevel2 = parts[level]

                    if (idLevel1 == parent!!.id && idLevel0 == parent.parent!!.id) {
                        val obj = HierarchyVariant(idLevel2, name, variant.priceV2, level, parent, parents, HashSet())
                        result.add(obj)
                    }
                }
            }
            return result
        }

        fun buildStructure(variants: ArrayList<Variant>): HashSet<HierarchyVariant> {
            val result: HashSet<HierarchyVariant> = HashSet()
            if (variants.isEmpty() == false) {
                val first = variants[0]
                val levels = getMaxLevel(first)

                if (levels > 0) {
                    result.addAll(buildChilds(0, null, null, variants))
                }
                if (levels > 1) {
                    for (item in result) {
                        item.childs.addAll(buildChilds(1, item, result, variants))
                    }
                }
                if (levels > 2) {
                    for (item in result) {
                        for (childItem in item.childs) {
                            childItem.childs.addAll(buildChilds(2, childItem, item.childs, variants))
                        }
                    }
                }
            }
            return result
        }

        private fun getMaxLevel(variant: Variant): Int {
            return variant.title.split("/").map { it.trim() }.size
        }
    }
}
