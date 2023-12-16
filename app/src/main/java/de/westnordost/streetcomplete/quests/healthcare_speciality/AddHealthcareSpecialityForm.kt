package de.westnordost.streetcomplete.quests.healthcare_speciality

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.preference.PreferenceManager
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ViewShopTypeBinding
import de.westnordost.streetcomplete.quests.AMultiValueQuestForm
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.TagEditor
import de.westnordost.streetcomplete.util.LastPickedValuesStore
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.ktx.hideKeyboard
import de.westnordost.streetcomplete.util.mostCommonWithin
import de.westnordost.streetcomplete.view.controller.FeatureViewController
import de.westnordost.streetcomplete.view.dialogs.SearchFeaturesDialog

class AddHealthcareSpecialityForm : AMultiValueQuestForm<String>() {

    override fun stringToAnswer(answerString: String) = answerString

    // the hacky UI switch breaks when using tag editor...
    override val otherAnswers get() = if (TagEditor.showingTagEditor) emptyList() else listOf(AnswerItem(R.string.quest_healthcare_speciality_switch_ui) {
        val f = MedicalSpecialityTypeForm()
        if (f.arguments == null) f.arguments = bundleOf()
        val args = createArguments(questKey, questType, geometry, 0f, 0f)
        f.requireArguments().putAll(args)
        val osmArgs = createArguments(element)
        f.requireArguments().putAll(osmArgs)
        activity?.currentFocus?.hideKeyboard()
        parentFragmentManager.commit {
            replace(id, f, "bottom_sheet")
            addToBackStack("bottom_sheet")
        }
    })

    override val onlyAllowSuggestions = true

    override val addAnotherValueResId = R.string.quest_healthcare_speciality_add_more

    override fun getConstantSuggestions() =
        (healthcareSpecialityFromWiki.split("\n").mapNotNull {
            if (it.isBlank()) null
            else it.trim()
        } + healthcareSpecialityValuesFromTaginfo.split("\n").mapNotNull {
            if (it.isBlank()) null
            else it.trim()
        }).toSet()

}


class MedicalSpecialityTypeForm : AbstractOsmQuestForm<String>() {

    override val contentLayoutResId = R.layout.view_shop_type // TODO?
    private val binding by contentViewBinding(ViewShopTypeBinding::bind)

    private lateinit var radioButtons: List<RadioButton>
    private var selectedRadioButtonId: Int = 0
    private lateinit var featureCtrl: FeatureViewController

    // the hacky UI switch breaks when using tag editor...
    override val otherAnswers = if (TagEditor.showingTagEditor) emptyList() else listOf(AnswerItem(R.string.quest_healthcare_speciality_switch_ui) {
        val f = AddHealthcareSpecialityForm()
        if (f.arguments == null) f.arguments = bundleOf()
        val args = createArguments(questKey, questType, geometry, 0f, 0f)
        f.requireArguments().putAll(args)
        val osmArgs = createArguments(element)
        f.requireArguments().putAll(osmArgs)
        activity?.currentFocus?.hideKeyboard()
        parentFragmentManager.commit {
            replace(id, f, "bottom_sheet")
            addToBackStack("bottom_sheet")
        }
    })

    private lateinit var favs: LastPickedValuesStore<String>

    private val lastPickedAnswers by lazy {
        favs.get()
            .mostCommonWithin(target = 12, historyCount = 50, first = 1)
            .toList()
    }

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            prefs,
            key = javaClass.simpleName,
            serialize = { it },
            deserialize = { it },
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radioButtons = listOf(binding.vacantRadioButton, binding.replaceRadioButton, binding.leaveNoteRadioButton)
        for (radioButton in radioButtons) {
            radioButton.setOnClickListener { selectRadioButton(it) }
        }

        featureCtrl = FeatureViewController(featureDictionary, binding.featureView.textView, binding.featureView.iconView)
        featureCtrl.countryOrSubdivisionCode = countryOrSubdivisionCode

