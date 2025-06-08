package de.westnordost.streetcomplete.quests

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.atp.AtpDao
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.getByLocation
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.atp.CreatePoiBasedOnAtp
import de.westnordost.streetcomplete.quests.accepts_cards.AddAcceptsCards
import de.westnordost.streetcomplete.quests.accepts_cash.AddAcceptsCash
import de.westnordost.streetcomplete.quests.access_point_ref.AddAccessPointRef
import de.westnordost.streetcomplete.quests.address.AddAddressStreet
import de.westnordost.streetcomplete.quests.address.AddHousenumber
import de.westnordost.streetcomplete.quests.air_conditioning.AddAirConditioning
import de.westnordost.streetcomplete.quests.air_pump.AddAirCompressor
import de.westnordost.streetcomplete.quests.amenity_cover.AddAmenityCover
import de.westnordost.streetcomplete.quests.amenity_indoor.AddIsAmenityIndoor
import de.westnordost.streetcomplete.quests.atm_cashin.AddAtmCashIn
import de.westnordost.streetcomplete.quests.atm_operator.AddAtmOperator
import de.westnordost.streetcomplete.quests.baby_changing_table.AddBabyChangingTable
import de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_installation.AddBicycleBarrierInstallation
import de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type.AddBicycleBarrierType
import de.westnordost.streetcomplete.quests.barrier_opening.AddBarrierOpening
import de.westnordost.streetcomplete.quests.barrier_type.AddBarrierOnPath
import de.westnordost.streetcomplete.quests.barrier_type.AddBarrierOnRoad
import de.westnordost.streetcomplete.quests.barrier_type.AddBarrierType
import de.westnordost.streetcomplete.quests.barrier_type.AddStileType
import de.westnordost.streetcomplete.quests.bbq_fuel.AddBbqFuel
import de.westnordost.streetcomplete.quests.bench_backrest.AddBenchBackrest
import de.westnordost.streetcomplete.quests.bicycle_repair_station.AddBicycleRepairStationServices
import de.westnordost.streetcomplete.quests.bike_parking_capacity.AddBikeParkingCapacity
import de.westnordost.streetcomplete.quests.bike_parking_cover.AddBikeParkingCover
import de.westnordost.streetcomplete.quests.bike_parking_type.AddBikeParkingType
import de.westnordost.streetcomplete.quests.bike_rental_capacity.AddBikeRentalCapacity
import de.westnordost.streetcomplete.quests.bike_rental_type.AddBikeRentalType
import de.westnordost.streetcomplete.quests.bike_shop.AddBicyclePump
import de.westnordost.streetcomplete.quests.bike_shop.AddBikeRepairAvailability
import de.westnordost.streetcomplete.quests.bike_shop.AddSecondHandBicycleAvailability
import de.westnordost.streetcomplete.quests.board_name.AddBoardName
import de.westnordost.streetcomplete.quests.board_type.AddBoardType
import de.westnordost.streetcomplete.quests.boat_rental.AddBoatRental
import de.westnordost.streetcomplete.quests.bollard_type.AddBollardType
import de.westnordost.streetcomplete.quests.bridge_structure.AddBridgeStructure
import de.westnordost.streetcomplete.quests.building_entrance.AddEntrance
import de.westnordost.streetcomplete.quests.building_entrance_reference.AddEntranceReference
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevels
import de.westnordost.streetcomplete.quests.building_type.AddBuildingType
import de.westnordost.streetcomplete.quests.building_underground.AddIsBuildingUnderground
import de.westnordost.streetcomplete.quests.bus_stop_bench.AddBenchStatusOnBusStop
import de.westnordost.streetcomplete.quests.bus_stop_bin.AddBinStatusOnBusStop
import de.westnordost.streetcomplete.quests.bus_stop_lit.AddBusStopLit
import de.westnordost.streetcomplete.quests.bus_stop_name.AddBusStopName
import de.westnordost.streetcomplete.quests.bus_stop_ref.AddBusStopRef
import de.westnordost.streetcomplete.quests.bus_stop_shelter.AddBusStopShelter
import de.westnordost.streetcomplete.quests.camera_type.AddCameraType
import de.westnordost.streetcomplete.quests.camping.AddCampDrinkingWater
import de.westnordost.streetcomplete.quests.camping.AddCampPower
import de.westnordost.streetcomplete.quests.camping.AddCampShower
import de.westnordost.streetcomplete.quests.camping.AddCampType
import de.westnordost.streetcomplete.quests.car_wash_type.AddCarWashType
import de.westnordost.streetcomplete.quests.charging_station_capacity.AddChargingStationCapacity
import de.westnordost.streetcomplete.quests.charging_station_operator.AddChargingStationOperator
import de.westnordost.streetcomplete.quests.clothing_bin_operator.AddClothingBinOperator
import de.westnordost.streetcomplete.quests.construction.MarkCompletedBuildingConstruction
import de.westnordost.streetcomplete.quests.construction.MarkCompletedHighwayConstruction
import de.westnordost.streetcomplete.quests.crossing.AddCrossing
import de.westnordost.streetcomplete.quests.crossing_island.AddCrossingIsland
import de.westnordost.streetcomplete.quests.crossing_kerb_height.AddCrossingKerbHeight
import de.westnordost.streetcomplete.quests.crossing_markings.AddCrossingMarkings
import de.westnordost.streetcomplete.quests.crossing_signals.AddCrossingSignals
import de.westnordost.streetcomplete.quests.cycleway.AddCycleway
import de.westnordost.streetcomplete.quests.defibrillator.AddDefibrillatorLocation
import de.westnordost.streetcomplete.quests.diet_type.AddGlutenFree
import de.westnordost.streetcomplete.quests.diet_type.AddHalal
import de.westnordost.streetcomplete.quests.diet_type.AddKosher
import de.westnordost.streetcomplete.quests.diet_type.AddVegan
import de.westnordost.streetcomplete.quests.diet_type.AddVegetarian
import de.westnordost.streetcomplete.quests.drinking_water.AddDrinkingWater
import de.westnordost.streetcomplete.quests.drinking_water_type.AddDrinkingWaterType
import de.westnordost.streetcomplete.quests.existence.CheckExistence
import de.westnordost.streetcomplete.quests.ferry.AddFerryAccessMotorVehicle
import de.westnordost.streetcomplete.quests.ferry.AddFerryAccessPedestrian
import de.westnordost.streetcomplete.quests.fire_hydrant.AddFireHydrantType
import de.westnordost.streetcomplete.quests.fire_hydrant_diameter.AddFireHydrantDiameter
import de.westnordost.streetcomplete.quests.fire_hydrant_position.AddFireHydrantPosition
import de.westnordost.streetcomplete.quests.fire_hydrant_ref.AddFireHydrantRef
import de.westnordost.streetcomplete.quests.foot.AddProhibitedForPedestrians
import de.westnordost.streetcomplete.quests.fuel_service.AddFuelSelfService
import de.westnordost.streetcomplete.quests.general_fee.AddGeneralFee
import de.westnordost.streetcomplete.quests.grit_bin_seasonal.AddGritBinSeasonal
import de.westnordost.streetcomplete.quests.hairdresser.AddHairdresserCustomers
import de.westnordost.streetcomplete.quests.handrail.AddHandrail
import de.westnordost.streetcomplete.quests.incline_direction.AddBicycleIncline
import de.westnordost.streetcomplete.quests.incline_direction.AddStepsIncline
import de.westnordost.streetcomplete.quests.internet_access.AddInternetAccess
import de.westnordost.streetcomplete.quests.kerb_height.AddKerbHeight
import de.westnordost.streetcomplete.quests.lanes.AddLanes
import de.westnordost.streetcomplete.quests.leaf_detail.AddForestLeafType
import de.westnordost.streetcomplete.quests.leaf_detail.AddTreeLeafType
import de.westnordost.streetcomplete.quests.level.AddLevel
import de.westnordost.streetcomplete.quests.max_height.AddMaxHeight
import de.westnordost.streetcomplete.quests.max_height.AddMaxPhysicalHeight
import de.westnordost.streetcomplete.quests.max_speed.AddMaxSpeed
import de.westnordost.streetcomplete.quests.max_weight.AddMaxWeight
import de.westnordost.streetcomplete.quests.memorial_type.AddMemorialType
import de.westnordost.streetcomplete.quests.moped.AddMopedAccess
import de.westnordost.streetcomplete.quests.motorcycle_parking_capacity.AddMotorcycleParkingCapacity
import de.westnordost.streetcomplete.quests.motorcycle_parking_cover.AddMotorcycleParkingCover
import de.westnordost.streetcomplete.quests.note_discussion.OsmNoteQuestType
import de.westnordost.streetcomplete.quests.oneway.AddOneway
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHoursAtp
import de.westnordost.streetcomplete.quests.opening_hours_signed.CheckOpeningHoursSigned
import de.westnordost.streetcomplete.quests.orchard_produce.AddOrchardProduce
import de.westnordost.streetcomplete.quests.parcel_locker_brand.AddParcelLockerBrand
import de.westnordost.streetcomplete.quests.parking_access.AddBikeParkingAccess
import de.westnordost.streetcomplete.quests.parking_access.AddParkingAccess
import de.westnordost.streetcomplete.quests.parking_fee.AddBikeParkingFee
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFee
import de.westnordost.streetcomplete.quests.parking_type.AddParkingType
import de.westnordost.streetcomplete.quests.pitch_lit.AddPitchLit
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName
import de.westnordost.streetcomplete.quests.playground_access.AddPlaygroundAccess
import de.westnordost.streetcomplete.quests.police_type.AddPoliceType
import de.westnordost.streetcomplete.quests.postbox_collection_times.AddPostboxCollectionTimes
import de.westnordost.streetcomplete.quests.postbox_ref.AddPostboxRef
import de.westnordost.streetcomplete.quests.postbox_royal_cypher.AddPostboxRoyalCypher
import de.westnordost.streetcomplete.quests.power_attachment.AddPowerAttachment
import de.westnordost.streetcomplete.quests.powerpoles_material.AddPowerPolesMaterial
import de.westnordost.streetcomplete.quests.railway_crossing.AddRailwayCrossingBarrier
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType
import de.westnordost.streetcomplete.quests.recycling_glass.DetermineRecyclingGlass
import de.westnordost.streetcomplete.quests.recycling_material.AddRecyclingContainerMaterials
import de.westnordost.streetcomplete.quests.religion.AddReligionToPlaceOfWorship
import de.westnordost.streetcomplete.quests.religion.AddReligionToWaysideShrine
import de.westnordost.streetcomplete.quests.road_name.AddRoadName
import de.westnordost.streetcomplete.quests.roof_shape.AddRoofShape
import de.westnordost.streetcomplete.quests.sanitary_dump_station.AddSanitaryDumpStation
import de.westnordost.streetcomplete.quests.seating.AddSeating
import de.westnordost.streetcomplete.quests.segregated.AddCyclewaySegregation
import de.westnordost.streetcomplete.quests.self_service.AddSelfServiceLaundry
import de.westnordost.streetcomplete.quests.shop_type.CheckShopExistence
import de.westnordost.streetcomplete.quests.shop_type.CheckShopType
import de.westnordost.streetcomplete.quests.shop_type.SpecifyShopType
import de.westnordost.streetcomplete.quests.sidewalk.AddSidewalk
import de.westnordost.streetcomplete.quests.smoking.AddSmoking
import de.westnordost.streetcomplete.quests.smoothness.AddPathSmoothness
import de.westnordost.streetcomplete.quests.smoothness.AddRoadSmoothness
import de.westnordost.streetcomplete.quests.sport.AddSport
import de.westnordost.streetcomplete.quests.step_count.AddStepCount
import de.westnordost.streetcomplete.quests.step_count.AddStepCountStile
import de.westnordost.streetcomplete.quests.steps_ramp.AddStepsRamp
import de.westnordost.streetcomplete.quests.summit.AddSummitCross
import de.westnordost.streetcomplete.quests.summit.AddSummitRegister
import de.westnordost.streetcomplete.quests.surface.AddCyclewayPartSurface
import de.westnordost.streetcomplete.quests.surface.AddFootwayPartSurface
import de.westnordost.streetcomplete.quests.surface.AddPathSurface
import de.westnordost.streetcomplete.quests.surface.AddPitchSurface
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface
import de.westnordost.streetcomplete.quests.surface.AddSidewalkSurface
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingBusStop
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingCrosswalk
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingKerb
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingSteps
import de.westnordost.streetcomplete.quests.toilet_availability.AddToiletAvailability
import de.westnordost.streetcomplete.quests.toilets_fee.AddToiletsFee
import de.westnordost.streetcomplete.quests.tourism_information.AddInformationToTourism
import de.westnordost.streetcomplete.quests.tracktype.AddTracktype
import de.westnordost.streetcomplete.quests.traffic_calming_type.AddTrafficCalmingType
import de.westnordost.streetcomplete.quests.traffic_signals_button.AddTrafficSignalsButton
import de.westnordost.streetcomplete.quests.traffic_signals_sound.AddTrafficSignalsSound
import de.westnordost.streetcomplete.quests.traffic_signals_vibrate.AddTrafficSignalsVibration
import de.westnordost.streetcomplete.quests.way_lit.AddWayLit
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessBusiness
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessOutside
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessPublicTransport
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessToilets
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessToiletsPart
import de.westnordost.streetcomplete.quests.width.AddCyclewayWidth
import de.westnordost.streetcomplete.quests.width.AddRoadWidth
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import de.westnordost.streetcomplete.util.ktx.getFeature
import org.koin.core.qualifier.named
import org.koin.dsl.module

