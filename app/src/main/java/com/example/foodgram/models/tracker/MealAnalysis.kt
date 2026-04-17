package com.example.foodgram.models.tracker

import com.google.gson.annotations.SerializedName

data class MealAnalysis(
    @SerializedName("dish_name") val dishName: String,
    @SerializedName("components") val components: List<FoodComponent>,
    @SerializedName("total_calories") val totalCalories: Double,
    @SerializedName("macronutrients_totals") val macronutrientsTotals: MacronutrientTotals,
    @SerializedName("macronutrient_distribution_percent") val distribution: MacronutrientDistribution,
    @SerializedName("confidence") val confidence: String
)

data class FoodComponent(
    @SerializedName("food") val food: String,
    @SerializedName("estimated_portion") val portion: String,
    @SerializedName("estimated_weight_g") val weightG: Double,
    @SerializedName("calories") val calories: Double,
    @SerializedName("protein_g") val proteinG: Double,
    @SerializedName("carbs_g") val carbsG: Double,
    @SerializedName("fat_g") val fatG: Double
)

data class MacronutrientTotals(
    @SerializedName("protein_g") val proteinG: Double,
    @SerializedName("carbs_g") val carbsG: Double,
    @SerializedName("fat_g") val fatG: Double
)

data class MacronutrientDistribution(
    @SerializedName("protein") val protein: Double,
    @SerializedName("carbohydrates") val carbohydrates: Double,
    @SerializedName("fat") val fat: Double
)
