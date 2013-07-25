package mlib.impl.protocol

/**
 *
 */
object BasicCode extends Enumeration {
  val SUCCESS = Value(0, "success")
  val INTERNAL_ERROR = Value(1, "something ba-a-ad happened")
  val PARSE_ERROR = Value(3, "message parsing error")
  implicit def BasicCode2Int(value: BasicCode.Value): Int = value.id
}