val questsModule = module {
    single {
        questTypeRegistry(
            get(),
            { location ->
                val countryInfos = get<CountryInfos>()
                val countryBoundaries = get<Lazy<CountryBoundaries>>(named("CountryBoundariesLazy")).value
                countryInfos.getByLocation(countryBoundaries, location.longitude, location.latitude)
            },
            { element ->
                get<Lazy<FeatureDictionary>>(named("FeatureDictionaryLazy")).value.getFeature(element)
            },
            { element ->
                // TODO replace hardcoding by actual call to API (later)
                // look at https://github.com/streetcomplete/StreetComplete/pull/5725/files
                // QuestsModule.kt
                arrayOf(94968802L, 288382015L, 5002908537L, 9074518026L, 5410074005L, 5355325384L, 5355356256L, 4753706785L, 4801407094L, 4621056486L, 3745920530L, 11646928463L, 7869711423L, 103569479L, 3076498887L, 2777486325L, 2111110777L, 5894178288L, 4332495812L, 4035446206L, 12463860186L, 4543989661L, 5894383585L, 4700836962L, 12027907934L, 5894180987L, 5846584301L, 5729544325L, 4848211909L, 9901568047L, 7232465879L, 3975064269L, 12154723397L, 1393803965L, 7997495987L, 10568554246L, 10048268222L, 9949928844L, 12066867629L, 701785352L, 12496989636L, 2309765715L, 551576749L, 442047598L, 231346739L, 986481548L, 5482216292L, 208303670L, 47038966L, 2285003861L, 469474148L, 250473557L, 3794166146L, 10685918571L, 4583891172L, 4884226066L, 1870457785L, 5922786967L, 5742105880L, 3785722883L, 3035221837L, 1878457316L, 2719784030L, 5059128076L, 4281246643L, 83521430L, 490986228L, 4749733847L, 9431790717L, 9721421278L, 8117909145L, 5734520515L, 4286222947L, 175184610L, 799213769L, 1911376148L, 1565225332L, 9131448457L, 4914501985L, 5581148888L, 1892027916L, 1912165387L, 2429202787L, 1775994476L, 11881332579L, 5230860224L, 3077266262L, 1917767026L, 5234905723L, 3077266263L, 3071747052L, 10165758142L, 12194109733L, 4796772882L, 3036639036L, 315935462L, 3635992315L, 5600608196L, 1739179525L, 323271480L, 9248004024L, 2441636303L, 6778423585L, 1680840225L, 1765056553L, 734878394L, 2292231799L, 1786556277L, 10582842612L, 3678983033L, 3663847734L, 236807659L, 1883394028L, 2482364654L, 219342197L, 10582012321L, 2483777412L, 2389966929L, 3347716873L, 5104477183L, 2891519497L, 12285360092L, 3708405278L, 841605028L, 1892920747L, 3215426758L, 6049516487L, 4980348321L, 6853230185L, 5688587126L, 4111144695L, 3706366569L, 10908482320L, 5132557281L, 6240325288L, 6713056916L, 5819058226L, 3635992316L, 4866708009L, 6919691586L, 1238620983L, 4729002779L, 5243233723L, 5429751728L, 10676788405L, 4933658121L, 4496922304L, 5113848324L, 3402291152L, 10115013217L, 9510981923L, 482287217L, 4861933221L, 8958135703L, 5271986725L, 8383323380L, 8360604008L, 6615085885L, 4732899655L, 5115867495L, 10729298809L, 8221101493L, 6724901759L, 184147921L, 6031994513L, 9263356311L, 8830109418L, 5432092155L, 6315166444L, 6905519383L, 1241313693L, 4729009614L, 9220814763L, 6919691388L, 5423993897L, 208298639L, 10582039058L, 6031988753L, 7009887354L, 7965221070L, 3987603501L, 9855141585L, 4231236403L, 3077266264L, 12093915484L, 1912831472L, 6653477285L, 8898610711L, 10129224288L, 10671195062L, 7177435485L, 3321194460L, 5970487523L, 7961264640L, 12155071697L, 8626004417L, 8537599476L, 6736780900L, 9110850406L, 1700392827L, 6637236205L, 4749733823L, 6917675186L, 10680338905L, 10061939049L, 11634419000L, 9334631995L, 8820541941L, 3024754400L, 4037052889L, 3781371133L, 3707200121L, 2790156783L, 3687915860L, 4447679008L, 10697790005L, 2817951266L, 5423993903L, 11035425437L, 9315627417L, 3562125512L, 8485258708L, 2360392546L, 9604574775L, 8070981385L, 9295897510L, 9476868653L, 9721421276L, 9989467869L, 1565225334L, 11198623229L, 6502548108L, 11028523489L, 12075616021L, 9063041395L, 11219229800L, 12073000369L, 10582842611L, 9200139930L, 10239538793L, 12108351375L, 2556890767L, 490986192L, 1028492248L, 2891562396L, 11676786735L, 9891909323L, 191119455L, 11775178769L, 8867620699L, 9994304431L, 11210253666L, 11883317019L, 10050689029L, 10091885799L, 11242237732L, 10570220263L, 10297918434L, 12075616020L, 10247665349L, 8021433308L, 2617578432L, 169236811L, 10980572050L, 12135933335L, 6512045585L, 10949046256L, 2510419226L, 11570652162L, 11158215834L, 10593089027L, 11579551988L, 11380143572L, 11952397965L, 5694304121L, 11920614117L, 12218775910L, 1272475007L, 11838402979L, 5628709078L, 12094952795L, 2570497690L, 12305999015L, 12339675484L, 12157658178L, 12175643244L, 12199560862L, 182398825L, 702493663L, 1228390913L, 490986329L, 202044161L, 991403000L, 5268862711L, 4585495327L, 4344273888L, 4699182289L, 4893973825L, 4640705364L, 1994285465L, 866215959L, 2609285643L, 6179172452L, 630961290L, 4573245165L, 12390484403L, 5046599316L, 6568207564L, 12155155901L, 3779583162L, 5354438390L, 9407934430L, 583543498L, 11733378748L, 11703396859L, 9402527684L, 11937867686L, 8168678875L, 4699315842L, 2097512814L, 6761428010L, 7846424045L, 5732642521L, 1725369246L, 1892913806L, 3490488630L, 9487486752L, 8368007153L, 4255406842L, 8391151201L, 8758590928L, 9508972155L, 9581346094L, 4602712941L, 3452916601L, 8572354395L, 6161796684L, 8458254225L, 4604333359L, 11088509312L, 4426462366L, 8521441436L, 4076639452L, 7254911987L, 9505689091L, 8750853888L, 5431937957L, 10056627668L, 3123784424L, 5537602924L, 3745503635L, 5753505454L, 11174714711L, 11141996272L, 9418505408L, 8758590925L, 6353454943L, 8595933951L, 9072253368L, 8461663799L, 5814596244L, 6436938492L, 6524308045L, 7179549288L, 6370705669L, 6684692675L, 6166482894L, 9530507426L, 6990060871L, 8476358530L, 11083847816L, 12078866167L, 9210450037L, 5821596377L, 8111668837L, 12450757352L, 8097241717L, 11283874924L, 8321548560L, 8712249668L, 9158986021L, 9203447047L, 11012665981L, 11141996271L, 2781139834L, 11141996273L, 9476248878L, 6161851446L, 353212940L, 9733156326L, 4997979721L, 9503059321L, 5683853921L, 10792385203L, 6186749684L, 11141996277L, 11141996275L, 11141996274L, 11141996276L, 10572585531L, 10834247514L, 10912570927L, 273581856L, 11075141775L, 11141996281L, 11421757124L, 11141996282L, 10919497891L, 8853626867L, 11141996283L, 11612210728L, 11556671379L, 12502207555L, 9059459532L, 12026727830L, 12146586473L, 9510982169L, 4505313389L, 1345997657L
                ).contains(element.id)
            },
            get()
        )
    }
}

