package de.westnordost.streetcomplete.quests.tree

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import de.westnordost.osmfeatures.StringUtils
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.databinding.QuestNameSuggestionBinding
import de.westnordost.streetcomplete.quests.shop_type.SearchAdapter
import de.westnordost.streetcomplete.screens.main.map.getTreeGenus
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import org.koin.android.ext.android.inject
import java.io.IOException
import androidx.core.widget.doAfterTextChanged
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.util.LastPickedValuesStore
import de.westnordost.streetcomplete.util.mostCommonWithin
import java.io.File

class AddTreeGenusForm : AbstractOsmQuestForm<Tree>() {

    override val contentLayoutResId = R.layout.quest_name_suggestion
    private val binding by contentViewBinding(QuestNameSuggestionBinding::bind)
    private val name: String get() = binding.nameInput.text?.toString().orEmpty().trim()
    private val mapDataSource: MapDataWithEditsSource by inject()
    private val trees get() = loadTrees()

    override fun onClickOk() {
        val tree = getSelectedTree()
        if (tree == null) {
            binding.nameInput.error = context?.resources?.getText(R.string.quest_tree_error)
        } else {
            favs.add("${tree.isSpecies}§${tree.name}")
            applyAnswer(tree)
        }
    }

    override fun isFormComplete(): Boolean {
        return name.isNotEmpty()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SearchAdapter(requireContext(), { term -> getTrees(term) }, { it.toDisplayString() })
        binding.nameInput.setAdapter(adapter)
        binding.nameInput.doAfterTextChanged { checkIsFormComplete() }
        // set some blank input
        // this is the only way I found how to display recent answers
        // unfortunately this still needs a tap on the field, so the keyboard will pop up...
        binding.nameInput.setOnFocusChangeListener { _, _ -> binding.nameInput.setText(" ", true) }
        binding.nameInput.requestFocus()
    }

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        val maxDist = clickAreaSizeInMeters + 5
        val bbox = position.enclosingBoundingBox(maxDist)
        val mapData = mapDataSource.getMapDataWithGeometry(bbox)
        var bestTree: Pair<String, Double>? = null

        mapData.forEach { element ->
            if (element is Node && element.tags["natural"] == "tree") {
                val name = getTreeGenus(element.tags) ?: return@forEach
                val distance = element.position.distanceTo(position)
                if (distance < (bestTree?.second ?: maxDist))
                    bestTree = Pair(name, distance)
            }
        }
        bestTree?.let { binding.nameInput.setText(getTrees(it.first).firstOrNull()?.toDisplayString() ?: "not found") }

        return true
    }

    private fun getSelectedTree(): Tree? {
        val input = binding.nameInput.text.toString()
        return getTrees(input).firstOrNull { StringUtils.canonicalize(it.toDisplayString()) == StringUtils.canonicalize(input) }
    }

    private fun getTrees(search: String): List<Tree> {
        val search = search.trim()
        // not working, i need a tree with the same name and species, but local name?
        if (search.isEmpty()) return lastPickedAnswers.mapNotNull { answer ->
            val treeString = answer.split('§')
            trees.firstOrNull { it.name == treeString[1] && it.isSpecies == (treeString[0] == "true") }
        }
        return trees.filter { tree ->
            tree.toDisplayString() == search
            || tree.name == search
            || tree.name.split(" ").any { it.startsWith(search, true) }
            || tree.localName?.contains(search, true) == true
        //sorting: genus-only first, then prefer trees with localName
        }.sortedBy { it.localName == null }.sortedBy { it.isSpecies }
    }

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext),
            key = javaClass.simpleName,
            serialize = { it },
            deserialize = { it },
            maxEntries = 25
        )
    }

    private lateinit var favs: LastPickedValuesStore<String>

    private val lastPickedAnswers by lazy {
        favs.get()
            .mostCommonWithin(target = 3, historyCount = 25, first = 1)
            .toList()
    }

    private fun loadTrees(): Set<Tree> {
        if (treeSet.isNotEmpty()) return treeSet

        // load from file, assuming format: <Genus/Species> (<localName>)
        //  assume species if it contains a space character
        try {
            context?.getExternalFilesDir(null)?.let { dir ->
                treeSet.addAll(File(dir, "trees.csv").readLines().mapNotNull { it.toTree(it.substringBefore(" (").contains(" ")) })
            }
        } catch (e: IOException) { } // file may not exist, so an exception is no surprise

        // load from other data, assuming format: <Genus> (<localName>) or <Species> (<localName>)
        treeSet.addAll(otherDataGenus.split("\n").mapNotNull { it.toTree(false) })
        treeSet.addAll(otherDataSpecies.split("\n").mapNotNull { it.toTree(true) })

        // load from osm data (no translation / localName!)
        // this is done after others to prefer data with local name
        treeSet.addAll(osmGenus.split("\n").filter { it.isNotBlank() }.map { Tree(it.trim(), false, null) })
        treeSet.addAll(osmSpecies.split("\n").filter { it.isNotBlank() }.map { Tree(it.trim(), true, null) })

        return treeSet
    }

    companion object {
        private val treeSet = mutableSetOf<Tree>()
    }

}

