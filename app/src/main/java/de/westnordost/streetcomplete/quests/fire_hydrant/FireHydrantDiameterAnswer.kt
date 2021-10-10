sealed interface FireHydrantDiameterAnswer

object NoFireHydrantDiameterSign : FireHydrantDiameterAnswer
data class FireHydrantDiameter(val diameter: Int) : FireHydrantDiameterAnswer
