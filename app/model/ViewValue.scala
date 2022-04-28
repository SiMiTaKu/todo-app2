package model

case class ViewValueList(
                          title:  String,
                          cssSrc: Seq[String],
                          jsSrc:  Seq[String],
                        ) extends ViewValueCommon

case class ViewValueEdit(
                          title:  String,
                          cssSrc: Seq[String],
                          jsSrc:  Seq[String],
                        ) extends ViewValueCommon

case class ViewValueDetail(
                            title:  String,
                            cssSrc: Seq[String],
                            jsSrc:  Seq[String],
                          ) extends ViewValueCommon

case class ViewValueRegister(
                              title:  String,
                              cssSrc: Seq[String],
                              jsSrc:  Seq[String],
                            ) extends ViewValueCommon

case class ViewValueError(
                              title:  String,
                              cssSrc: Seq[String],
                              jsSrc:  Seq[String],
                            ) extends ViewValueCommon