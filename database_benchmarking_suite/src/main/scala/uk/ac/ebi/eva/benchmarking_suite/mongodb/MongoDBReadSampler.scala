package uk.ac.ebi.eva.benchmarking_suite.mongodb

import org.apache.jmeter.samplers.{AbstractSampler, Entry, SampleResult}
import org.apache.jmeter.util.JMeterUtils
import org.mongodb.scala.model.Filters._
import uk.ac.ebi.eva.benchmarking_suite.DBSamplerProcessor

class MongoDBReadSampler() extends AbstractSampler {

  var mongoDBTestParams: MongoDBConnectionParams = _
  var randomNumGen: scala.util.Random = _

  val blockReadSize = 100

  override def sample(entry: Entry): SampleResult = {
    mongoDBTestParams = JMeterUtils.getJMeterProperties.get("connectionParams").asInstanceOf[MongoDBConnectionParams]
    DBSamplerProcessor.process(sampler = this,
      databaseAction = () => {
        mongoDBTestParams = JMeterUtils.getJMeterProperties.get("connectionParams").asInstanceOf[MongoDBConnectionParams]
        val numReadsPerThread = this.getPropertyAsInt("numOpsPerThread")
        val threadNum = this.getThreadContext.getThreadNum
        randomNumGen = new scala.util.Random(threadNum)
        (1 to numReadsPerThread).foreach(_ => readData())
      })
  }

  def readData(): Unit = {
    val chromosome = randomNumGen.nextInt(16)
    val startPos = randomNumGen.nextInt(1e9.toInt/16)
    mongoDBTestParams.mongoCollection.find(
      and(equal("chromosome", chromosome),
        gt("start_pos", startPos),
        lte("start_pos", startPos + blockReadSize))).foreach(doc => doc.get("start_pos")) //Force document retrieval by getting one attribute
  }
}