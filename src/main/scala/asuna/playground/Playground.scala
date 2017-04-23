package asuna.playground

import monix.execution.Scheduler.Implicits.global
import buildinfo.BuildInfo
import asuna.common._
import asuna.common.config._

abstract class Playground(deps: Seq[String]) {

  val cfgParser = new ConfigParser[Unit](
    name = BuildInfo.name,
    version = BuildInfo.version,
    dependencies = Set("alexandria", "vulgate"),
    port = 1,
    metaPort = 1,
    initial = (),
  ) {

  }

  var svc: BaseService[Unit] = null

  def main(args: Seq[String]): Unit = {
    svc = new BaseService(args, cfgParser)
    run()
  }

  def run(): Unit

}

