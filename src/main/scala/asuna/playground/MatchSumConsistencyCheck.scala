package asuna.playground

import monix.execution.Scheduler.Implicits.global
import asuna.common.legends._
import asuna.proto.league._
import asuna.proto.league.alexandria._
import asuna.proto.league.alexandria.rpc._
import asuna.proto.league.vulgate._
import asuna.proto.league.vulgate.rpc._
import scala.concurrent._, duration._
import cats.implicits._

object MatchSumConsistencyCheck extends Playground(Seq("alexandria", "vulgate")) {

  def main(args: Array[String]): Unit = {
    init(args)

    val alex = AlexandriaGrpc.stub(svc.clientFor("alexandria"))
    val vulgate = VulgateGrpc.stub(svc.clientFor("vulgate"))

    val patches = Seq("7.8")
    val ctx = VulgateHelpers.makeVulgateContext(patches, Region.NA)

    val annie = 1

    val fut = for {
      factors <- vulgate.getAggregationFactors(
        GetAggregationFactorsRequest(
          context = ctx.some,
          patches = patches,
        )
      )

      baseSpace = MatchFiltersSpace(
        championIds = Set(annie).toSeq,
        regions = Set(Region.NA).toSeq,
        roles = Set(Role.MID).toSeq,
        tiers = Set(Tier.GOLD).toSeq,
        queues = Set(
          Queue.TEAM_BUILDER_RANKED_SOLO,
          Queue.RANKED_FLEX_SR,
          Queue.RANKED_SOLO_5x5,
          Queue.TEAM_BUILDER_DRAFT_RANKED_5x5,
        ).toSeq,
        versions = patches,
      )
      base <- alex.getSum(GetSumRequest(space = baseSpace.some))

      withEnemiesSpace = baseSpace.update(_.enemyIds := (factors.champions :+ 0))
      withEnemies <- alex.getSum(GetSumRequest(space = withEnemiesSpace.some))

    } yield {
      println(base.statistics.map(_.plays))
      println(withEnemies.statistics.map(_.plays))
    }

    Await.result(fut, Duration.Inf)
  }

}