        binding.featureView.root.background = null
        binding.featureContainer.setOnClickListener {
            selectRadioButton(binding.replaceRadioButton)

            SearchFeaturesDialog(
                requireContext(),
                featureDictionary,
                element.geometryType,
                countryOrSubdivisionCode,
                featureCtrl.feature?.name,
                ::filterOnlySpecialitiesOfMedicalDoctors,
                ::onSelectedFeature,
                false,
                getSuggestions(),
                geometry.center
            ).show()
        }
    }

    private fun filterOnlySpecialitiesOfMedicalDoctors(feature: Feature): Boolean {
        if (!feature.tags.containsKey("healthcare:speciality")) {
            return false
        }
        return feature.tags["amenity"] == "doctors"
    }

    private fun onSelectedFeature(feature: Feature) {
        featureCtrl.feature = feature
        checkIsFormComplete()
    }

    override fun onClickOk() {
        when (selectedRadioButtonId) {
            R.id.vacantRadioButton    -> composeNote()
            R.id.leaveNoteRadioButton -> composeNote()
            R.id.replaceRadioButton   -> {
                applyAnswer(featureCtrl.feature!!.addTags["healthcare:speciality"]!!)
                favs.add(featureCtrl.feature!!.id)
            }
        }
    }

    override fun isFormComplete() = when (selectedRadioButtonId) {
        R.id.vacantRadioButton,
        R.id.leaveNoteRadioButton -> true
        R.id.replaceRadioButton   -> featureCtrl.feature != null
        else                      -> false
    }

    private fun selectRadioButton(radioButton: View) {
        selectedRadioButtonId = radioButton.id
        for (b in radioButtons) {
            b.isChecked = selectedRadioButtonId == b.id
        }
        checkIsFormComplete()
    }

    private fun getSuggestions(): Collection<String> {
        if (lastPickedAnswers.size >= 12) return lastPickedAnswers
        return (lastPickedAnswers + listOf(
                // based on https://taginfo.openstreetmap.org/keys/healthcare%3Aspeciality#values
                // with alternative medicine skipped
                "amenity/doctors/general",
                // chiropractic - skipped (alternative medicine)
                "amenity/doctors/ophthalmology",
                "amenity/doctors/paediatrics",
                "amenity/doctors/gynaecology",
                //biology skipped as that is value for laboratory
                // "amenity/dentist", would require changes in SCEE
                // psychiatry - https://github.com/openstreetmap/id-tagging-schema/issues/778
                "amenity/doctors/orthopaedics",
                "amenity/doctors/internal",
                // "healthcare/dentist/orthodontics", may require changes in SCEE
                "amenity/doctors/dermatology",
                // osteopathy - skipped (alternative medicine)
                "amenity/doctors/otolaryngology",
                "amenity/doctors/radiology",
                // vaccination? that is tagged differently, right? TODO
                "amenity/doctors/cardiology",
                "amenity/doctors/surgery", // TODO? really for doctors? Maybe that is used primarily for hospitals?
                // physiotherapy
                // urology
                // emergency
                // dialysis
                )
            ).distinct().take(12)
    }
}


const val healthcareSpecialityFromWiki = """
allergology
anaesthetics
cardiology
cardiothoracic_surgery
child_psychiatry
community
dermatology
dermatovenereology
diagnostic_radiology
emergency
endocrinology
gastroenterology
general
geriatrics
gynaecology
haematology
hepatology
infectious_diseases
intensive
internal
maxillofacial_surgery
nephrology
neurology
neuropsychiatry
neurosurgery
nuclear
occupational
oncology
ophthalmology
orthodontics
orthopaedics
otolaryngology
paediatric_surgery
paediatrics
palliative
pathology
physiatry
plastic_surgery
podiatry
proctology
psychiatry
pulmonology
radiology
radiotherapy
rheumatology
stomatology
surgery
transplant
trauma
tropical
urology
vascular_surgery
"""

const val healthcareSpecialityValuesFromTaginfo = """
general
chiropractic
ophthalmology
paediatrics
biology
gynaecology
psychiatry
dentist
orthopaedics
internal
dermatology
orthodontics
vaccination
osteopathy
otolaryngology
radiology
surgery
cardiology
urology
physiotherapy
dentistry
emergency
dialysis
covid19
community
neurology
acupuncture
plastic_surgery
traditional_chinese_medicine
weight_loss
intensive
naturopathy
oncology
physiatry
homeopathy
clinic
blood_check
occupational
gastroenterology
child_psychiatry
dental_oral_maxillo_facial_surgery
podiatry
maternity
pulmonology
optometry
fertility
endocrinology
massage_therapy
dermatovenereology
stomatology
psychotherapist
family_medicine
diagnostic_radiology
general;emergency
kinesitherapy
pathology
trauma
nephrology
behavior
psychology
geriatrics
ayurveda
anaesthetics
otorhinolaryngology
"""
