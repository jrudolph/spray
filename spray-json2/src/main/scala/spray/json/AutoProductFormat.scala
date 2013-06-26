package spray.json

object AutoProductFormat {
  implicit def autoProductFormat[T <: Product]: RootJsonFormat[T] = macro AutoProductFormatMacro.autoProductFormatMacro[T]
}

object AutoProductFormatMacro {
  import scala.reflect.macros.Context

  def autoProductFormatMacro[T: c.WeakTypeTag](c: Context): c.Expr[RootJsonFormat[T]] = {
    import c._

    val tt = weakTypeTag[T]
    val tS = tt.tpe.typeSymbol.asClass

    val apply = tS.companionSymbol.typeSignature.declaration(universe.newTermName("apply"))
    val numParams = apply.asMethod.paramss(0).length

    import universe._
    // generate `jsonFormatN(T.apply _)`
    c.Expr[RootJsonFormat[T]] {
      Apply(Select(reify(spray.json.ProductFormats).tree, newTermName("jsonFormat" + numParams)), List(
        Typed(Select(Ident(tS.companionSymbol.name), apply.asTerm.name), Function(Nil, EmptyTree))))
    }
  }
}