fun questTypeRegistry(
    arSupportChecker: ArSupportChecker,
    getCountryInfoByLocation: (LatLon) -> CountryInfo,
    getFeature: (Element) -> Feature?,
    atpOpeningHoursDao: (Element) -> Boolean, // TODO: remove hack
    actualAtpOpeningHoursDao: AtpDao
) = QuestTypeRegistry(listOf(

    /*
        The quest types are primarily sorted by how easy they can be solved:
        1. quests that are solvable from a distance or while passing by (fast)
        2. quests that require to be right in front of it (e.g. because it is small, you need to
          look for it or read text)
        3. quests that require some exploration or walking around to check (e.g. walking down the
          whole road to find the cycleway is the same along the whole way)
        4. quests that require to go inside, i.e. deviate from your walking route by a lot just
          to solve the quest
        5. quests that come in heaps (are spammy) come last: e.g. building type etc.

        The ordering within this primary sort order shall be whatever is faster so solve first:

        a. Yes/No quests, easy selections first,
        b. number and text inputs later,
        c. complex inputs (opening hours, ...) last. Quests that e.g. often require the way to be
          split up first are in effect also slow to answer

        The order can be watered down somewhat if it means that quests that usually apply to the
        same elements are in direct succession because we want to avoid that users are half-done
        answering all the quests for one element and then can't solve the last anymore because it
        is visually obscured by another quest.

        Finally, importance of the quest can still play a factor, but only secondarily.

        ---

        Each quest is assigned an ordinal. This is used for serialization and is thus never changed,
        even if the quest's order is changed or new quests are added somewhere in the middle. Each new
        quest always gets a new sequential ordinal.
     */
    // TODO: move this quests in appropriate location as far as priority goes
    177 to AddOpeningHoursAtp(getFeature, atpOpeningHoursDao, actualAtpOpeningHoursDao), // TODO should it be merged into AddOpeningHours? If not then massive duplciation must be fixed
    176 to CreatePoiBasedOnAtp(),

    /* always first: notes - they mark a mistake in the data so potentially every quest for that
    element is based on wrong data while the note is not resolved */
    0 to OsmNoteQuestType,

    /* ↓ 1. solvable from a distance or while passing by -----------------------------------  */

    // bus stop quests
    1 to AddBusStopShelter(), // used by at least OsmAnd
    2 to AddBenchStatusOnBusStop(), // can be seen from across the street
    3 to AddBinStatusOnBusStop(), // can be seen from across the street
    4 to AddTactilePavingBusStop(), // requires you to be very close to it
    5 to AddBusStopName(), // requires text input
    6 to AddBusStopRef(), // requires text input
    7 to AddBusStopLit(), // at least during day requires to stand in it to see if there is a light in the shelter

    8 to AddRailwayCrossingBarrier(), // useful for routing

    9 to AddCarWashType(),

    10 to AddBenchBackrest(),
    11 to AddAmenityCover(getFeature),

    12 to AddBridgeStructure(),

    13 to MarkCompletedBuildingConstruction(), // unlocks AddBuildingType which unlocks address and building detail quests

    // sport pitches
    14 to AddSport(),
    15 to AddPitchSurface(),
    16 to AddPitchLit(),

    // parking
    17 to AddParkingType(),
    18 to AddParkingAccess(), // used by OSM Carto, mapy.cz, OSMand, Sputnik etc
    19 to AddParkingFee(), // used by OsmAnd

    20 to AddTrafficCalmingType(),

    // steps
    21 to AddHandrail(), // for accessibility of pedestrian routing, can be gathered when walking past
    22 to AddStepsRamp(),
    23 to AddStepsIncline(), // can be gathered while walking perpendicular to the way e.g. the other side of the road or when running/cycling past, confuses some people, so not as high as it theoretically should be
    158 to AddTactilePavingSteps(), // need to check top and bottom

    24 to AddBicycleIncline(),

    25 to AddMemorialType(), // sometimes a bit hard to decide between the different types (something something sculpture)

    26 to AddReligionToPlaceOfWorship(), // icons on maps are different - OSM Carto, mapy.cz, OsmAnd, Sputnik etc
    27 to AddReligionToWaysideShrine(),

    172 to AddPowerAttachment(),
    28 to AddPowerPolesMaterial(),

    29 to AddIsBuildingUnderground(), // should be before AddHousenumber to avoid asking for underground buildings

    // motorcycle parking
    30 to AddMotorcycleParkingCover(),
    31 to AddMotorcycleParkingCapacity(), // counting + number input required but usually well visible

    // air pump, may require some checking within a garage forecourt
    32 to AddAirCompressor(),

    // recycling containers
    33 to AddRecyclingType(),
    34 to DetermineRecyclingGlass(), // because most recycling:glass=yes is a tagging mistake
    35 to AddRecyclingContainerMaterials(),

    // kerbs
    36 to AddKerbHeight(),
    /*
        AddKerbHeight is deliberately before AddTactilePavingKerb:
        - Also should be visible while waiting to cross
        - Some people are not interpreting flush or lowered kerb as a kerb on their own,
          and would be confused about asking about tactile status on kerb without kerb
          but with this quest first they are OK with such interpretation
     */
    37 to AddTactilePavingKerb(), // Paving can be completed while waiting to cross

    // crossing quests: A little later because they are not all solvable from a distance
    38 to AddCrossing(),
    164 to AddCrossingSignals(),
    39 to AddCrossingIsland(), // can be done at a glance
    163 to AddCrossingMarkings(),
    41 to AddTactilePavingCrosswalk(),
    159 to AddCrossingKerbHeight(),
    42 to AddTrafficSignalsSound(), // Sound needs to be done as or after you're crossing
    43 to AddTrafficSignalsButton(),
    44 to AddTrafficSignalsVibration(),

    /* ↓ 2.solvable when right in front of it ----------------------------------------------- */
    45 to AddInformationToTourism(), // OSM Carto

    46 to AddPoliceType(),

    47 to AddPlaygroundAccess(),

    /* pulled up in priority to be before CheckExistence because this is basically the check
       whether the postbox is still there in countries in which it is enabled */
    48 to AddPostboxCollectionTimes(),
    49 to CheckExistence(getFeature),
    155 to AddGritBinSeasonal(),

    50 to AddBoardType(),
    171 to AddBoardName(),

    51 to AddBarrierType(), // basically any more detailed rendering and routing: OSM Carto, mapy.cz, OSMand for start
    52 to AddBarrierOnPath(),
    53 to AddBarrierOnRoad(),
    54 to AddBicycleBarrierType(),
    55 to AddBicycleBarrierInstallation(),
    56 to AddStileType(),
    57 to AddStepCountStile(), // here to keep stile quest together - this quest will appear in low quest density anyway

    58 to AddBollardType(), // useful for first responders

    82 to AddSeating(), // easily visible from outside, but only seasonally

    59 to AddSelfServiceLaundry(),

    60 to AddGeneralFee(),

    61 to AddDrinkingWater(), // used by AnyFinder
    62 to AddDrinkingWaterType(),

    63 to AddCameraType(),

    64 to AddFireHydrantType(),
    65 to AddFireHydrantPosition(),
    66 to AddFireHydrantDiameter(),
    67 to AddFireHydrantRef(),

    160 to AddBbqFuel(),
    /* ↓ 2.solvable when right in front of it but takes longer to input --------------------- */

    // bike parking/rental: would be higher up if not for bike parking/rental capacity which is usually not solvable when moving past
    68 to AddBikeParkingCover(), // used by OsmAnd in the object description
    69 to AddBikeRentalType(), // generally less overlap of possible types/fewer choices/simpler to answer
    70 to AddBikeParkingType(), // used by OsmAnd
    71 to AddBikeParkingAccess(),
    72 to AddBikeParkingFee(),
    73 to AddBikeRentalCapacity(), // less ambiguous than bike parking
    74 to AddBikeParkingCapacity(), // used by cycle map layer on osm.org, OsmAnd

    173 to AddBicycleRepairStationServices(),

    167 to AddParcelLockerBrand(),

    // address: usually only visible when just in front + sometimes requires to take "other answer"
    75 to AddHousenumber(),
    76 to AddAddressStreet(),

    // shops: text input / opening hours input take longer than other quests
    157 to AddHairdresserCustomers(), // almost always marked on sign outside
    78 to SpecifyShopType(), // above add place name as some brand presets will set the name too
    79 to CheckShopType(),
    80 to AddPlaceName(getFeature),
    77 to CheckOpeningHoursSigned(getFeature),
    81 to AddOpeningHours(getFeature),
    83 to AddBicyclePump(), // visible from the outside, but only during opening hours

    84 to AddAtmOperator(),
    85 to AddAtmCashIn(),

    86 to AddClothingBinOperator(),

    87 to AddChargingStationCapacity(),
    88 to AddChargingStationOperator(),

    // postboxes (collection times are further up, see comment)
    89 to AddPostboxRoyalCypher(), // can be glanced across the road (if postbox facing the right way)
    90 to AddPostboxRef(), // requires text input and to be very close to the collection plate

    91 to AddAccessPointRef(), // requires text input and to be very close to the collection plate

    92 to AddWheelchairAccessOutside(),

    // road but information is visible usually at the beginning of the marked stretch of way
    93 to AddMaxWeight(), // used by OSRM and other routing engines
    94 to AddMaxHeight(), // OSRM and other routing engines
    95 to AddMaxPhysicalHeight(arSupportChecker), // same as above, best if it appears right after (if enabled)
    96 to AddRoadName(),
    97 to AddOneway(),

    99 to AddEntrance(),
    100 to AddEntranceReference(),

    166 to AddMopedAccess(),

    /* ↓ 3.quests that may need some exploration / walking around --------------------------- */

    // ferry: usually visible from looking at the boat, but not always...
    101 to AddFerryAccessPedestrian(),
    102 to AddFerryAccessMotorVehicle(),

    103 to AddProhibitedForPedestrians(), // need to understand the pedestrian situation

    104 to MarkCompletedHighwayConstruction(), // need to look the whole way

    105 to AddSummitCross(), // summit markings are not necessarily directly at the peak, need to look around
    106 to AddSummitRegister(), // register is harder to find than cross

    165 to AddTreeLeafType(), // may need to get close in trickier cases
    107 to AddForestLeafType(), // need to walk around in the highlighted section

    108 to AddOrchardProduce(), // difficult to find out if the orchard does not carry fruits right now

    109 to AddLevel(), // requires to search for the place on several levels (or at least find a mall map)

    110 to AddAirConditioning(), // often visible from the outside across the street, if not, visible/feelable inside

    111 to AddSmoking(), // often marked on the entrance, if not, visible/smellable inside

    /* ↓ 4.quests that may need to go inside ------------------------------------------------ */

    174 to AddBoatRental(),

    112 to AddWheelchairAccessPublicTransport(), // need to look out for lifts etc, maybe even enter the station

    113 to AddIsAmenityIndoor(getFeature), // need to go inside in case it is inside (or gone)
    161 to AddDefibrillatorLocation(), // need to go inside in case it is inside (or gone)

    // inside camping sites
    114 to AddCampType(),
    115 to AddCampDrinkingWater(),
    116 to AddCampShower(),
    117 to AddCampPower(),
    162 to AddSanitaryDumpStation(),

    // toilets
    118 to AddToiletAvailability(), // shown in OsmAnd descriptions
    119 to AddToiletsFee(), // used by OsmAnd in the object description
    122 to AddWheelchairAccessToilets(), // used by wheelmap, OsmAnd, Organic Maps

    // shop
    123 to AddBikeRepairAvailability(),
    124 to AddSecondHandBicycleAvailability(),
    125 to AddVegetarian(), // menus are often posted externally
    126 to AddVegan(),
    127 to AddHalal(), // there are ~ 100 times more Muslims than Jews
    128 to AddKosher(),
    175 to AddGlutenFree(),
    129 to AddWheelchairAccessBusiness(), // used by wheelmap, OsmAnd, Organic Maps
    130 to AddInternetAccess(), // used by OsmAnd
    131 to AddAcceptsCards(), // this will often involve going inside and near the till
    132 to AddAcceptsCash(),

    // shop and others, but have to go inside
    120 to AddBabyChangingTable(), // used by OsmAnd in the object description, have to go inside
    121 to AddWheelchairAccessToiletsPart(), // have to go inside

    133 to AddFuelSelfService(),
    156 to CheckShopExistence(getFeature), // after opening hours and similar so they will be preferred if enabled

    /* ↓ 5.quests that are very numerous ---------------------------------------------------- */

    // roads
    134 to AddSidewalk(), // for any pedestrian routers, needs minimal thinking
    135 to AddRoadSurface(), // used by BRouter, OsmAnd, OSRM, graphhopper, HOT map style... - sometimes requires way to be split
    136 to AddTracktype(), // widely used in map rendering - OSM Carto, OsmAnd...
    137 to AddCycleway(getCountryInfoByLocation), // for any cyclist routers (and cyclist maps)
    138 to AddLanes(), // abstreet, certainly most routing engines - often requires way to be split

    140 to AddRoadWidth(arSupportChecker),
    141 to AddRoadSmoothness(),
    142 to AddPathSmoothness(),
    170 to AddBarrierOpening(arSupportChecker),

    // footways
    143 to AddPathSurface(), // used by OSM Carto, BRouter, OsmAnd, OSRM, graphhopper...
    144 to AddCyclewaySegregation(), // Cyclosm, Valhalla, Bike Citizens Bicycle Navigation...
    145 to AddFootwayPartSurface(),
    146 to AddCyclewayPartSurface(),
    147 to AddSidewalkSurface(),
    148 to AddCyclewayWidth(arSupportChecker), // should be after cycleway segregation

    /* should best be after road surface because it excludes unpaved roads, also, need to search
     * for the sign which is one reason why it is disabled by default */
    149 to AddMaxSpeed(),

    // buildings
    150 to AddBuildingType(),
    151 to AddBuildingLevels(),
    152 to AddRoofShape(getCountryInfoByLocation),

    153 to AddStepCount(), // can only be gathered when walking along this way, also needs the most effort and least useful

    /* at the very last because it can be difficult to ascertain during day. used by OsmAnd if "Street lighting" is enabled. (Configure map, Map rendering, Details) */
    154 to AddWayLit(),
))