private fun String.toTree(isSpecies: Boolean): Tree? {
    val line = trim()
    return if (line.isBlank() || !line.contains(" (") || !line.contains(")"))
        null
    else
        Tree(line.substringBefore(" ("), isSpecies, line.substringAfter("(").substringBeforeLast(")"))
}

private const val otherDataGenus = """
    Acer (Maple)
    Tilia (Linden)
    Platanus (Plane)
    Fraxinus (Ash)
    Aesculus (Chestnut)
    Quercus (Oak)
    Populus (Poplar)
    Malus (Apple)
    Prunus (Plum)
    Pinus (Pine)
    Picea (Spruce)
    Celtis (Hackberry)
    Elaeis (Oil Palm)
    Betula (Birch)
    Musa (Plantain)
"""

private const val otherDataSpecies = ""

private const val osmGenus = """
    Acer
    Tilia
    Platanus
    Fraxinus
    Aesculus
    Eucalyptus
    Quercus
    Populus
    Malus
    Prunus
    Pinus
    Picea
    Celtis
    Elaeis
    Betula
    Musa
    Camellia
    Gleditsia
    Ulmus
    Sophora
    Carpinus
    Robinia
    Cocos
    Phoenix
    Syringa
    Salix
    Olea
    Ananas
    Fagus
    Pyrus
    Juglans
    Cerasus
    Persea
    Ginkgo
    Cedrus
    Cupressus
    Citrus
    Alnus
    Corylus
    Sorbus
    Amelanchier
    Acacia
    Morus
    Magnolia
    Liquidambar
    Arecaceae
    Cercis
    Vachellia
    Crataegus
    Humulus
    Thuja
    Castanea
    Taxus
    Abies
    Liriodendron
    Adansonia
    Zelkova
    Hylocereus
    Areca
    Ligustrum
    Hevea
    Carica
    Ficus
    Coffea
    Macadamia
    Styphnolobium
    Mangifera
    Larix
    Ailanthus
    Trachycarpus
    Opuntia
    Catalpa
    Pseudotsuga
    Chamaecyparis
    Schinus
    Paulownia
    Washingtonia
    Luma
    Ostrya
    Nothofagus
    Lagerstroemia
    Agave
    Pterocarya
    Koelreuteria
    Araucaria
    Embothrium
    Cedrela
    Callistemon
    Melia
    Jacaranda
    Quillaja
    Pirus
    Juniperus
    Cornus
    Prosopis
    Cordyline
    Maytenus
    Casuarina
    Jubaea
    Actinidia
    Tipuana
    Brachychiton
    Metasequoia
    Grevillea
    Diospyros
    Roystonea
    Parrotia
    Gleditschia
    Taxodium
    Sequoiadendron
    Syagrus
    Ilex
    Elaeagnus
    Gevuina
    Albizia
    Broussonetia
    Cercidiphyllum
    Toona
    Tsuga
    Gymnocladus
    Cinnamomum
    Tabebuia
    Sambucus
"""

