package com.example.data.model

enum class FoodCategory(val displayName: String, val iconEmoji: String, val colorHex: Long) {
    FRUITS_VEGETABLES("Frutta e Verdura", "🥗", 0xFF4CAF50),
    MEAT_FISH("Carne e Pesce", "🥩", 0xFFE53935),
    DAIRY_EGGS("Latticini e Uova", "🧀", 0xFFFFB300),
    GRAINS_PASTA("Cereali, Pane e Pasta", "🌾", 0xFFFB8C00),
    PANTRY_SPICES("Dispensa e Condimenti", "🥫", 0xFF8D6E63),
    EXTRA_HOUSEHOLD("Extra e Cura della Casa", "🧹", 0xFF7E57C2);

    fun getLocalizedName(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> displayName
        AppLanguage.EN -> when (this) {
            FRUITS_VEGETABLES -> "Fruits & Vegetables"
            MEAT_FISH -> "Meat & Seafood"
            DAIRY_EGGS -> "Dairy & Eggs"
            GRAINS_PASTA -> "Grains, Bread & Pasta"
            PANTRY_SPICES -> "Pantry & Condiments"
            EXTRA_HOUSEHOLD -> "Extra & Household"
        }
    }

    companion object {
        fun fromCategoryString(value: String?): FoodCategory? {
            if (value.isNullOrBlank()) return null
            return entries.firstOrNull { 
                it.name.equals(value, ignoreCase = true) || 
                it.displayName.equals(value, ignoreCase = true) 
            }
        }

        fun categorize(name: String, isManualExtra: Boolean = false, explicitCategory: String? = null): FoodCategory {
            if (!explicitCategory.isNullOrBlank()) {
                val found = fromCategoryString(explicitCategory)
                if (found != null) return found
            }
            if (isManualExtra) return EXTRA_HOUSEHOLD

            val lower = name.lowercase().trim()

            // Carne e Pesce
            if (lower.contains("pollo") || lower.contains("tacchino") || lower.contains("manzo") ||
                lower.contains("vitello") || lower.contains("pesce") || lower.contains("salmone") ||
                lower.contains("merluzzo") || lower.contains("tonno") || lower.contains("orata") ||
                lower.contains("spigola") || lower.contains("gamberi") || lower.contains("carne") ||
                lower.contains("prosciutto") || lower.contains("bresaola") || lower.contains("fesa") ||
                lower.contains("bistecca") || lower.contains("macinato") || lower.contains("trancio")
            ) {
                return MEAT_FISH
            }

            // Frutta e Verdura
            if (lower.contains("mela") || lower.contains("banana") || lower.contains("arancia") ||
                lower.contains("pera") || lower.contains("mirtill") || lower.contains("frutti") ||
                lower.contains("fragol") || lower.contains("pesca") || lower.contains("pomodor") ||
                lower.contains("zucchina") || lower.contains("zucchine") || lower.contains("spinaci") ||
                lower.contains("broccoli") || lower.contains("carot") || lower.contains("insalata") ||
                lower.contains("finocchi") || lower.contains("patat") || lower.contains("avocado") ||
                lower.contains("asparag") || lower.contains("cipoll") || lower.contains("aglio") ||
                lower.contains("cavol") || lower.contains("peperon") || lower.contains("melanzan") ||
                lower.contains("frutta") || lower.contains("verdura")
            ) {
                return FRUITS_VEGETABLES
            }

            // Latticini e Uova
            if (lower.contains("yogurt") || lower.contains("latte") || lower.contains("uov") ||
                lower.contains("formaggio") || lower.contains("parmigiano") || lower.contains("ricotta") ||
                lower.contains("mozzarella") || lower.contains("kefir") || lower.contains("burro") ||
                lower.contains("stracchino") || lower.contains("grana") || lower.contains("feta")
            ) {
                return DAIRY_EGGS
            }

            // Cereali, Pane e Pasta
            if (lower.contains("riso") || lower.contains("avena") || lower.contains("pasta") ||
                lower.contains("pane") || lower.contains("gallett") || lower.contains("cereali") ||
                lower.contains("farina") || lower.contains("quinoa") || lower.contains("farro") ||
                lower.contains("orzo") || lower.contains("couscous") || lower.contains("fette biscottate") ||
                lower.contains("piadina") || lower.contains("crackers")
            ) {
                return GRAINS_PASTA
            }

            // Extra e Casa
            if (lower.contains("detersivo") || lower.contains("sapone") || lower.contains("carta") ||
                lower.contains("sacchetti") || lower.contains("spugna") || lower.contains("candeggina")
            ) {
                return EXTRA_HOUSEHOLD
            }

            // Default a Dispensa e Condimenti (olio, sale, spezie, frutta secca, mandorle, salse, aceto, ecc.)
            return PANTRY_SPICES
        }
    }
}
