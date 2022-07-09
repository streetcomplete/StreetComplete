package de.westnordost.streetcomplete.quests.cuisine

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestCuisineSuggestionBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm

class AddCuisineForm : AbstractOsmQuestForm<String>() {

    override val contentLayoutResId = R.layout.quest_cuisine_suggestion
    private val binding by contentViewBinding(QuestCuisineSuggestionBinding::bind)

    val cuisines = mutableListOf<String>()

    val cuisine get() = binding.cuisineInput.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        suggestions.let {
            binding.cuisineInput.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    it
                )
            )
        }

        binding.cuisineInput.doAfterTextChanged { checkIsFormComplete() }

        binding.addCuisineButton.setOnClickListener {
            if (isFormComplete()) {
                cuisines.add(cuisine)
                binding.currentCuisines.text = cuisines.joinToString(";")
                binding.cuisineInput.text.clear()
            }
        }
    }

    override fun onClickOk() {
        if (cuisine.isBlank())
            applyAnswer(cuisines.joinToString(";"))
        else
            applyAnswer((cuisines + listOf(cuisine)).joinToString(";"))
    }

    override fun isFormComplete() = (cuisine.isNotEmpty() || cuisines.isNotEmpty()) && !cuisine.contains(";")

    companion object {
        val suggestions = cuisineValues.split("\n").mapNotNull {
            if (it.isBlank()) null
            else it.trim()
        }.toTypedArray()
    }
}

const val cuisineValues = """
pizza
burger
regional
coffee_shop
italian
chinese
sandwich
chicken
mexican
japanese
american
kebab
indian
asian
sushi
french
ice_cream
thai
german
greek
seafood
korean
international
steak_house
fish_and_chips
tex-mex
vietnamese
noodle
turkish
barbecue
spanish
local
fish
ramen
donut
mediterranean
friture
breakfast
bubble_tea
juice
crepe
beef_bowl
wings
lebanese
tapas
italian_pizza
georgian
hot_dog
indonesian
cake
arab
bagel
portuguese
pasta
polish
african
filipino
russian
pizza;kebab
malaysian
caribbean
peruvian
soba
grill
frozen_yogurt
bavarian
brazilian
curry
salad
dessert
heuriger
steak
diner
buschenschank
persian
dumpling
coffee
argentinian
middle_eastern
british
tea
oriental
sausage
balkan
pancake
fast_food
hotpot
bistro
moroccan
pretzel
fine_dining
taiwanese
bbq
pizza;pasta
pita
noodles
ethiopian
cafe
latin_american
hawaiian
fried_chicken
beef_noodle
irish
fried_food
tacos
austrian
udon
croatian
meat
danish
bolivian
shawarma
european
western
hungarian
fries
bakery
malagasy
lao
traditional
empanada
jamaican
cuban
buffet
piadina
yakiniku
deli
soup
waffle
uzbek
teahouse
nepalese
syrian
savory_pancakes
pie
falafel
czech
brasserie
snackbar
dumplings
afghan
pub
cantonese
bar&grill
swedish
potato
snack
poke
smørrebrød
malay
chili
belgian
pakistani
teppanyaki
crepes
ukrainian
couscous
organic
gyros
brunch
souvlaki
alpine_hut
pastry
canteen
cajun
basque
chocolate
yakitori
smoothie
beef
armenian
english
swiss
churro
fondue
takoyaki
mongolian
cafetaria
romanian
southern
pastel
"""