private const val osmSpecies = """
    Abies alba
    Abies balsamea
    Abies concolor
    Abies nordmanniana
    Acacia baileyana
    Acacia dealbata
    Acacia floribunda
    Acacia implexa
    Acacia iteaphylla
    Acacia mearnsii
    Acacia melanoxylon
    Acacia nilotica
    Acacia pycnantha
    Acer buergerianum
    Acer campestre
    Acer campestre 'Elegant'
    Acer campestre Elsrijk
    Acer cappadocicum
    Acer freemanii Autumn Blaze
    Acer ginnala
    Acer griseum
    Acer negundo
    Acer palmatum
    Acer platanoides
    Acer platanoïdes
    Acer platanoïdes Columnare
    Acer platanoides 'Crimson King'
    Acer platanoides 'Globosum'
    Acer platanoides 'Purpureum'
    Acer platanoides 'Schwedleri'
    Acer pseudoplatanus
    Acer pseudoplatanus 'Atropurpureum'
    Acer psuedoplatanus
    Acer rubrum
    Acer rubrum "Armstrong"
    Acer rubrum Morgan
    Acer rubrum Red Sunset
    Acer saccarinum
    Acer saccharinum
    Acer saccharinum laciniatum
    Acer saccharinum Laciniatum Wieri
    Acer saccharum
    Acer x freemanii
    Adansonia digitata
    Adansonia grandidieri
    Aesculus carnea
    Aesculus glabra
    Aesculus hippocastanum
    Aesculus rubicunda
    Aesculus x carnea
    Agave tequilana F.A.C.Weber
    Agonis flexuosa
    Ailanthus altissima
    Albizia julibrissin
    Albizia saman
    Allocasuarina verticillata
    Alnus cordata
    Alnus glutinosa
    Alnus incana
    Amelanchier canadensis
    Amelanchier laevis
    Amelanchier lamarckii
    Ananas comosus (L.) Merr.
    Angophora costata
    Araucaria araucana
    Araucaria heterophylla
    Arbutus unedo
    Azadirachta indica
    Azardiraecta indica
    Banksia integrifolia
    Banksia marginata
    Bauhinia kalbreyeri Harms.
    Betula alba
    Betula ermanii
    Betula jacquemontii
    Betula nigra
    Betula papyrifera
    Betula pendula
    Betula pendula Dalecarlica
    Betula platyphylla Sukaczev var. japonica (Miq.) H.Hara
    Betula populifolia
    Betula pubescens
    Betula utilis
    Betula verrucosa
    Brachychiton populneus
    Branchychiton populneus
    Broussonetia papyrifera
    Buxus sempervirens
    Caesalpinia pluviosa DC.
    Callistemon citrinus
    Callistemon salignus
    Callistemon viminalis
    Calocedrus decurrens
    Camellia sinensis (L.)
    Camellia sinensis (L.) Kuntze
    Carica papaya
    Carica papaya L.
    Carpinus betulus
    Carpinus betulus 'Fastigiata'
    Carpinus betulus 'Frans Fontaine'
    Carya illinoinensis (Wangenh.) K.Koch
    Castanea dentata
    Castanea sativa
    Casuarina cunninghamiana
    Casuarina glauca
    Catalpa bignonioides
    Catalpa bungeii
    Cedrus atlantica
    Cedrus atlantica glauca
    Cedrus deodara
    Cedrus libani
    Celtis australis
    Celtis occidentalis
    Celtis rugosa
    Cerasus itosakura
    Cerasus × yedoensis
    Cercidiphyllum japonicum
    Cercis canadensis
    Cercis siliquastrum
    Chamaecyparis lawsoniana
    Chamaecyparis lawsoniana Columnaris
    Chamaerops excelsa
    Chamaerops humilis
    Ciconia ciconia
    Cinnamomum camphora
    Citrus × aurantium
    Citrus × sinensis
    Cocos nucifera
    Cocos nucifera L.
    Coprosma repens
    Cordyline australis
    Cornus florida
    Cornus mas
    Corylus avellana
    Corylus colurna
    Corymbia citriodora
    Corymbia eximia
    Corymbia ficifolia
    Corymbia maculata
    Crataegus crus-gallii
    Crataegus douglasii
    Crataegus laevigata
    Crataegus laevigata 'Paul's Scarlet'
    Crataegus monogyna
    Crataegus monogyna 'Stricta'
    Crataegus oxyacantha
    Crataegus x lavallei
    Crataegus X prunifolia
    Cryptomeria japonica
    Cupressocyparis leylandii
    Cupressus arizonica
    Cupressus macrocarpa
    Cupressus sempervirens
    Cydonia oblonga
    Delonix raius keupartus
    Delonix regia
    Dipterocarpus alatus
    Drimys winteri
    Elaeagnus angustifolia
    Elaeis guineensis
    Embothrium coccineum
    Eriobotrya japonica
    Eucalyptus astringens
    Eucalyptus botryoides
    Eucalyptus camaldulensis
    Eucalyptus cladocalyx
    Eucalyptus globulus
    Eucalyptus gomphocephala
    Eucalyptus leucoxylon
    Eucalyptus macrandra
    Eucalyptus mannifera
    Eucalyptus melliodora
    Eucalyptus microcarpa
    Eucalyptus occidentalis
    Eucalyptus polyanthemos
    Eucalyptus scoparia
    Eucalyptus sideroxylon
    Eucalyptus viminalis
    Fagus grandifolia
    Fagus sylvatica
    Fagus sylvatica; Alnus viridis; Larix decidua
    Fagus sylvatica f. purpurea
    Fagus sylvatica; Ostrya carpinifolia; Quercus cerris
    Ficus benghalensis
    Ficus benjamina
    Ficus carica
    Ficus macrophylla
    Ficus microcarpa
    Ficus religiosa
    Fraxinus americana
    Fraxinus angustifolia
    Fraxinus angustifolia Raywood
    Fraxinus excelsior
    Fraxinus excelsior 'Westhofs Glorie'
    Fraxinus ornus
    Fraxinus oxycarpa
    Fraxinus oxycarpa 'Raywood'
    Fraxinus pennsylvanica
    Fraxinus raywoodii
    Fraxinus velutina
    Gevuina avellana
    Ginkgo biloba
    Ginkgo biloba fastigiata
    Gleditsia triacanthos
    Gleditsia triacanthos inermis
    Gleditsia triacanthos Shademaster
    Gleditsia triacanthos Sunburst
    Grevillea robusta
    Gymnocladus dioicus
    Hakea laurina
    Handroanthus chrysanthus
    Hevea brasiliensis Müll.Arg.
    Hibiscus syriacus
    Humulus lupulus L.
    Ilex aquifolium
    Jacaranda mimosifolia
    Jubaea chilensis
    Juglans nigra
    Juglans regia
    Juniperus communis
    Juniperus virginiana
    Koelreuteria paniculata
    Laburnum anagyroides
    Lagerstroemia indica
    Lagunaria patersonia
    Larix decidua
    Larix laricina
    Laurus nobilis
    Ligustrum japonica
    Ligustrum japonica "variegata"
    Ligustrum japonicum
    Ligustrum lucidum
    Ligustrum vulgare
    Liquidambar styraciflua
    Liquidambar styraciflua Worplesdon
    Liriodendron tulipifera
    Lophostemon confertus
    Luma apiculata
    Magnolia Galaxy
    Magnolia grandiflora
    Magnolia kobus
    Magnolia x soulangiana
    Malus domestica
    Malus 'Evereste'
    Malus floribunda
    Malus pumila
    Malus 'Rudolph'
    Malus sylvestris
    Malus toringo
    Malus tschonoskii
    Malus x domestica
    Malus x purpurea
    Malus Zierform
    Mangifera indica
    Mangifera indica L.
    Maytenus boaria
    Melaleuca armillaris
    Melaleuca ericifolia
    Melaleuca lanceolata
    Melaleuca linariifolia
    Melaleuca nesophila
    Melaleuca styphelioides
    Melia azedarach
    Melia azederach
    Metasequoia glyptostroboides
    Metrosideros excelsa
    Misc group of trees of mixed s
    Mistaken Tree Pit
    Mixed Species
    Morus alba
    Morus nigra
    Myoporum insulare
    Nerium oleander
    New Tree Pit
    Non replantation définitive
    Nothofagus dombeyi
    Nothofagus obliqua
    Olea europaea
    Olea europea
    Opuntia ficus-indica (L.) Mill.
    Ostrya carpinifolia
    Parrotia persica
    Paulownia tomentosa
    Persea americana Mill.
    Phellodendron amurense
    Phoenix canariensis
    Phoenix canariensis Hort. ex Chabaud
    Phoenix dactylifera
    Phoenix dactylifera L.
    Photinia serrulata
    Picea abies
    Picea abies (L.) Karst.
    Picea glauca
    Picea mariana
    Picea omorika
    Picea pungens
    Pinus banksiana
    Pinus halepensis
    Pinus mugo
    Pinus nigra
    Pinus nigra austriaca
    Pinus nigra nigra
    Pinus pinaster
    Pinus pinea
    Pinus ponderosa
    Pinus radiata
    Pinus resinosa
    Pinus strobus
    Pinus strobus L.
    Pinus sylvestris
    Pinus taeda
    Pinus wallichiana
    Pirus calleryana
    Pirus calleryana Chanticleer
    Pistacia chinensis
    Platanus acerifolia
    Platanus hybrida
    Platanus occidentalis
    Platanus orientalis
    Platanus Platanor "Vallis Clausa"
    Platanus x acerifolia
    Platanus x hybrida
    Populus alba
    Populus alba "Bolleana"
    Populus balsamifera
    Populus canadensis
    Populus canescens
    Populus deltoides
    Populus nigra
    Populus nigra 'Italica'
    Populus pyramidalis
    Populus robusta
    Populus simonii
    Populus tremula
    Populus tremula 'Erecta'
    Populus tremuloides
    Populus x canadensis
    Populus x canescens
    Populus x euramericana
    Prosopis chilensis
    Prunus Accolade
    Prunus 'Amanogawa'
    Prunus armeniaca
    Prunus avium
    Prunus avium 'Plena'
    Prunus blireiana
    Prunus cerasifera
    Prunus cerasifera 'Nigra'
    Prunus cerasifera 'Pissardii'
    Prunus cerasus
    Prunus domestica
    Prunus dulcis
    Prunus dulcis (Mill.) D.A.Webb
    Prunus hillierii x Spire
    Prunus kanzan
    Prunus laurocerasus
    Prunus lusitanica
    Prunus maackii
    Prunus padus
    Prunus persica
    Prunus Persica
    Prunus sargentii
    Prunus sargentii "Yedoensis"
    Prunus serotina
    Prunus serrula
    Prunus serrulata
    Prunus serrulata Kanzan
    Prunus subhirtella
    Prunus 'Sunset Boulevard'
    Prunus 'Tai Haku'
    Prunus 'Umineko'
    Prunus virginiana
    Prunus Virginiana `Schubert`
    Prunus x hillieri Spire
    Prunus x schmittii
    Prunus × yedoensis
    Pseudotsuga menziesii
    Pterocarya fraxinifolia
    Punica granatum
    Pyrus calleryana
    Pyrus calleryana var. chanticleer
    Pyrus communis
    Quercus alba
    Quercus bicolor
    Quercus castaneifolia
    Quercus cerris
    Quercus coccinea
    Quercus frainetto
    Quercus fusiformis
    Quercus ilex
    Quercus Ilex
    Quercus imbricaria
    Quercus macrocarpa
    Quercus muehlenbergii
    Quercus palustris
    Quercus pedunculata
    Quercus petraea
    Quercus phellos
    Quercus pubescens
    Quercus robur
    Quercus robur 'Fastigiata'
    Quercus robur 'Fastigiata Koster'
    Quercus robur L.
    Quercus rubra
    QUERCUS RUBRA (SINONIM Q. BOREALIS)
    Quercus suber
    Quillaja saponaria
    Rhus typhina
    River Red Gum
    Robinia pseudoacacia
    Robinia umbraculifera
    Roystonea regia
    Roystonea regia (Kunth) O. F. Cook
    SA Blue Gum
    Salix alba
    Salix alba 'Tristis'
    Salix babylonica
    Salix babylonica Pendula
    Salix caprea
    Salix fragilis
    Sambucus nigra
    Schinus molle
    Sequoiadendron giganteum
    Sequoia sempervirens
    Sophora cassioides
    Sophora japonica
    Sorbus aria
    Sorbus aucuparia
    Sorbus decora
    Sorbus domestica
    Sorbus intermedia
    Sorbus intermedia 'Brouwers'
    Sorbus torminalis
    Sorbus x arnoldiana Schouten
    Sorbus x thuringiaca
    Styphnolobium japonicum
    Syagrus sancona H. Karst.
    Syringa reticulata
    Syringa vulgaris
    Tamarix gallica
    Taxodium distichum
    Taxus baccata
    Tectona grandis L.f.
    Theobroma cacao L.
    Thuja occidentalis
    Thuja orientalis
    Thuja plicata
    Tilia americana
    Tilia argentea
    Tilia cordata
    Tilia cordata Green Spire
    Tilia cordata Mill.
    Tilia cordata 'Rancho'
    Tilia euchlora
    Tilia europaea
    Tilia europaea Pallida
    Tilia mongolica
    Tilia platyphyllos
    Tilia platyphyllos Scop.
    Tilia tomentosa
    Tilia x euchlora
    Tilia x europaea
    Tilia x europea
    Tilia x europeana
    Tilia x flavescens Glenleven
    Tilia x intermedia
    Tilia x vulgaris
    Tilia x vulgaris 'Pallida'
    Tipuana tipu
    Trachycarpus fortunei
    Tristaniopsis laurina
    Tsuga canadensis
    Ulmus americana
    Ulmus americana 'Brandon'
    Ulmus carpinifolia
    Ulmus glabra
    Ulmus hollandica
    Ulmus hybride Lobel
    Ulmus laevis
    Ulmus minor
    Ulmus parvifolia
    Ulmus parvifolia 'Yarralumla Clone'
    Ulmus procera
    Ulmus pumila
    Unplantable Tree Pit
    Washingtonia filifera
    Washingtonia robusta
    Wodyetia bifurcata
    Zelkova carpinifolia
    Zelkova serrata
    Zelkova serrata Green Vase
    Zellkova serrata
"""
